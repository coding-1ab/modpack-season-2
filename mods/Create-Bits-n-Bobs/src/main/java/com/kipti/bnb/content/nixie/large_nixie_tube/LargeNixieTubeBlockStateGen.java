package com.kipti.bnb.content.nixie.large_nixie_tube;

import com.kipti.bnb.CreateBitsnBobs;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;

public class LargeNixieTubeBlockStateGen {

    public static <T extends LargeNixieTubeBlock> void nixieTube(DataGenContext<Block, T> c, RegistrateBlockstateProvider p) {
        p.directionalBlock(c.get(), (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
            "block/large_nixie_tube/large_nixie_tube"
        )));
    }

}
