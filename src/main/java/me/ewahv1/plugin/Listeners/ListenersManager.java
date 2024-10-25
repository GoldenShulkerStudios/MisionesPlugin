package me.ewahv1.plugin.Listeners;

import me.ewahv1.plugin.Listeners.Misiones.CartelDeMisiones;
import me.ewahv1.plugin.Listeners.Misiones.MisionInventoryClickListener;
import me.ewahv1.plugin.Listeners.Misiones.VerificacionMisionCompletadaListener;
import org.bukkit.plugin.java.JavaPlugin;

public class ListenersManager {
    private CartelDeMisiones cartelDeMisiones;
    private MisionInventoryClickListener missionInventoryClickListener;
    private VerificacionMisionCompletadaListener verificacionMisionCompletadaListener;

    public void initializeListeners(JavaPlugin plugin) {
        // Inicializar y registrar CartelDeMisiones
        cartelDeMisiones = new CartelDeMisiones(plugin);
        cartelDeMisiones.setup();

        // Inicializar y registrar MisionInventoryClickListener
        missionInventoryClickListener = new MisionInventoryClickListener(plugin);
        plugin.getServer().getPluginManager().registerEvents(missionInventoryClickListener, plugin);

        // Inicializar y registrar VerificacionMisionCompletadaListener
        verificacionMisionCompletadaListener = new VerificacionMisionCompletadaListener(plugin);
        plugin.getServer().getPluginManager().registerEvents(verificacionMisionCompletadaListener, plugin);

        // Mensaje de confirmación
        plugin.getLogger().info("Listeners inicializados.");
    }
}
