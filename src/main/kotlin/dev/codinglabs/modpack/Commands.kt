package dev.codinglabs.modpack

import com.mojang.brigadier.CommandDispatcher
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.ryanhcode.sable.api.physics.`object`.box.BoxPhysicsObject
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer
import dev.ryanhcode.sable.companion.math.Pose3d
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent
import org.joml.Vector3d
import java.util.UUID
import net.minecraft.commands.Commands as MinecraftCommands

object Commands {
    const val SPAWN_PLAYER_ONLY: String = "${ID}.commands.spawn.failure.player_only"
    const val RTP_PLAYER_ONLY: String = "${ID}.commands.rtp.failure.player_only"
    const val TELEPORT_START: String = "${ID}.commands.teleport.success.timer_added"
    const val TELEPORT_INTERRUPTED: String = "${ID}.commands.teleport.failure.interrupted"
    const val TELEPORT_ON_COOLDOWN: String = "${ID}.commands.teleport.failure.cooldown"
    const val TELEPORT_OVERWORLD_ONLY: String = "${ID}.commands.teleport.failure.overworld_only"

    val DEVELOPERS = listOf(
        UUID.fromString("b6c7411f-15e2-48f6-b53f-876ec0577e82")
    )

    val TIMER_COOLDOWN_RULE: GameRules.Key<GameRules.IntegerValue> = GameRules.register(
        "teleport_cooldown_ticks",
        GameRules.Category.PLAYER,
        GameRules.IntegerValue.create(6000)
    )

    init {
        TIMER_COOLDOWN_RULE
    }

    fun register(event: RegisterCommandsEvent) {
        teleport(event.dispatcher, TeleportType.Spawn)
        teleport(event.dispatcher, TeleportType.Random)
        testDragon(event.dispatcher)
    }

    fun testDragon(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(MinecraftCommands.literal("testDragon").executes { context ->
            val source = context.source
            val player = source.entity as? ServerPlayer
            if (player == null || !DEVELOPERS.contains(player.uuid)) {
                source.sendFailure(Component.literal("Developer Only Command"))
                return@executes -1
            }

            val level = player.level() as ServerLevel
            val container = SubLevelContainer.getContainer(level)!!
            val pipeline = container.physicsSystem().pipeline

            pipeline.addBox(BoxPhysicsObject(Pose3d(), Vector3d(0.5, 0.5, 0.5), 1.0))

            return@executes 0
        })
    }

    fun teleport(dispatcher: CommandDispatcher<CommandSourceStack>, type: TeleportType) {
        val command = when(type) {
            TeleportType.Random -> "rtp"
            TeleportType.Spawn -> "spawn"
        }

        dispatcher.register(MinecraftCommands.literal(command).executes { context ->
            val source = context.source
            val player = source.entity as? ServerPlayer
            if (player == null) {
                val failureMessage = when (type) {
                    TeleportType.Random -> RTP_PLAYER_ONLY
                    TeleportType.Spawn -> SPAWN_PLAYER_ONLY
                }
                source.sendFailure(Component.translatable(failureMessage))
                return@executes -1
            }

            val level = source.level
            if (level.dimension() != Level.OVERWORLD) {
                source.sendFailure(Component.translatable(TELEPORT_OVERWORLD_ONLY))
                return@executes -1
            }

            val gameTime = level.gameTime
            val timeSince = gameTime - player.lastTeleportTick
            val threshold = source.server.gameRules.getRule(TIMER_COOLDOWN_RULE).get().toLong()
            if (timeSince < threshold) {
                val asSeconds = (threshold - timeSince) / 20
                val minute = asSeconds / 60
                val seconds = asSeconds % 60
                val timeString = "$minute:$seconds"
                source.sendFailure(Component.translatable(TELEPORT_ON_COOLDOWN, timeString))
                return@executes -1
            }

            player.teleportRequest = TeleportRequest(type, gameTime)
            level.playSound(null, player.x, player.y, player.z, SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS)
            source.sendSuccess(
                { Component.translatable(TELEPORT_START).withColor(ChatFormatting.DARK_PURPLE.color!!) },
                true
            )
            return@executes 0
        })
    }

    fun onTick(event: LevelTickEvent.Post) {
        val level = event.level as? ServerLevel ?: return
        if (level.dimension() != Level.OVERWORLD) {
            return
        }

        val fourSeconds = 20 * 4

        level.server.playerList.players.forEach { player ->
            val request = player.teleportRequest ?: return@forEach

            if (player.level().dimension() != Level.OVERWORLD) {
                player.sendSystemMessage(
                    Component.translatable(TELEPORT_INTERRUPTED).withColor(ChatFormatting.RED.color!!)
                )
                return@forEach
            }

            val timeSince = level.gameTime - request.time
            if (timeSince > fourSeconds) {
                val blockPos = when (request.type) {
                    TeleportType.Random -> {
                        val x = level.random.nextIntBetweenInclusive(-50000, 50000)
                        val z = level.random.nextIntBetweenInclusive(-50000, 50000)
                        val chunk = level.getChunkAt(BlockPos(x, 0, z))
                        val y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x.and(15), z.and(15)) + 1
                        BlockPos(x, y, z)
                    }

                    TeleportType.Spawn -> {
                        level.levelData.spawnPos
                    }
                }

                val pos = Vec3.atCenterOf(blockPos)
                player.teleportTo(pos.x, pos.y, pos.z)
                player.teleportRequest = null
                player.lastTeleportTick = level.gameTime
            }
        }
    }

    fun onHurt(event: LivingDamageEvent.Post) {
        val player = event.entity as? ServerPlayer ?: return
        if (player.teleportRequest != null) {
            player.teleportRequest = null
            player.sendSystemMessage(Component.translatable(TELEPORT_INTERRUPTED).withColor(ChatFormatting.RED.color!!))
            player.connection.send(
                ClientboundSoundPacket(
                    SoundEvents.NOTE_BLOCK_BASS,
                    SoundSource.MASTER,
                    player.x,
                    player.y,
                    player.z,
                    1.0F,
                    0.5F,
                    player.random.nextLong()
                )
            )
        }
    }

    fun onLeave(event: PlayerEvent.PlayerLoggedOutEvent) {
        val player = event.entity as? ServerPlayer ?: return
        player.teleportRequest = null
    }

    data class TeleportRequest(
        val type: TeleportType,
        val time: Long
    ) {
        companion object {
            val CODEC: Codec<TeleportRequest> = RecordCodecBuilder.create { builder ->
                builder.group(
                    TeleportType.CODEC.fieldOf("type").forGetter(TeleportRequest::type),
                    Codec.LONG.fieldOf("time").forGetter(TeleportRequest::time),
                ).apply(builder, ::TeleportRequest)
            }
        }
    }

    enum class TeleportType {
        Random,
        Spawn;

        companion object {
            val CODEC: Codec<TeleportType> = Codec.STRING.comapFlatMap({ string ->
                when (string) {
                    Random.name -> {
                        DataResult.success(Random)
                    }

                    Spawn.name -> {
                        DataResult.success(Spawn)
                    }

                    else -> {
                        DataResult.error { "Unknown request type $string" }
                    }
                }
            }, TeleportType::name)
        }
    }
}
