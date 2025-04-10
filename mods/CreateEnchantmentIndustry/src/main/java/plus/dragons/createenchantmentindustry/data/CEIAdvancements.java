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
import net.minecraft.world.item.Items;
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

    EXPERIENCED_ENGINEER = create("experienced_engineer",b -> b.icon(AllItems.EXP_NUGGET) // “经验丰富”的工程师
            .title("Experienced Engineer")
            .description("Get some Nuggets of Experience")
            .whenIconCollected()
            .after(ROOT)),

    SPIRIT_TAKING = create("spirit_taking",b -> b.icon(AllItems.EXP_NUGGET) // 摄人魂魄
            .title("Spirit Taking")
            .description("Store your experience through an experience hatch")
            .after(EXPERIENCED_ENGINEER)),

    SPIRITUAL_RETURN = create("spiritual_return",b -> b.icon(AllItems.EXP_NUGGET) // 灵质归流
            .title("Spiritual Return")
            .description("Retrieve some experience through an experience hatch")
            .after(SPIRIT_TAKING)),

    A_SHOWER_EXPERIENCE =create("a_shower_experience",b -> b.icon(AllItems.EXP_NUGGET) // 沐浴“经验”
            .title("A Shower \"Experience\"")
            .description("Break a Fluid Pipe and bathe in the leaked experience")
            .special(CDPAdvancement.TaskType.SECRET)
            .after(SPIRITUAL_RETURN)),

    // Mechanical Grindstone
    GONE_WITH_THE_FOIL = create("gone_with_the_foil",b -> b.icon(CEIBlocks.MECHANICAL_GRINDSTONE) // 洗尽铅华
            .title("Gone with the Foil")
            .description("Watch an enchanted item be disenchanted by a Mechanical Grindstone")
            .after(EXPERIENCED_ENGINEER)),

    GRIND_TO_POLISH = create("grind_to_polish",b -> b.icon(AllItems.SAND_PAPER) // 磨砺成莹
            .title("GRIND_TO_POLISH")
            .description("Sandpaper? I've gotten better one")
            .special(CDPAdvancement.TaskType.NOISY)
            .after(GONE_WITH_THE_FOIL)),

    EXPERIENCED_RECYCLER = create("experienced_recycler",b -> b.icon(CEIBlocks.GRINDSTONE_DRAIN) // “经验丰富”的回收工
            .title("Experienced Recycler")
            .description("Get 1,000,000 mB of experience from Mechanical Grindstone")
            .special(CDPAdvancement.TaskType.EXPERT)
            .whenStatReach(Stats.CUSTOM.get(CEIStats.GRINDSTONE_EXPERIENCE.get()), MinMaxBounds.Ints.atLeast(1000000))
            .after(GONE_WITH_THE_FOIL)),

    // Blaze Enchanter
    BLAZE_ENCHANTERY = create("blaze_enchantery",b -> b.icon(CEIBlocks.BLAZE_ENCHANTER) // 火灵工坊
            .title("Blaze Enchantery")
            .description("Blaze can do more than boil water. Obtain a Blaze Enchanter")
            .whenIconCollected()
            .after(EXPERIENCED_ENGINEER)),

    BLAZING_ENCHANTMENT = create("blazing_enchantment",b -> b.icon(Items.GOLDEN_HELMET) // 炎铸神兵
            .title("Blazing Enchantment")
            .description("Add a new enchantment to an unenchanted item using Blaze Enchanter")
            .after(BLAZE_ENCHANTERY)),

    SIGIL_FORGING = create("sigil_forging",b -> b.icon(CEIItems.ENCHANTING_TEMPLATE) // 烙咒成规
            .title("Sigil Forging")
            .description("Add a new enchantment to an enchanting template")
            .after(BLAZING_ENCHANTMENT)),

    THOUSAND_RUNES = create("thousand_runes",b -> b.icon(CEIBlocks.BLAZE_ENCHANTER) // 炎铸千咒
            .title("Thousand Runes")
            .description("Blaze Enchanter enchants 1,000 times")
            .whenStatReach(Stats.CUSTOM.get(CEIStats.ENCHANT.get()), MinMaxBounds.Ints.atLeast(1000))
            .special(CDPAdvancement.TaskType.EXPERT)
            .after(SIGIL_FORGING)),

    // Blaze Forger
    BORN_TALENT_OF_FIRE = create("born_talent_of_fire",b -> b.icon(CEIBlocks.BLAZE_FORGER) // 炽匠本源
            .title("Born Talent of Fire")
            .description("Blazer is good at this. Obtain a Blaze Forger")
            .whenIconCollected()
            .after(EXPERIENCED_ENGINEER)),

    BLAZING_FUSION = create("blazing_fusion",b -> b.icon(Items.GOLDEN_SWORD) // 炎熔合铸
            .title("Blazing Fusion")
            .description("Combine two items of same kind")
            .whenIconCollected()
            .after(BORN_TALENT_OF_FIRE)),

    SIGIL_CASTING = create("sigil_casting",b -> b.icon(CEIItems.ENCHANTING_TEMPLATE) // 烙印成规
            .title("Sigil Casting")
            .description("Apply an Enchanting Template")
            .whenIconCollected()
            .after(BLAZING_FUSION)),

    MAGIC_UNBINDING = create("magic_unbinding",b -> b.icon(CEIItems.ENCHANTING_TEMPLATE) // 淬咒返源
            .title("Magic Unbinding")
            .description("Strip enchantment off item")
            .whenIconCollected()
            .after(SIGIL_CASTING)),

    BLAZING_CENTURION = create("blazing_centurion",b -> b.icon(CEIBlocks.BLAZE_FORGER) // 百炼炎心
            .title("Blazing Centurion")
            .description("Blaze Forger forges 1,000 times")
            .whenStatReach(Stats.CUSTOM.get(CEIStats.FORGE.get()), MinMaxBounds.Ints.atLeast(1000))
            .special(CDPAdvancement.TaskType.EXPERT)
            .after(SIGIL_FORGING)),


    // Super Enchant
    LIGHTNING_CATALYSIS = create("lightning_catalysis",b -> b.icon(CEIBlocks.BLAZE_FORGER) // 雷霆萃取
            .title("Lightning Catalysis")
            .description("Obtain Super Experience")
            .whenItemCollected(CEIBlocks.SUPER_EXPERIENCE_BLOCK)
            .special(CDPAdvancement.TaskType.EXPERT)
            .after(EXPERIENCED_ENGINEER)),

    PROBABILITY_SPIKE = create("probability_spike",b -> b.icon(Items.DIAMOND) // 概率奇点
            .title("Probability Spike")
            .description("How did all these treasures get here?")
            .special(CDPAdvancement.TaskType.EXPERT)
            .after(LIGHTNING_CATALYSIS)),

    TRANSCENDENT_OVERCLOCK = create("transcendent_overclock",b -> b.icon(Items.EMERALD) // 维度突破
            .title("Transcendent Overclock")
            .description("How could it happen? Enchantment Level Cap Didn't exist?")
            .special(CDPAdvancement.TaskType.EXPERT)
            .after(PROBABILITY_SPIKE)),

    PARADOX_FUSION = create("paradox_fusion",b -> b.icon(Items.REDSTONE) // 悖论熔接
            .title("Paradox Fusion")
            .description("How could it happen? They should not appear at same time!")
            .special(CDPAdvancement.TaskType.EXPERT)
            .after(TRANSCENDENT_OVERCLOCK)),

    OSHA_VIOLATION = create("osha_violation",b -> b.icon(Items.BARRIER) // 天谴牛马
            .title("OSHA Violation")
            .description("You SHOULD NOT let it happen!!!")
            .special(CDPAdvancement.TaskType.SECRET)
            .after(LIGHTNING_CATALYSIS)),

    OMNI_ENCHANTER = create("omni_enchanter",b -> b.icon(Items.NETHER_STAR) // 万法归源
            .title("Omni-Enchanter")
            .description("Unbelievable! You've super-enchanted 1,000 times! Now the number speaks for itself")
            .whenStatReach(Stats.CUSTOM.get(CEIStats.SUPER_ENCHANT.get()), MinMaxBounds.Ints.atLeast(100))
            .special(CDPAdvancement.TaskType.EXPERT)
            .after(PARADOX_FUSION)),

    // Printer
    COPIABLE_MASTERPIECE = create("copiable_masterpiece",b -> b.icon(CEIBlocks.BLAZE_FORGER) // 誊写杰作
            .title("Copiable Masterpiece")
            .description("Copy a Written Book using Printer")
            .after(EXPERIENCED_ENGINEER)),

    COPIABLE_MYSTERY = create("copiable_mystery",b -> b.icon(Items.ENCHANTED_BOOK) // 复刻奥秘
            .title("Copiable Mystery")
            .description("Copy a Enchanted Book using Printer")
            .after(COPIABLE_MASTERPIECE)),

    BRAND_REGISTRY = create("brand_registry",b -> b.icon(CEIBlocks.BLAZE_FORGER) // 商标注册
            .title("Brand Registry")
            .description("Using the printer to name an item")
            .whenIconCollected()
            .after(COPIABLE_MYSTERY)),

    GREAT_PUBLISHER = create("great_publisher",b -> b.icon(CEIBlocks.PRINTER) // 大出版家
            .title("Great Publisher")
            .description("Printer prints 1,000 times")
            .whenStatReach(Stats.CUSTOM.get(CEIStats.PRINT.get()), MinMaxBounds.Ints.atLeast(1000))
            .special(CDPAdvancement.TaskType.EXPERT)
            .after(BRAND_REGISTRY));

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
