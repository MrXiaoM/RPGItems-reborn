package think.rpgitems.utils.nyaacore.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper for LangUtils
 */
public final class LocaleUtils {
    public static String getUnlocalizedName(Material material) {
        if (material == null) throw new IllegalArgumentException();
        return namespaceKeyToTranslationKey(material.isBlock() ? "block" : "item", material.getKey());
    }

    public static BaseComponent getNameComponent(ItemStack item) {
        if (item == null) throw new IllegalArgumentException();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            return new TextComponent(item.getItemMeta().getDisplayName());
        if (item.getItemMeta() instanceof SkullMeta && ((SkullMeta) item.getItemMeta()).hasOwner()) {
            String key = getUnlocalizedName(item.getType()) + ".named";
            return new TranslatableComponent(key, ((SkullMeta) item.getItemMeta()).getOwningPlayer().getName());
        }
        String key = item.getType().getItemTranslationKey();
        if (key == null) key = item.getType().getBlockTranslationKey();
        else key = (item.getType().isItem() ? "item" : "block") + ".minecraft." + item.getType().name().toLowerCase();
        return new TranslatableComponent(key);
    }

    public static String getUnlocalizedName(Enchantment ench) {
        return namespaceKeyToTranslationKey("enchantment", ench.getKey());
    }

    public static BaseComponent getNameComponent(Enchantment ench) {
        return new TranslatableComponent(getUnlocalizedName(ench));
    }

    public static String namespaceKeyToTranslationKey(String category, NamespacedKey namespacedKey) {
        return category + "." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
    }
}
