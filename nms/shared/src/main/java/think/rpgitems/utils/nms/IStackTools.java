package think.rpgitems.utils.nms;

import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public interface IStackTools {
    byte[] itemToBinary(ItemStack itemStack) throws IOException;
    default ItemStack itemFromBinary(byte[] nbt) throws IOException {
        return itemFromBinary(nbt, 0, nbt.length);
    }
    ItemStack itemFromBinary(byte[] nbt, int offset, int len) throws IOException;
    String itemsToBase64(List<ItemStack> items);
    List<ItemStack> itemsFromBase64(String base64);
    default String itemToBase64(ItemStack item) {
        if (item == null) throw new IllegalArgumentException();
        return itemsToBase64(Collections.singletonList(item));
    }
    default ItemStack itemFromBase64(String base64) {
        if (base64 == null) throw new IllegalArgumentException();
        List<ItemStack> ret = itemsFromBase64(base64);
        if (ret != null && !ret.isEmpty()) return ret.get(0);
        return null;
    }
    String itemToJson(ItemStack itemStack) throws RuntimeException;
    /**
     * @deprecated caller should use {@link CraftItemStack#asNMSCopy(ItemStack)} directly
     */
    @Deprecated
    Object asNMSCopy(ItemStack itemStack);
}
