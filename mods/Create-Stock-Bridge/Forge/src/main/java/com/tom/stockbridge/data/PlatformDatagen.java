package com.tom.stockbridge.data;

import java.util.function.Function;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

public class PlatformDatagen {

	public static <T extends Block> void variantBuilder(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, Function<BlockState, Object> modelFunc) {
		prov.getVariantBuilder(ctx.get()).forAllStates(state -> {
			Object model = modelFunc.apply(state);
			if (!(model instanceof ModelFile mf))
				throw new RuntimeException("Model Factory must return a ModelFile type. Got: " + (model == null ? "~~NULL~~" : model.getClass()));

			return ConfiguredModel.builder()
					.modelFile(mf)
					.build();
		});
	}
}
