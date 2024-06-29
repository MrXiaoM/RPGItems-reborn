package think.rpgitems.support;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import think.rpgitems.RPGItems;
import think.rpgitems.support.mythic.IMythic;
import think.rpgitems.support.mythic.Mythic4;
import think.rpgitems.support.mythic.Mythic5;
import think.rpgitems.utils.cast.PluginUtils;

public class MythicSupport implements Listener {
    private static final NamespacedKey type = new NamespacedKey("MythicMobs".toLowerCase(), "type");
    private static IMythic instance = null;
    public static void registerEvents(RPGItems plugin) {
        if (PluginUtils.isClassPresent("io.lumine.mythic.bukkit.MythicBukkit")) {
            instance = registerEvents(new Mythic5(), plugin);
            plugin.getLogger().info("MythicMobs v" + instance.getVersion() + " hooked");
            return;
        }
        if (PluginUtils.isClassPresent("io.lumine.xikage.mythicmobs.MythicMobs")) {
            instance = registerEvents(new Mythic4(), plugin);
            plugin.getLogger().info("MythicMobs v" + instance.getVersion() + " hooked");
            return;
        }
        instance = null;
    }

    private static <T extends Listener> T registerEvents(T listener, JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        return listener;
    }

    public static boolean isMythic(Entity entity) {
        return entity.getPersistentDataContainer().has(type, PersistentDataType.STRING);
    }

    public static boolean castSkill(Player player, String spell) {
        return instance != null && instance.castSkill(player, spell);
    }
}
