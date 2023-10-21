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
import io.github.foundationgames.animatica.util.Utilities;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AnimatedTexture extends NativeImageBackedTexture {
    public final Animation[] anims;
    private final NativeImage original;
    private int frame = 0;

    public static Optional<AnimatedTexture> tryCreate(ResourceManager resources, Identifier targetTexId, List<AnimationMeta> anims) {
        try (var targetTexResource = resources.getResourceOrThrow(targetTexId).getInputStream()) {
            return Optional.of(new AnimatedTexture(resources, anims, NativeImage.read(targetTexResource)));
        } catch (IOException e) { Animatica.LOG.error(e); }

        return Optional.empty();
    }

    @SuppressWarnings("resource")
    public AnimatedTexture(ResourceManager resources, @NotNull List<AnimationMeta> metas, @NotNull NativeImage image) throws IOException {
        super(new NativeImage(image.getFormat(), image.getWidth(), image.getHeight(), true));

        this.anims = new Animation[metas.size()];
        for (int i = 0; i < metas.size(); i++) {
            this.anims[i] = new Animation(metas.get(i), resources);
        }
        this.original = image;

        updateAndDraw(this.getImage(), true);
        this.upload();
    }

    public boolean canLoop() {
        for (var anim : anims) {
            if (!anim.isOnFrameZero()) {
                return false;
            }
        }
        // All animations for this texture are at zero again, so the frame counter can be reset
        return true;
    }

    public boolean updateAndDraw(NativeImage image, boolean force) {
        boolean changed = false;

        if (canLoop()) {
            if (frame > 0) {
                frame = 0;
            }
        } else if (frame <= 0) {
            changed = true;
        }

        for (var anim : anims) {
            if (anim.isChanged()) {
                changed = true;
                break;
            }
        }

        if (changed || force) {
            image.copyFrom(this.original);

            Phase phase;
            for (var anim : anims) {
                phase = anim.getCurrentPhase();
                if (phase instanceof InterpolatedPhase iPhase) {
                    Utilities.blendCopy(anim.sourceTexture, 0, iPhase.prevV, 0, iPhase.v, anim.width, anim.height, image, anim.targetX, anim.targetY, iPhase.blend.getBlend(anim.getPhaseFrame()));
                } else {
                    Utilities.copy(anim.sourceTexture, 0, phase.v, anim.width, anim.height, image, anim.targetX, anim.targetY);
                }
            }
        }

        for (var anim : anims) {
            anim.advance();
        }
        frame++;

        return changed;
    }

    public void tick() {
        if (this.updateAndDraw(this.getImage(), false)) {
            this.upload();
        }
    }

    @Override
    public void close() {
        for (var anim : anims) {
            anim.close();
        }

        this.original.close();
        super.close();
    }

    // Represents an active animation from an animation meta file; progresses through phases while being drawn
    public static class Animation implements AutoCloseable {
        private final List<Phase> phases;
        public final NativeImage sourceTexture;
        public final int targetX;
        public final int targetY;
        public final int width;
        public final int height;
        private final int duration;

        private int frame = 0;
        private Phase currentPhase = null;
        private int phaseFrame = 0;
        private boolean changed = true;

        // Assembles all animation phases for one texture animation being baked
        public Animation(@NotNull AnimationMeta meta, @NotNull ResourceManager resources) throws IOException {
            this.targetX = meta.targetX();
            this.targetY = meta.targetY();
            this.width = meta.width();
            this.height = meta.height();

            try (var source = resources.getResourceOrThrow(meta.source()).getInputStream()) {
                this.sourceTexture = NativeImage.read(source);
            }

            ObjectList<Phase> phases = new ObjectArrayList<>();
            int duration = 0;

            final int textureFrameCount = (int)Math.floor((float) sourceTexture.getHeight() / meta.height());
            final int animFrameCount = Math.max(textureFrameCount, meta.getGreatestUsedFrame() + 1);

            // The int array stored for each frame must contain the frame mapping and duration
            List<int[]> frames = new ObjectArrayList<>();
            for (int f = 0; f < animFrameCount; f++) {
                if (f >= textureFrameCount && !meta.frameMapping().containsKey(f)) {
                    continue;
                }

                frames.add(new int[] {
                        meta.frameMapping().getOrDefault(f, f),
                        meta.frameDurations().getOrDefault(f, meta.defaultFrameDuration())
                });
            }

            for (int i = 0; i < frames.size(); i++) {
                int[] frame = frames.get(i);

                int fMap = frame[0];
                int fDuration = frame[1];

                int v = getVForFrame(fMap, textureFrameCount);
                int nextV = getVForFrame(frames.get(Math.floorMod(i + 1, frames.size()))[0], textureFrameCount);

                if (meta.interpolate()) {
                    if (meta.interpolationDelay() > 0) {
                        // Adds a static version of the current phase as a "delay" before the next interpolated phase (if specified in animation)
                        phases.add(new Phase(meta.interpolationDelay(), v));
                        duration += meta.interpolationDelay();
                    }

                    // Add interpolated animation phase
                    final int interpolatedDuration = fDuration - meta.interpolationDelay();
                    phases.add(new InterpolatedPhase(interpolatedDuration, v, nextV, (phaseFrame) -> ((float) phaseFrame / interpolatedDuration)));
                    duration += interpolatedDuration;
                } else {
                    phases.add(new Phase(fDuration, v));
                    duration += fDuration;
                }
            }

            this.duration = duration;
            this.phases = ObjectLists.unmodifiable(phases);

            updateCurrentPhase();
        }

        public void updateCurrentPhase() {
            changed = false;
            int progress = frame;

            for (var phase : phases) {
                progress -= phase.duration; // Take away as much progress as each phase is long, until progress is below zero
                if (progress < 0) {
                    if (currentPhase != phase) {
                        // Marks baking anim as changed should it be in a new, unique phase
                        changed = true;
                    }
                    if (phase instanceof InterpolatedPhase iPhase) changed = iPhase.hasChangingV(); // Marks baking anim as changed should its current phase be changing

                    this.currentPhase = phase;
                    this.phaseFrame = phase.duration + progress; // Adding progress to the phase duration results in how far it is into the phase

                    return;
                }
            }
        }

        public Phase getCurrentPhase() {
            return currentPhase;
        }

        public int getPhaseFrame() {
            return phaseFrame;
        }

        public boolean isOnFrameZero() {
            return frame <= 0;
        }

        public boolean isChanged() {
            return changed;
        }

        public void advance() {
            frame++;
            if (frame >= duration) {
                frame = 0;
            }
            updateCurrentPhase();
        }

        @Override
        public void close() {
            this.sourceTexture.close();
        }

        private int getVForFrame(int frame, int textureFrameCount) {
            return MathHelper.clamp(frame * this.height, 0, (textureFrameCount - 1) * this.height);
        }
    }

    // Represents a phase that an animation is in (loosely defined by the animation file's "tile"s)
    // Base class represents the simplest possible type of animation phase, which only needs to generate
    // one texture to be used over a period of time
    public static class Phase {
        public final int duration;
        public final int v;

        public Phase(int duration, int v) {
            this.duration = duration;
            this.v = v;
        }

        @Override
        public String toString() {
            return "Animation Bakery Phase { v: "+this.v+" }";
        }
    }

    // A phase that blends between its previous phase and itself, requiring the generation of many
    // more textures to construct the blend animation
    public static class InterpolatedPhase extends Phase {
        public final int prevV;
        public final BlendInterpolator blend;

        public InterpolatedPhase(int duration, int v1, int v2, BlendInterpolator blend) {
            super(duration, v2);
            this.prevV = v1;
            this.blend = blend;
        }

        public boolean hasChangingV() {
            return this.prevV != this.v;
        }
    }

    @FunctionalInterface
    public interface BlendInterpolator {
        float getBlend(int phaseFrame);
    }
}
