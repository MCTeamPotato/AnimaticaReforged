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
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Properties;

public final class Utilities {
    public static @NotNull Identifier processPath(Identifier fileRelativeTo, @NotNull Identifier path) {
        if (path.getPath().startsWith("./")) {
            int lInd = fileRelativeTo.getPath().lastIndexOf("/");
            if (lInd > 0) {
                StringBuilder builder = new StringBuilder(fileRelativeTo.getPath());
                builder.replace(lInd, builder.length(), path.getPath().replaceFirst("\\./", "/"));
                return new Identifier(fileRelativeTo.getNamespace(), builder.toString());
            }
        } else if (path.getPath().startsWith("~/")) {
            return new Identifier(path.getNamespace(), path.getPath().replaceFirst("~/", "optifine/"));
        }
        return path;
    }

    public static @NotNull String get(Identifier file, @NotNull Properties properties, String key) throws PropertyParseException {
        String p = properties.getProperty(key);
        if (p == null) {
            throw new MissingPropertyException(file, key);
        }
        return p;
    }

    public static @NotNull Properties getSubProperties(@NotNull Properties properties, String key) {
        Properties p = new Properties();
        final String prefix = key + ".";
        for (String k : properties.stringPropertyNames()) {
            if (k.startsWith(prefix)) {
                String newKey = k.replaceFirst(prefix, "");
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
        String p = properties.getProperty(key);
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
        String p = properties.getProperty(key);
        if (p == null) {
            return defaultVal;
        }
        if ("false".equals(p) || "true".equals(p)) {
            return "true".equals(p);
        }
        throw new InvalidPropertyException(file, key, "boolean (false/true)");
    }

    public static @Unmodifiable @NotNull Map<Integer, Integer> intToIntMap(@NotNull Properties in) {
        Int2IntOpenHashMap map = new Int2IntOpenHashMap();
        for (String k : in.stringPropertyNames()) {
            try {
                map.put(Integer.parseInt(k), Integer.parseInt(in.getProperty(k)));
            } catch (NumberFormatException ignored) {}
        }
        return Int2IntMaps.unmodifiable(map);
    }

    /**
     * Copy a section of an image into another image
     *
     * @param src The source image to copy from
     * @param u The u coordinate on the source image to start the selection from
     * @param v The v coordinate on the source image to start the selection from
     * @param w The width of the selection area
     * @param h The height of the selection area
     * @param dest The destination image to copy to
     * @param du The u coordinate on the destination image to place the selection at
     * @param dv The v coordinate on the destination image to place the selection at
     */
    public static void copy(NativeImage src, int u, int v, int w, int h, NativeImage dest, int du, int dv) {
        // iterate through the entire section of the image to be copied over
        for (int rx = 0; rx < w; rx++) {
            for (int ry = 0; ry < h; ry++) {
                // the current x/y coordinates in the source image
                int srcX = u + rx;
                int srcY = v + ry;
                // the corresponding target x/y coordinates in the target image
                int trgX = du + rx;
                int trgY = dv + ry;

                // set the color of the target pixel on the destination image
                // to the color from the corresponding pixel on the source image
                dest.setPixelColor(trgX, trgY, src.getPixelColor(srcX, srcY));
            }
        }
    }

    /**
     * Copy a blend between 2 sections on a source image to a destination image
     *
     * @param src The source image to copy from
     * @param u0 The u coordinate on the source image to start the first selection from
     * @param v0 The v coordinate on the source image to start the first selection from
     * @param u1 The u coordinate on the source image to start the second selection from
     * @param v1 The v coordinate on the source image to start the second selection from
     * @param w The width of the selection area
     * @param h The height of the selection area
     * @param dest The destination image to copy to
     * @param du The u coordinate on the destination image to place the selection at
     * @param dv The v coordinate on the destination image to place the selection at
     * @param blend The blend between the first selection from the source and the
     *              second (0 = solid first image, 1 = solid second image)
     */
    public static void blendCopy(NativeImage src, int u0, int v0, int u1, int v1, int w, int h, NativeImage dest, int du, int dv, float blend) {
        // iterate through the entire section of the image to be copied over
        for (int rx = 0; rx < w; rx++) {
            for (int ry = 0; ry < h; ry++) {
                // the first set of x/y coordinates in the source image
                int srcX0 = u0 + rx;
                int srcY0 = v0 + ry;
                // the second set of x/y coordinates in the source image
                int srcX1 = u1 + rx;
                int srcY1 = v1 + ry;
                // the corresponding target x/y coordinates in the target image
                int trgX = du + rx;
                int trgY = dv + ry;

                // set the color of the target pixel on the destination image to a blend
                // of the colors from the corresponding pixels on the source image
                dest.setPixelColor(trgX, trgY, lerpColor(src.getFormat(), src.getPixelColor(srcX0, srcY0), src.getPixelColor(srcX1, srcY1), blend));
            }
        }
    }

    public static int lerpColor(NativeImage.@NotNull Format format, int c1, int c2, float delta) {
        int a1 = (c1 >> format.getAlphaChannelOffset()) & 0xFF;
        int r1 = (c1 >> format.redOffset) & 0xFF;
        int g1 = (c1 >> format.greenOffset) & 0xFF;
        int b1 = (c1 >> format.blueOffset) & 0xFF;

        int a2 = (c2 >> format.getAlphaChannelOffset()) & 0xFF;
        int r2 = (c2 >> format.redOffset) & 0xFF;
        int g2 = (c2 >> format.greenOffset) & 0xFF;
        int b2 = (c2 >> format.blueOffset) & 0xFF;

        // If the first or second color is transparent,
        // don't lerp any leftover rgb values and instead
        // only use those of the non-transparent color
        if (a1 == 0) {
            r1 = r2;
            g1 = g2;
            b1 = b2;
        } else if (a2 == 0) {
            r2 = r1;
            g2 = g1;
            b2 = b1;
        }

        int oa = (int) MathHelper.lerp(delta, a1, a2);
        int or = (int) MathHelper.lerp(delta, r1, r2);
        int og = (int) MathHelper.lerp(delta, g1, g2);
        int ob = (int) MathHelper.lerp(delta, b1, b2);

        return (oa << format.getAlphaChannelOffset()) | (or << format.redOffset) | (og << format.greenOffset) | (ob << format.blueOffset);
    }
}
