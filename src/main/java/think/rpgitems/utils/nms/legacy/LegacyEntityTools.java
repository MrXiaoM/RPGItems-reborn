package think.rpgitems.utils.nms.legacy;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import think.rpgitems.utils.nms.IEntityTools;

/**
 * 备用的设置实体NBT方法，未测试是否可用
 */
public class LegacyEntityTools implements IEntityTools {
    @Override
    public void setEntityTag(Entity e, String tag) {
        if (e instanceof Player) {
            throw new IllegalArgumentException("Player NBT cannot be edited");
        }
        ReadWriteNBT nbtToBeMerged;
        try {
            nbtToBeMerged = NBT.parseNBT(tag);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid NBTTag string", ex);
        }
        NBT.modify(e, it -> {
            it.mergeCompound(nbtToBeMerged);
        });
    }
}
