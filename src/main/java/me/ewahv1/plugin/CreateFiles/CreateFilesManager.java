package me.ewahv1.plugin.CreateFiles;

import me.ewahv1.plugin.CreateFiles.CreateJsonFiles.JsonMisionesAceptadasHora;
import me.ewahv1.plugin.CreateFiles.CreateJsonFiles.JsonMisionesActivasHora;
import me.ewahv1.plugin.CreateFiles.CreateJsonFiles.JsonMisionesHora;
import org.bukkit.plugin.java.JavaPlugin;

public class CreateFilesManager {

    public void createInitialFiles(JavaPlugin plugin) {
        JsonMisionesHora.createMissionFiles(plugin);
        JsonMisionesActivasHora.createActiveMissionFiles(plugin);
        JsonMisionesAceptadasHora.createActiveMissionFiles(plugin);
    }
}