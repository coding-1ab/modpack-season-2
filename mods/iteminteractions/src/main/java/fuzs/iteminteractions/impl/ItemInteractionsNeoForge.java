package fuzs.iteminteractions.impl;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.neoforged.fml.common.Mod;

@Mod(ItemInteractions.MOD_ID)
public class ItemInteractionsNeoForge {

    public ItemInteractionsNeoForge() {
        ModConstructor.construct(ItemInteractions.MOD_ID, ItemInteractions::new);
    }
}
