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

import com.teampotato.iterable.MergedIterable;
import io.github.foundationgames.animatica.util.PropertyUtil;
import io.github.foundationgames.animatica.util.Utilities;
import io.github.foundationgames.animatica.util.exception.InvalidPropertyException;
import io.github.foundationgames.animatica.util.exception.PropertyParseException;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Properties;

public class AnimationMeta {
    private final Identifier source;
    private final Identifier target;
    private final int targetX;
    private final int targetY;
    private final int width;
    private final int height;
    private final int defaultFrameDuration;
    private final boolean interpolate;
    private final int interpolationDelay;
    private final Map<Integer, Integer> frameMapping;
    private final Map<Integer, Integer> frameDurations;

    public AnimationMeta(Identifier source, Identifier target, int targetX, int targetY, int width, int height,
                         int defaultFrameDuration, boolean interpolate, int interpolationDelay,
                         Map<Integer, Integer> frameMapping, Map<Integer, Integer> frameDurations) {
        this.source = source;
        this.target = target;
        this.targetX = targetX;
        this.targetY = targetY;
        this.width = width;
        this.height = height;
        this.defaultFrameDuration = defaultFrameDuration;
        this.interpolate = interpolate;
        this.interpolationDelay = interpolationDelay;
        this.frameMapping = frameMapping;
        this.frameDurations = frameDurations;
    }

    @Contract("_, _ -> new")
    public static @NotNull AnimationMeta of(Identifier file, Properties properties) throws PropertyParseException {
        Identifier source;
        Identifier target;
        try {
            source = Utilities.processPath(file, new Identifier(PropertyUtil.get(file, properties, "from")));
        } catch (InvalidIdentifierException ex) {
            throw new InvalidPropertyException(file, "from", "resource location");
        }
        try {
            target = Utilities.processPath(file, new Identifier(PropertyUtil.get(file, properties, "to")));
        } catch (InvalidIdentifierException ex) {
            throw new InvalidPropertyException(file, "to", "resource location");
        }
        return new AnimationMeta(
                source,
                target,
                PropertyUtil.getInt(file, properties, "x"),
                PropertyUtil.getInt(file, properties, "y"),
                PropertyUtil.getInt(file, properties, "w"),
                PropertyUtil.getInt(file, properties, "h"),
                PropertyUtil.getIntOr(file, properties, "duration", 1),
                PropertyUtil.getBoolOr(file, properties, "interpolate", false),
                PropertyUtil.getIntOr(file, properties, "skip", 0),
                PropertyUtil.intToIntMap(PropertyUtil.getSubProperties(properties, "tile")),
                PropertyUtil.intToIntMap(PropertyUtil.getSubProperties(properties, "duration"))
        );
    }

    public int getGreatestUsedFrame() {
        int greatestFrame = 0;
        for (int frame : new MergedIterable<>(frameMapping.keySet(), frameDurations.keySet())) {
            greatestFrame = Math.max(frame, greatestFrame);
        }

        return greatestFrame;
    }

    // Getter methods for the fields go here
    public Identifier source() {
        return source;
    }

    public Identifier target() {
        return target;
    }

    public int targetX() {
        return targetX;
    }

    public int targetY() {
        return targetY;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int defaultFrameDuration() {
        return defaultFrameDuration;
    }

    public boolean interpolate() {
        return interpolate;
    }

    public int interpolationDelay() {
        return interpolationDelay;
    }

    public Map<Integer, Integer> frameMapping() {
        return frameMapping;
    }

    public Map<Integer, Integer> frameDurations() {
        return frameDurations;
    }
}
