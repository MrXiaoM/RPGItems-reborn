package think.rpgitems.gui.editor;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.api.IGui;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.item.RPGItem.AttributeMode;
import think.rpgitems.item.RPGItem.DamageMode;
import think.rpgitems.item.RPGItem.EnchantMode;
import think.rpgitems.utils.nyaacore.Pair;
import think.rpgitems.utils.prompt.BasicPrompt;
import think.rpgitems.utils.prompt.PromptManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static think.rpgitems.gui.editor.EditorHelper.list;

public class GuiItemEditor implements IGui {
    Player player;
    int raw = 3;
    RPGItem rpg;

    public GuiItemEditor(Player player, RPGItem rpg) {
        this.player = player;
        this.rpg = rpg;
    }

    enum DamageChangeMode {
        BOTH, MIN, MAX;

        public String getDisplay(Player player) {
            return I18n.getFormatted(player, "damage-change-mode." + this.name().toLowerCase());
        }
    }

    DamageChangeMode damageChangeMode = DamageChangeMode.BOTH;

    private void changeMode() {
        int i = this.damageChangeMode.ordinal() + 1;
        if (i >= DamageChangeMode.values().length)
            i = 0;
        this.damageChangeMode = DamageChangeMode.values()[i];
    }

    private void changeDamageMode() {
        int i = this.rpg.getDamageMode().ordinal() + 1;
        if (i >= DamageMode.values().length)
            i = 0;
        this.rpg.setDamageMode(DamageMode.values()[i]);
    }

    private void changeAttributeMode() {
        int i = this.rpg.getAttributeMode().ordinal() + 1;
        if (i >= AttributeMode.values().length)
            i = 0;
        this.rpg.setAttributeMode(AttributeMode.values()[i]);
    }

    private void changeEnchantMode() {
        int i = this.rpg.getEnchantMode().ordinal() + 1;
        if (i >= EnchantMode.values().length)
            i = 0;
        this.rpg.setEnchantMode(EnchantMode.values()[i]);
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory newInventory() {
        Inventory inv = Bukkit.createInventory(null, this.raw * 9, I18n.getFormatted(player, "gui.editor.title")
                .replace("%display%", this.rpg.getDisplayName()).replace("%name%", this.rpg.getName()));

        this.updateItems(inv);
        return inv;
    }

    private void runCmd(Player sender, String cmd) {
        Bukkit.getPluginManager().callEvent(new PlayerCommandPreprocessEvent(sender, cmd));
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
    

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        boolean left = event.isLeftClick();
        boolean right = event.isRightClick();
        boolean shift = event.isShiftClick();
        if (slot < this.raw * 9) {
            event.setCancelled(true);
        }

        // 编辑物品名
        if (slot == 1 && left && !right && !shift) {
            RPGItems.plugin.prompt
                    .runPrompt(new BasicPrompt(player, I18n.getFormatted(player, "gui.editor.prompt.display-name")) {
                        @Override
                        public void finishPrompt() {
                            GuiItemEditor.this.rpg.setDisplayName(this.getResult().get(0));
                            GuiItemEditor.this.saveItem();
                            PromptManager.send(player,
                                    I18n.getFormatted(player, "gui.editor.prompt.display-name-1")
                                            .replace("%name%", GuiItemEditor.this.rpg.getName())
                                            .replace("%display%", this.getResult().get(0)));

                            new GuiItemEditor(player, rpg).open();
                        }

                        @Override
                        public void cancelPrompt() {
                            super.cancelPrompt();
                            new GuiItemEditor(player, rpg).open();
                        }
                    });
            player.closeInventory();
            return;
        }
        if (slot == 2) {
            if (!shift) {
                // 最后一行追加 Lore
                if (left && !right) {

                    RPGItems.plugin.prompt
                            .runPrompt(new BasicPrompt(player, I18n.getFormatted(player, "gui.editor.prompt.add-lore")) {
                                @Override
                                public void finishPrompt() {
                                    GuiItemEditor.this.rpg.addDescription(this.getResult().get(0));
                                    GuiItemEditor.this.saveItem();
                                    PromptManager.send(player, I18n.getFormatted(player, "gui.editor.prompt.add-lore-1")
                                            .replace("%name%", GuiItemEditor.this.rpg.getName())
                                            .replace("%display%", GuiItemEditor.this.rpg.getDisplayName())
                                            .replace("%line%",
                                                    String.valueOf(GuiItemEditor.this.rpg.getDescription().size() - 1))
                                            .replace("%content%", this.getResult().get(0)));
                                    
                                    new GuiItemEditor(player, rpg).open();
                                }

                                @Override
                                public void cancelPrompt() {
                                    super.cancelPrompt();
                                    
                                    new GuiItemEditor(player, rpg).open();
                                }
                            });
                    player.closeInventory();
                    return;
                }
                // 指定行插入 Lore
                if (right && !left) {
                    RPGItems.plugin.prompt
                            .runPrompt(new BasicPrompt(player, I18n.getFormatted(player, "gui.editor.prompt.insert-lore"),
                                    I18n.getFormatted(player, "gui.editor.prompt.insert-lore-2")) {
                                @Override
                                public void finishPrompt() {
                                    int line = EditorHelper.strToInt(this.getResult().get(0), -1);
                                    if (line < 0) {
                                        PromptManager.send(player,
                                                I18n.getFormatted(player, "gui.editor.prompt.insert-lore-1")
                                                        .replace("%name%", GuiItemEditor.this.rpg.getName())
                                                        .replace("%display%", GuiItemEditor.this.rpg.getDisplayName())
                                                        .replace("%line%", this.getResult().get(0)));
                                        new GuiItemEditor(player, rpg).open();
                                        return;
                                    }

                                    List<String> desc = GuiItemEditor.this.rpg.getDescription();
                                    line = Math.max(line, desc.size() - 1);
                                    desc.add(line,
                                            ChatColor.translateAlternateColorCodes('&', this.getResult().get(1)));
                                    GuiItemEditor.this.rpg.setDescription(desc);
                                    GuiItemEditor.this.saveItem();
                                    PromptManager.send(player,
                                            I18n.getFormatted(player, "gui.editor.prompt.insert-lore-3")
                                                    .replace("%name%", GuiItemEditor.this.rpg.getName())
                                                    .replace("%display%", GuiItemEditor.this.rpg.getDisplayName())
                                                    .replace("%line%", String.valueOf(line))
                                                    .replace("%content%", this.getResult().get(1)));
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }

                                @Override
                                public void cancelPrompt() {
                                    super.cancelPrompt();
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }
                            });
                    player.closeInventory();
                    return;
                }
            } else {
                // 删除最后一行 Lore
                if (left && !right) {

                    List<String> desc = this.rpg.getDescription();
                    int line = desc.size() - 1;
                    if (line >= 0)
                        desc.remove(line);
                    this.rpg.setDescription(desc);
                    this.saveItem();

                    PromptManager.send(player,
                            I18n.getFormatted(player, "gui.editor.del-lore-last-line")
                                    .replace("%name%", GuiItemEditor.this.rpg.getName())
                                    .replace("%display%", GuiItemEditor.this.rpg.getDisplayName())
                                    .replace("%line%", String.valueOf(line)));
                    this.updateItems(view);
                    return;
                }
                // 删除指定行 Lore
                if (right && !left) {
                    RPGItems.plugin.prompt
                            .runPrompt(new BasicPrompt(player, I18n.getFormatted(player, "gui.editor.prompt.del-lore")) {
                                @Override
                                public void finishPrompt() {
                                    int line = EditorHelper.strToInt(this.getResult().get(0), -1);
                                    if (line < 0) {
                                        PromptManager.send(player,
                                                I18n.getFormatted(player, "gui.editor.prompt.del-lore-1")
                                                        .replace("%name%", GuiItemEditor.this.rpg.getName())
                                                        .replace("%display%", GuiItemEditor.this.rpg.getDisplayName())
                                                        .replace("%line%", this.getResult().get(0)));
                                        
                                                new GuiItemEditor(player, rpg).open();
                                        return;
                                    }
                                    List<String> desc = GuiItemEditor.this.rpg.getDescription();
                                    if (line < desc.size())
                                        desc.remove(line);
                                    GuiItemEditor.this.rpg.setDescription(desc);
                                    GuiItemEditor.this.saveItem();
                                    PromptManager.send(player,
                                            I18n.getFormatted(player, "gui.editor.prompt.del-lore-2")
                                                    .replace("%name%", GuiItemEditor.this.rpg.getName())
                                                    .replace("%display%", GuiItemEditor.this.rpg.getDisplayName())
                                                    .replace("%line%", String.valueOf(line)));
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }

                                @Override
                                public void cancelPrompt() {
                                    super.cancelPrompt();
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }
                            });
                    player.closeInventory();
                    return;
                }
            }
        }
        if (slot == 3 && left && !right && !shift) {
            RPGItems.plugin.prompt.runPrompt(new BasicPrompt(player,
                    I18n.getFormatted(player, "gui.editor.prompt.set-lore"), I18n.getFormatted(player, "gui.editor.prompt.set-lore-2")) {
                @Override
                public void finishPrompt() {
                    int line = EditorHelper.strToInt(this.getResult().get(0), -1);
                    if (line < 0) {
                        PromptManager.send(player,
                                I18n.getFormatted(player, "gui.editor.prompt.set-lore-1")
                                        .replace("%name%", GuiItemEditor.this.rpg.getName())
                                        .replace("%display%", GuiItemEditor.this.rpg.getDisplayName())
                                        .replace("%line%", this.getResult().get(0)));
                         new GuiItemEditor(player, rpg).open();
                        return;
                    }
                    List<String> desc = GuiItemEditor.this.rpg.getDescription();
                    desc.set(line, ChatColor.translateAlternateColorCodes('&', this.getResult().get(1)));
                    GuiItemEditor.this.rpg.setDescription(desc);
                    GuiItemEditor.this.saveItem();
                    PromptManager.send(player, I18n.getFormatted(player, "gui.editor.prompt.set-lore-3")
                            .replace("%name%", GuiItemEditor.this.rpg.getName())
                            .replace("%display%", GuiItemEditor.this.rpg.getDisplayName())
                            .replace("%line%", String.valueOf(line)).replace("%content%", this.getResult().get(1)));
                     new GuiItemEditor(player, rpg).open();
                }

                @Override
                public void cancelPrompt() {
                    super.cancelPrompt();
                     new GuiItemEditor(player, rpg).open();
                }
            });
            player.closeInventory();
            return;
        }
        // 操作伤害
        if (slot == 4) {
            if (!shift) {
                int offset = (left && !right ? 1 : -1);
                int min = this.rpg.getDamageMin();
                int max = this.rpg.getDamageMax();
                // 伤害 +-1
                if (this.damageChangeMode == DamageChangeMode.MIN || this.damageChangeMode == DamageChangeMode.BOTH) {
                    min += offset;
                }
                if (this.damageChangeMode == DamageChangeMode.MAX || this.damageChangeMode == DamageChangeMode.BOTH) {
                    max += offset;
                }
                this.rpg.setDamage(min, max);
                this.saveItem();
                this.updateItems(view);
                return;
            } else {
                // 切换模式
                if (left && !right) {
                    this.changeMode();
                    this.updateItems(view);
                    return;
                }
                // 手动输入伤害
                if (right && !left) {
                    RPGItems.plugin.prompt
                            .runPrompt(new BasicPrompt(player, I18n.getFormatted(player, "gui.editor.prompt.set-damage")) {
                                @Override
                                public void finishPrompt() {
                                    GuiItemEditor.this.runCmd(player, "rpgitem damage " + GuiItemEditor.this.rpg.getName() + " "
                                            + this.getResult().get(0));
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }

                                @Override
                                public void cancelPrompt() {
                                    super.cancelPrompt();
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }
                            });
                    player.closeInventory();
                    return;
                }
            }
        }
        // 修改护甲值
        if (slot == 5) {
            if (!shift) {
                if (left && !right) {
                    this.rpg.setArmour(this.rpg.getArmour() + 1);
                    this.saveItem();
                    this.updateItems(view);
                    return;
                }
                if (right && !left) {
                    this.rpg.setArmour(this.rpg.getArmour() - 1);
                    this.saveItem();
                    this.updateItems(view);
                    return;
                }
            } else {
                if (left && !right) {
                    RPGItems.plugin.prompt
                            .runPrompt(new BasicPrompt(player, I18n.getFormatted(player, "gui.editor.prompt.set-armour")) {
                                @Override
                                public void finishPrompt() {
                                    GuiItemEditor.this.runCmd(player, "rpgitem armour " + GuiItemEditor.this.rpg.getName() + " "
                                            + this.getResult().get(0));
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }

                                @Override
                                public void cancelPrompt() {
                                    super.cancelPrompt();
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }
                            });
                    player.closeInventory();
                    return;
                }
                if (right && !left) {
                    RPGItems.plugin.prompt.runPrompt(
                            new BasicPrompt(player, I18n.getFormatted(player, "gui.editor.prompt.set-armour-expression")) {
                                @Override
                                public void finishPrompt() {
                                    GuiItemEditor.this.runCmd(player, "rpgitem armorExpression "
                                            + GuiItemEditor.this.rpg.getName() + " " + this.getResult().get(0));
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }

                                @Override
                                public void cancelPrompt() {
                                    super.cancelPrompt();
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }
                            });
                    player.closeInventory();
                    return;
                }
            }
        }
        // 修改物品材质
        if (slot == 6) {
            if (!shift) {
                if (left && !right && currentItem != null && currentItem.getType() != Material.AIR) {
                    this.rpg.setItem(currentItem.getType());
                    this.saveItem();
                    this.updateItems(view);
                    return;
                }
                if (right && !left) {
                    RPGItems.plugin.prompt
                            .runPrompt(new BasicPrompt(player, I18n.getFormatted(player, "gui.editor.prompt.set-material")) {
                                @Override
                                public void finishPrompt() {
                                    Material material = Enums.valueOf(Material.class,
                                            this.getResult().get(0).toUpperCase(), null);
                                    if (material == null) {
                                        PromptManager.send(player,
                                                I18n.getFormatted(player, "gui.editor.prompt.set-material-1")
                                                        .replace("%name%", GuiItemEditor.this.rpg.getName())
                                                        .replace("%display%", GuiItemEditor.this.rpg.getDisplayName())
                                                        .replace("%material%", this.getResult().get(0)));
                                    } else {
                                        GuiItemEditor.this.rpg.setItem(material);
                                        GuiItemEditor.this.saveItem();
                                        PromptManager.send(player,
                                                I18n.getFormatted(player, "gui.editor.prompt.set-material-2")
                                                        .replace("%name%", GuiItemEditor.this.rpg.getName())
                                                        .replace("%display%", GuiItemEditor.this.rpg.getDisplayName())
                                                        .replace("%material%", material.name()));
                                    }
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }

                                @Override
                                public void cancelPrompt() {
                                    super.cancelPrompt();
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }
                            });
                    player.closeInventory();
                    return;
                }
            }
        }
        // 修改伤害模式
        if (slot == 7) {
            if (!shift && left && !right) {
                this.changeDamageMode();
                this.saveItem();
                this.updateItems(view);
            }
        }
        // 修改属性更新模式
        if (slot == 8) {
            if (!shift && left && !right) {
                this.changeAttributeMode();
                this.saveItem();
                this.updateItems(view);
            }
        }
        // 修改附魔
        if (slot == 9) {
            if (!shift) {
                if (currentItem == null || currentItem.getType() != Material.ENCHANTED_BOOK) {
                    if (left && !right) {
                        this.changeEnchantMode();
                        this.saveItem();
                        this.updateItems(view);
                        return;
                    }
                } else {
                    // 添加/覆盖/移除 附魔
                    Map<Enchantment, Integer> rpgEnchs = this.rpg.getEnchantMap();
                    if (rpgEnchs == null)
                        rpgEnchs = new HashMap<>();
                    Map<Enchantment, Integer> enchs = EditorHelper.getEnchantsFromBook(currentItem);
                    if (left && !right) {
                        for (Enchantment ench : enchs.keySet()) {
                            rpgEnchs.put(ench, enchs.get(ench));
                        }
                    }
                    if (right && !left) {
                        for (Enchantment ench : Lists.newArrayList(enchs.keySet())) {
                            rpgEnchs.remove(ench);
                        }
                    }
                    this.rpg.setEnchantMap(rpgEnchs);
                    this.saveItem();
                    this.updateItems(view);
                    return;
                }
            }
        }
        // 修改权限
        if (slot == 10) {
            if (!shift) {
                // 开关
                if (left && !right) {
                    this.rpg.setHasPermission(this.rpg.isHasPermission());
                    this.saveItem();
                    this.updateItems(view);
                    return;
                }
                // 设置
                if (right && !left) {
                    RPGItems.plugin.prompt
                            .runPrompt(new BasicPrompt(player, I18n.getFormatted(player, "gui.editor.prompt.set-permission")) {
                                @Override
                                public void finishPrompt() {
                                    GuiItemEditor.this.rpg.setPermission(this.getResult().get(0));
                                    GuiItemEditor.this.saveItem();
                                    PromptManager.send(player,
                                            I18n.getFormatted(player, "gui.editor.prompt.set-permission-1")
                                                    .replace("%name%", GuiItemEditor.this.rpg.getName())
                                                    .replace("%display%", GuiItemEditor.this.rpg.getDisplayName())
                                                    .replace("%permission%", this.getResult().get(0)));

                                    
                                            new GuiItemEditor(player, rpg).open();
                                }

                                @Override
                                public void cancelPrompt() {
                                    super.cancelPrompt();
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }
                            });
                    player.closeInventory();
                    return;
                }
            }
        }
        // 修改Lore显示
        if (slot == 11) {
            if (!shift) {
                // 基础属性
                if (left && !right) {
                    this.rpg.setShowArmourLore(!this.rpg.isShowArmourLore());
                    this.saveItem();
                    this.updateItems(view);
                    return;
                }
                // 技能文本
                if (right && !left) {
                    this.rpg.setShowPowerText(!this.rpg.isShowPowerText());
                    this.saveItem();
                    this.updateItems(view);
                    return;
                }
            }
        }
        // 自定义模型
        if (slot == 12) {
            if (!shift) {
                // 开关
                if (left && !right) {
                    this.rpg.setCustomItemModel(!this.rpg.isCustomItemModel());
                    this.saveItem();
                    this.updateItems(view);
                    return;
                }
                // 数据值
                if (right && !left) {

                    RPGItems.plugin.prompt.runPrompt(
                            new BasicPrompt(player, I18n.getFormatted(player, "gui.editor.prompt.set-custom-model-data")) {
                                @Override
                                public void finishPrompt() {
                                    GuiItemEditor.this.runCmd(player, "rpgitem customModel " + GuiItemEditor.this.rpg.getName()
                                            + " " + this.getResult().get(0));
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }

                                @Override
                                public void cancelPrompt() {
                                    super.cancelPrompt();
                                    
                                            new GuiItemEditor(player, rpg).open();
                                }
                            });
                    player.closeInventory();
                    return;
                }
            }
        }
        // 忽略 WorldGuard
        if (slot == 13) {
            if (!shift && left && !right) {
                this.rpg.setIgnoreWorldGuard(!this.rpg.isIgnoreWorldGuard());
                this.saveItem();
                this.updateItems(view);
                return;
            }
        }
        // 打开技能编辑器
        if (slot == 14) {
            if (!shift && left && !right) {
                player.closeInventory();
                new GuiPowerList(player, this.rpg, 1).open();
                return;
            }
        }
        // 修改 ItemFlag
        if (slot == 15) {
            if (!shift && left && !right) {
                ItemFlag flag = ItemFlag.HIDE_ATTRIBUTES;
                List<ItemFlag> flags = this.rpg.getItemFlags();
                if (flags.contains(flag)) {
                    flags.remove(flag);
                } else {
                    flags.add(flag);
                }
                this.rpg.setItemFlags(flags);
                this.saveItem();
                this.updateItems(view);
                return;
            }
        }
        if (slot == 16) {
            if (!shift && left && !right) {
                ItemFlag flag = ItemFlag.HIDE_DESTROYS;
                List<ItemFlag> flags = this.rpg.getItemFlags();
                if (flags.contains(flag)) {
                    flags.remove(flag);
                } else {
                    flags.add(flag);
                }
                this.rpg.setItemFlags(flags);
                this.saveItem();
                this.updateItems(view);
                return;
            }
        }
        if (slot == 17) {
            if (!shift && left && !right) {
                ItemFlag flag = ItemFlag.HIDE_ENCHANTS;
                List<ItemFlag> flags = this.rpg.getItemFlags();
                if (flags.contains(flag)) {
                    flags.remove(flag);
                } else {
                    flags.add(flag);
                }
                this.rpg.setItemFlags(flags);
                this.saveItem();
                this.updateItems(view);
                return;
            }
        }
        if (slot == 18) {
            if (!shift && left && !right) {
                ItemFlag flag = ItemFlag.HIDE_PLACED_ON;
                List<ItemFlag> flags = this.rpg.getItemFlags();
                if (flags.contains(flag)) {
                    flags.remove(flag);
                } else {
                    flags.add(flag);
                }
                this.rpg.setItemFlags(flags);
                this.saveItem();
                this.updateItems(view);
                return;
            }
        }
        if (slot == 19) {
            if (!shift && left && !right) {
                ItemFlag flag = ItemFlag.HIDE_POTION_EFFECTS;
                List<ItemFlag> flags = this.rpg.getItemFlags();
                if (flags.contains(flag)) {
                    flags.remove(flag);
                } else {
                    flags.add(flag);
                }
                this.rpg.setItemFlags(flags);
                this.saveItem();
                this.updateItems(view);
                return;
            }
        }
        if (slot == 20) {
            if (!shift && left && !right) {
                ItemFlag flag = ItemFlag.HIDE_UNBREAKABLE;
                List<ItemFlag> flags = this.rpg.getItemFlags();
                if (flags.contains(flag)) {
                    flags.remove(flag);
                } else {
                    flags.add(flag);
                }
                this.rpg.setItemFlags(flags);
                this.saveItem();
                this.updateItems(view);
            }
        }
    }

    private Map<Integer, ItemStack> getGUIItems() {
        Map<Integer, ItemStack> items = new HashMap<>();
        items.put(0, this.rpg.toItemStack(player));
        items.put(1,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.edit-display-name.material"),
                                Material.NAME_TAG),
                        I18n.getFormatted(player, "gui.editor.items.edit-display-name.name"),
                        list(player, "gui.editor.items.edit-display-name.lore")));
        items.put(2,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.add-del-lore.material"),
                                Material.BOOK),
                        I18n.getFormatted(player, "gui.editor.items.add-del-lore.name"),
                        list(player, "gui.editor.items.add-del-lore.lore")));
        items.put(3,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.set-lore.material"),
                                Material.BOOK),
                        I18n.getFormatted(player, "gui.editor.items.set-lore.name"),
                        list(player, "gui.editor.items.set-lore.lore")));
        items.put(
                4, EditorHelper
                        .buildItem(Enums.valueOf(Material.class,
                                        I18n.getFormatted(player, "gui.editor.items.set-damage.material"), Material.IRON_SWORD),
                                I18n.getFormatted(player, "gui.editor.items.set-damage.name"),
                                list(player, "gui.editor.items.set-damage.lore",
                                                Pair.of("%damage%",
                                                        this.rpg.getDamageMin() == this.rpg.getDamageMax()
                                                                ? String.valueOf(this.rpg.getDamageMax())
                                                                : (this.rpg.getDamageMin() + "-"
                                                                + this.rpg.getDamageMax())),
                                                Pair.of("%mode%", this.damageChangeMode.getDisplay(player)))));
        items.put(5,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.set-armour.material"),
                                Material.IRON_CHESTPLATE),
                        I18n.getFormatted(player, "gui.editor.items.set-armour.name"),
                        list(player, "gui.editor.items.set-armour.lore",
                                Pair.of("%armour%", String.valueOf(this.rpg.getArmour())),
                                        Pair.of("%armour_expression%", this.rpg.getArmourExpression()))));
        items.put(6, EditorHelper.buildItem(this.rpg.getItem(), I18n.getFormatted(player, "gui.editor.items.set-material.name"),
                list(player, "gui.editor.items.set-material.lore")));
        items.put(7,
                EditorHelper.buildItem(Enums.valueOf(Material.class,
                                I18n.getFormatted(player, "gui.editor.items.change-damage-mode.material"), Material.STONE_SWORD),
                        I18n.getFormatted(player, "gui.editor.items.change-damage-mode.name"),
                        list(player, "gui.editor.items.change-damage-mode.lore",
                                Pair.of("%mode%",
                                        I18n.getFormatted(player, "gui.editor.items.change-damage-mode.modes."
                                                + this.rpg.getDamageMode().name().toUpperCase())))));
        items.put(8,
                EditorHelper.buildItem(Enums.valueOf(Material.class,
                                I18n.getFormatted(player, "gui.editor.items.change-attribute-mode.material"), Material.GOLDEN_APPLE),
                        I18n.getFormatted(player, "gui.editor.items.change-attribute-mode.name"),
                        list(player, "gui.editor.items.change-attribute-mode.lore",
                                Pair.of("%mode%",
                                        I18n.getFormatted(player, "gui.editor.items.change-attribute-mode.modes."
                                                + this.rpg.getAttributeMode().name().toUpperCase())))));
        items.put(9,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.enchant.material"),
                                Material.ENCHANTED_BOOK),
                        I18n.getFormatted(player, "gui.editor.items.enchant.name"), this.getEnchantLore()));
        items.put(10,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.permission.material"), Material.IRON_INGOT),
                        I18n.getFormatted(player, "gui.editor.items.permission.name"),
                        list(player, "gui.editor.items.permission.lore",
                                Pair.of("%has%",
                                        this.rpg.isHasPermission() ? I18n.getFormatted(player, "gui.editor.items.permission.true")
                                                : I18n.getFormatted(player, "gui.editor.items.permission.false")),
                                Pair.of("%permission%", this.rpg.getPermission()))));
        items.put(11, EditorHelper.buildItem(
                Enums.valueOf(
                        Material.class, I18n.getFormatted(player, "gui.editor.items.set-lore-visible.material"), Material.LEVER),
                I18n.getFormatted(player, "gui.editor.items.set-lore-visible.name"),
                list(player, "gui.editor.items.set-lore-visible.lore",
                        Pair.of("%show_armour_lore%",
                                this.rpg.isShowArmourLore() ? I18n.getFormatted(player, "gui.editor.items.set-lore-visible.true")
                                        : I18n.getFormatted(player, "gui.editor.items.set-lore-visible.false")),
                        Pair.of("%show_power_lore%",
                                this.rpg.isShowPowerText() ? I18n.getFormatted(player, "gui.editor.items.set-lore-visible.true")
                                        : I18n.getFormatted(player, "gui.editor.items.set-lore-visible.false")))));
        items.put(12, EditorHelper.buildItem(
                Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.set-custom-model.material"), Material.LEATHER_CHESTPLATE),
                I18n.getFormatted(player, "gui.editor.items.set-custom-model.name"),
                list(player, "gui.editor.items.set-custom-model.lore",
                        Pair.of("%switch%",
                                this.rpg.isCustomItemModel() ? I18n.getFormatted(player, "gui.editor.items.set-custom-model.true")
                                        : I18n.getFormatted(player, "gui.editor.items.set-custom-model.false")),
                        Pair.of("%data%", String.valueOf(this.rpg.getCustomModelData())))));
        items.put(13,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class,
                                I18n.getFormatted(player, "gui.editor.items.set-ignore-worldguard.material"), Material.BARRIER),
                        I18n.getFormatted(player, "gui.editor.items.set-ignore-worldguard.name"),
                        list(player, "gui.editor.items.set-ignore-worldguard.lore",
                                        Pair.of("%switch%", this.rpg.isIgnoreWorldGuard()
                                                ? I18n.getFormatted(player, "gui.editor.items.set-ignore-worldguard.true")
                                                : I18n.getFormatted(player, "gui.editor.items.set-ignore-worldguard.false")))));
        items.put(14,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.edit-powers.material"),
                                Material.NETHER_STAR),
                        I18n.getFormatted(player, "gui.editor.items.edit-powers.name"),
                        list(player, "gui.editor.items.edit-powers.lore")));
        items.put(15,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.edit-item-flag.material"),
                                Material.PAPER),
                        I18n.getFormatted(player, "gui.editor.items.edit-item-flag.name").replace("%flag%",
                                I18n.getFormatted(player, "gui.editor.items.edit-item-flag.flags.HIDE_ATTRIBUTES")),
                        list(player, "gui.editor.items.edit-item-flag.lore",
                                Pair.of("%value%",
                                        this.rpg.getItemFlags().contains(ItemFlag.HIDE_ATTRIBUTES)
                                                ? I18n.getFormatted(player, "gui.editor.items.edit-item-flag.true")
                                                : I18n.getFormatted(player, "gui.editor.items.edit-item-flag.false")))));
        items.put(16,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.edit-item-flag.material"),
                                Material.PAPER),
                        I18n.getFormatted(player, "gui.editor.items.edit-item-flag.name").replace("%flag%",
                                I18n.getFormatted(player, "gui.editor.items.edit-item-flag.flags.HIDE_DESTROYS")),
                        list(player, "gui.editor.items.edit-item-flag.lore",
                                Pair.of("%value%",
                                        this.rpg.getItemFlags().contains(ItemFlag.HIDE_DESTROYS)
                                                ? I18n.getFormatted(player, "gui.editor.items.edit-item-flag.true")
                                                : I18n.getFormatted(player, "gui.editor.items.edit-item-flag.false")))));
        items.put(17,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.edit-item-flag.material"),
                                Material.PAPER),
                        I18n.getFormatted(player, "gui.editor.items.edit-item-flag.name").replace("%flag%",
                                I18n.getFormatted(player, "gui.editor.items.edit-item-flag.flags.HIDE_ENCHANTS")),
                        list(player, "gui.editor.items.edit-item-flag.lore",
                                Pair.of("%value%",
                                        this.rpg.getItemFlags().contains(ItemFlag.HIDE_ENCHANTS)
                                                ? I18n.getFormatted(player, "gui.editor.items.edit-item-flag.true")
                                                : I18n.getFormatted(player, "gui.editor.items.edit-item-flag.false")))));
        items.put(18,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.edit-item-flag.material"),
                                Material.PAPER),
                        I18n.getFormatted(player, "gui.editor.items.edit-item-flag.name").replace("%flag%",
                                I18n.getFormatted(player, "gui.editor.items.edit-item-flag.flags.HIDE_PLACED_ON")),
                        list(player, "gui.editor.items.edit-item-flag.lore",
                                Pair.of("%value%",
                                        this.rpg.getItemFlags().contains(ItemFlag.HIDE_PLACED_ON)
                                                ? I18n.getFormatted(player, "gui.editor.items.edit-item-flag.true")
                                                : I18n.getFormatted(player, "gui.editor.items.edit-item-flag.false")))));
        items.put(19,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.edit-item-flag.material"),
                                Material.PAPER),
                        I18n.getFormatted(player, "gui.editor.items.edit-item-flag.name").replace("%flag%",
                                I18n.getFormatted(player, "gui.editor.items.edit-item-flag.flags.HIDE_POTION_EFFECTS")),
                        list(player, "gui.editor.items.edit-item-flag.lore",
                                Pair.of("%value%",
                                        this.rpg.getItemFlags().contains(ItemFlag.HIDE_POTION_EFFECTS)
                                                ? I18n.getFormatted(player, "gui.editor.items.edit-item-flag.true")
                                                : I18n.getFormatted(player, "gui.editor.items.edit-item-flag.false")))));
        items.put(20,
                EditorHelper.buildItem(
                        Enums.valueOf(Material.class, I18n.getFormatted(player, "gui.editor.items.edit-item-flag.material"),
                                Material.PAPER),
                        I18n.getFormatted(player, "gui.editor.items.edit-item-flag.name").replace("%flag%",
                                I18n.getFormatted(player, "gui.editor.items.edit-item-flag.flags.HIDE_UNBREAKABLE")),
                        list(player, "gui.editor.items.edit-item-flag.lore",
                                Pair.of("%value%",
                                        this.rpg.getItemFlags().contains(ItemFlag.HIDE_UNBREAKABLE)
                                                ? I18n.getFormatted(player, "gui.editor.items.edit-item-flag.true")
                                                : I18n.getFormatted(player, "gui.editor.items.edit-item-flag.false")))));
        return items;
    }

    private List<String> getEnchantLore() {
        List<String> result = new ArrayList<>();
        for (String s : list(player, "gui.editor.items.enchant.lore")) {
            s = s.replace("%mode%", I18n.getFormatted(player, "gui.editor.items.enchant.modes." + this.rpg.getEnchantMode().name().toUpperCase()));

            if (s.contains("%enchants%")) {
                if (this.rpg.getEnchantMap() != null && !this.rpg.getEnchantMap().isEmpty()) {
                    for (Enchantment ench : this.rpg.getEnchantMap().keySet()) {
                        result.add(s.replace("%enchants%", EditorHelper.getEnchName(ench) + " "
                                + EditorHelper.getRomanNumber(this.rpg.getEnchantMap().get(ench))));
                    }
                    continue;
                }
            }
            result.add(s.replace("%enchants%", I18n.getFormatted(player, "gui.editor.items.enchant.nah")));
        }
        return result;
    }

    public void saveItem() {
        this.rpg.rebuild();
        ItemManager.save(this.rpg);
        ItemManager.refreshItem();
    }
}
