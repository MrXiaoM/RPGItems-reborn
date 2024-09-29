package think.rpgitems.support;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import think.rpgitems.RPGItems;
import think.rpgitems.api.PlaceholderHolder;
import think.rpgitems.data.Factor;

@SuppressWarnings({"deprecation"})
public class PlaceholderExt extends PlaceholderExpansion {
    RPGItems plugin;
    public PlaceholderExt(RPGItems plugin) {
        this.plugin = plugin;
    }
    @Override
    public @NotNull String getIdentifier() {
        return plugin.getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("factor_id")) {
            Factor factor = plugin.cfg.factorConfig.getFactor(player);
            return factor == null ? "" : factor.id;
        }
        if (params.equalsIgnoreCase("factor_name")) {
            Factor factor = plugin.cfg.factorConfig.getFactor(player);
            return factor == null ? "" : factor.name;
        }
        // %rpgitems_factor_compare_<target_factor>_<msg_when_player_higher_than_it>_<msg_when_player_lower_than_it>%
        if (params.startsWith("factor_compare_")) {
            String[] split = params.substring(15).split("_", 3);
            if (split.length == 3) {
                // player factor
                Factor factor = plugin.cfg.factorConfig.getFactor(player);
                if (factor == null) return "";
                String playerFactor = factor.id;
                int i = factor.damageToCompare.getOrDefault(split[0], 0);
                if (i > 0) return split[1];
                if (i < 0) return split[2];
                // target factor
                factor = plugin.cfg.factorConfig.getFactor(split[0]);
                if (factor == null) return "";
                i = factor.damageToCompare.getOrDefault(playerFactor, 0);
                if (i < 0) return split[1];
                if (i > 0) return split[2];
                return "";
            }
        }
        if (params.equalsIgnoreCase("magic")) {
            return String.valueOf(plugin.magic.getUserMagic(player));
        }
        if (params.equalsIgnoreCase("magic_total")) {
            return String.valueOf(plugin.magic.getUserTotalMagic(player));
        }
        if (params.equalsIgnoreCase("magic_gap")) {
            int magic = plugin.magic.getUserMagic(player);
            int total = plugin.magic.getUserTotalMagic(player);
            return String.valueOf(total - magic);
        }
        for (Plugin ext : plugin.ext()) {
            if (ext instanceof PlaceholderHolder) {
                PlaceholderHolder holder = (PlaceholderHolder) ext;
                String result = holder.onPlaceholderRequest(player, params);
                if (result != null) {
                    return result;
                }
            }
        }
        return super.onPlaceholderRequest(player, params);
    }
}
