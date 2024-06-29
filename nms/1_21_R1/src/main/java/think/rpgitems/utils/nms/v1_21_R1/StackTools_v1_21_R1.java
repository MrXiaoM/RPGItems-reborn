package think.rpgitems.utils.nms.v1_21_R1;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.ChatHoverable;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.utils.nms.IStackTools;

public class StackTools_v1_21_R1 implements IStackTools {
    @Override
    public String itemToJson(ItemStack itemStack) throws RuntimeException {
        try {
            // TODO: Waiting for test
            net.minecraft.world.item.ItemStack nmsItemStackObj = CraftItemStack.asNMSCopy(itemStack);

            ChatHoverable.c c = new ChatHoverable.c(nmsItemStackObj);
            JsonElement json = ChatHoverable.c.b.encodeStart(JsonOps.INSTANCE, c).getOrThrow();

            return json.toString();
        } catch (Throwable t) {
            throw new RuntimeException("failed to serialize itemstack to nms item", t);
        }
    }
}
