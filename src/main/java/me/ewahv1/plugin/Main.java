package me.ewahv1.plugin;

import me.ewahv1.plugin.CreateFiles.CreateFilesManager;
import me.ewahv1.plugin.Listeners.ListenersManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Plugin de misiones habilitado.");
        getLogger().info("");

        // Crear archivos iniciales
        CreateFilesManager createFilesManager = new CreateFilesManager();
        createFilesManager.createInitialFiles(this);

        // Inicializar los listeners
        ListenersManager listenersManager = new ListenersManager();
        listenersManager.initializeListeners(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin de misiones deshabilitado.");
        getLogger().info("");
    }
}
