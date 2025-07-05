package com.kipti.bnb.content.nixie.nixie_board;

import com.kipti.bnb.CreateBitsnBobs;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;

public class NixieBoardBlockStateGen {

    public static <T extends NixieBoardBlock> void nixieBoard(DataGenContext<Block, T> c, RegistrateBlockstateProvider p) {
        p.directionalBlock(c.get(), (state) -> {
                boolean left = state.getValue(NixieBoardBlock.LEFT);
                boolean right = state.getValue(NixieBoardBlock.RIGHT);

                return p.models().getExistingFile(CreateBitsnBobs.asResource(
                    left ? (right ? "block/nixie_board/nixie_board_middle" : "block/nixie_board/nixie_board_left") :
                        (right ? "block/nixie_board/nixie_board_right" : "block/nixie_board/nixie_board_single")
                ));
            }
        );
    }

}
