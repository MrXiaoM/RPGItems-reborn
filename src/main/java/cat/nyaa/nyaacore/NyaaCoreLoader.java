package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.configuration.NbtItemStack;
import cat.nyaa.nyaacore.utils.OfflinePlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public class NyaaCoreLoader {
    private static NyaaCoreLoader instance;

    static {
        ConfigurationSerialization.registerClass(NbtItemStack.class);
    }
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
