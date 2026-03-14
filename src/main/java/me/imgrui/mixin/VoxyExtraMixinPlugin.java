package me.imgrui.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

public class VoxyExtraMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        FabricLoader instance = FabricLoader.getInstance();

        if (mixinClassName.endsWith("minecraft.FogEnvironmentMixin")) {
            return instance.isModLoaded("sodium-extra");
        }

        if (mixinClassName.endsWith("flashback.FlashbackMetaMixin") || 
            mixinClassName.endsWith("flashback.FlashbackMixin") || 
            mixinClassName.endsWith("flashback.FlashbackRecorderMixin") || 
            mixinClassName.endsWith("flashback.FlashbackSaveReplayScreenMixin")) {
            return instance.isModLoaded("flashback");
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}