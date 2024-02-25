package org.antarcticgardens.cna.forge;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.CNABlocks;
import org.antarcticgardens.cna.platform.PlatformRegistrar;

public class ForgeRegistrar implements PlatformRegistrar {
    private static final DeferredRegister<CreativeModeTab> TAB_REGISTER = 
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateNewAge.MOD_ID);

    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPE_REGISTER = 
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, CreateNewAge.MOD_ID);
    
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTER = 
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreateNewAge.MOD_ID);
    
    @Override
    public void registerConfig(ModConfig.Type type, IConfigSpec<?> spec) {
        ModLoadingContext.get().registerConfig(type, spec);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public RecipeType<?> registerRecipe(String name, RecipeSerializer<?> serializer) {
        RecipeType type = new RecipeType() {};

        RECIPE_TYPE_REGISTER.register(name, () -> type);
        RECIPE_SERIALIZER_REGISTER.register(name, () -> serializer);
        
        return type;
    }

    @Override
    public void beforeRegistration() {
        RegistryObject<CreativeModeTab> tab = TAB_REGISTER.register("tab",
                () -> CreativeModeTab.builder()
                        .title(Component.translatable("tab." + CreateNewAge.MOD_ID + ".tab"))
                        .icon(CNABlocks.GENERATOR_COIL::asStack)
                        .build()
        );
        
        CreateNewAge.REGISTRATE.setCreativeTab(tab);
        TAB_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPE_TYPE_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPE_SERIALIZER_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @Override
    public void afterRegistration() {
        CreateNewAge.REGISTRATE.registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
