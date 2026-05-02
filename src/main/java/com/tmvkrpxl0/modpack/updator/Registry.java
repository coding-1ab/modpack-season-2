package com.tmvkrpxl0.modpack.updator;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

record Registry(
        List<ModEntry> mods
) {
    public static final Codec<Registry> CODEC = Codec.list(ModEntry.CODEC).xmap(Registry::new, Registry::mods);

    public record ModEntry(
            String fileName,
            long crc,
            AssetSource assetSource
    ) {
        public static final Codec<ModEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.fieldOf("fileName").forGetter(ModEntry::fileName),
                Codec.STRING.comapFlatMap(string -> {
                    try {
                        return DataResult.success(Long.parseLong(string, 16));
                    } catch (NumberFormatException e) {
                        return DataResult.error(() -> string + " is not a valid CRC32");
                    }
                }, Long::toHexString).fieldOf("crc").forGetter(ModEntry::crc),
                AssetSource.CODEC.optionalFieldOf("assetSource", AssetSource.Inline.INSTANCE).forGetter(ModEntry::assetSource)
        ).apply(builder, ModEntry::new));
    }

    public sealed interface AssetSource {
        Codec<AssetSource> CODEC = Codec
                .either(Inline.CODEC, CurseForge.CODEC)
                .xmap(either -> either.map(a -> a, b -> b),
                        source -> {
                            switch (source) {
                                case Inline inline -> {
                                    return Either.left(inline);
                                }
                                case CurseForge curseForge -> {
                                    return Either.right(curseForge);
                                }
                            }
                        }
                );

        final class Inline implements AssetSource {
            public static final Inline INSTANCE = new Inline();

            private Inline() {
            }

            public static final Codec<Inline> CODEC = Codec.EMPTY.xmap(
                    unit -> INSTANCE,
                    inline -> Unit.INSTANCE
            ).codec();
        }

        record CurseForge(Long projectId, Long fileId) implements AssetSource {
            public static final Codec<CurseForge> CODEC = RecordCodecBuilder.create(builder ->
                    builder.group(
                            Codec.LONG.fieldOf("projectId").forGetter(CurseForge::projectId),
                            Codec.LONG.fieldOf("fieldId").forGetter(CurseForge::fileId)
                    ).apply(builder, CurseForge::new)
            );
        }
        // TODO Modrinth API 쓰는법 알아내기 class Modrinth : AssetSource()
    }
}
