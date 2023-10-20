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
package io.github.foundationgames.animatica.util;

import io.github.foundationgames.animatica.util.exception.InvalidPropertyException;
import io.github.foundationgames.animatica.util.exception.MissingPropertyException;
import io.github.foundationgames.animatica.util.exception.PropertyParseException;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Properties;

public enum PropertyUtil {;
    public static @NotNull String get(Identifier file, @NotNull Properties properties, String key) throws PropertyParseException {
        var p = properties.getProperty(key);
        if (p == null) {
            throw new MissingPropertyException(file, key);
        }
        return p;
    }

    public static @NotNull Properties getSubProperties(@NotNull Properties properties, String key) {
        var p = new Properties();
        final var prefix = key + ".";
        for (String k : properties.stringPropertyNames()) {
            if (k.startsWith(prefix)) {
                var newKey = k.replaceFirst(prefix, "");
                p.setProperty(newKey, properties.getProperty(k));
            }
        }
        return p;
    }

    public static int getInt(Identifier file, Properties properties, String key) throws PropertyParseException {
        int r;
        try {
            r = Integer.parseInt(get(file, properties, key));
        } catch (NumberFormatException ignored) {
            throw new InvalidPropertyException(file, key, "integer (whole number)");
        }
        return r;
    }

    public static int getIntOr(Identifier file, @NotNull Properties properties, String key, int defaultVal) throws PropertyParseException {
        var p = properties.getProperty(key);
        if (p == null) {
            return defaultVal;
        }
        int r;
        try {
            r = Integer.parseInt(p);
        } catch (NumberFormatException ignored) {
            throw new InvalidPropertyException(file, key, "integer");
        }
        return r;
    }

    public static boolean getBoolOr(Identifier file, @NotNull Properties properties, String key, boolean defaultVal) throws PropertyParseException {
        var p = properties.getProperty(key);
        if (p == null) {
            return defaultVal;
        }
        if ("false".equals(p) || "true".equals(p)) {
            return "true".equals(p);
        }
        throw new InvalidPropertyException(file, key, "boolean (false/true)");
    }

    public static @NotNull @Unmodifiable Map<Integer, Integer> intToIntMap(@NotNull Properties in) {
        var map = new Int2IntOpenHashMap();
        for (String k : in.stringPropertyNames()) {
            try {
                map.put(Integer.parseInt(k), Integer.parseInt(in.getProperty(k)));
            } catch (NumberFormatException ignored) {}
        }
        return Int2IntMaps.unmodifiable(map);
    }
}
