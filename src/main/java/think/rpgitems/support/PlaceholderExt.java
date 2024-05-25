package think.rpgitems.support;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import think.rpgitems.RPGItems;
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
        return super.onPlaceholderRequest(player, params);
    }
}
