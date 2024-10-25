package me.ewahv1.plugin.Listeners.Misiones;

import me.ewahv1.plugin.CreateFiles.JsonHelpers;
import me.ewahv1.plugin.Models.MisionCaceria.MisionCaceriaItemsModel;
import me.ewahv1.plugin.Models.MisionCaceria.MisionCaceriaMisionModel;
import me.ewahv1.plugin.Models.MisionCaceria.MisionCaceriaRecompensaModel;
import me.ewahv1.plugin.Models.MisionRecoleccion.MisionRecoleccionItemsModel;
import me.ewahv1.plugin.Models.MisionRecoleccion.MisionRecoleccionMisionModel;
import me.ewahv1.plugin.Models.MisionRecoleccion.MisionRecoleccionRecompensaModel;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

import java.util.*;

public class CartelDeMisiones implements Listener {

    private final JavaPlugin plugin;
    private final Gson gson = new Gson();
    private final long TiempoCooldown = 60 * 60 * 20; // 1 hora en ticks (60 minutos * 60 segundos * 20 ticks)

    public CartelDeMisiones(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("Cartel de Misiones configurado.");
        plugin.getLogger().info("");

        // Programar la renovación de misiones cada hora usando la variable
        // TiempoCooldown
        Bukkit.getScheduler().runTaskTimer(plugin, this::renewMissions, 20, TiempoCooldown);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                String line0 = ChatColor.stripColor(sign.getLine(0)).toLowerCase();
                String line1 = ChatColor.stripColor(sign.getLine(1)).toLowerCase();

                // Verifica si el cartel ya ha sido configurado
                if (line0.contains("[misión]")) {
                    event.setCancelled(true);
                    if (line1.contains("cacería")) {
                        plugin.getLogger().info("Abriendo inventario de misiones de cacería para " + player.getName());
                        plugin.getLogger().info("");
                        openMissionsInventory(player, "matar_enemigos");
                    } else if (line1.contains("recolección")) {
                        plugin.getLogger()
                                .info("Abriendo inventario de misiones de recolección para " + player.getName());
                        plugin.getLogger().info("");
                        openMissionsInventory(player, "recoleccion_materiales");
                    }
                    return;
                }

                // Verifica si es un clic derecho con un NameTag para configurar el cartel
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand != null && itemInHand.getType() == Material.NAME_TAG) {
                    String nameTagName = ChatColor.stripColor(itemInHand.getItemMeta().getDisplayName());

                    // Generar cartel de cacería o recolección
                    if (nameTagName.equalsIgnoreCase("Caceria")) {
                        configurarCartel(sign, "Cacería");
                        player.sendMessage(ChatColor.GREEN + "¡Cartel configurado como misión de Cacería!");
                        event.setCancelled(true);
                        plugin.getLogger().info("Cartel configurado como misión de cacería.");
                        plugin.getLogger().info("");
                    } else if (nameTagName.equalsIgnoreCase("Recoleccion")) {
                        configurarCartel(sign, "Recolección");
                        player.sendMessage(ChatColor.GREEN + "¡Cartel configurado como misión de Recolección!");
                        event.setCancelled(true);
                        plugin.getLogger().info("Cartel configurado como misión de recolección.");
                        plugin.getLogger().info("");
                    }
                }
            }
        }
    }

    private void configurarCartel(Sign sign, String tipoMision) {
        sign.setLine(0, ChatColor.DARK_BLUE + "[Misión]");
        sign.setLine(1, ChatColor.BOLD + "" + ChatColor.GOLD + tipoMision);
        sign.setLine(2, ChatColor.YELLOW + "Haz clic derecho");
        sign.setLine(3, ChatColor.YELLOW + "para abrir");
        sign.update();
    }

    public void openMissionsInventory(Player player, String tipoMision) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Tablero de Misiones");

        // Determinar el archivo de misiones activas según el tipo de misión
        String activeMissionFileName;
        if (tipoMision.equalsIgnoreCase("matar_enemigos")) {
            activeMissionFileName = "MisionesActivasCazaHora.json";
        } else if (tipoMision.equalsIgnoreCase("recoleccion_materiales")) {
            activeMissionFileName = "MisionesActivasRecoleccionHora.json";
        } else {
            player.sendMessage(ChatColor.RED + "Tipo de misión desconocido.");
            plugin.getLogger().warning("Tipo de misión desconocido: " + tipoMision);
            plugin.getLogger().info("");
            return;
        }

        // Leer las misiones activas desde el archivo correspondiente
        Map<String, List<?>> activeMissions = JsonHelpers.readJsonFile(plugin, activeMissionFileName,
                new TypeToken<Map<String, List<?>>>() {
                }.getType());

        if (activeMissions == null || !activeMissions.containsKey(tipoMision.toLowerCase())) {
            player.sendMessage(ChatColor.RED + "No se pudieron cargar las misiones activas.");
            plugin.getLogger().warning("No se encontraron misiones en el archivo " + activeMissionFileName);
            plugin.getLogger().info("");
            return;
        }

        // Obtener las misiones activas para el tipo especificado
        List<?> misionesActivadas = activeMissions.get(tipoMision.toLowerCase());

        // Obtener las misiones aceptadas del jugador para el tipo de misión específico
        Set<String> acceptedMissions = getAcceptedMissions(player, tipoMision);

        // Colocar las misiones activas en el inventario
        for (Object obj : misionesActivadas) {
            if (obj instanceof Map) {
                String nombreMision;
                if (tipoMision.equalsIgnoreCase("matar_enemigos")) {
                    MisionCaceriaMisionModel mision = convertMapToMisionCaceria((Map<String, Object>) obj);
                    nombreMision = mision.nombre;

                    // Si la misión ya fue aceptada, la omitimos
                    if (acceptedMissions.contains(nombreMision)) {
                        continue;
                    }

                    // Agregar la misión al inventario
                    addMissionToInventory(inventory, mision);
                } else if (tipoMision.equalsIgnoreCase("recoleccion_materiales")) {
                    MisionRecoleccionMisionModel mision = convertMapToMisionRecoleccion((Map<String, Object>) obj);
                    nombreMision = mision.nombre;

                    // Si la misión ya fue aceptada, la omitimos
                    if (acceptedMissions.contains(nombreMision)) {
                        continue;
                    }

                    // Agregar la misión al inventario
                    addMissionToInventory(inventory, mision);
                } else {
                    plugin.getLogger().warning("Tipo de objeto inesperado en la lista de misiones activas.");
                    plugin.getLogger().info("");
                    continue;
                }
            } else {
                plugin.getLogger().warning("El objeto en la lista de misiones no es un Map.");
                plugin.getLogger().info("");
            }
        }

        // Abrir el inventario para el jugador
        player.openInventory(inventory);
    }

    private Set<String> getAcceptedMissions(Player player, String tipoMision) {
        String fileName;
        if (tipoMision.equalsIgnoreCase("matar_enemigos")) {
            fileName = "MisionesAceptadasCazaHora.json";
        } else if (tipoMision.equalsIgnoreCase("recoleccion_materiales")) {
            fileName = "MisionesAceptadasRecoleccionHora.json";
        } else {
            plugin.getLogger().warning("Tipo de misión desconocido: " + tipoMision);
            plugin.getLogger().info("");
            return Collections.emptySet();
        }

        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            return Collections.emptySet();
        }

        try {
            // Actualizar el TypeToken para reflejar la estructura correcta
            Type missionMapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {
            }.getType();
            Map<String, List<Map<String, Object>>> missionsData = gson.fromJson(new java.io.FileReader(file),
                    missionMapType);
            String playerUUID = player.getUniqueId().toString();

            // Obtener las misiones aceptadas del jugador específico
            if (missionsData.containsKey(playerUUID)) {
                List<Map<String, Object>> playerMissions = missionsData.get(playerUUID);
                Set<String> missionNames = new HashSet<>();
                for (Map<String, Object> mission : playerMissions) {
                    String missionName = (String) mission.get("nombre");
                    missionNames.add(missionName);
                }
                return missionNames;
            } else {
                return Collections.emptySet();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    // Convertir Map a MisionCaceriaMisionModel
    private MisionCaceriaMisionModel convertMapToMisionCaceria(Map<String, Object> map) {
        MisionCaceriaMisionModel mision = new MisionCaceriaMisionModel();
        mision.id = ((Double) map.get("id")).intValue();
        mision.nombre = (String) map.get("nombre");
        mision.logo = (String) map.get("logo");
        mision.descripcion = (String) map.get("descripcion");
        mision.tier = ((Double) map.get("tier")).intValue();

        // Configurar la recompensa de la misión
        Map<String, Object> recompensaMap = (Map<String, Object>) map.get("recompensa");
        if (recompensaMap != null) {
            MisionCaceriaRecompensaModel recompensa = new MisionCaceriaRecompensaModel();
            Map<String, Object> itemMap = (Map<String, Object>) recompensaMap.get("item");
            if (itemMap != null) {
                MisionCaceriaItemsModel item = new MisionCaceriaItemsModel();
                item.item = (String) itemMap.get("item");
                item.cantidad = ((Double) itemMap.get("cantidad")).intValue();
                recompensa.item = item;
            }
            mision.recompensa = recompensa;
        }

        return mision;
    }

    // Convertir Map a MisionRecoleccionMisionModel
    private MisionRecoleccionMisionModel convertMapToMisionRecoleccion(Map<String, Object> map) {
        MisionRecoleccionMisionModel mision = new MisionRecoleccionMisionModel();
        mision.id = ((Double) map.get("id")).intValue();
        mision.nombre = (String) map.get("nombre");
        mision.logo = (String) map.get("logo");
        mision.descripcion = (String) map.get("descripcion");
        mision.tier = ((Double) map.get("tier")).intValue();

        // Configurar la recompensa de la misión
        Map<String, Object> recompensaMap = (Map<String, Object>) map.get("recompensa");
        if (recompensaMap != null) {
            MisionRecoleccionRecompensaModel recompensa = new MisionRecoleccionRecompensaModel();
            Map<String, Object> itemMap = (Map<String, Object>) recompensaMap.get("item");
            if (itemMap != null) {
                MisionRecoleccionItemsModel item = new MisionRecoleccionItemsModel();
                item.item = (String) itemMap.get("item");
                item.cantidad = ((Double) itemMap.get("cantidad")).intValue();
                recompensa.item = item;
            }
            mision.recompensa = recompensa;
        }

        return mision;
    }

    private void addMissionToInventory(Inventory inventory, Object obj) {
        Material material = Material.PAPER;
        String nombre = "Misión desconocida";
        String descripcion = "Descripción no disponible.";
        String recompensaItem = "Ninguna";
        int recompensaCantidad = 0;
        int tier = 1;

        // Configura el objeto dependiendo de su tipo
        if (obj instanceof MisionCaceriaMisionModel) {
            MisionCaceriaMisionModel mision = (MisionCaceriaMisionModel) obj;
            material = Material.matchMaterial(mision.logo);
            if (material == null) {
                plugin.getLogger().warning("Material no encontrado para el logo: " + mision.logo);
                plugin.getLogger().info("");
                material = Material.PAPER; // Usar un valor por defecto
            }
            nombre = mision.nombre != null ? mision.nombre : "Misión sin nombre";
            descripcion = mision.descripcion != null ? mision.descripcion : "Descripción no disponible.";

            // Obtener recompensa de MisionCaceriaItemsModel
            if (mision.recompensa != null && mision.recompensa.item != null) {
                recompensaItem = mision.recompensa.item.item;
                recompensaCantidad = mision.recompensa.item.cantidad;
            }
            tier = mision.tier;

        } else if (obj instanceof MisionRecoleccionMisionModel) {
            MisionRecoleccionMisionModel mision = (MisionRecoleccionMisionModel) obj;
            material = Material.matchMaterial(mision.logo);
            if (material == null) {
                plugin.getLogger().warning("Material no encontrado para el logo: " + mision.logo);
                plugin.getLogger().info("");
                material = Material.PAPER; // Usar un valor por defecto
            }
            nombre = mision.nombre != null ? mision.nombre : "Misión sin nombre";
            descripcion = mision.descripcion != null ? mision.descripcion : "Descripción no disponible.";

            // Obtener recompensa de MisionRecoleccionItemsModel
            if (mision.recompensa != null && mision.recompensa.item != null) {
                recompensaItem = mision.recompensa.item.item;
                recompensaCantidad = mision.recompensa.item.cantidad;
            }
            tier = mision.tier;
        }

        // Establecer color según el tier
        ChatColor color = getTierColor(tier);

        // Crear ItemStack para la misión
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + nombre);
            meta.setLore(Arrays.asList(
                    ChatColor.WHITE + descripcion,
                    ChatColor.YELLOW + "Recompensa: " + recompensaItem + " x" + recompensaCantidad,
                    ChatColor.YELLOW + "Tier: " + tier));
            item.setItemMeta(meta);
        }

        // Añadir el ítem al inventario
        inventory.addItem(item);
    }

    private ChatColor getTierColor(int tier) {
        switch (tier) {
            case 1:
                return ChatColor.GREEN;
            case 2:
                return ChatColor.YELLOW;
            case 3:
                return ChatColor.DARK_BLUE;
            case 4:
                return ChatColor.DARK_RED;
            case 5:
                return ChatColor.GOLD;
            default:
                return ChatColor.WHITE;
        }
    }

    private void clearAcceptedMissionsForAllPlayers() {
        // Archivos de misiones aceptadas
        String[] fileNames = {
                "MisionesAceptadasCazaHora.json",
                "MisionesAceptadasRecoleccionHora.json"
        };

        // Iterar sobre los archivos de misiones para reemplazar su contenido con "{}"
        for (String fileName : fileNames) {
            File file = new File(plugin.getDataFolder(), fileName);

            if (file.exists()) {
                try (FileWriter writer = new FileWriter(file)) {
                    // Reemplazar el contenido del archivo con "{}"
                    writer.write("{}");
                    plugin.getLogger().info("El archivo " + fileName + " ha sido limpiado correctamente.");
                    plugin.getLogger().info("");
                } catch (IOException e) {
                    plugin.getLogger().warning("No se pudo limpiar el archivo " + fileName);
                    plugin.getLogger().info("");
                    e.printStackTrace();
                }
            } else {
                plugin.getLogger().warning("El archivo " + fileName + " no existe.");
                plugin.getLogger().info("");
            }
        }
    }

    // Método para renovar las misiones activas
    private void renewMissions() {
        clearAcceptedMissionsForAllPlayers(); // Limpiar misiones aceptadas antes de renovar.
        renewMissionsForType("MisionesActivasRecoleccionHora.json", "MisionesRecoleccionHora.json",
                "recoleccion_materiales", MisionRecoleccionMisionModel.class);
        renewMissionsForType("MisionesActivasCazaHora.json", "MisionesCazaHora.json",
                "matar_enemigos", MisionCaceriaMisionModel.class);
    }

    private <T> void renewMissionsForType(String activeMissionFile, String sourceFile, String missionType,
            Class<T> missionClass) {
        Map<String, List<T>> sourceMissions = JsonHelpers.readJsonFile(plugin, sourceFile,
                new TypeToken<Map<String, List<T>>>() {
                }.getType());

        if (sourceMissions == null || !sourceMissions.containsKey(missionType)) {
            plugin.getLogger().warning("No se pudieron cargar las misiones del archivo " + sourceFile
                    + " o no hay misiones de tipo " + missionType + ".");
            plugin.getLogger().info("");
            return;
        }
        {
        }
        List<T> missionsList = sourceMissions.get(missionType);
        if (missionsList == null || missionsList.isEmpty()) {
            plugin.getLogger().warning("La lista de misiones está vacía o es nula para el tipo " + missionType + ".");
            plugin.getLogger().info("");
            return;
        }

        Collections.shuffle(missionsList);
        List<T> selectedMissions = new ArrayList<>(missionsList.subList(0, Math.min(9, missionsList.size())));

        JsonHelpers.writeJsonFile(plugin, activeMissionFile, Collections.singletonMap(missionType, selectedMissions));
        plugin.getLogger().info("Misiones renovadas y guardadas en " + activeMissionFile);
        plugin.getLogger().info("");
    }

    public void registrarMissionInventoryClickListener() {
        // Registrar el listener del inventario
        MisionInventoryClickListener missionInventoryClickListener = new MisionInventoryClickListener(plugin);
        plugin.getServer().getPluginManager().registerEvents(missionInventoryClickListener, plugin);
    }

}