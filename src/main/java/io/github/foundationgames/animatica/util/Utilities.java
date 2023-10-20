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

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

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
}
