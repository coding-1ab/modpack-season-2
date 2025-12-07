package com.kipti.bnb.content.nixie.nixie_board;

import com.kipti.bnb.CreateBitsnBobs;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

public class NixieBoardBlockStateGen {

    public static <T extends NixieBoardBlock> void nixieBoard(final DataGenContext<Block, T> c, final RegistrateBlockstateProvider p) {

        p.getVariantBuilder(c.get())
                .forAllStates(state -> {
                    final boolean left = state.getValue(NixieBoardBlock.LEFT);
                    final boolean right = state.getValue(NixieBoardBlock.RIGHT);
                    final boolean bottom = state.getValue(NixieBoardBlock.BOTTOM);
                    final boolean top = state.getValue(NixieBoardBlock.TOP);

                    final String modelName = "nixie_board"
                            + (left ? right ? "_middle" : "_right" : right ? "_left" : "_single")
                            + (bottom ? top ? "_middle" : "_top" : top ? "_bottom" : "");

                    return ConfiguredModel.builder()
                            .modelFile(p.models().getExistingFile(CreateBitsnBobs.asResource("block/nixie_board/" + modelName)))
                            .build();
                });
    }

}
