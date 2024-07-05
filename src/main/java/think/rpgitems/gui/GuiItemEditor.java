package think.rpgitems.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.I18n;
import think.rpgitems.api.IGui;
import think.rpgitems.item.RPGItem;

public class GuiItemEditor implements IGui {
    private static final int size = 36;
    Player player;
    RPGItem rpg;
    public GuiItemEditor(Player player, RPGItem rpg) {
        this.player = player;
        this.rpg = rpg;
    }
    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory newInventory() {
        Inventory inv = Bukkit.createInventory(null, size, I18n.getFormatted(player, "gui.editor.title", rpg.getName()));
        // TODO: 添加编辑按钮
        return inv;
    }

    @Override
    public void onClick(
            InventoryAction action, ClickType click,
            InventoryType.SlotType slotType, int slot,
            ItemStack currentItem, ItemStack cursor,
            InventoryView view, InventoryClickEvent event
    ) {
        if (slot >= 36) return; // 允许点击物品栏
        event.setCancelled(true);
        // TODO: 点击界面执行操作
    }

    @Override
    public void onClose(InventoryView view) {

    }
}
