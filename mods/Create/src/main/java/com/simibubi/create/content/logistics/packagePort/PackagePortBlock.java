package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PackagePortBlock extends Block implements IBE<PackagePortBlockEntity>, IWrenchable {

	public PackagePortBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public Class<PackagePortBlockEntity> getBlockEntityClass() {
		return PackagePortBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends PackagePortBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.PACKAGE_PORT.get();
	} 

}
