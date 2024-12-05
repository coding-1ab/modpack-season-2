package com.simibubi.create.content.decoration.palettes;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class RandomisedWindowCTBehaviour extends ConnectedTextureBehaviour.Base {

	private List<CTSpriteShiftEntry> shifts;

	public RandomisedWindowCTBehaviour(List<CTSpriteShiftEntry> shifts) {
		this.shifts = shifts;
	}

	@Override
	public @Nullable CTSpriteShiftEntry getShift(BlockState state, Direction direction,
		@Nullable TextureAtlasSprite sprite) {
		if (direction.getAxis() == Axis.Y || sprite == null)
			return null;
		for (CTSpriteShiftEntry entry : shifts)
			if (entry.getOriginal() == sprite)
				return entry;
		return null;
	}
	
	@Override
	public @Nullable CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
		return AllCTTypes.RECTANGLE;
	}

}
