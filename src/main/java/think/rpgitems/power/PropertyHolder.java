package think.rpgitems.power;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import think.rpgitems.RPGItems;

import java.util.Locale;

public interface PropertyHolder {
    @Deprecated
    default void init(ConfigurationSection s) {
        init(s, "");
    }
    /**
     * Loads configuration for this object
     *
     * @param s Configuration
     */
    void init(ConfigurationSection s, String itemName);

    /**
     * Saves configuration for this object
     *
     * @param s Configuration
     */
    void save(ConfigurationSection s);

    /**
     * Static. NamespacedKey of this object
     *
     * @return NamespacedKey
     */
    NamespacedKey getNamespacedKey();

    /**
     * Static. Code name of this object
     *
     * @return Code name
     */
    String getName();

    /**
     * Static. Type of this object
     *
     * @return Type
     */
    String getPropertyHolderType();

    /**
     * Static. Localized name of this object
     *
     * @param locale Locale tag
     * @return Localized name
     */
    default String getLocalizedName(String locale) {
        return getName();
    }

    default String getLocalizedName(CommandSender sender) {
        return getLocalizedName((sender instanceof Player) ? ((Player) sender).getLocale() : RPGItems.plugin.cfg.language);
    }

    /**
     * Static. Localized name of this object
     *
     * @param locale Locale
     * @return Localized name
     */
    default String getLocalizedName(Locale locale) {
        return getLocalizedName(locale.toString());
    }
}
