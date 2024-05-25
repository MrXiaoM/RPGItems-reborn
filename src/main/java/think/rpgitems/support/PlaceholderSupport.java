package think.rpgitems.support;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import think.rpgitems.RPGItems;

import java.util.List;
import java.util.logging.Level;

public class PlaceholderSupport {
    static RPGItems plugin;
    static boolean usePlaceholderAPI;
    static boolean hasSupport;

    public static void init(RPGItems pl) {
        try {
            plugin = pl;

            usePlaceholderAPI = plugin.cfg.usePlaceholderAPI;
            Plugin papiPlugin = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
            if (!usePlaceholderAPI || papiPlugin == null) {
                return;
            }

            PlaceholderExt placeholder = new PlaceholderExt(plugin);

            ((PlaceholderAPIPlugin) papiPlugin) // unregister exists
                    .getLocalExpansionManager()
                    .findExpansionByIdentifier(placeholder.getIdentifier())
                    .ifPresent(PlaceholderExpansion::unregister);

            if (placeholder.isRegistered()) return;
            boolean result = placeholder.register();
            if (!result) {
                RPGItems.logger.log(Level.WARNING, "PlaceholderAPI expansion register failed");
            }
            hasSupport = true;
        } catch (Exception e) {
            RPGItems.logger.log(Level.WARNING, "Error enabling PlaceholderAPI support", e);
            hasSupport = false;
        }
    }

    public static String setPlaceholders(Player player, String s) {
        if (player == null) return s;
        if (!hasSupport) return s.replace("%player_name%", player.getName());
        return PlaceholderAPI.setPlaceholders(player, s);
    }

    public static List<String> setPlaceholders(Player player, List<String> s) {
        if (player == null) return s;
        if (!hasSupport) return Lists.newArrayList(String
                .join("\n", s)
                .replace("%player_name%", player.getName())
                .split("\n")
        );
        return PlaceholderAPI.setPlaceholders(player, s);
    }
}
