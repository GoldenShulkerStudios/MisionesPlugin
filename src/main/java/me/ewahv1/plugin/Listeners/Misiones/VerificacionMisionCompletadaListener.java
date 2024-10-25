package me.ewahv1.plugin.Listeners.Misiones;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.ewahv1.plugin.CreateFiles.JsonHelpers;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class VerificacionMisionCompletadaListener implements Listener {
    private JavaPlugin plugin;
    private String cazaHoraFileName = "MisionesAceptadasCazaHora.json";
    private String recoleccionHoraFileName = "MisionesAceptadasRecoleccionHora.json";

    public VerificacionMisionCompletadaListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = (Player) event.getEntity().getKiller();
            String mobType = event.getEntityType().name();

            // Verificar progreso para misiones de caza
            verificarProgresoMision(player, mobType, 1, cazaHoraFileName, "MisionesCazaHora.json", "mob");
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        String materialType = item.getType().name();

        // Verificar progreso para misiones de recolección
        verificarProgresoMision(player, materialType, item.getAmount(), recoleccionHoraFileName,
                "MisionesRecoleccionHora.json", "material");
    }

    private void verificarProgresoMision(Player player, String tipo, int cantidad, String archivoMisionesAceptadas,
            String archivoMisiones, String tipoObjetivo) {
        // Leer misiones aceptadas del jugador
        Type tipoMap = new TypeToken<Map<String, List<Map<String, Object>>>>() {
        }.getType();
        Map<String, List<Map<String, Object>>> misionesAceptadas = null;
        try {
            misionesAceptadas = JsonHelpers.readJsonFile(plugin, archivoMisionesAceptadas, tipoMap);
        } catch (JsonSyntaxException e) {
            plugin.getLogger().severe(
                    "El archivo " + archivoMisionesAceptadas + " tiene un formato incorrecto: " + e.getMessage());
            plugin.getLogger().info("");
            return; // Salir si el archivo no tiene el formato esperado
        }

        if (misionesAceptadas == null || !misionesAceptadas.containsKey(player.getUniqueId().toString())) {
            return; // El jugador no tiene misiones aceptadas o el archivo está vacío
        }

        // Leer la lista de misiones aceptadas del jugador
        List<Map<String, Object>> listaMisionesJugador = misionesAceptadas.get(player.getUniqueId().toString());

        // Leer misiones disponibles
        Type tipoListaMisiones = new TypeToken<Map<String, List<Map<String, Object>>>>() {
        }.getType();
        Map<String, List<Map<String, Object>>> misionesDisponibles = null;
        try {
            misionesDisponibles = JsonHelpers.readJsonFile(plugin, archivoMisiones, tipoListaMisiones);
        } catch (JsonSyntaxException e) {
            plugin.getLogger()
                    .severe("El archivo " + archivoMisiones + " tiene un formato incorrecto: " + e.getMessage());
            plugin.getLogger().info("");
            return; // Salir si el archivo no tiene el formato esperado
        }

        if (misionesDisponibles == null) {
            return; // No hay misiones definidas en el archivo
        }

        // Verificar y actualizar el progreso de cada misión aceptada
        String misionKey = archivoMisionesAceptadas.equals(cazaHoraFileName) ? "matar_enemigos"
                : "recoleccion_materiales";
        List<Map<String, Object>> misionesDeTipo = misionesDisponibles.get(misionKey);

        if (misionesDeTipo == null) {
            return; // No hay misiones definidas para este tipo
        }

        for (Map<String, Object> misionJugador : listaMisionesJugador) {
            String nombreMision = (String) misionJugador.get("nombre");
            for (Map<String, Object> misionDefinida : misionesDeTipo) {
                if (nombreMision.equals(misionDefinida.get("nombre"))) {
                    // Obtener los detalles del objetivo
                    Map<String, Object> objetivo = (Map<String, Object>) misionDefinida.get("objetivo");
                    String tipoObjetivoMision = (String) objetivo.get(tipoObjetivo);
                    int cantidadRequerida = ((Number) objetivo.get("cantidad")).intValue();

                    // Verificar si el tipo coincide y actualizar el progreso
                    if (tipoObjetivoMision.equals(tipo)) {
                        int cantidadActual = ((Number) misionJugador.getOrDefault("progreso", 0)).intValue();
                        cantidadActual += cantidad;

                        // Actualizar el progreso en el archivo de misiones aceptadas
                        misionJugador.put("progreso", cantidadActual);
                        if (cantidadActual >= cantidadRequerida) {
                            completarMision(player, nombreMision, archivoMisionesAceptadas, misionesAceptadas,
                                    misionDefinida);
                        }
                    }
                }
            }
        }

        // Guardar los cambios en el archivo de misiones aceptadas
        JsonHelpers.writeJsonFile(plugin, archivoMisionesAceptadas, misionesAceptadas);
    }

    private void completarMision(Player player, String nombreMision, String archivoMisionesAceptadas,
            Map<String, List<Map<String, Object>>> misionesAceptadas, Map<String, Object> misionDefinida) {

        // Obtener la lista de misiones aceptadas del jugador
        List<Map<String, Object>> listaMisionesJugador = misionesAceptadas.get(player.getUniqueId().toString());
        if (listaMisionesJugador != null) {
            for (Map<String, Object> mision : listaMisionesJugador) {
                if (nombreMision.equals(mision.get("nombre"))) {
                    // Verificar si la misión ya fue completada
                    boolean estado = (Boolean) mision.getOrDefault("estado", false);
                    if (estado) {
                        // La misión ya está completada, no hacer nada
                        player.sendMessage("Esta misión ya fue completada: " + nombreMision);
                        plugin.getLogger().info("La misión '" + nombreMision
                                + "' ya fue completada para el jugador " + player.getUniqueId());
                        return; // Salir del método, no realizar más acciones
                    }

                    // La misión no está completada, proceder a completarla
                    // Obtener la recompensa
                    Map<String, Object> recompensa = (Map<String, Object>) misionDefinida.get("recompensa");
                    entregarRecompensa(player, recompensa);

                    // Actualizar el estado de la misión a true
                    mision.put("estado", true);
                    break; // Salir del bucle una vez que la misión ha sido encontrada y actualizada
                }
            }
        }

        // Guardar los cambios en el archivo JSON
        JsonHelpers.writeJsonFile(plugin, archivoMisionesAceptadas, misionesAceptadas);

        // Notificar al jugador
        player.sendMessage("¡Has completado la misión: " + nombreMision + "!");
        plugin.getLogger().info("El estado de la misión '" + nombreMision
                + "' ha sido actualizado a 'true' para el jugador " + player.getUniqueId());
        plugin.getLogger().info("");
    }

    private void entregarRecompensa(Player player, Map<String, Object> recompensa) {
        // Manejar recompensa de items
        Map<String, Object> itemRecompensa = (Map<String, Object>) recompensa.get("item");
        if (itemRecompensa != null) {
            String itemName = (String) itemRecompensa.get("item");
            Number cantidadObj = (Number) itemRecompensa.get("cantidad");
            int cantidad = cantidadObj != null ? cantidadObj.intValue() : 0; // Usar intValue para evitar problemas de
                                                                             // casting
            Material material = Material.matchMaterial(itemName);

            if (material != null) {
                player.getInventory().addItem(new ItemStack(material, cantidad));
                player.sendMessage("¡Has recibido " + cantidad + " " + itemName + "!");
            } else {
                plugin.getLogger().severe("El material " + itemName + " no es válido.");
                plugin.getLogger().info("");
            }
        }

        // Manejar recompensa de niveles
        Object nivelesRecompensaObj = recompensa.get("xp");
        int nivelesRecompensa = 0;
        if (nivelesRecompensaObj instanceof Number) {
            nivelesRecompensa = ((Number) nivelesRecompensaObj).intValue(); // Convertir a int
        }

        if (nivelesRecompensa > 0) {
            // Obtener el nivel actual y agregar la recompensa
            int nivelActual = player.getLevel();
            int nuevoNivel = nivelActual + nivelesRecompensa;
            player.setLevel(nuevoNivel);
            player.sendMessage("¡Has subido " + nivelesRecompensa + " niveles! Tu nuevo nivel es " + nuevoNivel + ".");
        }
    }

}
