package plus.dragons.quicksand.mixin;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    private boolean isModLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        MixinExtrasBootstrap.init();
        try {
            Class.forName("plus.dragons.quicksand.common.QuicksandCommon", false, this.getClass().getClassLoader());
            isModLoaded = true;
        } catch (Exception ignored) {
            isModLoaded = false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        IClassBytecodeProvider bytecodeProvider = MixinService.getService().getBytecodeProvider();
        String loadWhen = Type.getDescriptor(LoadWhen.class);
        ClassNode mixinClass;

        try {
            mixinClass = bytecodeProvider.getClassNode(mixinClassName);
        } catch (ClassNotFoundException | IOException ignored) {
            return isModLoaded;
        }

        List<AnnotationNode> annotations = new ArrayList<>();
        {
            if (mixinClass.invisibleAnnotations != null) {
                annotations.addAll(mixinClass.invisibleAnnotations);
            }
            if (mixinClass.visibleAnnotations != null) {
                annotations.addAll(mixinClass.visibleAnnotations);
            }
        }

        for (AnnotationNode annotation : annotations) {
            if (annotation.desc.equals(loadWhen)) {
                String modId = Annotations.getValue(annotation, "modId");
                return FMLLoader.getLoadingModList().getMods().stream().anyMatch(
                        toLoad -> toLoad.getModId().equals(modId)
                );
            }
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public @Nullable List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
