package think.rpgitems.utils.nyaacore.utils;

import org.bukkit.entity.Entity;

import static think.rpgitems.utils.nms.NMS.entityTools;

/**
 * A collection of operations that cannot be done with NMS.
 * Downstream plugin authors can add methods here, so that
 * their plugins do not need to depend on NMS for just a
 * single function. It also makes upgrade a bit easier,
 * since all NMS codes are here.
 */
public final class NmsUtils {
    /* see CommandEntityData.java */
    public static void setEntityTag(Entity e, String tag) {
        entityTools().setEntityTag(e, tag);
    }
}
