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
import io.github.foundationgames.animatica.util.Flags;
import io.github.foundationgames.animatica.util.Utilities;
import io.github.foundationgames.animatica.util.exception.PropertyParseException;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

public final class AnimationLoader implements SynchronousResourceReloader {
    public static final String[] ANIM_PATHS = {
            "animatica/anim",
            "mcpatcher/anim",
            "optifine/anim"
    };
    public static final AnimationLoader INSTANCE = new AnimationLoader();

    private final Map<Identifier, BakedTextureAnimation> animatedTextures = new HashMap<>();

    private AnimationLoader() {
    }

    private static void findAllMCPAnimations(ResourceManager manager, Consumer<Identifier> action) {
        for (var path : ANIM_PATHS) {
            manager.findResources(path, p -> p.endsWith(".properties")).forEach(action);
        }
    }

    public BakedTextureAnimation getAnimation(Identifier id) {
        return animatedTextures.get(id);
    }

    @Override
    public void reload(ResourceManager manager) {
        if (!Animatica.CONFIG.animatedTextures) {
            this.animatedTextures.clear();
            return;
        }

        Flags.ALLOW_INVALID_ID_CHARS = true;

        this.animatedTextures.clear();
        var animations = new HashMap<Identifier, List<AnimationMeta>>();

        findAllMCPAnimations(manager, id -> {
            try {
                try (var resource = manager.getResource(id).getInputStream()) {
                    var ppt = new Properties();
                    ppt.load(resource);

                    var anim = AnimationMeta.of(id, ppt);

                    var targetId = anim.target();
                    if (!animations.containsKey(targetId)) animations.put(targetId, new ArrayList<>());
                    animations.get(targetId).add(anim);
                }
            } catch (IOException | PropertyParseException e) {
                Animatica.LOG.error(e.getMessage());
            }
        });

        int[] totalSize = {0};

        for (var targetId : animations.keySet()) {
            if (Animatica.CONFIG.safeMode) {
                try {
                    debugAnimation(totalSize, manager, targetId, animations.get(targetId));
                } catch (IOException e) {
                    Animatica.LOG.error("Error printing Safe Mode debug for animation {}\n {}: {}", targetId, e.getClass().getName(), e.getMessage());
                }
            } else this.animatedTextures.put(targetId, BakedTextureAnimation.bake(manager, targetId, animations.get(targetId)));
        }

        if (Animatica.CONFIG.safeMode) {
            Animatica.LOG.info("=== ESTIMATED TOTAL ANIMATION SIZE: {} BYTES ===", totalSize[0]);
        }

        Flags.ALLOW_INVALID_ID_CHARS = false;
    }

    public static void debugAnimation(int[] totalSize, ResourceManager manager, Identifier targetTex, List<AnimationMeta> anims) throws IOException {
        int[] frameCounts = new int[anims.size()];
        int frameWidth;
        int frameHeight;
        int bytesPerPix;

        try (var target = manager.getResource(targetTex).getInputStream()) {
            try (var img = NativeImage.read(target)) {
                frameWidth = img.getWidth();
                frameHeight = img.getHeight();
                bytesPerPix = img.getFormat().getChannelCount();
            }
        }

        for (int i = 0; i < anims.size(); i++) {
            var meta = anims.get(i);

            try (var source = manager.getResource(meta.source()).getInputStream()) {
                var tex = NativeImage.read(source);
                frameCounts[i] = Math.max((int)Math.floor((float) tex.getHeight() / meta.height()), meta.getGreatestUsedFrame() + 1);
            }
        }

        int frameCount = Utilities.lcm(frameCounts);
        int animSizeEstimate = frameWidth * frameHeight * bytesPerPix * frameCount;

        totalSize[0] += animSizeEstimate;

        Animatica.LOG.info("--- ANIMATION DEBUG FOR TEXTURE '{}' ---", targetTex.toString());
        Animatica.LOG.info(" - Total Compiled Frame Count: {}", frameCount);
        Animatica.LOG.info(" - Frame Dimensions: {}px by {}px", frameWidth, frameHeight);
        Animatica.LOG.info(" - Estimated Animation Size: {} BYTES", animSizeEstimate);
    }
}
