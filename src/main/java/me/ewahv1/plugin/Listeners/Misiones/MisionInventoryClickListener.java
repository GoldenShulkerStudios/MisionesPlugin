package me.ewahv1.plugin.Listeners.Misiones;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class MisionInventoryClickListener implements Listener {
    private final JavaPlugin plugin;
    private final Gson gson = new Gson();

    public MisionInventoryClickListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Verificar si el inventario tiene el título "Tablero de Misiones"
        Inventory inventory = event.getInventory();
        if (!event.getView().getTitle().equals("Tablero de Misiones")) {
            return; // No es el inventario de misiones
        }

        // Cancelar el evento para que el ítem no sea movido
        event.setCancelled(true);

        // Verificar si el clic fue en un slot válido
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return; // No se ha clicado en un ítem válido
        }

        // Obtener el nombre de la misión desde el display name del ítem
        ItemMeta meta = clickedItem.getItemMeta();
        String missionName = ChatColor.stripColor(meta.getDisplayName()); // Remover colores para obtener el nombre

        // Obtener el jugador que hizo clic
        Player player = (Player) event.getWhoClicked();

        // Cerrar el inventario
        player.closeInventory();

        // Determinar el tipo de misión y el archivo correspondiente
        String fileName = determineMissionFile(missionName);
        if (fileName == null) {
            player.sendMessage(ChatColor.RED + "No se pudo determinar el tipo de misión.");
            plugin.getLogger().warning("No se pudo determinar el archivo de misiones para la misión: " + missionName);
            plugin.getLogger().info("");
            return;
        }

        // Registrar la misión como aceptada por el jugador
        addMissionToPlayer(player, missionName, fileName);
    }

    private String determineMissionFile(String missionName) {
        // Verificar si la misión está en el archivo de caza
        File cazaFile = new File(plugin.getDataFolder(), "MisionesActivasCazaHora.json");
        if (missionExistsInFile(cazaFile, missionName)) {
            return "MisionesAceptadasCazaHora.json";
        }

        // Verificar si la misión está en el archivo de recolección
        File recoleccionFile = new File(plugin.getDataFolder(), "MisionesActivasRecoleccionHora.json");
        if (missionExistsInFile(recoleccionFile, missionName)) {
            return "MisionesAceptadasRecoleccionHora.json";
        }

        // Si no se encuentra la misión en ninguno de los archivos, devolver null
        return null;
    }

    private boolean missionExistsInFile(File file, String missionName) {
        if (!file.exists()) {
            plugin.getLogger().warning("El archivo no existe: " + file.getName());
            plugin.getLogger().info("");
            return false;
        }

        try {
            // Leer el contenido del archivo JSON como un objeto Map
            Type missionMapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {
            }.getType();
            Map<String, List<Map<String, Object>>> missionsMap = gson.fromJson(new java.io.FileReader(file),
                    missionMapType);

            // Buscar la misión en cada lista de misiones dentro del mapa
            for (List<Map<String, Object>> missionsList : missionsMap.values()) {
                for (Map<String, Object> mission : missionsList) {
                    if (missionName.equalsIgnoreCase((String) mission.get("nombre"))) {
                        return true;
                    }
                }
            }

        } catch (IOException e) {
            plugin.getLogger().warning("Error al leer el archivo: " + file.getName() + " - " + e.getMessage());
            plugin.getLogger().info("");
            e.printStackTrace();
        }

        // Si no se encontró la misión, devolver false
        return false;
    }

    private void addMissionToPlayer(Player player, String missionName, String fileName) {
        String playerUUID = player.getUniqueId().toString();
        File file = new File(plugin.getDataFolder(), fileName);

        // Mapa para almacenar las misiones aceptadas
        Map<String, List<Map<String, Object>>> acceptedMissions = new HashMap<>();
        if (file.exists()) {
            try {
                // Leer misiones actuales del archivo
                Type missionMapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {
                }.getType();
                acceptedMissions = gson.fromJson(new java.io.FileReader(file), missionMapType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Obtener la lista de misiones para el jugador o crear una nueva lista si no
        // existe
        List<Map<String, Object>> playerMissions = acceptedMissions.getOrDefault(playerUUID, new ArrayList<>());

        // Verificar si la misión ya ha sido aceptada
        boolean missionAlreadyAccepted = false;
        for (Map<String, Object> mission : playerMissions) {
            String acceptedMissionName = (String) mission.get("nombre");
            if (acceptedMissionName.equals(missionName)) {
                missionAlreadyAccepted = true;
                break; // Salimos del loop si encontramos la misión
            }
        }

        if (!missionAlreadyAccepted) {
            // Crear un nuevo mapa para la misión con progreso inicial 0 y estado "false"
            Map<String, Object> newMission = new HashMap<>();
            newMission.put("nombre", missionName);
            newMission.put("progreso", 0);
            newMission.put("estado", false); // Agregar el estado inicial "false"

            // Agregar la misión a la lista del jugador
            playerMissions.add(newMission);
            acceptedMissions.put(playerUUID, playerMissions);

            // Guardar las misiones actualizadas en el archivo
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(acceptedMissions, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Enviar mensaje de aceptación
            player.sendMessage(ChatColor.GREEN + "Aceptaste la misión: " + ChatColor.YELLOW + missionName);
        } else {
            player.sendMessage(ChatColor.RED + "Ya has aceptado esta misión. Espera a que el cooldown termine.");
        }
    }

}
