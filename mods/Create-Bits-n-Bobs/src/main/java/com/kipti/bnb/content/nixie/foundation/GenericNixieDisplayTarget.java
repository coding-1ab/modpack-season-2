package com.kipti.bnb.content.nixie.foundation;

import com.google.gson.JsonParser;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.SingleLineDisplayTarget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GenericNixieDisplayTarget extends SingleLineDisplayTarget {
    @Override
    protected void acceptLine(MutableComponent text, DisplayLinkContext context) {
        String tagElement = JsonParser.parseString(Component.Serializer.toJson(text, context.level().registryAccess())).getAsString();
        if (context.getTargetBlockEntity() instanceof GenericNixieDisplayBlockEntity blockEntity) {
            blockEntity.findControllerBlockEntity().applyTextToDisplay(tagElement);
        }
    }

    @Override
    protected int getWidth(DisplayLinkContext context) {
        if (context.getTargetBlockEntity() instanceof GenericNixieDisplayBlockEntity blockEntity) {
            return blockEntity.findControllerBlockEntity().seekWidth();
        }
        return 0; // Placeholder, should return the width of the display
    }
}
