package think.rpgitems.utils.nyaacore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import think.rpgitems.utils.nyaacore.utils.OfflinePlayerUtils;

public class NyaaCoreLoader {
    private static NyaaCoreLoader instance;
    private static JavaPlugin plugin;
    private boolean isTest = false;

    public NyaaCoreLoader(JavaPlugin plugin) {
        NyaaCoreLoader.plugin = plugin;
    }

    public static NyaaCoreLoader getInstance() {
        return instance;
    }
    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public void onLoad() {
        instance = this;
    }

    public void onEnable() {
        if (!isTest) {
            Bukkit.getPluginManager().registerEvents(new OfflinePlayerUtils._Listener(), plugin);
            OfflinePlayerUtils.init();
        }
    }

    public void onDisable() {

    }
}
