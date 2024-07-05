package think.rpgitems.utils.nms.v1_9_R1;

import net.minecraft.server.v1_9_R1.MojangsonParseException;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.MojangsonParser;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import think.rpgitems.utils.nms.IEntityTools;

import java.util.UUID;

public class EntityTools_v1_9_R1 implements IEntityTools {
    @Override
    public void setEntityTag(Entity e, String tag) {
        net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) e).getHandle();

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
                nmsEntity.f(nmsClonedNBT); // set nbt
                nmsEntity.a(uuid); // set uuid
            }
        }
    }
}
