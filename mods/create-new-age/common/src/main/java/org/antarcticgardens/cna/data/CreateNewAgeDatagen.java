package org.antarcticgardens.cna.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.tterrag.registrate.providers.ProviderType;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.CNATags;
import org.antarcticgardens.cna.data.recipe.CNAMechanicalCraftingRecipeGen;
import org.antarcticgardens.cna.data.recipe.CNAProcessingRecipeGen;
import org.antarcticgardens.cna.data.recipe.CNASequencedAssemblyRecipeGen;
import org.antarcticgardens.cna.data.recipe.CNAStandardRecipeGen;
import org.antarcticgardens.cna.platform.DataProviderAdder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.Map;

public class CreateNewAgeDatagen {
    protected void setupDatagen(DataProviderAdder providerConsumer) {
        CreateNewAge.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
            try {
                File defaultLang = new File(ClassLoader.getSystemResource("assets/create_new_age/lang/default/").toURI());

                for (File file : defaultLang.listFiles()) {
                    if (file.getName().endsWith(".json")) {
                        JsonElement root = JsonParser.parseReader(new FileReader(file));

                        for (Map.Entry<String, JsonElement> e : root.getAsJsonObject().entrySet()) {
                            provider.add(e.getKey(), e.getValue().getAsString());
                        }
                    }
                }
            } catch (FileNotFoundException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
        
        CreateNewAge.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, provider -> {
            for (CNATags.Block tag : CNATags.Block.values()) {
                provider.copy(tag.blockTag, tag.itemTag);
            }
        });
        
        CreateNewAge.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, CNATags.Block::generate);
        CreateNewAge.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, CNATags.Item::generate);
        
        providerConsumer.addProvider(CNAStandardRecipeGen::new);
        providerConsumer.addProvider(CNAProcessingRecipeGen::new);
        providerConsumer.addProvider(CNAMechanicalCraftingRecipeGen::new);
        providerConsumer.addProvider(CNASequencedAssemblyRecipeGen::new);
        providerConsumer.addProvider(CNAGeneratedEntriesProvider::new);
    }
}
