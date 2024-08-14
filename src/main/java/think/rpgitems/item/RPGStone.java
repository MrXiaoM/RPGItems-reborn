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
import think.rpgitems.RPGItems;
import think.rpgitems.power.*;
import think.rpgitems.utils.ColorHelper;
import think.rpgitems.utils.ISubItemTagContainer;
import think.rpgitems.utils.MaterialUtils;
import think.rpgitems.utils.nyaacore.utils.ItemTagUtils;
import think.rpgitems.utils.pdc.ItemPDC;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static think.rpgitems.item.RPGItem.TAG_META;

public class RPGStone implements RPGBaseHolder {
    static RPGItems plugin;
    public static final NamespacedKey TAG_POWER_STONE_ITEM_UID = new NamespacedKey(RPGItems.plugin, "power_stone_uid");
    public static final String NBT_POWER_STONES = "rpgitem_power_stones";
    public static final String NBT_POWER_STONE_UID = "rpgitem_power_stone_uid";
    public static final String NBT_POWER_STONE_ITEM_UUID = "rpgitem_power_stone_item_uuid";

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
    @Setter @Getter private List<String> description;
    @Setter @Getter private List<String> extraDescription;
    @Getter @Setter private boolean customItemModel;
    @Getter @Setter private int customModelData;

    @Getter @Setter private String author = plugin.cfg.defaultAuthor;
    @Getter @Setter private String note = plugin.cfg.defaultNote;
    @Getter @Setter private String license = plugin.cfg.defaultLicense;

    public RPGStone(String name, int uid, CommandSender author) {
        this.name = name;
        this.uid = uid;
        this.setAuthor(author instanceof Player ? ((Player) author).getUniqueId().toString() : plugin.cfg.defaultAuthor);
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
        addPower(key, pow, false);
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
        addCondition(key, cond, false);
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
        addPower(key, power, true);
    }

    private void addPower(NamespacedKey key, Power power, boolean update) {
        powers.add(power);
        keys.put(power, key);
    }

    @Override
    public void removePower(Power power) {
        powers.remove(power);
        keys.remove(power);
        power.deinit();
    }

    @Override
    public void addCondition(NamespacedKey key, Condition<?> condition) {
        addCondition(key, condition, true);
    }

    private void addCondition(NamespacedKey key, Condition<?> condition, boolean update) {
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

        meta.setLore(new ArrayList<>(getDescription()));

        if (loreOnly) {
            rpgitemsTagContainer.commit();
            item.setItemMeta(meta);
            return;
        }

        meta.setCustomModelData(getCustomModelData());
        rpgitemsTagContainer.commit();
        item.setItemMeta(meta);
        try {
            ItemTagUtils.setInt(item, NBT_POWER_STONE_UID, uid);
            if (RPGItems.plugin.cfg.itemStackUuid) {
                if (ItemTagUtils.getString(item, NBT_POWER_STONE_ITEM_UUID).isEmpty()) {
                    UUID uuid = UUID.randomUUID();
                    ItemTagUtils.setString(item, NBT_POWER_STONE_ITEM_UUID, uuid.toString());
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
            }
            plugin.getLogger().warning(sw.toString());
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
    @Override
    public NamespacedKey getPropertyHolderKey(PropertyHolder power) {
        return Objects.requireNonNull(keys.get(power));
    }
    @Override
    public NamespacedKey removePropertyHolderKey(PropertyHolder power) {
        return Objects.requireNonNull(keys.remove(power));
    }
}
