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

import com.teampotato.potacore.iteration.MergedIterable;
import io.github.foundationgames.animatica.util.Utilities;
import io.github.foundationgames.animatica.util.exception.InvalidPropertyException;
import io.github.foundationgames.animatica.util.exception.PropertyParseException;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Properties;

public record AnimationMeta(Identifier source, Identifier target, int targetX, int targetY, int width, int height, int defaultFrameDuration, boolean interpolate, int interpolationDelay, Map<Integer, Integer> frameMapping, Map<Integer, Integer> frameDurations) {
    @Contract("_, _ -> new")
    public static @NotNull AnimationMeta of(Identifier file, Properties properties) throws PropertyParseException {
        Identifier source;
        Identifier target;
        try {
            source = Utilities.processPath(file, new Identifier(Utilities.get(file, properties, "from")));
        } catch (InvalidIdentifierException ex) { throw new InvalidPropertyException(file, "from", "resource location"); }
        try {
            target = Utilities.processPath(file, new Identifier(Utilities.get(file, properties, "to")));
        } catch (InvalidIdentifierException ex) { throw new InvalidPropertyException(file, "to", "resource location"); }
        return new AnimationMeta(
                source,
                target,
                Utilities.getInt(file, properties, "x"),
                Utilities.getInt(file, properties, "y"),
                Utilities.getInt(file, properties, "w"),
                Utilities.getInt(file, properties, "h"),
                Utilities.getIntOr(file, properties, "duration", 1),
                Utilities.getBoolOr(file, properties, "interpolate", false),
                Utilities.getIntOr(file, properties, "skip", 0),
                Utilities.intToIntMap(Utilities.getSubProperties(properties, "tile")),
                Utilities.intToIntMap(Utilities.getSubProperties(properties, "duration"))
        );
    }

    public int getGreatestUsedFrame() {

        int greatestFrame = 0;
        for (int frame : new MergedIterable<>(frameMapping.keySet(), frameDurations.keySet())) {
            greatestFrame = Math.max(frame, greatestFrame);
        }

        return greatestFrame;
    }
}
