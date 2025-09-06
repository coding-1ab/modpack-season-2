package plus.dragons.createenchantmentindustry.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import plus.dragons.createdragonsplus.util.CodeReference;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.registry.CEIDamageTypes;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CodeReference(source = "create",license = "mit", targets = "com.simibubi.create.infrastructure.data.GenerateEntriesProvider")
public class CEIGenerateEntriesProvider extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, CEIDamageTypes::bootstrap);

    public CEIGenerateEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(CEICommon.ID));
    }

    @Override
    public String getName() {
        return "Create: Enchantment Industry Generated Registry Entries";
    }
}
