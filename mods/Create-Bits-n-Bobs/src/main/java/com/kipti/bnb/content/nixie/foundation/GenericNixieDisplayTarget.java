package com.kipti.bnb.content.nixie.foundation;

import com.google.gson.JsonParser;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class GenericNixieDisplayTarget extends DisplayTarget {
    @Override
    public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
        //TODO: multline support
        String tagElement = JsonParser.parseString(Component.Serializer.toJson(text.get(0), context.level().registryAccess())).getAsString();
        if (context.getTargetBlockEntity() instanceof GenericNixieDisplayBlockEntity blockEntity) {
            blockEntity.findControllerBlockEntity().applyTextToDisplay(tagElement, line);
        }
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        GenericNixieDisplayBlockEntity displayBlockEntity = (GenericNixieDisplayBlockEntity) context.getTargetBlockEntity();
        return new DisplayTargetStats(
            displayBlockEntity.currentDisplayOption == GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.DOUBLE_CHAR_DOUBLE_LINES
                ? 2 : 1,
            displayBlockEntity.seekWidth(),
            this
        );
    }
}
