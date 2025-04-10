package plus.dragons.createenchantmentindustry.data;

import com.google.common.collect.Sets;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.advancement.AllTriggers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import plus.dragons.createdragonsplus.common.advancements.CDPAdvancement;
import plus.dragons.createdragonsplus.common.advancements.criterion.BuiltinTrigger;
import plus.dragons.createdragonsplus.util.CodeReference;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.util.CEIAdvancement;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class CEIAdvancements implements DataProvider {

    public static final List<CDPAdvancement> ENTRIES = new ArrayList<>();
    public static final CDPAdvancement START = null,

    ROOT = create("root", b -> b.icon(CEIItems.EXPERIENCE_BUCKET)
            .title("Welcome to Create: Enchantment Industry")
            .description("Road to master enchanting begins")
            .awardedForFree()
            .special(CDPAdvancement.TaskType.SILENT)),

    EXPERIENCED_ENGINEER = create("experienced_engineer",b -> b.icon(AllItems.EXP_NUGGET) // 灵魂流转
            .title("Experienced Engineer")
            .description("Get some Nuggets of Experience")
            .whenIconCollected()
            .after(ROOT)),

    SPIRIT_TAKING = create("spirit_taking",b -> b.icon(AllItems.EXP_NUGGET)
            .title("Spirit Taking")
            .description("Store your experience through an experience hatch")
            .after(EXPERIENCED_ENGINEER)),

    SPIRITUAL_RETURN = create("spiritual_return",b -> b.icon(AllItems.EXP_NUGGET)
            .title("Spiritual Return")
            .description("Retrieve some experience through an experience hatch")
            .after(SPIRIT_TAKING)),

    A_SHOWER_EXPERIENCE =create("a_shower_experience",b -> b.icon(AllItems.EXP_NUGGET)
            .title("A Shower \"Experience\"")
            .description("Break a Fluid Pipe and bathe in the leaked experience")
            .special(CDPAdvancement.TaskType.SECRET)
            .after(SPIRITUAL_RETURN)),

    GONE_WITH_THE_FOIL = create("gone_with_the_foil",b -> b.icon(CEIBlocks.MECHANICAL_GRINDSTONE)
            .title("Gone with the Foil")
            .description("Watch an enchanted item be disenchanted by a Mechanical Grindstone")
            .after(EXPERIENCED_ENGINEER)),

    ULTIMATE_SANDPAPER = create("ultimate_sandpaper",b -> b.icon(AllItems.SAND_PAPER)
            .title("Ultimate Sandpaper")
            .description("Sandpaper? Never heard of it.")
            .special(CDPAdvancement.TaskType.NOISY)
            .after(GONE_WITH_THE_FOIL)),

    EXPERIENCED_RECYCLER = create("experienced_recycler",b -> b.icon(CEIBlocks.GRINDSTONE_DRAIN)
            .title("Experienced Recycler")
            .description("Get 1,000,000 mB of experience from Mechanical Grindstone")
            .special(CDPAdvancement.TaskType.EXPERT)
            .whenStatReach(Stats.CUSTOM.get(CEIStats.GRINDSTONE_EXPERIENCE_GRIND.get()), MinMaxBounds.Ints.atLeast(1000000))
            .after(ULTIMATE_SANDPAPER));

    private static CDPAdvancement create(String id, UnaryOperator<CDPAdvancement.Builder> b) {
        return new CEIAdvancement(id, b);
    }

    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public CEIAdvancements(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return this.registries.thenCompose(provider -> {
            PackOutput.PathProvider pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
            List<CompletableFuture<?>> futures = new ArrayList<>();

            Set<ResourceLocation> set = Sets.newHashSet();
            Consumer<AdvancementHolder> consumer = (advancement) -> {
                ResourceLocation id = advancement.id();
                if (!set.add(id))
                    throw new IllegalStateException("Duplicate advancement " + id);
                Path path = pathProvider.json(id);
                LOGGER.info("Saving advancement {}", id);
                futures.add(DataProvider.saveStable(cache, provider, Advancement.CODEC, advancement.value(), path));
            };

            for (CDPAdvancement advancement : ENTRIES)
                advancement.save(consumer, provider);

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    public static void provideLang(BiConsumer<String, String> consumer) {
        for (CDPAdvancement advancement : ENTRIES)
            advancement.provideLang(consumer);
    }

    public static void register() {
    }

    @Override
    public String getName() {
        return "Create: Enchantment Industry Advancements";
    }

    @CodeReference(value = AllTriggers.class, source = "create", license = "mit")
    public static class BuiltinTriggersQuickDeploy {
        private static final Map<ResourceLocation, BuiltinTrigger> triggers = new IdentityHashMap<>();

        public static BuiltinTrigger add(ResourceLocation id) {
            var instance = new BuiltinTrigger();
            triggers.put(id,instance);
            return instance;
        }

        public static void register() {
            triggers.entrySet().forEach(set -> {
                Registry.register(BuiltInRegistries.TRIGGER_TYPES, set.getKey(), set.getValue());
            });
        }
    }
}
