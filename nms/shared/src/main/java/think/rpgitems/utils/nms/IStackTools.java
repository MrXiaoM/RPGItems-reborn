package think.rpgitems.utils.nms;

import org.bukkit.inventory.ItemStack;

public interface IStackTools {
    String itemToJson(ItemStack itemStack) throws RuntimeException;
}
