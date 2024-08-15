package think.rpgitems.item;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.power.*;
import think.rpgitems.power.trigger.BaseTriggers;
import think.rpgitems.utils.ColorHelper;
import think.rpgitems.utils.ISubItemTagContainer;
import think.rpgitems.utils.MaterialUtils;
import think.rpgitems.utils.nyaacore.utils.ItemTagUtils;
import think.rpgitems.utils.pdc.ItemPDC;

import java.io.File;
import java.util.*;

import static org.bukkit.Material.BRICK;
import static think.rpgitems.item.RPGItem.TAG_META;

public class RPGStone implements RPGBaseHolder {
    static RPGItems plugin;
    public static final NamespacedKey TAG_POWER_STONE_ITEM_UID = new NamespacedKey(RPGItems.plugin, "power_stone_uid");
    public static final String NBT_POWER_STONES = "rpgitem_power_stones";
    public static final String NBT_POWER_STONE_UID = "rpgitem_power_stone_uid";
    public static final String NBT_POWER_STONE_ITEM_UUID = "rpgitem_power_stone_item_uuid";
    public static final String NBT_POWER_STONE_TRIGGER = "rpgitem_power_stone_trigger";

    @Getter private final List<Power> powers = new ArrayList<>();
    @Getter private final List<Condition<?>> conditions = new ArrayList<>();
    private final HashMap<PropertyHolder, NamespacedKey> keys = new HashMap<>();
    @Getter private File file;

    @Getter @Setter private Material item;
    @Getter @Setter private int dataValue;
    @Getter private int uid;
    @Getter private final String name;

    private String displayName;
    private String displayNameColored;
    @Setter @Getter private List<String> description = new ArrayList<>();
    @Setter @Getter private List<String> extraDescription = new ArrayList<>();
    @Getter @Setter private boolean customItemModel;
    @Getter @Setter private int customModelData;

    @Getter @Setter private List<String> allowTriggers = new ArrayList<>();
    @Getter @Setter private List<String> allowTriggersArmour = new ArrayList<>();

    @Getter @Setter private double successRate = 1;
    @Getter @Setter private List<String> failCommands = new ArrayList<>(); // TODO: Allow users to edit them in StoneCommands

    @Getter @Setter private String author = plugin.cfg.defaultAuthor;
    @Getter @Setter private String note = plugin.cfg.defaultNote;
    @Getter @Setter private String license = plugin.cfg.defaultLicense;

    private boolean customTrigger;

    public RPGStone(String name, int uid, CommandSender author) {
        this.name = name;
        this.uid = uid;
        this.setAuthor(author instanceof Player ? ((Player) author).getUniqueId().toString() : plugin.cfg.defaultAuthor);
        setItem(BRICK);
        setDisplayName(getItem().toString());
    }

    public RPGStone(ConfigurationSection s, File f) throws UnknownPowerException {
        setFile(f);
        name = s.getString("name");
        uid = s.getInt("uid");
        if (uid == 0) {
            uid = ItemManager.nextUid();
        }
        restore(s);
    }

    @Nullable
    public String getTrigger(ItemStack item) {
        RPGStone stone = ItemManager.toRPGStone(item).orElse(null);
        if (stone == null || !stone.equals(this) || !useCustomTrigger()) return null;
        return ItemTagUtils.getString(item, NBT_POWER_STONE_TRIGGER).orElse(null);
    }

    public void setTrigger(ItemStack item, @Nullable String trigger) {
        RPGStone stone = ItemManager.toRPGStone(item).orElse(null);
        if (stone == null || !stone.equals(this)) return;
        if (trigger == null || trigger.trim().isEmpty() || !useCustomTrigger()) {
            ItemTagUtils.remove(item, NBT_POWER_STONE_TRIGGER);
        } else {
            ItemTagUtils.setString(item, NBT_POWER_STONE_TRIGGER, trigger);
        }
    }

    public boolean checkTriggerCanUse(String trigger, boolean isArmour, @Nullable Player player) {
        List<String> allowTriggers = isArmour ? plugin.cfg.stoneTriggersArmour : plugin.cfg.stoneTriggers;
        if (!allowTriggers.contains(trigger)) {
            if (player != null) {
                String triggerDisplay = I18n.getFormatted(player, "properties.triggers." + trigger + ".display");
                player.sendMessage(I18n.getFormatted(player, "message.stone.not-allow-trigger", triggerDisplay));
            }
            return false;
        }
        allowTriggers = isArmour ? getAllowTriggersArmour() : getAllowTriggers();
        if (!allowTriggers.isEmpty() && !allowTriggers.contains(trigger)) {
            if (player != null) {
                String triggerDisplay = I18n.getFormatted(player, "properties.triggers." + trigger + ".display");
                player.sendMessage(I18n.getFormatted(player, "message.stone.not-allow-trigger", triggerDisplay));
            }
            return false;
        }
        return true;
    }

    private void restore(ConfigurationSection s) throws UnknownPowerException {
        setAuthor(s.getString("author", ""));
        setNote(s.getString("note", ""));
        setLicense(s.getString("license", ""));

        String display = s.getString("display");
        setDisplayName(display);
        setCustomModelData(s.getInt("customModelData",  -1));
        String materialName = s.getString("item");
        setItem(MaterialUtils.getMaterial(materialName, Bukkit.getConsoleSender()));

        List<String> desc = s.getStringList("description");
        desc.replaceAll(ColorHelper::parseColor);
        setDescription(desc);

        setAllowTriggers(s.getStringList("allowTriggers"));
        setAllowTriggersArmour(s.getStringList("allowTriggersArmour"));

        setSuccessRate(s.getDouble("successRate"));
        setFailCommands(s.getStringList("failCommands"));

        // Powers
        ConfigurationSection powerList = s.getConfigurationSection("powers");
        if (powerList != null) {
            for (String sectionKey : powerList.getKeys(false)) {
                ConfigurationSection section = powerList.getConfigurationSection(sectionKey);
                String powerName = Objects.requireNonNull(section).getString("powerName");
                if (powerName == null) continue;
                loadPower(section, powerName);
            }
        }
        // Conditions
        ConfigurationSection conditionList = s.getConfigurationSection("conditions");
        if (conditionList != null) {
            for (String sectionKey : conditionList.getKeys(false)) {
                ConfigurationSection section = Objects.requireNonNull(conditionList).getConfigurationSection(sectionKey);
                String conditionName = Objects.requireNonNull(Objects.requireNonNull(section).getString("conditionName"));
                loadCondition(section, conditionName);
            }
        }
    }

    @Override
    public void save() {
        ItemManager.save(this);
    }

    public void save(ConfigurationSection s) {
        s.set("author", getAuthor());
        s.set("note", getNote());
        s.set("license", getLicense());

        s.set("name", getName());
        s.set("uid", getUid());

        s.set("display", getDisplayNameRaw().replaceAll("§", "&"));
        ArrayList<String> descriptionConv = new ArrayList<>(getDescription());
        descriptionConv.replaceAll(s1 -> s1.replaceAll("§", "&"));
        s.set("description", descriptionConv);
        s.set("item", getItem().toString());
        s.set("customModelData", getCustomModelData());

        s.set("allowTriggers", getAllowTriggers());
        s.set("allowTriggersArmour", getAllowTriggersArmour());

        s.set("successRate", getSuccessRate());
        s.set("failCommands", getFailCommands());

        ConfigurationSection powerConfigs = s.createSection("powers");
        int i = 0;
        for (Power p : powers) {
            MemoryConfiguration pConfig = new MemoryConfiguration();
            pConfig.set("powerName", getPropertyHolderKey(p).toString());
            p.save(pConfig);
            powerConfigs.set(Integer.toString(i), pConfig);
            i++;
        }
        ConfigurationSection conditionConfigs = s.createSection("conditions");
        i = 0;
        for (Condition<?> p : conditions) {
            MemoryConfiguration pConfig = new MemoryConfiguration();
            pConfig.set("conditionName", p.getNamespacedKey().toString());
            p.save(pConfig);
            conditionConfigs.set(Integer.toString(i), pConfig);
            i++;
        }
    }

    private void loadPower(ConfigurationSection section, String powerName) throws UnknownPowerException {
        NamespacedKey key = PowerManager.parseKey(powerName);
        Class<? extends Power> power = PowerManager.getPower(key);
        if (power == null) {
            plugin.getLogger().warning("Unknown power:" + key + " on item " + this.name);
            throw new UnknownPowerException(key);
        }
        Power pow = PowerManager.instantiate(power);
        pow.init(section, getName());
        addPower(key, pow);
    }

    private void loadCondition(ConfigurationSection section, String powerName) throws UnknownPowerException {
        NamespacedKey key = PowerManager.parseKey(powerName);
        Class<? extends Condition<?>> condition = PowerManager.getCondition(key);
        if (condition == null) {
            plugin.getLogger().warning("Unknown condition:" + key + " on item " + this.name);
            throw new UnknownPowerException(key);
        }
        Condition<?> cond = PowerManager.instantiate(condition);
        cond.init(section, getName());
        addCondition(key, cond);
    }

    @Override
    public <T extends Power> List<T> getPower(NamespacedKey key, Class<T> power) {
        List<T> list = new ArrayList<>();
        for (Power p : powers) {
            if (p.getClass().equals(power) && getPropertyHolderKey(p).equals(key)) {
                list.add(power.cast(p));
            }
        }
        return list;
    }

    @Override
    public <T extends Condition<?>> List<T> getCondition(NamespacedKey key, Class<T> condition) {
        List<T> list = new ArrayList<>();
        for (Condition<?> p : conditions) {
            if (p.getClass().equals(condition) && getPropertyHolderKey(p).equals(key)) {
                list.add(condition.cast(p));
            }
        }
        return list;
    }

    @Override
    public Condition<?> getCondition(String id) {
        for (Condition<?> c : conditions) {
            if (c.id().equals(id)) return c;
        }
        return null;
    }

    @Override
    public void addPower(NamespacedKey key, Power power) {
        power.setStoneFlag(getName());
        powers.add(power);
        keys.put(power, key);
        refreshCustomTriggerState();
    }

    @Override
    public void removePower(Power power) {
        power.setStoneFlag("");
        powers.remove(power);
        keys.remove(power);
        power.deinit();
        refreshCustomTriggerState();
    }

    @Override
    public void addCondition(NamespacedKey key, Condition<?> condition) {
        conditions.add(condition);
        keys.put(condition, key);
    }

    @Override
    public void removeCondition(Condition<?> condition) {
        conditions.remove(condition);
        keys.remove(condition);
    }

    @Override
    public void deinit() {
        powers.forEach(Power::deinit);
        refreshCustomTriggerState();
    }

    void setFile(File itemFile) {
        file = itemFile;
    }

    public void updateItem(ItemStack item) {
        updateItem(item, false);
    }

    public void updateItem(ItemStack item, boolean loreOnly) {
        if (item == null) return;

        item.setType(getItem());

        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer itemTagContainer = Objects.requireNonNull(meta).getPersistentDataContainer();
        ISubItemTagContainer rpgitemsTagContainer = ItemPDC.makeTag(itemTagContainer, TAG_META);
        ItemPDC.set(rpgitemsTagContainer, TAG_POWER_STONE_ITEM_UID, getUid());
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(Color.fromRGB(getDataValue()));
        }
        
        meta.setDisplayName(getDisplayName());
        meta.setLore(new ArrayList<>(getDescription()));

        if (loreOnly) {
            rpgitemsTagContainer.commit();
            item.setItemMeta(meta);
            if (!useCustomTrigger()) ItemTagUtils.remove(item, NBT_POWER_STONE_TRIGGER);
            return;
        }

        meta.setCustomModelData(getCustomModelData());
        rpgitemsTagContainer.commit();
        item.setItemMeta(meta);

        ItemTagUtils.setInt(item, NBT_POWER_STONE_UID, uid);
        if (RPGItems.plugin.cfg.itemStackUuid) {
            if (ItemTagUtils.getString(item, NBT_POWER_STONE_ITEM_UUID).isEmpty()) {
                UUID uuid = UUID.randomUUID();
                ItemTagUtils.setString(item, NBT_POWER_STONE_ITEM_UUID, uuid.toString());
            }
        }
    }

    public ItemStack toItemStack() {
        ItemStack rStack = new ItemStack(getItem());
        ItemMeta meta = rStack.getItemMeta();
        PersistentDataContainer itemTagContainer = Objects.requireNonNull(meta).getPersistentDataContainer();
        ISubItemTagContainer rpgitemsTagContainer = ItemPDC.makeTag(itemTagContainer, TAG_META);
        ItemPDC.set(rpgitemsTagContainer, TAG_POWER_STONE_ITEM_UID, getUid());
        rpgitemsTagContainer.commit();
        meta.setDisplayName(getDisplayName());
        rStack.setItemMeta(meta);

        updateItem(rStack, false);
        return rStack;
    }

    public void give(Player player, int count) {
        ItemStack itemStack = toItemStack();
        itemStack.setAmount(count);
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(itemStack);
        for (ItemStack o : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), o);
        }
    }

    public String getDisplayNameRaw() {
        return displayName;
    }
    @Override
    public String getDisplayName() {
        return displayNameColored;
    }
    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        this.displayNameColored = ColorHelper.parseColor(displayName);
    }
    @Override
    public void addDescription(String str) {
        getDescription().add(ColorHelper.parseColor(str));
    }
    public void addExtraDescription(String str) {
        getExtraDescription().add(ColorHelper.parseColor(str));
    }

    private void refreshCustomTriggerState() {
        for (Power power : getPowers()) {
            if (power.getTriggers().contains(BaseTriggers.CUSTOM_TRIGGER)) {
                customTrigger = true;
                return;
            }
        }
        customTrigger = false;
    }

    public boolean useCustomTrigger() {
        return this.customTrigger;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RPGStone)) return false;
        RPGStone rpgStone = (RPGStone) o;
        return Objects.equals(name, rpgStone.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public NamespacedKey getPropertyHolderKey(PropertyHolder power) {
        return Objects.requireNonNull(keys.get(power));
    }
    @Override
    public NamespacedKey removePropertyHolderKey(PropertyHolder power) {
        return Objects.requireNonNull(keys.remove(power));
    }
}
