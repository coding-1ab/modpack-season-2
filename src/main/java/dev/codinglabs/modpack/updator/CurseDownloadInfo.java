package dev.codinglabs.modpack.updator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CurseDownloadInfo(
        String fileName,
        String url,
        String sha1Hash
) {
    public static final Codec<CurseDownloadInfo> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("file_name").forGetter(CurseDownloadInfo::fileName),
            Codec.STRING.fieldOf("url").forGetter(CurseDownloadInfo::url),
            Codec.STRING.fieldOf("hash").forGetter(CurseDownloadInfo::sha1Hash)
    ).apply(builder, CurseDownloadInfo::new));
}
