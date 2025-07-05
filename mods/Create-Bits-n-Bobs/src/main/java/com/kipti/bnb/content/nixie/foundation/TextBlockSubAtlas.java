package com.kipti.bnb.content.nixie.foundation;

import com.kipti.bnb.CreateBitsnBobs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.HashMap;
import java.util.Map;

/**
 * Util class to handle a sub-atlas of text characters for Nixie displays.
 * This class generates UV coordinates for a set of characters, the texture must be already in the block atlas.
 */
public class TextBlockSubAtlas {

    /**
     * Additional chars are from Unicode private use area
     * */
    public static final TextBlockSubAtlas NIXIE_TEXT_SUB_ATLAS = new TextBlockSubAtlas(
        CreateBitsnBobs.asResource("block/nixie_board/text_atlas"),
        512, 16,
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ\nabcdefghijklmnopqrstuvwxyz\n1234567890(),.!?#$%\\@;:\n" +
            "\uE000\uE001\uE002\uE003\uE004\uE005\uE006\uE007\uE008\uE009\uE00A\uE00B\uE00C\uE00D\uE00E\uE00F" +
            "\uE010\uE011\uE012\uE013\uE014\uE015\uE016\uE017\uE018\uE019\uE01A\uE01B\uE01C\uE01D\uE01E\uE01F"
    );

    private final ResourceLocation atlasLocation;
    private final Map<Integer, Uv> characterUvs;

    public TextBlockSubAtlas(ResourceLocation atlasLocation, int textureSize, int elementSize, String characterSet) {
        this.atlasLocation = atlasLocation;
        Map<Character, Uv> localUvs = new HashMap<>();

        //Calculate the local uvs for each character in the set
        //Process the uv for each character in the set, if the cursor is at the end of a line, it will wrap to the next line
        // if a \n is encountered, it will reset the cursor to the start of the next line
        float step = ((float) textureSize / elementSize);

        float x = 0, y = 0;

        for (int i = 0; i < characterSet.length(); i++) {
            char c = characterSet.charAt(i);
            if (c == '\n') {
                x = 0;
                y += step;
                continue;
            }

            float u0 = x / textureSize;
            float v0 = y / textureSize;
            float u1 = (x + step) / textureSize;
            float v1 = (y + step) / textureSize;

            localUvs.put(c, new Uv(u0, v0, u1, v1));

            x += step;
            if (x >= textureSize) {
                x = 0;
                y += step;
            }
        }

        // Calculate the UVs for the block atlas
        Map<Integer, Uv> blockAtlasUvs = new HashMap<>();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(atlasLocation);
        float u0 = sprite.getU0(), v0 = sprite.getV0(),
              u1 = sprite.getU1(), v1 = sprite.getV1();
        for (Map.Entry<Character, Uv> entry : localUvs.entrySet()) {
            char c = entry.getKey();
            Uv local = entry.getValue();
            blockAtlasUvs.put((int) c, new Uv(
                u0 + local.u0 * (u1 - u0),
                v0 + local.v0 * (v1 - v0),
                u0 + local.u1 * (u1 - u0),
                v0 + local.v1 * (v1 - v0)
            ));
        }
        this.characterUvs = blockAtlasUvs;
    }

    public ResourceLocation getAtlasLocation() {
        return atlasLocation;
    }

    public boolean isInCharacterSet(int c) {
        return characterUvs.containsKey(c);
    }

    public Uv getUvForCharacter(int c) {
        return characterUvs.getOrDefault(c, new Uv(0, 0, 0, 0)); // Return empty UVs if character not found
    }

    public static class Uv {
        public final float u0;
        public final float v0;
        public final float u1;
        public final float v1;

        public Uv(float u0, float v0, float u1, float v1) {
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
        }

        public float getU0() {
            return u0;
        }

        public float getV0() {
            return v0;
        }

        public float getU1() {
            return u1;
        }

        public float getV1() {
            return v1;
        }
    }

}
