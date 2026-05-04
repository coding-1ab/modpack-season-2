package dev.codinglabs.modpack.updator;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Registry(
        List<ModEntry> builtMods,
        List<ModSource.ExternalSource> extraMods
) {
    public static final Codec<Registry> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    ModEntry.CODEC.listOf().fieldOf("built_mods").forGetter(Registry::builtMods),
                    ModSource.ExternalSource.CODEC.listOf().fieldOf("extra_mods").forGetter(Registry::extraMods)
            ).apply(builder, Registry::new)
    );

    public record ModEntry(
            String fileName,
            long crc,
            ModSource assetSource
    ) {
        public static final Codec<ModEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.fieldOf("file_name").forGetter(ModEntry::fileName),
                Codec.STRING.comapFlatMap(string -> {
                    try {
                        return DataResult.success(Long.parseLong(string, 16));
                    } catch (NumberFormatException e) {
                        return DataResult.error(() -> string + " is not a valid CRC32");
                    }
                }, Long::toHexString).fieldOf("crc").forGetter(ModEntry::crc),
                ModSource.CODEC.optionalFieldOf("asset_source", ModSource.Inline.INSTANCE).forGetter(ModEntry::assetSource)
        ).apply(builder, ModEntry::new));
    }

    public sealed interface ModSource {
        Kind getKind();

        enum Kind {
            Inline("inline"),
            CurseForge("curseforge"),
            Modrinth("modrinth");

            private final String kind;
            public static final Codec<Kind> CODEC = Codec.STRING.comapFlatMap(string -> {
                switch(string) {
                    case "inline" -> {
                        return DataResult.success(Kind.Inline);
                    }
                    case "curseforge" -> {
                        return DataResult.success(Kind.CurseForge);
                    }
                    case "modrinth" -> {
                        return DataResult.success(Kind.Modrinth);
                    }
                    default -> {
                        return DataResult.error(() -> "Unknown asset source type: " + string);
                    }
                }
            }, Kind::kind);

            Kind(String name) {
                this.kind = name;
            }

            public @NotNull String kind() {
                return this.kind;
            }
        }

        Codec<ModSource> CODEC = Kind.CODEC.dispatch(ModSource::getKind, kind -> switch (kind) {
            case Inline -> Inline.CODEC;
            case CurseForge -> CurseForge.CODEC;
            case Modrinth -> Modrinth.CODEC;
        });

        sealed interface ExternalSource extends ModSource {
            Codec<ExternalSource> CODEC = ModSource.CODEC.comapFlatMap(modSource -> {
                switch (modSource) {
                    case ExternalSource externalSource -> {
                        return DataResult.success(externalSource);
                    }
                    case Inline ignored -> {
                        return DataResult.error(() -> "Inline is not external source");
                    }
                }
            }, it -> it);
        }

        final class Inline implements ModSource {
            public static final Inline INSTANCE = new Inline();

            private Inline() {
            }

            public static final MapCodec<Inline> CODEC = Codec.EMPTY.xmap(
                    unit -> INSTANCE,
                    inline -> Unit.INSTANCE
            );

            @Override
            public Kind getKind() {
                return Kind.Inline;
            }
        }

        record CurseForge(Long projectId, Long fileId) implements ExternalSource {
            public static final MapCodec<CurseForge> CODEC = RecordCodecBuilder.mapCodec(builder ->
                    builder.group(
                            Codec.LONG.fieldOf("project_id").forGetter(CurseForge::projectId),
                            Codec.LONG.fieldOf("file_id").forGetter(CurseForge::fileId)
                    ).apply(builder, CurseForge::new)
            );

            @Override
            public Kind getKind() {
                return Kind.CurseForge;
            }
        }

        record Modrinth(String version) implements ExternalSource {
            public static final MapCodec<Modrinth> CODEC = RecordCodecBuilder.mapCodec(builder ->
                    builder.group(
                            Codec.STRING.fieldOf("version").forGetter(Modrinth::version)
                    ).apply(builder, Modrinth::new)
            );

            @Override
            public Kind getKind() {
                return Kind.Modrinth;
            }
        }
    }
}
