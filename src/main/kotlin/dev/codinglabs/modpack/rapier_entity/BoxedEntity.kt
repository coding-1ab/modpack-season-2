package dev.codinglabs.modpack.rapier_entity

import dev.ryanhcode.sable.api.physics.`object`.box.BoxPhysicsObject
import net.minecraft.world.entity.Entity

interface BoxedEntity {
    val boxes: MutableSet<BoxPhysicsObject>
    val entity: Entity
}
