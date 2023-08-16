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
package io.github.foundationgames.animatica.animation;

import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.animation.bakery.AnimationBakery;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.List;

public class BakedTextureAnimation {
    private static final Identifier EMPTY = new Identifier("empty");

    private final Identifier[] frames;

    private BakedTextureAnimation(Identifier[] frames) {
        this.frames = frames;
    }

    public static BakedTextureAnimation bake(ResourceManager resources, Identifier targetId, List<AnimationMeta> anims) {
        try (var bakery = new AnimationBakery(resources, targetId, anims)) {
            return new BakedTextureAnimation(bakery.bakeAndUpload());
        } catch (IOException e) { Animatica.LOG.error(e); }
        return new BakedTextureAnimation(new Identifier[] {EMPTY});
    }

    public Identifier getTextureForFrame() {
        if (frames.length <= 0) {
            return EMPTY;
        }
        return frames[(int)(Animatica.getTime() % frames.length)];
    }
}
