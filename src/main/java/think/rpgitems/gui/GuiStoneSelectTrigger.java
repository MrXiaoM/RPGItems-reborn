package think.rpgitems.gui;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.api.IGui;
import think.rpgitems.item.RPGStone;
import think.rpgitems.utils.MaterialUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class GuiStoneSelectTrigger implements IGui {
    private final Player player;
    private final RPGStone stone;
    private Map<Integer, String> triggerMap = new HashMap<>();
    public GuiStoneSelectTrigger(Player player, RPGStone stone) {
        this.player = player;
        this.stone = stone;
    }
    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory newInventory() {
        String title = I18n.getFormatted(player, "message.gui.stone-select-trigger.title");
        Set<String> triggers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        triggers.addAll(stone.getAllowTriggers());
        triggers.addAll(stone.getAllowTriggersArmour());
        if (triggers.isEmpty()) {
            triggers.addAll(RPGItems.plugin.cfg.stoneTriggers);
            triggers.addAll(RPGItems.plugin.cfg.stoneTriggersArmour);
        }
        int size = 9;
        while (triggers.size() > size) {
            size += 9;
            if (size >= 54) break;
        }
        String currentTrigger = stone.getTrigger(player.getInventory().getItemInMainHand());
        Inventory inv = Bukkit.createInventory(null, size, title);
        int i = 0;
        for (String trigger : triggers) {
            String triggerItem = I18n.getFormatted(player, "properties.triggers." + trigger.toUpperCase() + ".item");
            String triggerDisplay = I18n.getFormatted(player, "properties.triggers." + trigger.toUpperCase() + ".display");
            String display = I18n.getFormatted(player, "message.gui.stone-select-trigger.display", triggerDisplay);
            ItemStack item = new ItemStack(MaterialUtils.parse(triggerItem, Material.PAPER));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(display);
                if (trigger.equals(currentTrigger)) {
                    meta.setLore(Lists.newArrayList(I18n.getFormatted(player, "message.gui.stone-select-trigger.lore-selected")));
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                item.setItemMeta(meta);
            }
            triggerMap.put(i, trigger);
            inv.setItem(i, item);
            i++;
        }
        return inv;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        event.setCancelled(true);
        if (!event.isShiftClick() && event.isLeftClick()) {
            String trigger = triggerMap.get(slot);
            if (trigger != null) {
                player.closeInventory();
                ItemStack item = player.getInventory().getItemInMainHand();
                if (stone.setTrigger(item, trigger)) {
                    stone.updateItem(item);
                    String triggerDisplay = I18n.getFormatted(player, "properties.triggers." + trigger.toUpperCase() + ".display");
                    player.sendMessage(I18n.getFormatted(player, "message.gui.stone-select-trigger.set", triggerDisplay));
                } else {
                    player.sendMessage(I18n.getFormatted(player, "message.gui.stone-select-trigger.set-fail"));
                }
            }
        }
    }
}
