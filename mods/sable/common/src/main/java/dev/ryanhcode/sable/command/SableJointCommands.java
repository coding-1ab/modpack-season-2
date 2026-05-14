package dev.ryanhcode.sable.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ryanhcode.sable.api.command.SableCommandHelper;
import dev.ryanhcode.sable.api.command.SubLevelArgumentType;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.constraint.rotary.RotaryConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.spherical.SphericalConstraintConfiguration;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public class SableJointCommands {

    public static final SimpleCommandExceptionType MISSING_JOINT_SUBLEVEL_TARGET =
            new SimpleCommandExceptionType(Component.translatable("commands.sable.joint.missing_sublevel_target"));

    public static final SimpleCommandExceptionType UNSUPPORTED_JOINT_TYPE =
            new SimpleCommandExceptionType(Component.translatable("commands.sable.joint.unsupported_joint_type"));

    /**
     * Adds the following commands:
     * <ul>
     *     <li>{@code /sable joint add <subLevel> <subLevel> rotary <pos1> <pos2> <axis1> <axis2>}</li>
     *     <li>{@code /sable joint add <subLevel> <subLevel> spherical <pos1> <pos2> <contacts>}</li>
     * </ul>
     */
    public static void register(final LiteralArgumentBuilder<CommandSourceStack> sableBuilder, final CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> addRotary = Commands.literal("rotary")
                .then(Commands.argument("pos1", Vec3Argument.vec3(false))
                        .then(Commands.argument("pos2", Vec3Argument.vec3(false))
                                .then(Commands.argument("axis1", Vec3Argument.vec3(false))
                                        .then(Commands.argument("axis2", Vec3Argument.vec3(false))
                                                .executes(SableJointCommands::executeAddRotaryCommand)))));

        LiteralArgumentBuilder<CommandSourceStack> addSpherical = Commands.literal("spherical")
                .then(Commands.argument("pos1", Vec3Argument.vec3(false))
                        .then(Commands.argument("pos2", Vec3Argument.vec3(false))
                                .then(Commands.argument("contacts", BoolArgumentType.bool())
                                        .executes(SableJointCommands::executeAddSphericalCommand)
                                )
                        ));

        sableBuilder.then(Commands.literal("joint")
                .then(Commands.literal("add")
                        .then(Commands.argument("subLevel1", SubLevelArgumentType.subLevels())
                                .then(Commands.argument("subLevel2", SubLevelArgumentType.subLevels())
                                        .then(addRotary)
                                        .then(addSpherical)
                                )
                        )
                )
        );

    }

    private static int executeAddSphericalCommand(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final ServerSubLevelContainer container = SableCommandHelper.requireSubLevelContainer(ctx);
        final PhysicsPipeline pipeline = SableCommandHelper.requireSubLevelPhysicsSystem(container).getPipeline();
        addSphericalJoint(
                pipeline,
                SubLevelArgumentType.getSubLevels(ctx, "subLevel1"),
                SubLevelArgumentType.getSubLevels(ctx, "subLevel2"),
                Vec3Argument.getVec3(ctx, "pos1"), Vec3Argument.getVec3(ctx, "pos2"),
                BoolArgumentType.getBool(ctx, "contacts")
        );

        ctx.getSource().sendSuccess(() -> Component.translatable("commands.sable.joint.success"), true);
        return 1;
    }

    private static int executeAddRotaryCommand(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final ServerSubLevelContainer container = SableCommandHelper.requireSubLevelContainer(ctx);
        final PhysicsPipeline pipeline = SableCommandHelper.requireSubLevelPhysicsSystem(container).getPipeline();
        addRotaryJoint(
                pipeline,
                SubLevelArgumentType.getSubLevels(ctx, "subLevel1"),
                SubLevelArgumentType.getSubLevels(ctx, "subLevel2"),
                Vec3Argument.getVec3(ctx, "pos1"), Vec3Argument.getVec3(ctx, "pos2"),
                Vec3Argument.getVec3(ctx, "axis1"), Vec3Argument.getVec3(ctx, "axis2")
        );

        ctx.getSource().sendSuccess(() -> Component.translatable("commands.sable.joint.success"), true);
        return 1;
    }

    private static void addSphericalJoint(
            final PhysicsPipeline pipeline,
            final Collection<ServerSubLevel> subLevel1,
            final Collection<ServerSubLevel> subLevel2,
            final Vec3 pos1, final Vec3 pos2,
            final boolean contacts
    ) throws CommandSyntaxException {
        final SphericalConstraintConfiguration constraintConfig = new SphericalConstraintConfiguration(
                JOMLConversion.toJOML(pos1),
                JOMLConversion.toJOML(pos2),
                contacts
        );

        final ServerSubLevel jointSubLevel1 = subLevel1.stream().findFirst()
                .orElseThrow(MISSING_JOINT_SUBLEVEL_TARGET::create);
        final ServerSubLevel jointSubLevel2 = subLevel2.stream().findFirst()
                .orElseThrow(MISSING_JOINT_SUBLEVEL_TARGET::create);

        pipeline.addConstraint(jointSubLevel1, jointSubLevel2, constraintConfig);
    }

    private static void addRotaryJoint(
            final PhysicsPipeline pipeline,
            final Collection<ServerSubLevel> subLevel1,
            final Collection<ServerSubLevel> subLevel2,
            final Vec3 pos1, final Vec3 pos2,
            final Vec3 axis1, final Vec3 axis2
    ) throws CommandSyntaxException {
        final RotaryConstraintConfiguration constraintConfig = new RotaryConstraintConfiguration(
                JOMLConversion.toJOML(pos1),
                JOMLConversion.toJOML(pos2),
                JOMLConversion.toJOML(axis1),
                JOMLConversion.toJOML(axis2)
        );

        final ServerSubLevel jointSubLevel1 = subLevel1.stream().findFirst()
                .orElseThrow(MISSING_JOINT_SUBLEVEL_TARGET::create);
        final ServerSubLevel jointSubLevel2 = subLevel2.stream().findFirst()
                .orElseThrow(MISSING_JOINT_SUBLEVEL_TARGET::create);

        pipeline.addConstraint(jointSubLevel1, jointSubLevel2, constraintConfig);
    }

}
