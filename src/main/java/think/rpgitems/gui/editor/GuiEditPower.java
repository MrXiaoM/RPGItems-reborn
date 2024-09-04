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
import org.bukkit.potion.PotionEffectType;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.api.IGui;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGBaseHolder;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerManager;
import think.rpgitems.power.PropertyInstance;
import think.rpgitems.utils.nyaacore.Pair;
import think.rpgitems.utils.prompt.BasicPrompt;
import think.rpgitems.utils.prompt.PromptManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static think.rpgitems.gui.editor.EditorHelper.list;
import static think.rpgitems.gui.editor.EditorHelper.openGuiEditor;

public class GuiEditPower implements IGui {
    Player player;
    RPGBaseHolder rpg;
    Power power;
    boolean add;
    int page;
    int row = 6;
    boolean close = false;

    public GuiEditPower(Player player, RPGBaseHolder rpg, Power power, boolean add, int page) {
        this.player = player;
        this.rpg = rpg;
        this.power = power;
        this.add = add;
        this.page = page;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory newInventory() {
        Inventory inv = Bukkit.createInventory(null, this.row * 9,
                I18n.getFormatted(player, "gui.edit-power.title")
                        .replace("%type%",
                                this.add ? I18n.getFormatted(player, "gui.edit-power.title-type-add")
                                        : I18n.getFormatted(player, "gui.edit-power.title-type-edit"))
                        .replace("%power%", this.power.getName()).replace("%name%", this.rpg.getName())
                        .replace("%display%", this.rpg.getDisplayName()));
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

    private String getType(Class<?> cls) {
        String prefix = "";
        if (cls.equals(int.class) || cls.equals(double.class) || cls.equals(float.class)) {
            prefix = I18n.getFormatted(player, "gui.edit-power.types.number");
        } else if (cls.equals(String.class)) {
            prefix = I18n.getFormatted(player, "gui.edit-power.types.string");
        } else if (Enum.class.isAssignableFrom(cls)) {
            prefix = I18n.getFormatted(player, "gui.edit-power.types.enum");
        } else if (cls.equals(boolean.class) || cls.equals(Boolean.class)) {
            prefix = I18n.getFormatted(player, "gui.edit-power.types.boolean");
        } else if (cls.equals(PotionEffectType.class)) {
            prefix = I18n.getFormatted(player, "gui.edit-power.types.potion");
        }
        return prefix + cls.getName();
    }

    private List<String> getActionLore(Class<?> cls) {
        if (cls.equals(int.class) || cls.equals(double.class) || cls.equals(float.class)) {
            return list(player, "gui.edit-power.items.property.lores.type-number");
        } else if (cls.equals(String.class)) {
            return list(player, "gui.edit-power.items.property.lores.type-string");
        } else if (Enum.class.isAssignableFrom(cls)) {
            return list(player, "gui.edit-power.items.property.lores.type-enum");
        } else if (cls.equals(boolean.class) || cls.equals(Boolean.class)) {
            return list(player, "gui.edit-power.items.property.lores.type-boolean");
        } else if (cls.equals(PotionEffectType.class)) {
            return list(player, "gui.edit-power.items.property.lores.type-potion");
        }
        return list(player, "gui.edit-power.items.property.lores.type-unknown");
    }

    Map<Integer, Pair<String, PropertyInstance>> names = new HashMap<>();

    public Map<Integer, ItemStack> getGUIItems() {
        Map<Integer, ItemStack> items = new HashMap<>();
        this.names.clear();
        int i = 0;
        int j = 45 * this.page;
        int k = 0;
        Map<String, Pair<Method, PropertyInstance>> props = PowerManager.getProperties(this.power.getNamespacedKey());
        for (String name : props.keySet()) {
            if (i >= j - 45 && i < j) {
                PropertyInstance prop = props.get(name).getValue();
                String value = "&7暂无";
                try {
                    Object v = prop.field().get(this.power);
                    if (v != null) {
                        if (v instanceof Enum)
                            value = ((Enum<?>) v).name();
                        else
                            value = v.toString();
                    }
                } catch (Throwable t) {
                    // 收声
                }
                List<String> lore = list(player, "gui.edit-power.items.property.lores.head",
                                        Pair.of("%description%", EditorHelper.i18nEmptyWhenNotFound("properties." + this.power.getNamespacedKey().getKey() + "." + name)),
                                        Pair.of("%type%", this.getType(prop.field().getType())),
                                        Pair.of("%value%", value));
                lore.addAll(this.getActionLore(prop.field().getType()));
                items.put(
                        k, EditorHelper
                                .buildItem(
                                        prop.required()
                                                ? Enums.valueOf(Material.class,
                                                I18n.getFormatted(player, 
                                                        "gui.edit-power.items.property.material-required"),
                                                Material.RED_DYE)
                                                : Enums.valueOf(Material.class,
                                                I18n.getFormatted(player, "gui.edit-power.items.property.material"),
                                                Material.ITEM_FRAME),
                                        I18n.getFormatted(player, "gui.edit-power.items.property.name").replace("%name%", name),
                                        lore));
                this.names.put(k, new Pair<>(name, prop));
                k++;
            }
            i++;
        }
        if (this.page - 1 > 0) {
            items.put(45,
                    EditorHelper.buildItem(
                            Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.edit-power.items.prev-page.material"),
                                    Material.LIME_STAINED_GLASS_PANE),
                            I18n.getFormatted(player, "gui.edit-power.items.prev-page.name"),
                            list(player, "gui.edit-power.items.prev-page.lore",
                                    Pair.of("%page%", String.valueOf(this.page)),
                                    Pair.of("%max_page%", String.valueOf((int) Math.ceil(props.size() / 45.0D))))));
        }
        if (this.page < (double) (props.size() / 45.0D)) {
            items.put(53,
                    EditorHelper.buildItem(
                            Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.edit-power.items.next-page.material"),
                                    Material.LIME_STAINED_GLASS_PANE),
                            I18n.getFormatted(player, "gui.edit-power.items.next-page.name"),
                            list(player, "gui.edit-power.items.next-page.lore",
                                    Pair.of("%page%", String.valueOf(this.page)),
                                    Pair.of("%max_page%", String.valueOf((int) Math.ceil(props.size() / 45.0D))))));
        }
        if (this.add) {
            items.put(47,
                    EditorHelper.buildItem(
                            Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.edit-power.items.add-power.material"),
                                    Material.LIME_DYE),
                            I18n.getFormatted(player, "gui.edit-power.items.add-power.name"),
                            list(player, "gui.edit-power.items.add-power.lore")));
        }
        items.put(
                49, EditorHelper
                        .buildItem(
                                Enums.valueOf(Material.class,
                                        I18n.getFormatted(player, "gui.edit-power.items.back.material" + (this.add ? "-add" : "")),
                                        Material.BARRIER),
                                I18n.getFormatted(player, "gui.edit-power.items.back.name" + (this.add ? "-add" : "")),
                                list(player, "gui.edit-power.items.back.lore" + (this.add ? "-add" : ""))));

        return items;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        event.setCancelled(true);
        // 点击属性图标
        if (slot < 45 && this.names.containsKey(slot)) {
            Pair<String, PropertyInstance> pair = this.names.get(slot);
            boolean left = event.isLeftClick();
            boolean right = event.isRightClick();
            boolean shift = event.isShiftClick();
            Field field = pair.getValue().field();
            Class<?> cls = field.getType();
            // 类型: 数字
            if (cls.equals(int.class) || cls.equals(Integer.class) || cls.equals(Double.class)
                    || cls.equals(double.class) || cls.equals(Float.class) || cls.equals(float.class)) {
                if (shift) {
                    if (left) {
                        RPGItems.plugin.prompt
                                .runPrompt(new BasicPrompt(player, I18n.getFormatted(player, "gui.edit-power.prompt.number")) {
                                    @Override
                                    public void finishPrompt() {
                                        if (cls.equals(int.class) || cls.equals(Integer.class)) {
                                            try {
                                                int value = Integer.parseInt(this.getResult().get(0));
                                                field.set(GuiEditPower.this.power, value);
                                            } catch (Throwable t) {
                                                PromptManager.send(player,
                                                        I18n.getFormatted(player, "gui.edit-power.prompt.number-1"));
                                            }
                                        } else if (cls.equals(double.class) || cls.equals(Double.class)) {
                                            try {
                                                double value = Double.parseDouble(this.getResult().get(0));
                                                field.set(GuiEditPower.this.power, value);
                                            } catch (Throwable t) {
                                                PromptManager.send(player,
                                                        I18n.getFormatted(player, "gui.edit-power.prompt.number-1"));
                                            }
                                        } else if (cls.equals(float.class) || cls.equals(Float.class)) {
                                            try {
                                                float value = Float.parseFloat(this.getResult().get(0));
                                                field.set(GuiEditPower.this.power, value);
                                            } catch (Throwable t) {
                                                PromptManager.send(player,
                                                        I18n.getFormatted(player, "gui.edit-power.prompt.number-1"));
                                            }
                                        }
                                        GuiEditPower.this.close = false;
                                        new GuiEditPower(player, rpg, power, add, page).open();
                                    }

                                    @Override
                                    public void cancelPrompt() {
                                        super.cancelPrompt();
                                        openGuiEditor(player, rpg);
                                    }
                                });
                        this.close = true;
                        player.closeInventory();
                        return;
                    }
                } else {
                    if (cls.equals(int.class) || cls.equals(Integer.class)) {
                        try {
                            int value = pair.getValue().field().getInt(this.power);
                            field.set(this.power, value + 1 * (left && !right ? 1 : -1));
                        } catch (Throwable t) {
                            try {
                                field.set(this.power, 1 * (left && !right ? 1 : -1));
                            } catch (Throwable t1) {
                            }
                        }
                    } else if (cls.equals(double.class) || cls.equals(Double.class)) {
                        try {
                            double value = pair.getValue().field().getDouble(this.power);
                            field.set(this.power, value + 1 * (left && !right ? 1 : -1));
                        } catch (Throwable t) {
                            try {
                                field.set(this.power, 1.0D * (left && !right ? 1 : -1));
                            } catch (Throwable t1) {
                            }
                        }
                    } else if (cls.equals(float.class) || cls.equals(Float.class)) {
                        try {
                            float value = pair.getValue().field().getFloat(this.power);
                            field.set(this.power, value + 1 * (left && !right ? 1 : -1));
                        } catch (Throwable t) {
                            try {
                                field.set(this.power, 1.0F * (left && !right ? 1 : -1));
                            } catch (Throwable t1) {
                            }
                        }
                    }
                }
            }
            // 类型: 文本
            else if (cls.equals(String.class)) {
                if (!shift && left && !right) {
                    RPGItems.plugin.prompt
                            .runPrompt(new BasicPrompt(player, I18n.getFormatted(player, "gui.edit-power.prompt.string")) {
                                @Override
                                public void finishPrompt() {
                                    try {
                                        field.set(GuiEditPower.this.power, this.getResult().get(0));
                                    } catch (Throwable t) {
                                        PromptManager.send(player, I18n.getFormatted(player, "gui.edit-power.prompt.string-1"));
                                        t.printStackTrace();
                                    }
                                    GuiEditPower.this.close = false;
                                    new GuiEditPower(player, rpg, power, add, page).open();
                                }

                                @Override
                                public void cancelPrompt() {
                                    super.cancelPrompt();
                                    openGuiEditor(player, rpg);
                                }
                            });
                    this.close = true;
                    player.closeInventory();
                    return;
                }
            }
            // 类型: 枚举
            else if (Enum.class.isAssignableFrom(cls)) {
                if (!shift) {
                    if (left && !right) {
                        Enum<?> obj = (Enum<?>) cls.getEnumConstants()[0];
                        try {
                            obj = (Enum<?>) field.get(this.power);
                        } catch (Throwable t) {
                        }
                        obj = (Enum<?>) cls.getEnumConstants()[obj.ordinal() + 1 < cls.getEnumConstants().length
                                ? obj.ordinal() + 1
                                : 0];
                        try {
                            field.set(this.power, obj);
                        } catch (Throwable t) {
                        }
                    }
                    if (right && !left) {
                        RPGItems.plugin.prompt
                                .runPrompt(new BasicPrompt(player, I18n.getFormatted(player, "gui.edit-power.prompt.enum")) {
                                    @Override
                                    public void finishPrompt() {
                                        Object obj = Enums.valueOfForce(cls, this.getResult().get(0).toUpperCase(),
                                                null);
                                        if (obj != null) {
                                            try {
                                                field.set(GuiEditPower.this.power, obj);
                                            } catch (Throwable t) {
                                                t.printStackTrace();
                                                PromptManager.send(player,
                                                        I18n.getFormatted(player, "gui.edit-power.prompt.enum-1"));
                                            }
                                        } else {
                                            PromptManager.send(player, I18n.getFormatted(player, "gui.edit-power.prompt.enum-2")
                                                    .replace("%type%", cls.getSimpleName()));
                                        }
                                        GuiEditPower.this.close = false;
                                        new GuiEditPower(player, rpg, power, add, page).open();
                                    }

                                    @Override
                                    public void cancelPrompt() {
                                        super.cancelPrompt();
                                        openGuiEditor(player, rpg);
                                    }
                                });
                        this.close = true;
                        player.closeInventory();
                        return;
                    }
                }
            }
            // 类型: 布尔值
            else if (cls.equals(boolean.class) || cls.equals(Boolean.class)) {
                if (!shift) {
                    if (left && !right) {
                        try {
                            field.set(this.power, true);
                        } catch (Throwable t) {
                        }
                    }
                    if (right && !left) {
                        try {
                            field.set(this.power, false);
                        } catch (Throwable t) {
                        }
                    }
                }
            }
            // 类型: 药水效果
            else if (cls.equals(PotionEffectType.class)) {
                if (!shift) {
                    if (left && !right) {
                        // TODO 编写选择药水效果GUI
                        return;
                    }
                    if (right && !left) {
                        RPGItems.plugin.prompt
                                .runPrompt(new BasicPrompt(player, I18n.getFormatted(player, "gui.edit-power.prompt.potion")) {
                                    @Override
                                    public void finishPrompt() {
                                        boolean flag = false;
                                        for (PotionEffectType effect : PotionEffectType.values()) {
                                            if (effect.getName().toUpperCase()
                                                    .equals(this.getResult().get(0).toUpperCase())) {
                                                try {
                                                    field.set(GuiEditPower.this.power, effect);
                                                } catch (Throwable t) {
                                                    t.printStackTrace();
                                                    PromptManager.send(player,
                                                            I18n.getFormatted(player, "gui.edit-power.prompt.potion-1"));
                                                }
                                                flag = true;
                                            }
                                        }
                                        if (!flag) {
                                            PromptManager.send(player, I18n.getFormatted(player, "gui.edit-power.prompt.potion-2"));
                                        }
                                        GuiEditPower.this.close = false;
                                        new GuiEditPower(player, rpg, power, add, page).open();
                                    }

                                    @Override
                                    public void cancelPrompt() {
                                        super.cancelPrompt();
                                        openGuiEditor(player, rpg);
                                    }
                                });
                        this.close = true;
                        player.closeInventory();
                        return;
                    }
                }
            }
        }
        // 上一页
        if (slot == 45 && this.page - 1 > 0) {
            this.page--;
            this.close = true;
            new GuiEditPower(player, this.rpg, this.power, this.add, this.page).open();
            return;
        }
        // 下一页
        if (slot == 53
                && this.page < (PowerManager.getProperties(this.power.getNamespacedKey()).size() / 45.0D)) {
            this.page++;
        }
        // 新建技能
        if (this.add && slot == 47) {
            // this.power.setItem(this.rpg);
            this.rpg.addPower(this.power.getNamespacedKey(), this.power);
            ItemManager.refreshItem();
            rpg.save();
            this.close = true;
            new GuiPowerList(player, this.rpg, 1).open();
            return;
        }
        // 返回技能列表菜单
        if (slot == 49) {
            this.close = true;
            IGui gui = this.add ? new GuiAddPower(player, this.rpg, 1)
                    : new GuiPowerList(player, this.rpg, 1);
            gui.open();
            return;
        }
        this.updateItems(view);
    }

    @Override
    public void onClose(InventoryView view) {
        if (!this.close) {
            IGui gui = this.add ? new GuiAddPower(player, this.rpg, 1)
                    : new GuiPowerList(player, this.rpg, 1);
            gui.open();
        }
    }
}
