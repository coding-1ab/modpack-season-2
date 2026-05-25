package dev.codinglabs.modpack.rapier_entity.dragon

import dev.codinglabs.modpack.rapier_entity.BoxedEntity
import dev.ryanhcode.sable.api.physics.`object`.box.BoxPhysicsObject
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.level.Level

class EnderDragonPrototype(entityType: EntityType<EnderDragonPrototype>, level: Level) : Mob(entityType, level), Enemy,
    BoxedEntity {
    override val boxes: MutableSet<BoxPhysicsObject> = HashSet()
    override val entity: Entity
        get() = this
}
