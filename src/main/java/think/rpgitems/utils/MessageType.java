package think.rpgitems.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import think.rpgitems.utils.ColorHelper;

public enum MessageType {
    NONE, CHAT, TITLE, ACTIONBAR;

    @SuppressWarnings({"deprecation"})
    public void send(Player player, String message) {
        if (this.equals(CHAT)) {
            player.sendMessage(ColorHelper.parseColor(message));
        } else if (this.equals(TITLE)) {
            String[] split = message.split("\n", 2);
            String title = split[0];
            String subtitle = split.length > 1 ? split[1] : "&r";
            player.sendTitle(ColorHelper.parseColor(title), ColorHelper.parseColor(subtitle), 10, 30, 10);
        } else if (this.equals(ACTIONBAR)) {
            player.spigot().sendMessage(ColorHelper.bungee(message));
        }
    }

    public static MessageType getFromConfig(ConfigurationSection section, String key, MessageType def) {
        String value = section.getString(key);
        for (MessageType type : values()) {
            if (type.name().equalsIgnoreCase(value)) return type;
        }
        return def;
    }
}
