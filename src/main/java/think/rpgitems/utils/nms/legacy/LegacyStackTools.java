package think.rpgitems.utils.nms.legacy;

import org.bukkit.inventory.ItemStack;
import think.rpgitems.utils.nms.IStackTools;

public class LegacyStackTools implements IStackTools {
    @Override
    public String itemToJson(ItemStack itemStack) throws RuntimeException {
        return "{\"id\":\"minecraft:" + itemStack.getType().name().toLowerCase() + "\"," +
                "Count:" + itemStack.getAmount() + "}";
    }

    @Override
    public Object asNMSCopy(ItemStack itemStack) {
        return null;
    }
}
