package think.rpgitems.utils.nms.v1_8_R3;

import net.minecraft.server.v1_8_R3.MojangsonParseException;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MojangsonParser;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import think.rpgitems.utils.nms.IEntityTools;

import java.lang.reflect.Field;
import java.util.UUID;

public class EntityTools_v1_8_R3 implements IEntityTools {
    @Override
    public void setEntityTag(Entity e, String tag) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) e).getHandle();

        if (nmsEntity instanceof EntityPlayer) {
            throw new IllegalArgumentException("Player NBT cannot be edited");
        } else {
            NBTTagCompound nbtToBeMerged;

            try {
                nbtToBeMerged = MojangsonParser.parse(tag);
            } catch (MojangsonParseException ex) {
                throw new IllegalArgumentException("Invalid NBTTag string");
            }
            NBTTagCompound nmsOrigNBT = new NBTTagCompound();
            nmsEntity.e(nmsOrigNBT); // entity to nbt
            NBTTagCompound nmsClonedNBT = (NBTTagCompound) nmsOrigNBT.clone(); // clone
            nmsClonedNBT.a(nbtToBeMerged); // merge NBT
            if (!nmsClonedNBT.equals(nmsOrigNBT)) {
                UUID uuid = nmsEntity.getUniqueID(); // store UUID
                nmsClonedNBT.setString("UUID", uuid.toString());
                nmsEntity.f(nmsClonedNBT); // set nbt
                try {
                    Field field = net.minecraft.server.v1_8_R3.Entity.class.getDeclaredField("uniqueID");
                    field.setAccessible(true);
                    field.set(nmsEntity, uuid); // set uuid
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
