package io.github.foundationgames.animatica.mixin.embeddium;

import io.github.foundationgames.animatica.Animatica;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.client.gui.options.*;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.SodiumOptionsStorage;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = SodiumGameOptionPages.class, remap = false)
public abstract class SodiumGameOptionPagesMixin {
    @Shadow @Final private static SodiumOptionsStorage sodiumOpts;

    @Inject(method = "general", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false, ordinal = 2, shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void insertSetting(CallbackInfoReturnable<OptionPage> cir, @NotNull List<OptionGroup> groups) {
        OptionImpl<SodiumGameOptions, Boolean> animatedTextures = OptionImpl.createBuilder(Boolean.TYPE, sodiumOpts)
                .setName(Text.translatable("option.animatica.animated_textures"))
                .setTooltip(Text.of(""))
                .setControl(TickBoxControl::new)
                .setBinding((sodiumGameOptions, aBoolean) -> Animatica.ANIMATED_TEXTURES.set(aBoolean), sodiumGameOptions -> Animatica.ANIMATED_TEXTURES.get())
                .setImpact(OptionImpact.VARIES)
                .setFlags(new OptionFlag[]{OptionFlag.REQUIRES_ASSET_RELOAD})
                .build();
        groups.add(OptionGroup.createBuilder().add(animatedTextures).build());
    }
}
