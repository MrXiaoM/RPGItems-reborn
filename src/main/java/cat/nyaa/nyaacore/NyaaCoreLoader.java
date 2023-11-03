package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.configuration.NbtItemStack;
import cat.nyaa.nyaacore.http.client.HttpClient;
import cat.nyaa.nyaacore.utils.ClickSelectionUtils;
import cat.nyaa.nyaacore.utils.OfflinePlayerUtils;
import net.minecraft.SharedConstants;
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
            String serverVersion = "", targetVersion;
            try {
                serverVersion = SharedConstants.getCurrentVersion().getName();
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
            try {
                var VersionResource = plugin.getResource("MCVersion");
                targetVersion = VersionResource == null ? "" : new String(VersionResource.readAllBytes());
                plugin.getLogger().info("target minecraft version:" + targetVersion + "server version:" + serverVersion);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Bukkit.getPluginManager().registerEvents(new ClickSelectionUtils._Listener(), plugin);
            Bukkit.getPluginManager().registerEvents(new OfflinePlayerUtils._Listener(), plugin);
            OfflinePlayerUtils.init();
        }
    }

    public void onDisable() {
        HttpClient.shutdown();
    }
}
