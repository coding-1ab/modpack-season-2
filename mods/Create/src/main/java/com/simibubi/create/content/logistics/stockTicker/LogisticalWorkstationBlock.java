package com.simibubi.create.content.logistics.stockTicker;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.core.PartialModel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface LogisticalWorkstationBlock {
	
	@Nullable
	@OnlyIn(Dist.CLIENT)
	public PartialModel getHat(LevelAccessor level, BlockPos pos, LivingEntity keeper);

}
