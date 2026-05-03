package com.tmvkrpxl0.modpack.updator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record ModrinthVersionInfo(
        String name,
        String versionNumber,
        Optional<String> changelog,
        List<ModrinthDependency> dependencies,
        List<String> gameVersions,
        String versionType,
        List<String> loaders,
        Optional<String> status,
        Optional<String> requestedStatus,
        String id,
        String projectId,
        String authorId,
        String datePublished,
        long downloads,
        Optional<String> changelogUrl,
        List<ModrinthFile> files
) {
    public static final Codec<ModrinthVersionInfo> CODEC = RecordCodecBuilder.create(instance ->
            instance.group (
                    Codec.STRING.fieldOf("name").forGetter(ModrinthVersionInfo::name),
                    Codec.STRING.fieldOf("version_number").forGetter(ModrinthVersionInfo::versionNumber),
                    Codec.STRING.optionalFieldOf("changelog").forGetter(ModrinthVersionInfo::changelog),
                    ModrinthDependency.CODEC.listOf().optionalFieldOf("dependencies", List.of()).forGetter(ModrinthVersionInfo::dependencies),
                    Codec.STRING.listOf().optionalFieldOf("game_versions", List.of()).forGetter(ModrinthVersionInfo::gameVersions),
                    Codec.STRING.fieldOf("version_type").forGetter(ModrinthVersionInfo::versionType),
                    Codec.STRING.listOf().optionalFieldOf("loaders", List.of()).forGetter(ModrinthVersionInfo::loaders),
                    Codec.STRING.optionalFieldOf("status").forGetter(ModrinthVersionInfo::status),
                    Codec.STRING.optionalFieldOf("requested_status").forGetter(ModrinthVersionInfo::requestedStatus),
                    Codec.STRING.fieldOf("id").forGetter(ModrinthVersionInfo::id),
                    Codec.STRING.fieldOf("project_id").forGetter(ModrinthVersionInfo::projectId),
                    Codec.STRING.fieldOf("author_id").forGetter(ModrinthVersionInfo::authorId),
                    Codec.STRING.fieldOf("date_published").forGetter(ModrinthVersionInfo::datePublished),
                    Codec.LONG.fieldOf("downloads").forGetter(ModrinthVersionInfo::downloads),
                    Codec.STRING.optionalFieldOf("changelog_url").forGetter(ModrinthVersionInfo::changelogUrl),
                    ModrinthFile.CODEC.listOf().fieldOf("files").forGetter(ModrinthVersionInfo::files)
            ).apply(instance, ModrinthVersionInfo::new)
    );

    public record ModrinthDependency(
            Optional<String> versionId,
            Optional<String> projectId,
            Optional<String> fileName,
            String dependencyType
    ) {
        public static final Codec<ModrinthDependency> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.optionalFieldOf("version_id").forGetter(ModrinthDependency::versionId),
                        Codec.STRING.optionalFieldOf("project_id").forGetter(ModrinthDependency::projectId),
                        Codec.STRING.optionalFieldOf("file_name").forGetter(ModrinthDependency::fileName),
                        Codec.STRING.fieldOf("dependency_type").forGetter(ModrinthDependency::dependencyType)
                ).apply(instance, ModrinthDependency::new)
        );
    }

    public record ModrinthFile(
            ModrinthHashes hashes,
            String url,
            String filename,
            boolean primary,
            long size,
            Optional<String> fileType
    ) {
        public static final Codec<ModrinthFile> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ModrinthHashes.CODEC.fieldOf("hashes").forGetter(ModrinthFile::hashes),
                        Codec.STRING.fieldOf("url").forGetter(ModrinthFile::url),
                        Codec.STRING.fieldOf("filename").forGetter(ModrinthFile::filename),
                        Codec.BOOL.fieldOf("primary").forGetter(ModrinthFile::primary),
                        Codec.LONG.fieldOf("size").forGetter(ModrinthFile::size),
                        Codec.STRING.optionalFieldOf("file_type").forGetter(ModrinthFile::fileType)
                ).apply(instance, ModrinthFile::new)
        );
    }

    public record ModrinthHashes(
            String sha512,
            String sha1
    ) {
        public static final Codec<ModrinthHashes> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("sha512").forGetter(ModrinthHashes::sha512),
                        Codec.STRING.fieldOf("sha1").forGetter(ModrinthHashes::sha1)
                ).apply(instance, ModrinthHashes::new)
        );
    }
}
