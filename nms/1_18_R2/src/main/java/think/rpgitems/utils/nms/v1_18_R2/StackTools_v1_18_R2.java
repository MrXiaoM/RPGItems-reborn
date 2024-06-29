package think.rpgitems.utils.nms.v1_18_R2;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.utils.nms.IStackTools;

public class StackTools_v1_18_R2 implements IStackTools {
    @Override
    public String itemToJson(ItemStack itemStack) throws RuntimeException {
        NBTTagCompound nmsCompoundTagObj; // This will just be an empty CompoundTag instance to invoke the saveNms method
        net.minecraft.world.item.ItemStack nmsItemStackObj; // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
        NBTTagCompound itemAsJsonObject; // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method

        try {
            nmsCompoundTagObj = new NBTTagCompound();
            nmsItemStackObj = CraftItemStack.asNMSCopy(itemStack);
            itemAsJsonObject = nmsItemStackObj.b(nmsCompoundTagObj); // save
        } catch (Throwable t) {
            throw new RuntimeException("failed to serialize itemstack to nms item", t);
        }

        // Return a string representation of the serialized object
        return itemAsJsonObject.toString();
    }
}
