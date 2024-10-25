package me.ewahv1.plugin.CreateFiles.CreateJsonFiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonMisionesHora {

        public static void createMissionFiles(JavaPlugin plugin) {
                createFile(plugin, "MisionesCazaHora.json", getDefaultHuntingMissionsJson());
                createFile(plugin, "MisionesRecoleccionHora.json", getDefaultGatheringMissionsJson());
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

        private static String getDefaultHuntingMissionsJson() {
                List<Map<String, Object>> missions = new ArrayList<>();

                // Misiones de caza
                missions.add(createHuntingMission(
                                1,
                                "Dulce libertad",
                                "BEE_SPAWN_EGG",
                                "Mata a 5 abejas para que los aldeanos puedan recolectar la miel.",
                                "BEE",
                                5,
                                "HONEYCOMB",
                                5,
                                2,
                                1));
                missions.add(createHuntingMission(
                                2,
                                "Aracnofobia",
                                "SPIDER_SPAWN_EGG",
                                "Mata a 10 arañas para evitar su expansion.",
                                "SPIDER",
                                10,
                                "COBWEB",
                                10,
                                2,
                                1));
                missions.add(createHuntingMission(
                                3,
                                "Oscuro en las cavernas",
                                "CAVE_SPIDER_SPAWN_EGG",
                                "Mata a 5 arañas de cueva para liberar las cuevas.",
                                "CAVE_SPIDER",
                                5,
                                "COBWEB",
                                10,
                                2,
                                1));
                missions.add(createHuntingMission(
                                4,
                                "Desendermanizador",
                                "ENDERMAN_SPAWN_EGG",
                                "Mata 6 Endermans y obtén perlas.",
                                "ENDERMAN",
                                6,
                                "ENDER_PEARL",
                                2,
                                10,
                                2));
                missions.add(createHuntingMission(
                                5,
                                "Hierronico no es un chiste",
                                "IRON_GOLEM_SPAWN_EGG",
                                "Derrota a 2 Golems de Hierro para mostrar tu poder.",
                                "IRON_GOLEM",
                                2,
                                "IRON_BLOCK",
                                1,
                                15,
                                3));
                missions.add(createHuntingMission(
                                6,
                                "Cerdos rojos",
                                "PIGLIN_SPAWN_EGG",
                                "Elimina a 15 Piglins en el Nether.",
                                "PIGLIN",
                                15,
                                "GOLD_NUGGET",
                                12,
                                12,
                                3));
                missions.add(createHuntingMission(
                                7,
                                "Ahogados Ahogados",
                                "DROWNED_SPAWN_EGG",
                                "Mata a 10 Drowneds solo porque si",
                                "DROWNED",
                                5,
                                "DIAMOND",
                                1,
                                1,
                                4));
                missions.add(createHuntingMission(
                                8,
                                "Zombien narizones",
                                "ZOMBIE_VILLAGER_SPAWN_EGG",
                                "Derrota a 10 aldeanos zombie",
                                "ZOMBIE_VILLAGER",
                                5,
                                "DIAMOND",
                                1,
                                1,
                                4));
                missions.add(createHuntingMission(
                                9,
                                "Cazador de Blazes",
                                "BLAZE_SPAWN_EGG",
                                "Derrota a 7 blazes",
                                "BLAZE",
                                5,
                                "BLAZE_ROD",
                                1,
                                1,
                                4));

                Map<String, Object> jsonOutput = new HashMap<>();
                jsonOutput.put("matar_enemigos", missions);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return gson.toJson(jsonOutput);
        }

        private static String getDefaultGatheringMissionsJson() {
                List<Map<String, Object>> missions = new ArrayList<>();

                // Misiones de recolección
                missions.add(createGatheringMission(
                                3,
                                "Recolector de Madera",
                                "OAK_LOG",
                                "Recolecta 64 troncos de roble.",
                                "OAK_LOG",
                                64,
                                "EMERALD",
                                5,
                                80,
                                3));
                missions.add(createGatheringMission(
                                4,
                                "Recolector de Piedras",
                                "STONE_PICKAXE",
                                "Recolecta 64 bloques de piedra.",
                                "STONE",
                                64,
                                "COBBLESTONE",
                                10,
                                8,
                                4));

                Map<String, Object> jsonOutput = new HashMap<>();
                jsonOutput.put("recoleccion_materiales", missions);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return gson.toJson(jsonOutput);
        }

        private static Map<String, Object> createHuntingMission(int id, String nombre, String logo, String descripcion,
                        String mob, int cantidad, String recompensaItem,
                        int recompensaCantidad, int experiencia, int tier) {
                Map<String, Object> mission = new HashMap<>();
                mission.put("id", id);
                mission.put("nombre", nombre);
                mission.put("logo", logo);
                mission.put("descripcion", descripcion);

                Map<String, Object> objetivo = new HashMap<>();
                objetivo.put("mob", mob);
                objetivo.put("cantidad", cantidad);
                mission.put("objetivo", objetivo);

                Map<String, Object> recompensaItemMap = new HashMap<>();
                recompensaItemMap.put("item", recompensaItem);
                recompensaItemMap.put("cantidad", recompensaCantidad);

                Map<String, Object> recompensa = new HashMap<>();
                recompensa.put("item", recompensaItemMap);
                recompensa.put("xp", experiencia);
                mission.put("recompensa", recompensa);

                mission.put("tier", tier);
                return mission;
        }

        private static Map<String, Object> createGatheringMission(int id, String nombre, String logo,
                        String descripcion,
                        String material, int cantidad, String recompensaItem,
                        int recompensaCantidad, int experiencia, int tier) {
                Map<String, Object> mission = new HashMap<>();
                mission.put("id", id);
                mission.put("nombre", nombre);
                mission.put("logo", logo);
                mission.put("descripcion", descripcion);

                Map<String, Object> objetivo = new HashMap<>();
                objetivo.put("material", material);
                objetivo.put("cantidad", cantidad);
                mission.put("objetivo", objetivo);

                Map<String, Object> recompensaItemMap = new HashMap<>();
                recompensaItemMap.put("item", recompensaItem);
                recompensaItemMap.put("cantidad", recompensaCantidad);

                Map<String, Object> recompensa = new HashMap<>();
                recompensa.put("item", recompensaItemMap);
                recompensa.put("xp", experiencia);
                mission.put("recompensa", recompensa);

                mission.put("tier", tier);
                return mission;
        }
}
