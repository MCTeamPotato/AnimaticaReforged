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
package io.github.foundationgames.animatica;

import io.github.foundationgames.animatica.animation.AnimationLoader;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Animatica.NAMESPACE)
public class Animatica {
    public static final Logger LOG = LogManager.getLogger("Animatica");

    public static final String NAMESPACE = "animatica";
    public static volatile boolean ALLOW_INVALID_ID_CHARS = false;

    public Animatica() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, TickEvent.ClientTickEvent.class, event -> {
            if (event.phase == TickEvent.Phase.START) AnimationLoader.INSTANCE.tickTextures();
        });
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIG);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        FMLJavaModLoadingContext.get().getModEventBus().addListener((RegisterClientReloadListenersEvent event)-> event.registerReloadListener(AnimationLoader.INSTANCE));
    }

    public static final ModConfigSpec CONFIG;
    public static final ModConfigSpec.BooleanValue ANIMATED_TEXTURES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("Animatica");
        ANIMATED_TEXTURES = builder.define("AnimatedTextures", true);
        builder.pop();
        CONFIG = builder.build();
    }
}
