package think.rpgitems.utils.nyaacore.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.critereon.CriterionConditionNBT;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

import java.util.UUID;

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
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) e).getHandle();

        if (nmsEntity instanceof EntityPlayer) {
            throw new IllegalArgumentException("Player NBT cannot be edited");
        } else {
            NBTTagCompound nbtToBeMerged;

            try {
                nbtToBeMerged = MojangsonParser.a(tag);
            } catch (CommandSyntaxException ex) {
                throw new IllegalArgumentException("Invalid NBTTag string");
            }
            NBTTagCompound nmsOrigNBT = CriterionConditionNBT.b(nmsEntity); // entity to nbt
            NBTTagCompound nmsClonedNBT = nmsOrigNBT.h(); // clone
            nmsClonedNBT.a(nbtToBeMerged); // merge NBT
            if (!nmsClonedNBT.equals(nmsOrigNBT)) {
                UUID uuid = nmsEntity.cs(); // store UUID
                nmsEntity.g(nmsClonedNBT); // set nbt
                nmsEntity.a_(uuid); // set uuid
            }
        }
    }
}
