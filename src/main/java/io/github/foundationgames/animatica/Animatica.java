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
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Animatica.NAMESPACE)
public class Animatica {
    public static final Logger LOG = LogManager.getLogger("Animatica");

    public static final String NAMESPACE = "animatica";

    public Animatica() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, (TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.START) AnimationLoader.INSTANCE.tickTextures();
        });
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIG);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        FMLJavaModLoadingContext.get().getModEventBus().addListener((RegisterClientReloadListenersEvent event)-> event.registerReloadListener(AnimationLoader.INSTANCE));
    }
    public static final ForgeConfigSpec CONFIG;
    public static final ForgeConfigSpec.BooleanValue ANIMATED_TEXTURES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("Animatica");
        ANIMATED_TEXTURES = builder.define("AnimatedTextures", true);
        builder.pop();
        CONFIG = builder.build();
    }
}
