package think.rpgitems.gui.editor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import think.rpgitems.power.PropertyInstance;
import think.rpgitems.utils.nyaacore.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static think.rpgitems.gui.editor.EditorHelper.list;
import static think.rpgitems.gui.editor.EditorHelper.openGuiEditor;

public class GuiPowerList implements IGui {
    Player player;
    RPGBaseHolder rpg;
    int row = 6;
    int page;
    boolean close = false;

    public GuiPowerList(Player player, RPGBaseHolder rpg, int page) {
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
                I18n.getFormatted(player, "gui.power-list.title").replace("%page%", String.valueOf(this.page))
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

    public Map<Integer, ItemStack> getGUIItems() {
        Map<Integer, ItemStack> items = new HashMap<>();
        int i = 0;
        int j = 45 * this.page;
        int k = 0;
        for (Power power : this.rpg.getPowers()) {
            if (i >= j - 45 && i < j) {
                Map<String, Pair<Method, PropertyInstance>> props = PowerManager
                        .getProperties(power.getNamespacedKey());

                List<String> lore = new ArrayList<>();
                for (String s : list(player, "gui.power-list.items.power.lore")) {
                    s = s.replace("%display%", power.displayText() != null ? power.displayText() : "")
                            .replace("%description%", "&a" + EditorHelper.i18nEmptyWhenNotFound(
                                    "properties." + power.getNamespacedKey().getKey() + ".main_description"));
                    if (s.contains("%key%") && s.contains("%value%")) {
                        int l = 0;
                        for (String name : props.keySet()) {
                            try {
                                Field field = props.get(name).getValue().field();
                                lore.add(s.replace("%key%", name).replace("%value%", field.get(power).toString()));
                                l++;
                            } catch (Throwable t) {
                                // 收声
                            }
                            if (l > 6) {
                                lore.add(I18n.getFormatted(player, "gui.power-list.items.power.more-items").replace("%count%",
                                        String.valueOf(props.keySet().size())));
                                break;
                            }
                        }
                        continue;
                    }
                    lore.add(s);
                }
                items.put(k, EditorHelper.buildItem(EditorHelper.getPowerIconFromConfig(player, power.getNamespacedKey()),
                        "&e" + EditorHelper.i18n("properties." + power.getNamespacedKey().getKey() + ".main_name"), lore));
                k++;
            }
            i++;
        }
        if (this.page - 1 > 0) {
            items.put(45,
                    EditorHelper.buildItem(
                            Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.power-list.items.prev-page.material"),
                                    Material.LIME_STAINED_GLASS_PANE),
                            I18n.getFormatted(player, "gui.power-list.items.prev-page.name"),
                            list(player, "gui.power-list.items.prev-page.lore",
                                    Pair.of("%page%", String.valueOf(this.page)), Pair.of(
                                            "%max_page%",
                                            String.valueOf((int) Math.ceil(this.rpg.getPowers().size() / 45.0D))))));
        }
        if (this.page < (double) (this.rpg.getPowers().size() / 45.0D)) {
            items.put(53,
                    EditorHelper.buildItem(
                            Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.power-list.items.next-page.material"),
                                    Material.LIME_STAINED_GLASS_PANE),
                            I18n.getFormatted(player, "gui.power-list.items.next-page.name"),
                            list(player, "gui.power-list.items.next-page.lore",
                                    Pair.of("%page%", String.valueOf(this.page)), Pair.of(
                                            "%max_page%",
                                            String.valueOf((int) Math.ceil(this.rpg.getPowers().size() / 45.0D))))));
        }
        items.put(47,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.power-list.items.add-power.material"),
                                Material.HOPPER),
                        I18n.getFormatted(player, "gui.power-list.items.add-power.name"),
                        list(player, "gui.power-list.items.add-power.lore")));
        items.put(49,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.power-list.items.back.material"),
                                Material.BARRIER),
                        I18n.getFormatted(player, "gui.power-list.items.back.name"),
                        list(player, "gui.power-list.items.back.lore")));
        return items;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        event.setCancelled(true);
        boolean left = event.isLeftClick();
        boolean right = event.isRightClick();
        boolean shift = event.isShiftClick();
        // 点击技能图标
        if (slot < 45 && (45 * (this.page - 1) + slot) < this.rpg.getPowers().size()) {
            if (!shift) {
                if (left && !right) {
                    Power power = rpg.getPowers().get(45 * (this.page - 1) + slot);
                    this.close = true;
                    new GuiEditPower(player, this.rpg, power, false, 1).open();
                    return;
                }
                if (right && !left) {
                    Power power = rpg.getPowers().get(45 * (this.page - 1) + slot);
                    rpg.removePower(power);
                }
            }
        }
        // 上一页
        if (slot == 45 && this.page - 1 > 0) {
            this.page--;
            this.close = true;
            open();
            return;
        }
        // 下一页
        if (slot == 53 && this.page < (this.rpg.getPowers().size() / 45.0D)) {
            this.page++;
            open();
            return;
        }
        // 新建技能
        if (slot == 47) {
            this.close = true;
            new GuiAddPower(player, this.rpg, 1).open();
            return;
        }
        // 返回编辑菜单
        if (slot == 49) {
            this.close = true;
            openGuiEditor(player, rpg);
            return;
        }
        this.updateItems(view);
    }

    @Override
    public void onClose(InventoryView view) {
        if (!this.close) {
            openGuiEditor(player, rpg);
        }
    }
}
