package think.rpgitems.item;

import think.rpgitems.utils.nyaacore.utils.ItemTagUtils;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static think.rpgitems.item.RPGItem.*;

public class ItemStackWrapper {
    private static HashMap<String, ItemStackWrapper> wrapperMap = new HashMap<>();
    ItemStack handle;
    Optional<String> itemUuid;

    private ItemStackWrapper(ItemStack itemStack){
        handle = itemStack;
        itemUuid = ItemTagUtils.getString(itemStack, NBT_ITEM_UUID);
    }

    public static ItemStackWrapper of(ItemStack itemStack){
        if (itemStack == null) {
            throw new NullPointerException();
        }
        return ItemTagUtils.getString(itemStack, NBT_ITEM_UUID)
                .map(s -> wrapperMap.computeIfAbsent(s, (u) -> new ItemStackWrapper(itemStack)))
                .orElseGet(() -> new ItemStackWrapper(itemStack));
    }


    @Override
    public int hashCode() {
        return itemUuid.map(String::hashCode).orElseGet(() -> handle.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        Optional<String> toCmpUuid;
        if (obj instanceof ItemStack) {
            ItemStack toCmp = (ItemStack) obj;
            toCmpUuid = ItemTagUtils.getString(toCmp, NBT_ITEM_UUID);
        }else if (obj instanceof ItemStackWrapper) {
            toCmpUuid = ((ItemStackWrapper) obj).itemUuid;
        }else {
            return false;
        }

        if (itemUuid.isPresent()){
            if (!toCmpUuid.isPresent()) {
                return false;
            }
            String uuid = itemUuid.get();
            return Objects.equals(uuid, toCmpUuid.get());
        }
        return Objects.equals(handle, obj);
    }
}
