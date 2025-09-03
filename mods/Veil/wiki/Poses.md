Veil provides a system that allows you to apply `ExtendedPose`s to players. The ideal usecase for this is for custom
item using animations, but can be applied elsewhere. Extended poses contain 2 pose types - 3rd person and 1st person.
Each pose type is given a different set of data:

```
3rd person -
age in ticks
walk time
limb swing
limb swing amount
head yaw
head pitch
use time
max use time
main hand model part
offhand model part
boolean whether the player is using an item in their offhand or not
```

```
1st person -
the PoseStack of the player model
the itemstack being rendered in the hand
limb swing amount
use time
max use time
the hand being used
the equip progress
```

The JavaDoc in
the [PoseRegistry](https://github.com/FoundryMC/Veil/blob/main/Common/src/main/java/foundry/veil/model/pose/PoseRegistry.java)
and [VeilPoseable](https://github.com/FoundryMC/Veil/blob/main/Common/src/main/java/foundry/veil/model/pose/VeilPoseable.java)
classes provides much more insight into how this system works, including examples.