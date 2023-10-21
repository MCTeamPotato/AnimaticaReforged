/*
 * This file is part of Animatica - https://github.com/FoundationGames/Animatica
 * Copyright (C) FoundationGames: https://github.com/FoundationGames/Animatica
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.foundationgames.animatica.mixin;

import io.github.foundationgames.animatica.Animatica;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Allows invalid characters in paths to support packs with extremely outdated formatting (because OptiFine does too)
// animatica$reportInvalidIdentifierCharacters and animatica$isPathAllowed are very expensive during Identifier initialization, so they're disabled
@SuppressWarnings("CommentedOutCode")
@Mixin(Identifier.class)
public class IdentifierMixin {
    /*@Inject(method = "<init>([Ljava/lang/String;)V", at = @At("TAIL"))
    private void animatica$reportInvalidIdentifierCharacters(String[] id, CallbackInfo ci) {
        if (Flags.ALLOW_INVALID_ID_CHARS && !animatica$isPathAllowed(id[1]) && !id[1].startsWith("~/")) {
            Animatica.LOG.warn("Legacy resource pack is using an invalid namespaced identifier '{}:{}'! DO NOT use non [a-z0-9_.-] characters for resource pack files and file names!", id[0], id[1]);
        }
    }*/

    @Inject(method = "isPathCharacterValid", at = @At("RETURN"), cancellable = true)
    private static void animatica$allowInvalidCharacters(char character, CallbackInfoReturnable<Boolean> cir) {
        if (Animatica.ALLOW_INVALID_ID_CHARS) {
            cir.setReturnValue(true);
        }
    }

   /* @Unique
    private static boolean animatica$isPathAllowed(String path) {
        if (path == null) return true;
        for (char c : path.toCharArray()) {
            if (!(c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '/' || c == '.')) {
                return false;
            }
        }
        return true;
    }*/
}
