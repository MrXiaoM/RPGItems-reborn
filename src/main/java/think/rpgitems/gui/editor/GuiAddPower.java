package think.rpgitems.gui.editor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
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
import think.rpgitems.item.RPGBaseHolder;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerManager;
import think.rpgitems.utils.nyaacore.Pair;

import java.util.HashMap;
import java.util.Map;

import static think.rpgitems.gui.editor.EditorHelper.list;

public class GuiAddPower implements IGui {
    Player player;
    RPGBaseHolder rpg;
    int row = 6;
    int page;
    boolean close = false;

    public GuiAddPower(Player player, RPGBaseHolder rpg, int page) {
        this.player = player;
        this.rpg = rpg;
        this.page = page;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory newInventory() {
        Inventory inv = Bukkit.createInventory(null, this.row * 9,
                I18n.getFormatted(player, "gui.add-power.title").replace("%page%", String.valueOf(this.page))
                        .replace("%name%", this.rpg.getName()).replace("%display%", this.rpg.getDisplayName()));
        this.updateItems(inv);
        return inv;
    }

    private void updateItems(Inventory inv) {
        inv.clear();
        Map<Integer, ItemStack> items = this.getGUIItems();
        for (int slot : items.keySet()) {
            inv.setItem(slot, items.get(slot));
        }
    }

    private void updateItems(InventoryView inv) {
        inv.getTopInventory().clear();
        Map<Integer, ItemStack> items = this.getGUIItems();
        for (int slot : items.keySet()) {
            inv.setItem(slot, items.get(slot));
        }
    }

    Map<Integer, NamespacedKey> keys = new HashMap<>();

    public Map<Integer, ItemStack> getGUIItems() {
        Map<Integer, ItemStack> items = new HashMap<>();
        this.keys.clear();
        int i = 0;
        int j = 45 * this.page;
        int k = 0;
        for (NamespacedKey power : PowerManager.getPowers().keySet()) {
            if (i >= j - 45 && i < j) {
                this.keys.put(k, power);
                items.put(k,
                        EditorHelper.buildItem(EditorHelper.getPowerIconFromConfig(player, power),
                                I18n.getFormatted(player, "gui.add-power.items.power.name").replace("%name%", power.getKey())
                                        .replace("%display%", EditorHelper.i18n("properties." + power.getKey() + ".main_name")),
                                list(player, "gui.add-power.items.power.lore",
                                        Pair.of("%namespace%", power.getNamespace()),
                                                Pair.of("%key%", power.getKey()),
                                                Pair.of("%description%", EditorHelper.i18nEmptyWhenNotFound(
                                                        "properties." + power.getKey() + ".main_description")))));
                k++;
            }
            i++;
        }
        if (this.page - 1 > 0) {
            items.put(45, EditorHelper.buildItem(Enums.valueOf(Material.class,
                            I18n.getFormatted(player, "gui.add-power.items.prev-page.material"), Material.LIME_STAINED_GLASS_PANE),
                    I18n.getFormatted(player, "gui.add-power.items.prev-page.name"),
                    list(player, "gui.add-power.items.prev-page.lore",
                            Pair.of("%page%", String.valueOf(this.page)), Pair.of("%max_page%",
                                    String.valueOf((int) Math.ceil(PowerManager.getPowers().size() / 45.0D))))));
        }
        if (this.page < (PowerManager.getPowers().size() / 45.0D)) {
            items.put(53, EditorHelper.buildItem(Enums.valueOf(Material.class,
                            I18n.getFormatted(player, "gui.add-power.items.next-page.material"), Material.LIME_STAINED_GLASS_PANE),
                    I18n.getFormatted(player, "gui.add-power.items.next-page.name"),
                    list(player, "gui.add-power.items.next-page.lore",
                            Pair.of("%page%", String.valueOf(this.page)), Pair.of("%max_page%",
                                    String.valueOf((int) Math.ceil(PowerManager.getPowers().size() / 45.0D))))));
        }
        items.put(49,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.add-power.items.back.material"),
                                Material.BARRIER),
                        I18n.getFormatted(player, "gui.add-power.items.back.name"),
                        list(player, "gui.add-power.items.back.lore")));
        return items;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        event.setCancelled(true);
        // 上一页
        if (slot == 45 && this.page - 1 > 0) {
            this.page--;
            updateItems(view);
            return;
        }
        // 下一页
        if (slot == 53 && this.page < (PowerManager.getPowers().size() / 45.0D)) {
            this.page++;
            updateItems(view);
            return;
        }
        // 点击添加技能
        if (slot < 45 && this.keys.containsKey(slot)) {
            NamespacedKey key = this.keys.get(slot);
            Class<? extends Power> type = PowerManager.getPower(key);
            if (type != null) {
                Power power = PowerManager.instantiate(type);
                power.init(new YamlConfiguration(), rpg.getName());

                this.close = true;
                new GuiEditPower(player, this.rpg, power, true, 1).open();
            }
            return;
        }
        // 返回技能列表菜单
        if (slot == 49) {
            this.close = true;
            new GuiPowerList(player, this.rpg, 1).open();
            return;
        }
    }

    @Override
    public void onClose(InventoryView view) {
        if (!this.close) {
            new GuiPowerList(player, this.rpg, 1).open();
        }
    }
}
