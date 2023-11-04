package org.antarcticgardens.newage.content.motors.extension;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class MotorExtensionScrollValueBehaviour extends ScrollValueBehaviour {

    protected int scaler;

    public MotorExtensionScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot, int scaler) {
        super(label, be, slot);
        this.scaler = scaler;
        withFormatter(v -> String.valueOf(Math.abs(v)));
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        ImmutableList<Component> rows = ImmutableList.of(Components.literal("%")
                        .withStyle(ChatFormatting.BOLD));
        ValueSettingsFormatter formatter = new ValueSettingsFormatter(this::formatSettings);
        value /= scaler;
        return new ValueSettingsBoard(label, max/scaler, 100, rows, formatter);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value = Math.max(1, valueSetting.value());
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue(value*scaler);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, Math.abs(value));
    }

    public MutableComponent formatSettings(ValueSettings settings) {
        return Lang.number(Math.max(1, Math.abs(settings.value() * scaler)))
                .add(Lang.text("%")
                        .style(ChatFormatting.BOLD))
                .component();
    }

    @Override
    public String getClipboardKey() {
        return "Stress Multiplier";
    }

}