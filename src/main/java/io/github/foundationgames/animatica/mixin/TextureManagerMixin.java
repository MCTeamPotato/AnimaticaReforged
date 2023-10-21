package io.github.foundationgames.animatica.mixin;

import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.animation.AnimationLoader;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
    @ModifyVariable(method = "bindTexture", at = @At("HEAD"), index = 1, argsOnly = true)
    private Identifier animatica$replaceWithAnimatedTexture(Identifier old) {
        if (Animatica.ANIMATED_TEXTURES.get()) {
            Identifier anim = AnimationLoader.INSTANCE.getAnimationId(old);
            if (anim != null) {
                return anim;
            }
        }
        return old;
    }
}
