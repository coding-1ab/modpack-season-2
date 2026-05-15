package dev.codinglabs.modpack.rapier_entity

import dev.ryanhcode.sable.api.physics.`object`.box.BoxPhysicsObject

data class BoxSet(val boxes: Set<BoxPhysicsObject>)

val boxes = HashSet<BoxSet>()
