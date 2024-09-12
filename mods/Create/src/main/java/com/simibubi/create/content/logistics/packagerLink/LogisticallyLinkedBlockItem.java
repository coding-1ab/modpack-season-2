package com.simibubi.create.content.logistics.packagerLink;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlock;
import com.simibubi.create.content.redstone.displayLink.ClickToLinkBlockItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;

public class LogisticallyLinkedBlockItem extends ClickToLinkBlockItem {

	public LogisticallyLinkedBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public boolean isValidTarget(LevelAccessor level, BlockPos pos) {
		Block block = level.getBlockState(pos)
			.getBlock();
		return (!AllBlocks.STOCK_TICKER.is(this) && block instanceof StockTickerBlock)
			|| block instanceof PackagerLinkBlock;
	}
	
	@Override
	public boolean placeWhenInvalid() {
		return true;
	}

	@Override
	public int getMaxDistanceFromSelection() {
		return -1;
	}

	@Override
	public String getMessageTranslationKey() {
		return "packager_link";
	}

}
