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
package io.github.foundationgames.animatica.config;

import io.github.foundationgames.animatica.Animatica;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.BooleanOption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AnimaticaConfig {
    public static String ANIMATED_TEXTURES_KEY = "animated_textures";

    public static final String FILE_NAME = "animatica.properties";

    public static final BooleanOption animatedTexturesOption = new BooleanOption(
            "option.animatica.animated_textures",
            gameOptions -> {
                try {
                    Animatica.CONFIG.load();
                } catch (IOException e) { Animatica.LOG.error("Error loading config for options screen!", e); }
                return Animatica.CONFIG.animatedTextures;
            },
            (gameOptions, aBoolean) -> {
                Animatica.CONFIG.animatedTextures = aBoolean;
                try {
                    Animatica.CONFIG.save();
                } catch (IOException e) { Animatica.LOG.error("Error saving config while changing in game!", e); }
            }
    );


    public boolean animatedTextures;

    public AnimaticaConfig() {
        try {
            load();
        } catch (IOException e) {
            Animatica.LOG.error("Error loading config during initialization!", e);
        }
    }

    public void writeTo(Properties properties) {
        properties.put(ANIMATED_TEXTURES_KEY, Boolean.toString(animatedTextures));
    }

    public void readFrom(Properties properties) {
        this.animatedTextures = boolFrom(properties.getProperty(ANIMATED_TEXTURES_KEY), true);
    }

    public Path getFile() throws IOException {
        Path file = MinecraftClient.getInstance().runDirectory.toPath().getFileSystem().getPath("config").resolve(FILE_NAME);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }

        return file;
    }

    public void save() throws IOException {
        Path file = getFile();
        Properties properties = new Properties();

        writeTo(properties);

        try (OutputStream out = Files.newOutputStream(file)) {
            properties.store(out, "Configuration file for Animatica");
        }
    }

    public void load() throws IOException {
        Path file = getFile();
        Properties properties = new Properties();

        try (InputStream in = Files.newInputStream(file)) {
            properties.load(in);
        }

        readFrom(properties);
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean boolFrom(String s, boolean defaultVal) {
        return s == null ? defaultVal : "true".equals(s);
    }
}
