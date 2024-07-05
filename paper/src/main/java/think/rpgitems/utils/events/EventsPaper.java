package think.rpgitems.utils.events;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class EventsPaper implements Listener {

    PlayerArmorChange playerArmorChangeCallback;
    public EventsPaper(JavaPlugin plugin, PlayerArmorChange playerArmorChangeCallback) {
        this.playerArmorChangeCallback = playerArmorChangeCallback;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerArmorUpdate(PlayerArmorChangeEvent e) {
        playerArmorChangeCallback.run(e, e.getPlayer(), e.getNewItem());
    }
}
