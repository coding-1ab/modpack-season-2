package dev.codinglabs.modpack.rapier_entity

import dev.ryanhcode.sable.api.physics.`object`.box.BoxPhysicsObject
import net.minecraft.world.entity.Entity
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn

interface BoxedEntity: IEntityWithComplexSpawn {
    val boxes: Set<BoxPhysicsObject>
    val entity: Entity
}
