package think.rpgitems.support;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

public class MythicSupport {
    private static final NamespacedKey type = new NamespacedKey("MythicMobs".toLowerCase(), "type");;
    public static boolean isMythic(Entity entity) {
        return entity.getPersistentDataContainer().has(type, PersistentDataType.STRING);
    }
}
