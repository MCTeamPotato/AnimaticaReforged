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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.util.Flags;
import io.github.foundationgames.animatica.util.exception.PropertyParseException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;

public final class AnimationLoader implements SynchronousResourceReloader {
    public static final String[] ANIM_PATHS = {
            "animatica/anim",
            "mcpatcher/anim",
            "optifine/anim"
    };

    public static final AnimationLoader INSTANCE = new AnimationLoader();

    private final Map<Identifier, Identifier> animationIds = new Object2ObjectOpenHashMap<>();
    private final Set<AnimatedTexture> animatedTextures = new ObjectOpenHashSet<>();

    private AnimationLoader() {
    }

    private static void findAllMCPAnimations(ResourceManager manager, BiConsumer<Identifier, Resource> action) {
        for (var path : ANIM_PATHS) {
            manager.findResources(path, p -> p.getPath().endsWith(".properties")).forEach(action);
        }
    }

    public @Nullable Identifier getAnimationId(Identifier id) {
        return animationIds.get(id);
    }

    public void tickTextures() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::tickTextures);
        } else {
            for (var texture : animatedTextures) {
                texture.tick();
            }
        }
    }

    @Override
    public void reload(ResourceManager manager) {
        this.animatedTextures.clear();
        this.animationIds.clear();

        if (!Animatica.CONFIG.animatedTextures) {
            return;
        }

        Flags.ALLOW_INVALID_ID_CHARS = true;

        var animations = new Object2ObjectOpenHashMap<Identifier, List<AnimationMeta>>();

        findAllMCPAnimations(manager, (id, resource) -> {
            try {
                try (var resourceInputStream = resource.getInputStream()) {
                    var ppt = new Properties();
                    ppt.load(resourceInputStream);

                    var anim = AnimationMeta.of(id, ppt);

                    var targetId = anim.target();
                    if (!animations.containsKey(targetId)) animations.put(targetId, new ObjectArrayList<>());
                    animations.get(targetId).add(anim);
                }
            } catch (IOException | PropertyParseException e) {
                Animatica.LOG.error(e.getMessage());
            }
        });

        for (var targetId : animations.keySet()) {
            AnimatedTexture.tryCreate(manager, targetId, animations.get(targetId))
                    .ifPresent(tex -> {
                        var animId = new Identifier(targetId.getNamespace(), targetId.getPath() + "-anim");
                        this.animationIds.put(targetId, animId);
                        this.animatedTextures.add(tex);
                        tex.registerTexture(MinecraftClient.getInstance().getTextureManager(), manager, animId, MinecraftClient.getInstance());
                    });
        }

        Flags.ALLOW_INVALID_ID_CHARS = false;
    }
}
