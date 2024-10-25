package me.ewahv1.plugin.CreateFiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonHelpers {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static <T> T readJsonFile(JavaPlugin plugin, String fileName, Type type) {
        File dataFolder = plugin.getDataFolder();
        File file = new File(dataFolder, fileName);

        if (!file.exists()) {
            plugin.getLogger().warning("El archivo " + fileName + " no existe.");
            plugin.getLogger().info("");
            return null;
        }

        plugin.getLogger().info("Leyendo el archivo JSON: " + fileName);
        plugin.getLogger().info("");

        try (FileReader reader = new FileReader(file)) {
            T result = gson.fromJson(reader, type);
            if (result == null) {
                plugin.getLogger().warning("El archivo " + fileName + " está vacío o no tiene el formato esperado.");
                plugin.getLogger().info("");
            } else {
                plugin.getLogger().info("El archivo " + fileName + " se ha leído correctamente.");
                plugin.getLogger().info("");
            }
            return result;
        } catch (IOException e) {
            plugin.getLogger().severe("Error al leer el archivo JSON " + fileName + ": " + e.getMessage());
            plugin.getLogger().info("");
            e.printStackTrace();
            return null;
        }
    }

    public static <T> void writeJsonFile(JavaPlugin plugin, String fileName, T data) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().severe("No se pudo crear la carpeta de datos: " + dataFolder.getPath());
            plugin.getLogger().info("");
            return;
        }

        File file = new File(dataFolder, fileName);
        plugin.getLogger().info("Escribiendo datos en el archivo JSON: " + fileName);
        plugin.getLogger().info("");

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(data, writer);
            plugin.getLogger().info("El archivo " + fileName + " se ha escrito correctamente.");
            plugin.getLogger().info("");
        } catch (IOException e) {
            plugin.getLogger().severe("Error al escribir en el archivo JSON " + fileName + ": " + e.getMessage());
            plugin.getLogger().info("");
            e.printStackTrace();
        }
    }

    public static boolean jsonFileExists(JavaPlugin plugin, String fileName) {
        File dataFolder = plugin.getDataFolder();
        File file = new File(dataFolder, fileName);
        boolean exists = file.exists();
        plugin.getLogger()
                .info("Verificación de existencia del archivo " + fileName + ": " + (exists ? "existe" : "no existe"));
        plugin.getLogger().info("");
        return exists;
    }

    public static Map<String, List<String>> loadMissionsFromFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, List<String>>>() {
            }.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static void saveMissionsToFile(File file, Map<String, List<String>> data) {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
