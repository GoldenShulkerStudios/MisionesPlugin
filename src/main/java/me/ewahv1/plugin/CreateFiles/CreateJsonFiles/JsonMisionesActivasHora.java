package me.ewahv1.plugin.CreateFiles.CreateJsonFiles;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JsonMisionesActivasHora {

    public static void createActiveMissionFiles(JavaPlugin plugin) {
        createFile(plugin, "MisionesActivasCazaHora.json", "{}"); // Archivo vacío
        createFile(plugin, "MisionesActivasRecoleccionHora.json", "{}"); // Archivo vacío
    }

    private static void createFile(JavaPlugin plugin, String fileName, String defaultContent) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs(); // Asegura que el directorio de destino existe
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(defaultContent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
