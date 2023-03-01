package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.component.IMessageQueue;
import cat.nyaa.nyaacore.component.NyaaComponent;
import cat.nyaa.nyaacore.configuration.NbtItemStack;
import cat.nyaa.nyaacore.http.client.HttpClient;
import cat.nyaa.nyaacore.timer.TimerManager;
import cat.nyaa.nyaacore.utils.ClickSelectionUtils;
import cat.nyaa.nyaacore.utils.OfflinePlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.plugin.java.JavaPlugin;
import think.rpgitems.RPGItems;

public class NyaaCoreLoader {
    public static final String TARGET_MAPPING = "c2d5d7871edcc4fb0f81d18959c647af";
    private static NyaaCoreLoader instance;

    static {
        ConfigurationSerialization.registerClass(NbtItemStack.class);
    }

    public TimerManager timerManager;
    RPGItems plugin;
    public NyaaCoreLoader(RPGItems plugin) {
        this.plugin = plugin;
    }
    public static NyaaCoreLoader getInstance() {
        return instance;
    }

    public void onLoad() {
        instance = this;
        LanguageRepository.initInternalMap(this);
        //timerManager = new TimerManager(this);
    }

    public void onEnable() {
        try {
            boolean check = MappingChecker.check();
            if (!check) {
                plugin.getLogger().severe("Unsupported NMS Mapping version detected. Unexpected error may occurred.");
            }
        } catch (NoSuchMethodError e) {
            plugin.getLogger().info("Can not detect CraftBukkit NMS Mapping version. Unexpected error may occurred.");
        }
        HttpClient.init(0);
        IMessageQueue.DefaultMessageQueue defaultMessageQueue = new IMessageQueue.DefaultMessageQueue();
        Bukkit.getPluginManager().registerEvents(defaultMessageQueue, plugin);
        Bukkit.getPluginManager().registerEvents(new ClickSelectionUtils._Listener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OfflinePlayerUtils._Listener(), plugin);
        NyaaComponent.register(IMessageQueue.class, defaultMessageQueue);
        OfflinePlayerUtils.init();
        //timerManager.load();
    }

    public void onDisable() {
        HttpClient.shutdown();
        //timerManager.save();
    }

    private static class MappingChecker {
        static boolean check() {
            String mappingsVersion = ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion();
            return TARGET_MAPPING.equals(mappingsVersion);
        }
    }
}
