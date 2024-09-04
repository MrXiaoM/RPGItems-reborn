package think.rpgitems.gui.editor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.item.RPGBaseHolder;
import think.rpgitems.item.RPGItem;
import think.rpgitems.item.RPGStone;
import think.rpgitems.utils.nyaacore.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorHelper {

    public static void openGuiEditor(Player player, RPGBaseHolder rpg) {
        if (rpg instanceof RPGItem) {
            new GuiItemEditor(player, (RPGItem) rpg).open();
        }
        if (rpg instanceof RPGStone) {
            // TODO: new GuiStoneEditor(player, (RPGStone) rpg).open();
        }
    }

    @SafeVarargs
    public static List<String> list(Player player, String key, Pair<String, Object>... pairs) {
        String[] split = I18n.getFormatted(player, key).split("\n");
        List<String> list = new ArrayList<>();
        for (String s : split) {
            for (Pair<String, Object> pair : pairs) {
                s = s.replace(pair.getKey(), String.valueOf(pair.getValue()));
            }
            list.add(s);
        }
        return list;
    }

    public static String i18n(String key, Object... args) {
        for (String lang : RPGItems.plugin.cfg.enabledLanguages) {
            I18n i18n = I18n.getInstance(lang);
            if (i18n.hasKey(key)) {
                return i18n.format(key, args);
            }
        }
        return key;
    }

    public static String i18nEmptyWhenNotFound(String key, Object... args) {
        for (String lang : RPGItems.plugin.cfg.enabledLanguages) {
            I18n i18n = I18n.getInstance(lang);
            if (i18n.hasKey(key)) {
                return i18n.format(key, args);
            }
        }
        return "";
    }

    /**
     * RPGItems 全技能图标 已将写死的值迁移到配置文件中
     *
     * @author MrXiaoM
     */
    public static Material getPowerIconFromConfig(Player player, NamespacedKey power) {
        return Enums.valueOf(Material.class,
                I18n.getFormatted(player, "gui.powers.materials." + power.getNamespace().toLowerCase() + "." + power.getKey().toLowerCase())
                        .toUpperCase(),
                Material.BOOK);
    }

    public static Map<Enchantment, Integer> getEnchantsFromBook(ItemStack item) {
        if (item != null && item.getType() == Material.ENCHANTED_BOOK) {
            ItemMeta im = getItemMeta(item);
            return ((EnchantmentStorageMeta) im).getStoredEnchants();
        }
        return new HashMap<>();
    }

    public static ItemStack buildItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta im = getItemMeta(item);
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (!lore.isEmpty()) {
            List<String> l = new ArrayList<>();
            for (String s : lore) {
                l.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', s));
            }
            im.setLore(l);
        }
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        im.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        item.setItemMeta(im);
        return item;
    }

    public static ItemMeta getItemMeta(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Bukkit.getItemFactory().getItemMeta(item.getType());
        return meta;
    }

    public static String getRomanNumber(int level) {
        switch (level) {
            case 0:
                return "";
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            case 11:
                return "XI";
            case 12:
                return "XII";
            case 13:
                return "XIII";
            case 14:
                return "XIV";
            case 15:
                return "XV";
            case 16:
                return "XVI";
            case 17:
                return "XVII";
            case 18:
                return "XVIII";
            case 19:
                return "XIX";
            case 20:
                return "XX";
            default:
                return String.valueOf(level);
        }
    }

    @SuppressWarnings("deprecation")
    public static String getEnchName(Enchantment ench) {
        switch (ench.getName().toUpperCase()) {
            case "PROTECTION_ENVIRONMENTAL":
                return "保护";
            case "PROTECTION_FIRE":
                return "火焰保护";
            case "PROTECTION_FALL":
                return "摔落保护";
            case "PROTECTION_EXPLOSIONS":
                return "爆炸保护";
            case "PROTECTION_PROJECTILE":
                return "弹射物保护";
            case "OXYGEN":
                return "水下呼吸";
            case "WATER_WORKER":
                return "水下速掘";
            case "THORNS":
                return "荆棘";
            case "DEPTH_STRIDER":
                return "深海探索者";
            case "FROST_WALKER":
                return "冰霜行者";
            case "BINDING_CURSE":
                return "绑定诅咒";
            case "DAMAGE_ALL":
                return "锋利";
            case "DAMAGE_UNDEAD":
                return "亡灵杀手";
            case "DAMAGE_ARTHROPODS":
                return "节肢杀手";
            case "KNOCKBACK":
                return "击退";
            case "FIRE_ASPECT":
                return "火焰附加";
            case "LOOT_BONUS_MOBS":
                return "抢夺";
            case "SWEEPING_EDGE":
                return "横扫之刃";
            case "DIG_SPEED":
                return "效率";
            case "SILK_TOUCH":
                return "精准采集";
            case "DURABILITY":
                return "耐久";
            case "LOOT_BONUS_BLOCKS":
                return "时运";
            case "ARROW_DAMAGE":
                return "力量";
            case "ARROW_KNOCKBACK":
                return "冲击";
            case "ARROW_FIRE":
                return "火矢";
            case "ARROW_INFINITE":
                return "无限";
            case "LUCK":
                return "海之眷顾";
            case "LURE":
                return "钓饵";
            case "LOYALTY":
                return "忠诚";
            case "IMPALING":
                return "穿刺";
            case "RIPTIDE":
                return "激流";
            case "CHANNELING":
                return "引雷";
            case "MULTISHOT":
                return "多重射击";
            case "QUICK_CHARGE":
                return "快速装填";
            case "PIERCING":
                return "穿透";
            case "MENDING":
                return "经验修补";
            case "VANISHING_CURSE":
                return "消失诅咒";
            default:
                return ench.getName();
        }
    }

    public static int strToInt(String str, int nullValue) {
        try {
            return Integer.parseInt(str);
        } catch (Throwable t) {
            return nullValue;
        }
    }
}
