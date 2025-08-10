package com.dragon.lastlife;

import com.quiptmc.minecraft.CoreUtils;
import com.quiptmc.minecraft.utils.loaders.ServerLoader;
import com.quiptmc.paper.api.PaperIntegration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Initializer extends JavaPlugin {

    @Override
    public void onEnable() {
        PaperIntegration quipt = new LastLife("LastLife", new ServerLoader<>(ServerLoader.Type.PAPER, this));
        CoreUtils.init(quipt);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static class LastLife extends PaperIntegration {


        public LastLife(String name, ServerLoader<JavaPlugin> loader) {
            super(name, loader);
        }
    }
}
