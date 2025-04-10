package plus.dragons.createenchantmentindustry.util;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createdragonsplus.common.advancements.CDPAdvancement;
import plus.dragons.createdragonsplus.common.advancements.criterion.BuiltinTrigger;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.data.CEIAdvancements;

import java.util.function.UnaryOperator;

public class CEIAdvancement extends CDPAdvancement {
    public CEIAdvancement(String id, UnaryOperator<Builder> b) {
        super(id, b);
    }

    @Override
    protected @NotNull BuiltinTrigger add(@NotNull ResourceLocation id) {
        return CEIAdvancements.BuiltinTriggersQuickDeploy.add(id);
    }

    @Override
    protected void addToAdvancementEntries() {
        CEIAdvancements.ENTRIES.add(this);
    }

    @Override
    protected @NotNull ResourceLocation getBackground() {
        return CEICommon.asResource("textures/gui/advancements.png");
    }

    @Override
    protected @NotNull String namespace() {
        return CEICommon.ID;
    }
}
