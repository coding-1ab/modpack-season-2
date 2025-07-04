package com.kipti.bnb;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class CreateBitsnBobsData {

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        CreateBitsnBobs.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
            BiConsumer<String, String> langConsumer = provider::add;

//            PonderIndex.addPlugin(new DDPonderPlugin());
//            PonderIndex.getLangAccess().provideLang(CreateBitsnBobs.MOD_ID, langConsumer);
        });
//        event.getGenerator().addProvider(true,
//            CreateBitsnBobs.REGISTRATE.setDataProvider(
//                new RegistrateDataProvider(CreateBitsnBobs.REGISTRATE, CreateBitsnBobs.MOD_ID, event)
//            )
//        );
    }

}
