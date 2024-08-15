package think.rpgitems.item;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.commands.AdminCommands;
import think.rpgitems.data.Context;
import think.rpgitems.data.FactorModifier;
import think.rpgitems.event.ItemUpdateEvent;
import think.rpgitems.event.LoreUpdateEvent;
import think.rpgitems.power.*;
import think.rpgitems.power.cond.SlotCondition;
import think.rpgitems.power.marker.*;
import think.rpgitems.power.propertymodifier.Modifier;
import think.rpgitems.power.proxy.Interceptor;
import think.rpgitems.power.trigger.BaseTriggers;
import think.rpgitems.power.trigger.Trigger;
import think.rpgitems.support.MythicSupport;
import think.rpgitems.utils.ColorHelper;
import think.rpgitems.utils.ISubItemTagContainer;
import think.rpgitems.utils.MaterialUtils;
import think.rpgitems.utils.MessageType;
import think.rpgitems.utils.nyaacore.Message;
import think.rpgitems.utils.nyaacore.Pair;
import think.rpgitems.utils.nyaacore.utils.ItemStackUtils;
import think.rpgitems.utils.nyaacore.utils.ItemTagUtils;
import think.rpgitems.utils.pdc.ItemPDC;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bukkit.Material.*;
import static org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER;

@SuppressWarnings({"deprecation", "rawtypes", "unused"})
public class RPGItem implements RPGBaseHolder {
    @Deprecated
    public static final int MC_ENCODED_ID_LENGTH = 16;
    public static final NamespacedKey TAG_META = new NamespacedKey(RPGItems.plugin, "meta");
    public static final NamespacedKey TAG_ITEM_UID = new NamespacedKey(RPGItems.plugin, "item_uid");
    public static final NamespacedKey TAG_IS_MODEL = new NamespacedKey(RPGItems.plugin, "is_model");
    public static final NamespacedKey TAG_DURABILITY = new NamespacedKey(RPGItems.plugin, "durability");
    public static final NamespacedKey TAG_OWNER = new NamespacedKey(RPGItems.plugin, "owner");
    public static final NamespacedKey TAG_STACK_ID = new NamespacedKey(RPGItems.plugin, "stack_id");
    public static final NamespacedKey TAG_MODIFIER = new NamespacedKey(RPGItems.plugin, "property_modifier");
    public static final NamespacedKey TAG_VERSION = new NamespacedKey(RPGItems.plugin, "version");
    public static final String DAMAGE_TYPE = "RGI_DAMAGE_TYPE";
    public static final String NBT_UID = "rpgitem_uid";
    public static final String NBT_ITEM_UUID = "rpgitem_item_uuid";
    public static final String NBT_IS_MODEL = "rpgitem_is_model";

    private static final Cache<UUID, List<Modifier>> modifierCache = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterAccess(1, TimeUnit.MINUTES).build();

    private final static NamespacedKey RGI_UNIQUE_MARK = new NamespacedKey(RPGItems.plugin, "RGI_UNIQUE_MARK");
    private final static NamespacedKey RGI_UNIQUE_ID = new NamespacedKey(RPGItems.plugin, "RGI_UNIQUE_ID");
    static RPGItems plugin;
    @Getter @Setter private boolean ignoreWorldGuard = false;
    @Getter @Setter private List<String> description = new ArrayList<>();
    @Getter @Setter private boolean showPowerText = true;
    @Getter @Setter private boolean showArmourLore = true;
    @Getter @Setter private Map<Enchantment, Integer> enchantMap = null;
    @Getter @Setter private List<ItemFlag> itemFlags = new ArrayList<>();
    @Getter @Setter private boolean customItemModel = false;
    @Getter @Setter private EnchantMode enchantMode = EnchantMode.DISALLOW;

    // Powers
    @Getter private List<Power> powers = new ArrayList<>();
    @Getter private List<Condition<?>> conditions = new ArrayList<>();
    @Getter private List<Marker> markers = new ArrayList<>();
    private final Map<String, PlaceholderHolder> placeholders= new HashMap<>();
    @SuppressWarnings("rawtypes")
    @Getter private final Map<String, Trigger> triggers = new HashMap<>();
    private final HashMap<PropertyHolder, NamespacedKey> keys = new HashMap<>();
    @Getter private File file;

    @Getter @Setter private NamespacedKey namespacedKey;
    @Getter @Setter private Material item;
    @Getter @Setter private Material fakeItem = AIR;
    @Getter @Setter private int dataValue;
    private int id;
    @Getter private int uid;
    @Getter private final String name;

    @Getter @Setter private boolean hasPermission;
    @Setter private String permission;
    private String displayName;
    private String displayNameColored;
    @Getter @Setter private String factor;
    @Getter @Setter Map<String, FactorModifier> factorModifiers = new HashMap<>();
    @Getter private int damageMin = 0;
    @Getter private int damageMax = 3;
    @Getter private int damageMinPlayer = -1;
    @Getter private int damageMaxPlayer = -1;
    private int damageMinMythic = -1;
    private int damageMaxMythic = -1;

    @Getter @Setter private double criticalRate;
    @Getter @Setter private double criticalArmorRate;
    @Getter @Setter private double criticalDamage;
    @Getter @Setter private double criticalMultiple;
    @Getter @Setter private double criticalBackRate;
    @Getter @Setter private double criticalBackArmorRate;
    @Getter @Setter private double criticalBackDamage;
    @Getter @Setter private double criticalBackMultiple;
    @Getter @Setter private double dodgeRate;
    @Setter private MessageType dodgeMessageType = MessageType.TITLE;
    @Getter @Setter private String dodgeMessage = I18n.formatDefault("item.dodge_default").replace("\\n", "\n");
    @Getter @Setter private double criticalAntiRate;
    @Getter @Setter private double criticalAntiArmorRate;

    @Getter @Setter private double mythicSkillDamage = 0;
    @Getter @Setter private double mythicSkillDamageMultiple = 1;

    @Getter @Setter private double mythicSkillCriticalRate = 0;
    @Getter @Setter private double mythicSkillCriticalDamage = 0;
    @Getter @Setter private double mythicSkillCriticalDamageMultiple = 1;

    @Getter @Setter private double atkSpeed = 0;
    @Getter @Setter private double moveSpeed = 0;
    @Getter @Setter private DamageMode damageMode = DamageMode.FIXED;
    @Getter @Setter private AttributeMode attributeMode = AttributeMode.PARTIAL_UPDATE;
    @Getter private int armour = 0;
    @Getter private int armourProjectile = 0;
    @Getter @Setter private String armourExpression = "";
    @Getter @Setter private String damageType = "";
    @Getter @Setter private boolean canBeOwned = false;
    @Getter @Setter private boolean hasStackId = false;
    @Getter @Setter private boolean alwaysAllowMelee = false;

    @Getter @Setter private String author = plugin.cfg.defaultAuthor;
    @Getter @Setter private String note = plugin.cfg.defaultNote;
    @Getter @Setter private String license = plugin.cfg.defaultLicense;

    private int tooltipWidth = 150;
    // Durability
    @Getter private int maxDurability = -1;
    @Getter @Setter private boolean hasDurabilityBar = plugin.cfg.forceBar;
    @Getter @Setter private int defaultDurability;
    @Getter @Setter private int durabilityLowerBound;
    @Getter @Setter private int durabilityUpperBound;
    @Getter @Setter private BarFormat barFormat = BarFormat.DEFAULT;

    @Getter @Setter private int blockBreakingCost = 0;
    @Getter @Setter private int hittingCost = 0;
    @Getter @Setter private int hitCost = 0;
    @Getter @Setter private boolean hitCostByDamage = false;
    @Getter @Setter private String mcVersion;
    @Getter @Setter private int pluginVersion;
    @Getter @Setter private int pluginSerial;
    private List<String> lore;
    @Getter @Setter private int customModelData;
    @Getter @Setter private boolean isTemplate;
    @Getter private Set<String> templates = new HashSet<>();
    private final Set<String> templatePlaceholders = new HashSet<>();
    @Getter @Setter private String quality;
    @Getter @Setter private String type = "item";

    public RPGItem(String name, int uid, CommandSender author) {
        this.name = name;
        this.uid = uid;
        this.setAuthor(author instanceof Player ? ((Player) author).getUniqueId().toString() : plugin.cfg.defaultAuthor);
        setEnchantMode(plugin.cfg.defaultEnchantMode);
        setItem(WOODEN_SWORD);
        setDisplayName(getItem().toString());
        getItemFlags().add(ItemFlag.HIDE_ATTRIBUTES);
        setMcVersion(RPGItems.getServerMCVersion());
        setPluginSerial(RPGItems.getSerial());
        setPluginVersion(RPGItems.getVersion());
        rebuild();
    }

    public RPGItem(ConfigurationSection s, File f) throws UnknownPowerException, UnknownExtensionException {
        setFile(f);
        name = s.getString("name");
        id = s.getInt("id");
        uid = s.getInt("uid");

        if (uid == 0) {
            uid = ItemManager.nextUid();
        }
        restore(s);
    }

    public RPGItem(ConfigurationSection s, String name, int uid) throws UnknownPowerException, UnknownExtensionException {
        if (uid >= 0) throw new IllegalArgumentException();
        this.name = name;
        this.uid = uid;
        restore(s);
    }

    @Deprecated
    public static List<Modifier> getModifiers(ItemStack stack) {
        return getModifiers(null, stack);
    }
    public static List<Modifier> getModifiers(@Nullable Player player, ItemStack stack) {
        Optional<String> opt = ItemTagUtils.getString(stack, NBT_ITEM_UUID);
        if (opt.isEmpty()) {
            Optional<RPGItem> rpgItemOpt = ItemManager.toRPGItemByMeta(stack);
            if (rpgItemOpt.isEmpty()) {
                return Collections.emptyList();
            }
            RPGItem rpgItem = rpgItemOpt.get();
            rpgItem.updateItem(player, stack);
            Optional<String> opt1 = ItemTagUtils.getString(stack, NBT_ITEM_UUID);
            if (opt1.isEmpty()) {
                return Collections.emptyList();
            }
            opt = opt1;
        }

        UUID key = UUID.fromString(opt.get());
        List<Modifier> modifiers = modifierCache.getIfPresent(key);
        if (modifiers == null) {
            ItemMeta itemMeta = stack.getItemMeta();
            if (itemMeta == null) return new ArrayList<>();
            ISubItemTagContainer tag = ItemPDC.makeTag(Objects.requireNonNull(itemMeta).getPersistentDataContainer(), TAG_MODIFIER);
            modifiers = getModifiers(tag, key);
        }
        return modifiers;
    }

    public static List<Modifier> getModifiers(Player player) {
        UUID key = player.getUniqueId();
        List<Modifier> modifiers = modifierCache.getIfPresent(key);
        if (modifiers == null) {
            ISubItemTagContainer tag = ItemPDC.makeTag(player.getPersistentDataContainer(), TAG_MODIFIER);
            modifiers = getModifiers(tag, key);
        }
        return modifiers;
    }

    public static List<Modifier> getModifiers(ISubItemTagContainer tag) {
        return getModifiers(tag, null);
    }

    public static void invalidateModifierCache() {
        modifierCache.invalidateAll();
    }

    public static List<Modifier> getModifiers(ISubItemTagContainer tag, UUID key) {
        Optional<UUID> uuid = Optional.ofNullable(key);
        if (uuid.isEmpty()) {
            uuid = Optional.of(UUID.randomUUID());
        }

        try {
            return modifierCache.get(uuid.get(), () -> getModifiersUncached(tag));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            tag.tryDispose();
        }
    }

    private static List<Modifier> getModifiersUncached(ISubItemTagContainer tag) {
        List<Modifier> ret = new ArrayList<>();
        int i = 0;
        try {
            for (NamespacedKey key = PowerManager.parseKey(String.valueOf(i)); tag.has(key, PersistentDataType.TAG_CONTAINER); key = PowerManager.parseKey(String.valueOf(++i))) {
                PersistentDataContainer container = ItemPDC.getTag(tag, key);
                String modifierName = ItemPDC.getString(container, "modifier_name");
                Class<? extends Modifier> modifierClass = PowerManager.getModifier(PowerManager.parseKey(modifierName));
                if (modifierClass != null) {
                    Modifier modifier = PowerManager.instantiate(modifierClass);
                    modifier.init(container);
                    ret.add(modifier);
                }
            }
            return ret;
        } finally {
            tag.commit();
        }
    }

    private void restore(ConfigurationSection s) throws UnknownPowerException, UnknownExtensionException {
        setAuthor(s.getString("author", ""));
        setNote(s.getString("note", ""));
        setLicense(s.getString("license", ""));
        setPluginVersion(s.getInt("pluginVersion", 0));
        setPluginSerial(s.getInt("pluginSerial", 0));
        setMcVersion(s.getString("mcVersion", ""));

        setDisplayName(s.getString("display"));
        setFactor(s.getString("factor", ""));
        factorModifiers.clear();
        ConfigurationSection factorModifierSection = s.getConfigurationSection("factorModifier");
        if (factorModifierSection != null) for (String target : factorModifierSection.getKeys(false)) {
            FactorModifier modifier = FactorModifier.load(s, "factorModifier", target);
            factorModifiers.put(target, modifier);
        }
        List<String> desc = s.getStringList("description");
        desc.replaceAll(ColorHelper::parseColor);
        setDescription(desc);
        setDamageMin(s.getInt("damageMin"));
        setDamageMax(s.getInt("damageMax"));
        setDamageMinMythic(s.getInt("damageMinMythic", -1));
        setDamageMaxMythic(s.getInt("damageMaxMythic", -1));
        damageMinPlayer = damageMin;
        damageMaxPlayer = damageMax;

        setCriticalRate(s.getDouble("critical.normal.rate", 0.0d));
        setCriticalDamage(s.getDouble("critical.normal.damage", 0.0d));
        setCriticalMultiple(s.getDouble("critical.normal.multiple", 1.0d));
        setCriticalBackRate(s.getDouble("critical.back.rate", 0.0d));
        setCriticalBackDamage(s.getDouble("critical.back.damage", 0.0d));
        setCriticalBackMultiple(s.getDouble("critical.back.multiple", 1.0d));
        setCriticalAntiRate(s.getDouble("critical.anti.rate", 0.0d));

        setMythicSkillDamage(s.getDouble("mythic.skill.damage-add", 0.0d));
        setMythicSkillDamageMultiple(s.getDouble("mythic.skill.damage-add-multiple", 1.0d));

        setMythicSkillCriticalRate(s.getDouble("mythic.skill.critical.rate", 0.0d));
        setMythicSkillCriticalDamage(s.getDouble("mythic.skill.critical.damage-add", 0.0d));
        setMythicSkillCriticalDamageMultiple(s.getDouble("mythic.skill.critical.damage-add-multiple", 1.0d));

        setDodgeRate(s.getDouble("dodge.rate", 0.0d));
        setDodgeMessageType(MessageType.getFromConfig(s, "dodge.message-type", MessageType.TITLE));
        setDodgeMessage(s.getString("dodge.message", "&e当心\\n&f躲避判定成功").replace("\\n", "\n"));

        setAtkSpeed(s.getInt("atkSpeed"));
        setMoveSpeed(s.getInt("moveSpeed"));
        setArmour(s.getInt("armour", 0), false);
        setArmourProjectile(s.getInt("armourProjectile", 0), false);
        setArmourExpression(s.getString("armourExpression", ""));
        setDamageType(s.getString("DamageType", ""));
        setAttributeMode(AttributeMode.valueOf(s.getString("attributemode", "PARTIAL_UPDATE")));
        String materialName = s.getString("item");
        setItem(MaterialUtils.getMaterial(materialName, Bukkit.getConsoleSender()));
        String materialNameFake = s.getString("item-fake", "AIR");
        setFakeItem(MaterialUtils.getMaterial(materialNameFake, Bukkit.getConsoleSender()));
        ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(getItem());
        if (itemMeta instanceof LeatherArmorMeta) {
            setDataValue(s.getInt("item_colour"));
        } else if (itemMeta instanceof Damageable) {
            setDataValue(s.getInt("item_data"));
        }
        setIgnoreWorldGuard(s.getBoolean("ignoreWorldGuard", false));
        setCanBeOwned(s.getBoolean("canBeOwned", false));
        setHasStackId(s.getBoolean("hasStackId", false));
        placeholders.clear();
        // Powers
        ConfigurationSection powerList = s.getConfigurationSection("powers");
        if (powerList != null) {
            for (String sectionKey : powerList.getKeys(false)) {
                ConfigurationSection section = powerList.getConfigurationSection(sectionKey);
                String powerName = Objects.requireNonNull(section).getString("powerName");
                if (powerName == null) continue;
                if (powerName.equalsIgnoreCase("rpgitems:criticalhit")) { // merge critical power into RPGItem
                    setCriticalRate(section.getDouble("chance", 0.0d));
                    setCriticalMultiple(section.getDouble("factor", 1.0d));
                    setCriticalBackRate(section.getDouble("backstabChance", 0.0d));
                    setCriticalBackMultiple(section.getDouble("backstabFactor", 1.0d));
                    continue;
                }
                // 3.7 -> 3.8 Migration
                if (powerName.endsWith("condition")) {
                    loadCondition(section, powerName);
                } else if (Stream.of("attributemodifier", "lorefilter", "ranged", "rangedonly", "selector", "unbreakable").anyMatch(powerName::endsWith)) {
                    loadMarker(section, powerName);
                } else {
                    loadPower(section, powerName);
                }
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
        // Markers
        ConfigurationSection markerList = s.getConfigurationSection("markers");
        if (markerList != null) {
            for (String sectionKey : markerList.getKeys(false)) {
                ConfigurationSection section = Objects.requireNonNull(markerList).getConfigurationSection(sectionKey);
                String markerName = Objects.requireNonNull(Objects.requireNonNull(section).getString("markerName"));
                loadMarker(section, markerName);
            }
        }
        // Triggers
        ConfigurationSection triggerList = s.getConfigurationSection("triggers");
        if (triggerList != null) {
            for (String sectionKey : triggerList.getKeys(false)) {
                ConfigurationSection section = Objects.requireNonNull(triggerList).getConfigurationSection(sectionKey);
                if (section != null) loadTrigger(section, sectionKey);
            }
        }

        Map<String, List<PlaceholderHolder>> duplicatePlaceholderIds = checkDuplicatePlaceholderIds();
        if (!duplicatePlaceholderIds.isEmpty()){
            Logger logger = RPGItems.plugin.getLogger();
            String duplicateMsg = getDuplicatePlaceholderMsg(duplicatePlaceholderIds);
            logger.log(Level.WARNING, duplicateMsg);
        }
        setHasPermission(s.getBoolean("haspermission", false));
        setPermission(s.getString("permission", "rpgitem.item." + name));
        setCustomItemModel(s.getBoolean("customItemModel", false));

        setHitCost(s.getInt("hitCost", 1));
        setHittingCost(s.getInt("hittingCost", 1));
        setBlockBreakingCost(s.getInt("blockBreakingCost", 1));
        setHitCostByDamage(s.getBoolean("hitCostByDamage", false));
        setMaxDurability(s.getInt("maxDurability", getItem().getMaxDurability()));
        setDefaultDurability(s.getInt("defaultDurability", getMaxDurability()));
        if (getDefaultDurability() <= 0) {
            setDefaultDurability(getMaxDurability());
        }
        setDurabilityLowerBound(s.getInt("durabilityLowerBound", 0));
        setDurabilityUpperBound(s.getInt("durabilityUpperBound", getItem().getMaxDurability()));
        if (s.isBoolean("forceBar")) {
            setHasDurabilityBar(getItem().getMaxDurability() == 0 || s.getBoolean("forceBar") || isCustomItemModel());
        }
        setHasDurabilityBar(s.getBoolean("hasDurabilityBar", isHasDurabilityBar()));

        setShowPowerText(s.getBoolean("showPowerText", true));
        setShowArmourLore(s.getBoolean("showArmourLore", true));
        setCustomModelData(s.getInt("customModelData",  -1));
        setQuality(s.getString("quality", null));
        setType(s.getString("item", "item"));

        if (s.isConfigurationSection("enchantments")) {
            ConfigurationSection enchConf = s.getConfigurationSection("enchantments");
            setEnchantMap(new HashMap<>());
            for (String enchName : Objects.requireNonNull(enchConf).getKeys(false)) {
                Enchantment ench;
                try {
                    ench = Enchantment.getByKey(NamespacedKey.minecraft(enchName));
                } catch (IllegalArgumentException e) {
                    @SuppressWarnings("deprecation")
                    Enchantment old = Enchantment.getByName(enchName);
                    if (old == null) {
                        throw new IllegalArgumentException("Unknown enchantment " + enchName);
                    }
                    ench = old;
                }
                if (ench != null) {
                    getEnchantMap().put(ench, enchConf.getInt(enchName));
                }
            }
        }
        String enchantModeStr = s.getString("enchantMode", plugin.cfg.defaultEnchantMode.name());
        try {
            setEnchantMode(EnchantMode.valueOf(enchantModeStr));
        } catch (IllegalArgumentException e) {
            setEnchantMode(EnchantMode.DISALLOW);
        }
        setItemFlags(new ArrayList<>());
        if (s.isList("itemFlags")) {
            List<String> flags = s.getStringList("itemFlags");
            for (String flagName : flags) {
                try {
                    getItemFlags().add(ItemFlag.valueOf(flagName));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "Ignoring unknown item flags", e);
                }
            }
        }
        if (s.isBoolean("numericBar")) {
            setBarFormat(s.getBoolean("numericBar") ? BarFormat.NUMERIC : BarFormat.DEFAULT);
        }
        if (s.isString("barFormat")) {
            setBarFormat(BarFormat.valueOf(s.getString("barFormat")));
        }
        String damageModeStr = s.getString("damageMode", "FIXED");
        try {
            setDamageMode(DamageMode.valueOf(damageModeStr));
        } catch (IllegalArgumentException e) {
            setDamageMode(DamageMode.FIXED);
        }
        setAlwaysAllowMelee(s.getBoolean("alwaysAllowMelee", false));
        this.setTemplate(s.getBoolean("isTemplate", false));
        templates.clear();
        ConfigurationSection templatesList = s.getConfigurationSection("templates");
        if (templatesList != null) {
            for (String sectionKey : templatesList.getKeys(false)) {
                String tmp = (String) templatesList.get(sectionKey);
                templates.add(tmp);
            }
        }
        ConfigurationSection templatePlaceholdersList = s.getConfigurationSection("templatePlaceholders");
        if (templatePlaceholdersList != null) {
            for (String sectionKey : templatePlaceholdersList.getKeys(false)) {
                String tmp = (String) templatePlaceholdersList.get(sectionKey);
                templatePlaceholders.add(tmp);
            }
        }
        rebuild();
    }

    public String getDuplicatePlaceholderMsg(Map<String, List<PlaceholderHolder>> duplicatePlaceholderIds) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("duplicate placeholder key found in item: ")
                .append(getName()).append("\n");
        duplicatePlaceholderIds.forEach((k,v)->{
            stringBuilder.append("key: ")
                    .append(k)
                    .append(", values: [");
            v.forEach((ph) ->{
                stringBuilder.append(ph.toString());
                stringBuilder.append(",");
            });
            stringBuilder.append("]");
        });
        return stringBuilder.toString();
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
        Condition<?> pow = PowerManager.instantiate(condition);
        pow.init(section, getName());
        addCondition(key, pow, false);
    }

    private void loadMarker(ConfigurationSection section, String powerName) throws UnknownPowerException {
        NamespacedKey key = PowerManager.parseKey(powerName);
        Class<? extends Marker> marker = PowerManager.getMarker(key);
        if (marker == null) {
            plugin.getLogger().warning("Unknown marker:" + key + " on item " + this.name);
            throw new UnknownPowerException(key);
        }
        Marker pow = PowerManager.instantiate(marker);
        pow.init(section, getName());
        addMarker(key, pow, false);
    }

    @SuppressWarnings("rawtypes")
    private void loadTrigger(ConfigurationSection section, String triggerName) throws UnknownPowerException {
        String baseTrigger = section.getString("base");
        if (baseTrigger == null) {
            throw new IllegalArgumentException();
        }
        Trigger base = Trigger.get(baseTrigger);
        if (base == null) {
            plugin.getLogger().warning("Unknown base trigger:" + baseTrigger + " on item " + this.name);
            throw new UnknownPowerException(new NamespacedKey(RPGItems.plugin, baseTrigger));
        }

        Trigger newTrigger = base.copy(triggerName);

        newTrigger.init(section, getName());
        triggers.put(triggerName, newTrigger);
    }

    @Override
    public void save() {
        ItemManager.save(this);
    }

    @SuppressWarnings("rawtypes")
    public void save(ConfigurationSection s) {
        s.set("name", name);
        if (id != 0) {
            s.set("id", id);
        }
        s.set("uid", uid);

        s.set("author", getAuthor());
        s.set("note", getNote());
        s.set("license", getLicense());

        s.set("mcVersion", getMcVersion());
        s.set("pluginSerial", getPluginSerial());

        s.set("haspermission", isHasPermission());
        s.set("permission", getPermission());
        s.set("display", getDisplayNameRaw().replaceAll("§", "&"));
        s.set("factor", getFactor());

        for (FactorModifier modifier : factorModifiers.values()) {
            modifier.save(s, "factorModifier");
        }

        s.set("damageMin", getDamageMin());
        s.set("damageMax", getDamageMax());
        s.set("damageMinMythic", getDamageMinMythic());
        s.set("damageMaxMythic", getDamageMaxMythic());

        s.set("critical.normal.rate", getCriticalRate());
        s.set("critical.normal.damage", getCriticalDamage());
        s.set("critical.normal.multiple", getCriticalMultiple());
        s.set("critical.back.rate", getCriticalBackRate());
        s.set("critical.back.damage", getCriticalBackDamage());
        s.set("critical.back.multiple", getCriticalBackMultiple());
        s.set("critical.anti.rate", getCriticalAntiRate());

        s.set("mythic.skill.damage-add", getMythicSkillDamage());
        s.set("mythic.skill.damage-add-multiple", getMythicSkillDamageMultiple());

        s.set("mythic.skill.critical.rate", getMythicSkillCriticalRate());
        s.set("mythic.skill.critical.damage-add", getMythicSkillCriticalDamage());
        s.set("mythic.skill.critical.damage-add-multiple", getMythicSkillCriticalDamageMultiple());

        s.set("dodge.rate", getDodgeRate());
        s.set("dodge.message-type", getDodgeMessageType().name().toUpperCase());
        s.set("dodge.message", getDodgeMessage().replace("\n", "\\n"));

        s.set("atkSpeed", getAtkSpeed());
        s.set("moveSpeed", getMoveSpeed());
        s.set("armour", getArmour());
        s.set("armourProjectile", getArmourProjectile());
        s.set("armourExpression", getArmourExpression());
        s.set("DamageType", getDamageType());
        s.set("attributemode", attributeMode.name());
        ArrayList<String> descriptionConv = new ArrayList<>(getDescription());
        descriptionConv.replaceAll(s1 -> s1.replaceAll("§", "&"));
        s.set("description", descriptionConv);
        s.set("item", getItem().toString());
        s.set("ignoreWorldGuard", isIgnoreWorldGuard());
        s.set("canBeOwned", isCanBeOwned());
        s.set("hasStackId", isHasStackId());

        ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(getItem());

        if (itemMeta instanceof LeatherArmorMeta) {
            s.set("item_colour", getDataValue());
        } else if (itemMeta instanceof Damageable) {
            s.set("item_data", getDataValue());
        }
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
        ConfigurationSection markerConfigs = s.createSection("markers");
        i = 0;
        for (Marker p : markers) {
            MemoryConfiguration pConfig = new MemoryConfiguration();
            pConfig.set("markerName", p.getNamespacedKey().toString());
            p.save(pConfig);
            markerConfigs.set(Integer.toString(i), pConfig);
            i++;
        }
        ConfigurationSection triggerConfigs = s.createSection("triggers");
        for (Entry<String, Trigger> p : triggers.entrySet()) {
            MemoryConfiguration pConfig = new MemoryConfiguration();
            p.getValue().save(pConfig);
            pConfig.set("base", p.getValue().getBase());
            triggerConfigs.set(p.getKey(), pConfig);
            i++;
        }

        s.set("hitCost", getHitCost());
        s.set("hittingCost", getHittingCost());
        s.set("blockBreakingCost", getBlockBreakingCost());
        s.set("hitCostByDamage", isHitCostByDamage());
        s.set("maxDurability", getMaxDurability());
        s.set("defaultDurability", getDefaultDurability());
        s.set("durabilityLowerBound", getDurabilityLowerBound());
        s.set("durabilityUpperBound", getDurabilityUpperBound());
        s.set("hasDurabilityBar", isHasDurabilityBar());
        s.set("showPowerText", isShowPowerText());
        s.set("showArmourLore", isShowArmourLore());
        s.set("damageMode", getDamageMode().name());
        s.set("customModelData", getCustomModelData());

        Map<Enchantment, Integer> enchantMap = getEnchantMap();
        if (enchantMap != null) {
            ConfigurationSection ench = s.createSection("enchantments");
            for (Enchantment e : enchantMap.keySet()) {
                ench.set(e.getKey().getKey(), enchantMap.get(e));
            }
        } else {
            s.set("enchantments", null);
        }
        s.set("enchantMode", enchantMode.name());
        List<ItemFlag> itemFlags = getItemFlags();
        if (!itemFlags.isEmpty()) {
            List<String> tmp = new ArrayList<>();
            for (ItemFlag flag : itemFlags) {
                tmp.add(flag.name());
            }
            s.set("itemFlags", tmp);
        } else {
            s.set("itemFlags", null);
        }
        s.set("customItemModel", isCustomItemModel());
        s.set("barFormat", getBarFormat().name());
        s.set("alwaysAllowMelee", isAlwaysAllowMelee());

        s.set("isTemplate", isTemplate());
        s.set("quality", getQuality());
        s.set("type", getType());
        ConfigurationSection templatesConfigs = s.createSection("templates");
        Set<String> templates = getTemplates();
        Iterator<String> it = templates.iterator();
        for (i = 0; i < templates.size(); i++) {
            String next = it.next();
            templatesConfigs.set(String.valueOf(i), next);
        }
        ConfigurationSection templatePlaceHolderConfigs = s.createSection("templatePlaceholders");
        Set<String> templatePlaceHolders = getTemplatePlaceHolders();
        Iterator<String> it1 = templatePlaceHolders.iterator();
        for (i = 0; i < templatePlaceHolders.size(); i++) {
            String next = it1.next();
            templatePlaceHolderConfigs.set(String.valueOf(i), next);
        }
    }

    private Material getMaterial() {
        if (RPGItems.protocolLibAvailable() && plugin.cfg.useProtocolLib && plugin.cfg.plAutoReplaceArmorMaterial) {
            boolean n = RPGItems.isNetheriteAvailable();
            switch (getItem()) {
                case LEATHER_HELMET:
                case IRON_HELMET:
                case GOLDEN_HELMET:
                case DIAMOND_HELMET:
                    return n ? NETHERITE_HELMET : DIAMOND_HELMET;
                case LEATHER_CHESTPLATE:
                case IRON_CHESTPLATE:
                case GOLDEN_CHESTPLATE:
                case DIAMOND_CHESTPLATE:
                    return n ? NETHERITE_CHESTPLATE : DIAMOND_CHESTPLATE;
                case LEATHER_LEGGINGS:
                case IRON_LEGGINGS:
                case GOLDEN_LEGGINGS:
                case DIAMOND_LEGGINGS:
                    return n ? NETHERITE_LEGGINGS : DIAMOND_LEGGINGS;
                case LEATHER_BOOTS:
                case IRON_BOOTS:
                case GOLDEN_BOOTS:
                case DIAMOND_BOOTS:
                    return n ? NETHERITE_BOOTS : DIAMOND_BOOTS;
            }
        }
        return getItem();
    }

    @Deprecated
    public void updateItem(ItemStack item) {
        updateItem(null, item, false);
    }

    @Deprecated
    public void updateItem(ItemStack item, boolean loreOnly) {
        updateItem(null, item, loreOnly);
    }

    public void updateItem(@Nullable Player player, ItemStack item) {
        updateItem(player, item, false);
    }

    public void updateItem(@Nullable Player player, ItemStack item, boolean loreOnly) {
        if (item == null) return;
        List<String> oldLore = item.getItemMeta() == null || item.getItemMeta().getLore() == null ? new ArrayList<>() : new ArrayList<>(item.getItemMeta().getLore());
        List<String> reservedLore = this.filterLores(item);

        Material material = getMaterial();
        Integer cmd = getCustomModelData() == -1 ? null : getCustomModelData();
        ItemUpdateEvent itemUpdateEvent = new ItemUpdateEvent(this, player, item, loreOnly, material, cmd, getItemFlags());
        Bukkit.getPluginManager().callEvent(itemUpdateEvent);
        item.setType(itemUpdateEvent.getMaterial());

        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>(getLore());
        Map<RPGStone, String> map = ItemManager.toRPGStoneList(item);
        if (!map.isEmpty()) {
            lore.add("");
            for (Map.Entry<RPGStone, String> entry : map.entrySet()) {
                RPGStone stone = entry.getKey();
                String trigger = entry.getValue();
                if (trigger != null && stone.useCustomTrigger()) {
                    String triggerDisplay = I18n.getFormatted(player, "properties.triggers." + trigger + ".display");
                    List<String> extra = new ArrayList<>();
                    for (String line : stone.getExtraDescription()) {
                        extra.add(line.replace("%trigger%", triggerDisplay));
                    }
                    lore.addAll(extra);
                } else {
                    lore.addAll(stone.getExtraDescription());
                }
            }
        }

        PersistentDataContainer itemTagContainer = Objects.requireNonNull(meta).getPersistentDataContainer();
        ISubItemTagContainer rpgitemsTagContainer = ItemPDC.makeTag(itemTagContainer, TAG_META);
        ItemPDC.set(rpgitemsTagContainer, TAG_ITEM_UID, getUid());
        addDurabilityBar(rpgitemsTagContainer, lore);
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(Color.fromRGB(getDataValue()));
        }
        Damageable damageable = (Damageable) meta;
        if (getMaxDurability() > 0) {
            int durability = ItemPDC.computeIfAbsent(rpgitemsTagContainer, TAG_DURABILITY, PersistentDataType.INTEGER, this::getDefaultDurability);
            if (isCustomItemModel()) {
                damageable.setDamage(getDataValue());
            } else {
                damageable.setDamage((getItem().getMaxDurability() - ((short) ((double) getItem().getMaxDurability() * ((double) durability / (double) getMaxDurability())))));
            }
        } else {
            if (isCustomItemModel()) {
                damageable.setDamage(getDataValue());
            } else {
                damageable.setDamage(getItem().getMaxDurability() != 0 ? 0 : getDataValue());
            }
        }
        // Patch for mcMMO buff. See SkillUtils.java#removeAbilityBuff in mcMMO
        if (oldLore.contains("mcMMO Ability Tool"))
            lore.add("mcMMO Ability Tool");

        lore.addAll(reservedLore);
        LoreUpdateEvent loreUpdateEvent = new LoreUpdateEvent(this, player, item, oldLore, lore);
        Bukkit.getPluginManager().callEvent(loreUpdateEvent);
        item = loreUpdateEvent.item;
        meta.setLore(loreUpdateEvent.newLore);

        //quality prefix
        String qualityPrefix = plugin.cfg.qualityPrefixes.get(getQuality());
        if (qualityPrefix != null){
            if (meta.hasDisplayName() && !meta.getDisplayName().startsWith(qualityPrefix)){
                String displayName = meta.getDisplayName();
                meta.setDisplayName(qualityPrefix + displayName);
            }
        }

        if (armour > 0 || armourProjectile > 0 || !armourExpression.isEmpty()) {
            String m = item.getType().name().toUpperCase();
            meta.removeAttributeModifier(Attribute.GENERIC_ARMOR);
            meta.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
            int modifier = 0;
            if (m.endsWith("_HELMET")) modifier = 3;
            if (m.endsWith("_CHESTPLATE")) modifier = 8;
            if (m.endsWith("_LEGGINGS")) modifier = 6;
            if (m.endsWith("_BOOTS")) modifier = 3;
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new org.bukkit.attribute.AttributeModifier("RPGItems", modifier, ADD_NUMBER));
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new org.bukkit.attribute.AttributeModifier("RPGItems", 2, ADD_NUMBER));
        }

        if (atkSpeed != 0) {
            meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new org.bukkit.attribute.AttributeModifier("RPGItems", atkSpeed, ADD_NUMBER));
        }

        if (moveSpeed != 0) {
            meta.removeAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED);
            meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new org.bukkit.attribute.AttributeModifier("RPGItems", moveSpeed, ADD_NUMBER));
        }

        if (loreOnly) {
            rpgitemsTagContainer.commit();
            item.setItemMeta(meta);
            return;
        }

        meta.setUnbreakable(isCustomItemModel() || hasMarker(Unbreakable.class));
        meta.removeItemFlags(meta.getItemFlags().toArray(new ItemFlag[0]));

        meta.setCustomModelData(itemUpdateEvent.getCustomModelData());
        for (ItemFlag flag : itemUpdateEvent.getItemFlags()) {
            meta.addItemFlags(flag);
        }
        if (getEnchantMode() == EnchantMode.DISALLOW) {
            Set<Enchantment> enchs = meta.getEnchants().keySet();
            for (Enchantment e : enchs) {
                meta.removeEnchant(e);
            }
        }
        Map<Enchantment, Integer> enchantMap = getEnchantMap();
        if (enchantMap != null) {
            for (Entry<Enchantment, Integer> e : enchantMap.entrySet()) {
                meta.addEnchant(e.getKey(), Math.max(meta.getEnchantLevel(e.getKey()), e.getValue()), true);
            }
        }
        checkAndMakeUnique(rpgitemsTagContainer);
        rpgitemsTagContainer.commit();
        item.setItemMeta(refreshAttributeModifiers(meta));
        try {
            ItemTagUtils.setInt(item, NBT_UID, uid);
            if (RPGItems.plugin.cfg.itemStackUuid) {
                if (ItemTagUtils.getString(item, NBT_ITEM_UUID).isEmpty()) {
                    UUID uuid = UUID.randomUUID();
                    ItemTagUtils.setString(item, NBT_ITEM_UUID, uuid.toString());
                }
            }
            LoreUpdateEvent.Post post = new LoreUpdateEvent.Post(loreUpdateEvent, this, item);
            Bukkit.getPluginManager().callEvent(post);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
            }
            plugin.getLogger().warning(sw.toString());
        }
    }

    private void checkAndMakeUnique(ISubItemTagContainer meta) {
        List<Unique> markers = getMarker(Unique.class);
        List<SlotCondition> conditions = getConditions(SlotCondition.class);

        if (!markers.isEmpty() ) {
            Unique unique = markers.get(0);
            if(unique.enabled){
                if (!meta.has(RGI_UNIQUE_MARK, PersistentDataType.BYTE)) {
                    meta.set(RGI_UNIQUE_MARK, PersistentDataType.BYTE, (byte) 0);
                }
                meta.set(RGI_UNIQUE_ID, PersistentDataType.STRING, UUID.randomUUID().toString());
            }else {
                meta.remove(RGI_UNIQUE_MARK);
                meta.remove(RGI_UNIQUE_ID);
            }
        }
        if(!conditions.isEmpty()){
            if (!meta.has(RGI_UNIQUE_MARK, PersistentDataType.BYTE)) {
                meta.set(RGI_UNIQUE_MARK, PersistentDataType.BYTE, (byte) 0);
            }
            meta.set(RGI_UNIQUE_ID, PersistentDataType.STRING, UUID.randomUUID().toString());
        }
    }

    private void addDurabilityBar(PersistentDataContainer meta, List<String> lore) {
        int maxDurability = getMaxDurability();
        if (maxDurability > 0) {
            int durability = ItemPDC.computeIfAbsent(meta, TAG_DURABILITY, PersistentDataType.INTEGER, this::getDefaultDurability);
            if (isHasDurabilityBar()) {
                StringBuilder out = new StringBuilder();
                char boxChar = '■';
                double ratio = (double) durability / (double) maxDurability;
                BarFormat barFormat = getBarFormat();
                switch (barFormat) {
                    case NUMERIC_BIN:
                    case NUMERIC_BIN_MINUS_ONE:
                    case NUMERIC_HEX:
                    case NUMERIC_HEX_MINUS_ONE:
                    case NUMERIC:
                    case NUMERIC_MINUS_ONE: {
                        out.append(ChatColor.GREEN).append(boxChar).append(" ");
                        out.append(ratio < 0.1 ? ChatColor.RED : ratio < 0.3 ? ChatColor.YELLOW : ChatColor.GREEN);
                        out.append(formatBar(durability, maxDurability, barFormat));
                        out.append(ChatColor.RESET).append(" / ").append(ChatColor.AQUA);
                        out.append(formatBar(maxDurability, maxDurability, barFormat));
                        out.append(ChatColor.GREEN).append(boxChar);
                        break;
                    }
                    case DEFAULT: {
                        int boxCount = tooltipWidth / 7;
                        int mid = (int) ((double) boxCount * (ratio));
                        for (int i = 0; i < boxCount; i++) {
                            out.append(i < mid ? ChatColor.GREEN : i == mid ? ChatColor.YELLOW : ChatColor.RED);
                            out.append(boxChar);
                        }
                        break;
                    }
                }
                if (lore.isEmpty() || !lore.get(lore.size() - 1).contains(boxChar + ""))
                    lore.add(out.toString());
                else
                    lore.set(lore.size() - 1, out.toString());
            }
        }
    }

    private String formatBar(int durability, int maxDurability, BarFormat barFormat) {
        switch (barFormat) {
            case NUMERIC: {
                return String.valueOf(durability);
            }
            case NUMERIC_MINUS_ONE: {
                return String.valueOf(durability - 1);
            }
            case NUMERIC_HEX: {
                int hexLen = String.format("%X", maxDurability).length();
                return String.format(String.format("0x%%0%dX", hexLen), durability);
            }
            case NUMERIC_HEX_MINUS_ONE: {
                int hexLenM1 = String.format("%X", maxDurability - 1).length();
                return String.format(String.format("0x%%0%dX", hexLenM1), durability - 1);
            }
            case NUMERIC_BIN: {
                int binLen = Integer.toBinaryString(maxDurability).length();
                return String.format(String.format("0b%%%ds", binLen), Integer.toBinaryString(durability)).replace(' ', '0');
            }
            case NUMERIC_BIN_MINUS_ONE: {
                int binLenM1 = Integer.toBinaryString(maxDurability - 1).length();
                return String.format(String.format("0b%%%ds", binLenM1), Integer.toBinaryString(durability - 1)).replace(' ', '0');
            }
        }
        throw new UnsupportedOperationException();
    }

    private List<String> filterLores(ItemStack i) {
        List<String> ret = new ArrayList<>();
        List<LoreFilter> patterns = getMarker(LoreFilter.class).stream()
                .filter(p -> !Strings.isNullOrEmpty(p.regex))
                .map(LoreFilter::compile)
                .collect(Collectors.toList());
        if (patterns.isEmpty()) return Collections.emptyList();
        if (!i.hasItemMeta() || !Objects.requireNonNull(i.getItemMeta()).hasLore()) return Collections.emptyList();
        for (String str : Objects.requireNonNull(i.getItemMeta().getLore())) {
            for (LoreFilter p : patterns) {
                Matcher matcher = p.pattern().matcher(ChatColor.stripColor(str));
                if (p.find ? matcher.find() : matcher.matches()) {
                    ret.add(str);
                    break;
                }
            }
        }

        return ret;
    }

    private ItemMeta refreshAttributeModifiers(ItemMeta itemMeta) {
        List<AttributeModifier> attributeModifiers = getMarker(AttributeModifier.class);
        Multimap<Attribute, org.bukkit.attribute.AttributeModifier> old = itemMeta.getAttributeModifiers();
        if (attributeMode.equals(AttributeMode.FULL_UPDATE)) {
            if (old != null && !old.isEmpty()) {
                old.forEach(itemMeta::removeAttributeModifier);
            }
        }
        if (!attributeModifiers.isEmpty()) {
            for (AttributeModifier attributeModifier : attributeModifiers) {
                Attribute attribute = attributeModifier.attribute;
                UUID uuid = new UUID(attributeModifier.uuidMost, attributeModifier.uuidLeast);
                org.bukkit.attribute.AttributeModifier modifier = new org.bukkit.attribute.AttributeModifier(
                        uuid,
                        attributeModifier.name,
                        attributeModifier.amount,
                        attributeModifier.operation,
                        attributeModifier.slot
                );
                if (old != null) {
                    old.entries().stream().filter(m -> m.getValue().getUniqueId().equals(uuid)).findAny().ifPresent(
                            e -> itemMeta.removeAttributeModifier(e.getKey(), e.getValue())
                    );
                }
                itemMeta.addAttributeModifier(attribute, modifier);
            }
        }
        return itemMeta;
    }

    public boolean canDoMeleeTo(ItemStack item, Entity entity) {
        if (hasMarker(RangedOnly.class)) {
            return false;
        }
        if (item.getType() == Material.BOW || item.getType() == Material.SNOWBALL || item.getType() == Material.EGG || item.getType() == Material.POTION) {
            return isAlwaysAllowMelee();
        }
        return true;
    }

    public boolean canDoProjectileTo(ItemStack item, double distance, Entity entity) {
        List<Ranged> ranged = getMarker(Ranged.class, true);
        if (!ranged.isEmpty()) {
            return !(ranged.get(0).rm > distance) && !(distance > ranged.get(0).r);
        }
        return true;
    }

    /**
     * Event-type independent melee damage event
     *
     * @param p            Player who launched the damager
     * @param originDamage Origin damage value
     * @param stack        ItemStack of this item
     * @param entity       Victim of this damage event
     * @return Final damage or -1 if should cancel this event
     */
    public double meleeDamage(Player p, double originDamage, ItemStack stack, Entity entity) {
        double damage = originDamage;
        if (!canDoMeleeTo(stack, entity) || ItemManager.canUse(p, this) == Event.Result.DENY) {
            return -1;
        }
        boolean can = consumeDurability(p, stack, getHittingCost());
        if (!can) {
            return -1;
        }
        boolean isMythic = MythicSupport.isMythic(entity);
        boolean isPlayer = entity instanceof Player;
        int min = isMythic ? getDamageMinMythic() : isPlayer ? getDamageMinPlayer() : getDamageMin();
        int max = isMythic ? getDamageMaxMythic() : isPlayer ? getDamageMaxPlayer() : getDamageMax();

        switch (getDamageMode()) {
            case MULTIPLY:
            case FIXED:
            case ADDITIONAL:
                damage = min != max ? (min + ThreadLocalRandom.current().nextInt(max - min + 1)) : min;

                if (getDamageMode() == DamageMode.MULTIPLY) {
                    damage *= originDamage;
                    break;
                }

                Collection<PotionEffect> potionEffects = p.getActivePotionEffects();
                double strength = 0, weak = 0;
                for (PotionEffect pe : potionEffects) {
                    if (pe.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                        strength = 3 * (pe.getAmplifier() + 1);//MC 1.9+
                    }
                    if (pe.getType().equals(PotionEffectType.WEAKNESS)) {
                        weak = 4 * (pe.getAmplifier() + 1);//MC 1.9+
                    }
                }
                damage = damage + strength - weak;

                if (getDamageMode() == DamageMode.ADDITIONAL) {
                    damage += originDamage;
                }
                if (damage < 0) damage = 0;
                break;
            case VANILLA:
                //no-op
                break;
        }
        return damage;
    }

    /**
     * Event-type independent projectile damage event
     *
     * @param p            Player who launched the damager
     * @param originDamage Origin damage value
     * @param stack        ItemStack of this item
     * @param damager      Projectile of this damage event
     * @param entity       Victim of this damage event
     * @return Final damage or -1 if should cancel this event
     */
    public double projectileDamage(Player p, double originDamage, ItemStack stack, Entity damager, Entity entity) {
        double damage = originDamage;
        if (ItemManager.canUse(p, this) == Event.Result.DENY) {
            return -1;
        }

        double distance = p.getLocation().distance(entity.getLocation());
        if (!canDoProjectileTo(stack, distance, entity)) {
            return -1;
        }

        boolean isMythic = MythicSupport.isMythic(entity);
        boolean isPlayer = entity instanceof Player;
        int min = isMythic ? getDamageMinMythic() : isPlayer ? getDamageMinPlayer() : getDamageMin();
        int max = isMythic ? getDamageMaxMythic() : isPlayer ? getDamageMaxPlayer() : getDamageMax();

        switch (getDamageMode()) {
            case FIXED:
            case ADDITIONAL:
            case MULTIPLY:
                damage = min != max ? (min + ThreadLocalRandom.current().nextInt(max - min + 1)) : min;

                if (getDamageMode() == DamageMode.MULTIPLY) {
                    damage *= originDamage;
                    break;
                }

                //Apply force adjustments
                if (damager.hasMetadata("RPGItems.Force")) {
                    damage *= damager.getMetadata("RPGItems.Force").get(0).asFloat();
                }
                if (getDamageMode() == DamageMode.ADDITIONAL) {
                    damage += originDamage;
                }
                break;
            case VANILLA:
                //no-op
                break;
        }
        return damage;
    }

    @Deprecated
    public double takeDamage(Player p, double originDamage, ItemStack stack, Entity damager) {
        return takeDamage(p, originDamage, stack, damager, false);
    }

    /**
     * Event-type independent take damage event
     *
     * @param p            Player taking damage
     * @param originDamage Origin damage value
     * @param stack        ItemStack of this item
     * @param damager      Cause of this damage. May be null
     * @return Final damage or -1 if should cancel this event
     */
    public double takeDamage(Player p, double originDamage, ItemStack stack, Entity damager, boolean projectile) {
        if (ItemManager.canUse(p, this) == Event.Result.DENY) {
            return originDamage;
        }
        boolean can;
        if (!isHitCostByDamage()) {
            can = consumeDurability(p, stack, getHitCost());
        } else {
            can = consumeDurability(p, stack, (int) (getHitCost() * originDamage / 100d));
        }
        if (can) {
            double rate = projectile && getArmourProjectile() > 0 ? getArmourProjectile() : getArmour();
            if (rate > 0) {
                originDamage -= Math.round(originDamage * (rate / 100d));
            }
        }
        return originDamage;
    }

    /**
     * Event-type independent take damage event
     *
     * @param p     Player taking damage
     * @param stack ItemStack of this item
     * @param block Block
     * @return If should process this event
     */
    public boolean breakBlock(Player p, ItemStack stack, Block block) {
        return consumeDurability(p, stack, getBlockBreakingCost());
    }

    private <TEvent extends Event, TPower extends Pimpl, TResult, TReturn> boolean triggerPreCheck(Player player, ItemStack i, TEvent event, Trigger<TEvent, TPower, TResult, TReturn> trigger, List<TPower> powers) {
        if (i.getType().equals(AIR)) return false;
        if (powers.isEmpty()) return false;
        if (checkPermission(player, true) == Event.Result.DENY) return false;

        RPGItemsPowersPreFireEvent<TEvent, TPower, TResult, TReturn> preFire = new RPGItemsPowersPreFireEvent<>(player, i, event, this, trigger, powers);
        Bukkit.getServer().getPluginManager().callEvent(preFire);
        return !preFire.isCancelled();
    }

    private <T> PowerResult<T> checkConditions(Player player, ItemStack i, Pimpl pimpl, List<Condition<?>> conds, Map<PropertyHolder, PowerResult<?>> context) {
        Set<String> ids = pimpl.getPower().getConditions();
        List<Condition<?>> conditions = conds.stream()
                .filter(p -> ids.contains(p.id()))
                .collect(Collectors.toList());
        List<Condition<?>> failed = conditions.stream()
                .filter(p -> p.isStatic() ? !context.get(p).isOK() : !p.check(player, i, context).isOK())
                .collect(Collectors.toList());
        if (failed.isEmpty()) return null;
        return failed.stream().anyMatch(Condition::isCritical) ? PowerResult.abort() : PowerResult.condition();
    }

    private Map<Condition<?>, PowerResult<?>> checkStaticCondition(Player player, ItemStack i, List<Power> powers, List<Condition<?>> conds) {
        Set<String> ids = powers.stream()
                .flatMap(p -> p.getConditions().stream())
                .collect(Collectors.toSet());
        List<Condition<?>> statics = conds.stream()
                .filter(Condition::isStatic)
                .filter(p -> ids.contains(p.id()))
                .collect(Collectors.toList());
        Map<Condition<?>, PowerResult<?>> result = new LinkedHashMap<>();
        for (Condition<?> c : statics) {
            result.put(c, c.check(player, i, Collections.unmodifiableMap(result)));
        }
        return result;
    }

    public Pair<List<Power>, List<Condition<?>>> getAllPowersAndConditions(ItemStack item) {
        RPGItem rpg = ItemManager.toRPGItem(item).orElse(null);
        if (rpg == null || !rpg.name.equals(this.name)) return Pair.of(getPowers(), getConditions());
        Map<RPGStone, String> map = ItemManager.toRPGStoneList(item);
        if (map.isEmpty()) return Pair.of(getPowers(), getConditions());
        List<Power> extraPowers = new ArrayList<>(getPowers());
        List<Condition<?>> extraConditions = new ArrayList<>(getConditions());
        for (RPGStone stone : map.keySet()) {
            extraPowers.addAll(stone.getPowers());
            extraConditions.addAll(stone.getConditions());
        }
        return Pair.of(extraPowers, extraConditions);
    }

    public <TEvent extends Event, TPower extends Pimpl, TResult, TReturn> TReturn power(Player player, ItemStack i, TEvent event, Trigger<TEvent, TPower, TResult, TReturn> trigger, Object context) {
        powerCustomTrigger(player, i, event, trigger, context);

        Pair<List<Power>, List<Condition<?>>> pair = getAllPowersAndConditions(i);

        List<TPower> powers = this.getPower(pair.getKey(), trigger, player, i);
        TReturn ret = trigger.def(player, i, event);
        if (!triggerPreCheck(player, i, event, trigger, powers)) return ret;
        try {
            List<Condition<?>> conds = pair.getValue();
            Map<Condition<?>, PowerResult<?>> staticCond = checkStaticCondition(player, i, pair.getKey(), conds);
            Map<PropertyHolder, PowerResult<?>> resultMap = new LinkedHashMap<>(staticCond);
            int magicFlag = 0;
            for (TPower power : powers) {
                PowerResult<TResult> result = checkConditions(player, i, power, conds, resultMap);
                if (result != null) {
                    resultMap.put(power.getPower(), result);
                } else {
                    boolean flag = true;
                    if (power.getPower() instanceof BasePower) {
                        BasePower base = (BasePower) power.getPower();
                        if (!plugin.magic.costMagic(player, base.getCostMagic())) {
                            result = PowerResult.cost();
                            flag = false;
                            magicFlag += base.getCostMagic();
                        }
                    }
                    if (flag) {
                        if (power.getPower().requiredContext() != null) {
                            result = handleContext(player, i, event, trigger, power);
                        } else {
                            result = trigger.run(this, power, player, i, event, context);
                        }
                    }
                    resultMap.put(power.getPower(), result);
                }
                ret = trigger.next(ret, result);
                if (result.isAbort()) break;
            }
            triggerPostFire(player, i, event, trigger, resultMap, ret);
            if (magicFlag > 0) {
                String message = I18n.getFormatted(player, "message.magic.not-enough", magicFlag);
                if (player.hasPermission("rpgitems.actionbar.magic")) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                else player.sendMessage(message);
            }
            return ret;
        } finally {
            Context.instance().cleanTemp(player.getUniqueId());
        }
    }

    @SuppressWarnings("unchecked")
    public <TEvent extends Event, TPower extends Pimpl, TResult, TReturn> void powerCustomTrigger(Player player, ItemStack i, TEvent event, Trigger<TEvent, TPower, TResult, TReturn> trigger, Object context) {
        this.triggers.entrySet()
                .parallelStream()
                .filter(e -> trigger.getClass().isInstance(e.getValue()))
                .sorted(Comparator.comparing(en -> en.getValue().getPriority()))
                .filter(e -> e.getValue().check(player, i, event))
                .forEachOrdered(e -> this.power(player, i, event, e.getValue(), context));
    }

    public <TEvent extends Event, TPower extends Pimpl, TResult, TReturn> TReturn power(Player player, ItemStack i, TEvent event, Trigger<TEvent, TPower, TResult, TReturn> trigger) {
        return power(player, i, event, trigger, null);
    }

    public <TEvent extends Event, TPower extends Pimpl, TResult, TReturn> PowerResult<TResult> handleContext(Player player, ItemStack i, TEvent event, Trigger<TEvent, TPower, TResult, TReturn> trigger, TPower power) {
        PowerResult<TResult> result;
        String contextKey = power.getPower().requiredContext();
        Object context = Context.instance().get(player.getUniqueId(), contextKey);
        if (context == null) {
            return PowerResult.context();
        }
        if (context instanceof Location) {
            if (power instanceof PowerLocation) {
                PowerResult<Void> overrideResult = BaseTriggers.LOCATION.run(this, (PowerLocation) power, player, i, event, context);
                result = trigger.warpResult(overrideResult, power, player, i, event);
            } else {
                throw new IllegalStateException();
            }
        } else if (context instanceof Pair) {
            Object key = ((Pair<?, ?>) context).getKey();
            if (key instanceof LivingEntity) {
                PowerResult<Void> overrideResult = BaseTriggers.LIVINGENTITY.run(this, (PowerLivingEntity) power, player, i, event, context);
                result = trigger.warpResult(overrideResult, power, player, i, event);
            } else {
                throw new IllegalStateException();
            }
        } else {
            throw new IllegalStateException();
        }
        return result;
    }

    private <TEvent extends Event, TPower extends Pimpl, TResult, TReturn> void triggerPostFire(Player player, ItemStack itemStack, TEvent event, Trigger<TEvent, TPower, TResult, TReturn> trigger, Map<PropertyHolder, PowerResult<?>> resultMap, TReturn ret) {
        RPGItemsPowersPostFireEvent<TEvent, TPower, TResult, TReturn> postFire = new RPGItemsPowersPostFireEvent<>(player, itemStack, event, this, trigger, resultMap, ret);
        Bukkit.getServer().getPluginManager().callEvent(postFire);

        if (getItemStackDurability(itemStack).map(d -> d <= 0).orElse(false)) {
            itemStack.setAmount(0);
            itemStack.setType(AIR);
        }
    }

    public void rebuild() {
        List<String> lines = getTooltipLines();
        lines.remove(0);
        setLore(lines);
    }

    @SuppressWarnings("deprecation")
    public List<String> getTooltipLines() {
        ArrayList<String> output = new ArrayList<>();
        output.add(getDisplayName());

        // add powerLores
        if (isShowPowerText()) {
            for (Power p : getPowers()) {
                String txt = p.displayText();
                if (txt != null && !txt.isEmpty()) {
                    output.add(txt);
                }
            }
        }

        // add descriptions
        output.addAll(getDescription());

        // compute width
        int width = 0;
        for (String str : output) {
            width = Math.max(width, Utils.getStringWidth(ChatColor.stripColor(str)));
        }

        // compute armorMinLen
        int armorMinLen = 0;
        String damageStr = null;
        String criticalStr = null;
        String mythicSkillStr = null;
        String mythicSkillStr2 = null;
        if (isShowArmourLore()) {
            if (getArmour() > 0 && getArmourProjectile() > 0) {
                damageStr = I18n.formatDefault("item.armour-projectile", getArmour(), getArmourProjectile());
            } else if (getArmour() > 0) {
                damageStr = I18n.formatDefault("item.armour", getArmour());
            } else if (getArmourProjectile() > 0) {
                damageStr = I18n.formatDefault("item.armour-projectile-only", getArmourProjectile());
            }
            if ((getDamageMin() != 0 || getDamageMax() != 0) && getDamageMode() != DamageMode.VANILLA) {
                damageStr = damageStr == null ? "" : damageStr + " & ";
                if (getDamageMode() == DamageMode.ADDITIONAL) {
                    damageStr += I18n.formatDefault("item.additionaldamage", getDamageMin() == getDamageMax() ? String.valueOf(getDamageMin()) : getDamageMin() + "-" + getDamageMax());
                } else if (getDamageMode() == DamageMode.MULTIPLY) {
                    damageStr += I18n.formatDefault("item.multiplydamage", getDamageMin() == getDamageMax() ? String.valueOf(getDamageMin()) : getDamageMin() + "-" + getDamageMax());
                } else {
                    damageStr += I18n.formatDefault("item.damage", getDamageMin() == getDamageMax() ? String.valueOf(getDamageMin()) : getDamageMin() + "-" + getDamageMax());
                }
            }
            if (getCriticalRate() > 0) {
                criticalStr = getDamageText(getCriticalDamage(), getCriticalMultiple(), "item.critical_damage");
            }
            if (getMythicSkillDamage() > 0 || getMythicSkillDamageMultiple() != 1) {
                mythicSkillStr = getDamageText(getMythicSkillDamage(), getMythicSkillDamageMultiple(), "item.mythic_skill.damage");
            }
            if (getMythicSkillCriticalRate() > 0) {
                mythicSkillStr2 = getDamageText(getMythicSkillCriticalDamage(), getMythicSkillCriticalDamageMultiple(), "item.mythic_skill.critical_damage");
            }
            if (damageStr != null) {
                armorMinLen = Math.max(armorMinLen, Utils.getStringWidth(ChatColor.stripColor(damageStr)));
            }
        }
        tooltipWidth = Math.max(width, armorMinLen);

        if (isShowArmourLore()) {
            if (mythicSkillStr2 != null) output.add(1, ChatColor.WHITE + mythicSkillStr2);
            if (mythicSkillStr != null) output.add(1, ChatColor.WHITE + mythicSkillStr);
            if (criticalStr != null) output.add(1, ChatColor.WHITE + criticalStr);
            if (damageStr != null) output.add(1, ChatColor.WHITE + damageStr);
        }

        return output;
    }

    @Nullable
    private String getDamageText(double dmg, double dmgMultiple, String path) {
        if (dmg > 0 && (dmgMultiple <= 0 || dmgMultiple == 1)) {
            return I18n.formatDefault(path + ".normal",
                    String.valueOf(dmg)
            );
        } else if (dmgMultiple > 0 && dmgMultiple != 1) {
            String symbol = "";
            if (dmgMultiple > 1) symbol = "+";
            if (dmgMultiple < 1) symbol = "-";
            if (dmg > 0) {
                return I18n.formatDefault(path + ".normal_multiple",
                        String.valueOf(dmg),
                        symbol + (dmgMultiple * 100) + "%"
                );
            }else {
                return I18n.formatDefault(path + ".multiple",
                        symbol + (dmgMultiple * 100) + "%"
                );
            }
        }
        return null;
    }

    @Deprecated
    public ItemStack toItemStack() {
        return toItemStack(null);
    }
    public ItemStack toItemStack(Player player) {
        ItemStack rStack = new ItemStack(getItem());
        ItemMeta meta = rStack.getItemMeta();
        PersistentDataContainer itemTagContainer = Objects.requireNonNull(meta).getPersistentDataContainer();
        ISubItemTagContainer rpgitemsTagContainer = ItemPDC.makeTag(itemTagContainer, TAG_META);
        ItemPDC.set(rpgitemsTagContainer, TAG_ITEM_UID, getUid());
        if (isHasStackId()) {
            ItemPDC.set(rpgitemsTagContainer, TAG_STACK_ID, UUID.randomUUID());
        }
        rpgitemsTagContainer.commit();
        meta.setDisplayName(getDisplayName());
        rStack.setItemMeta(meta);

        updateItem(player, rStack, false);
        return rStack;
    }

    @Deprecated
    public void toModel(ItemStack itemStack) {
        toModel(null, itemStack);
    }
    public void toModel(@Nullable Player player, ItemStack itemStack) {
        updateItem(player, itemStack);
        ItemMeta itemMeta = itemStack.getItemMeta();
        ISubItemTagContainer meta = ItemPDC.makeTag(Objects.requireNonNull(itemMeta).getPersistentDataContainer(), TAG_META);
        meta.remove(TAG_OWNER);
        meta.remove(TAG_STACK_ID);
        ItemPDC.set(meta, TAG_IS_MODEL, true);
        try {
            ItemTagUtils.setBoolean(itemStack, NBT_IS_MODEL, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
            }
            plugin.getLogger().warning(sw.toString());
        }
        meta.commit();
        itemMeta.setDisplayName(getDisplayName());
        itemStack.setItemMeta(itemMeta);
    }

    public void unModel(ItemStack itemStack, Player owner) {
        updateItem(owner, itemStack);
        ItemMeta itemMeta = itemStack.getItemMeta();
        ISubItemTagContainer meta = ItemPDC.makeTag(Objects.requireNonNull(itemMeta).getPersistentDataContainer(), TAG_META);
        if (isCanBeOwned()) {
            ItemPDC.set(meta, TAG_OWNER, owner);
        }
        if (isHasStackId()) {
            ItemPDC.set(meta, TAG_STACK_ID, UUID.randomUUID());
        }
        meta.remove(TAG_IS_MODEL);
        meta.commit();
        itemMeta.setDisplayName(getDisplayName());
        itemStack.setItemMeta(itemMeta);
    }

    public Event.Result checkPermission(Player p, boolean showWarn) {
        if (isHasPermission() && !p.hasPermission(getPermission())) {
            if (showWarn)
                p.sendMessage(I18n.getInstance(p.getLocale()).format("message.error.permission", getDisplayName()));
            return Event.Result.DENY;
        }
        return Event.Result.ALLOW;
    }

    public void print(CommandSender sender) {
        print(sender, true);
    }

    public void print(CommandSender sender, boolean advance) {
        String author = this.getAuthor();
        BaseComponent authorComponent = new TextComponent(author);
        try {
            UUID uuid = UUID.fromString(this.getAuthor());
            OfflinePlayer authorPlayer = Bukkit.getOfflinePlayer(uuid);
            author = authorPlayer.getName();
            authorComponent = AdminCommands.getAuthorComponent(authorPlayer, author);
        } catch (IllegalArgumentException ignored) {
        }

        String locale = RPGItems.plugin.cfg.language;
        if (sender instanceof Player) {
            locale = ((Player) sender).getLocale();
            new Message("")
                    .append(I18n.getInstance(((Player) sender).getLocale()).format("message.item.print"), toItemStack((Player) sender))
                    .send(sender);
        } else {
            List<String> lines = getTooltipLines();
            for (String line : lines) {
                sender.sendMessage(line);
            }
        }
        I18n.getInstance(locale);

        new Message("").append(I18n.formatDefault("message.print.author"), Collections.singletonMap("{author}", authorComponent)).send(sender);
        if (!advance) {
            return;
        }

        new Message(I18n.formatDefault("message.print.license", getLicense())).send(sender);
        new Message(I18n.formatDefault("message.print.note", getNote())).send(sender);

        sender.sendMessage(I18n.formatDefault("message.durability.info", getMaxDurability(), getDefaultDurability(), getDurabilityLowerBound(), getDurabilityUpperBound()));
        if (isCustomItemModel()) {
            sender.sendMessage(I18n.formatDefault("message.print.customitemmodel", getItem().name() + ":" + getDataValue()));
        }
        if (!getItemFlags().isEmpty()) {
            StringBuilder str = new StringBuilder();
            for (ItemFlag flag : getItemFlags()) {
                if (str.length() != 0) {
                    str.append(", ");
                }
                str.append(flag.name());
            }
            sender.sendMessage(I18n.formatDefault("message.print.itemflags") + str);
        }
    }

    @Deprecated
    public void setItemStackDurability(ItemStack item, int val) {
        setItemStackDurability(null, item, val);
    }

    public void setItemStackDurability(Player player, ItemStack item, int val) {
        ItemMeta itemMeta = item.getItemMeta();
        ISubItemTagContainer tagContainer = ItemPDC.makeTag(Objects.requireNonNull(itemMeta), TAG_META);
        if (getMaxDurability() != -1) {
            ItemPDC.set(tagContainer, TAG_DURABILITY, val);
        }
        tagContainer.commit();
        item.setItemMeta(itemMeta);
        this.updateItem(player, item, true);
    }

    public Optional<Integer> getItemStackDurability(ItemStack item) {
        if (getMaxDurability() == -1) {
            return Optional.empty();
        }
        ItemMeta itemMeta = item.getItemMeta();
        //Power Consume will make this null in triggerPostFire().
        if(itemMeta == null){
            return Optional.empty();
        }
        ISubItemTagContainer tagContainer = ItemPDC.makeTag(itemMeta, TAG_META);
        int durability = ItemPDC.computeIfAbsent(tagContainer, TAG_DURABILITY, PersistentDataType.INTEGER, this::getDefaultDurability);
        tagContainer.commit();
        item.setItemMeta(itemMeta);
        return Optional.of(durability);
    }

    @Deprecated
    public boolean consumeDurability(ItemStack item, int val) {
        return consumeDurability(null, item, val, true);
    }

    public boolean consumeDurability(@Nullable Player player, ItemStack item, int val) {
        return consumeDurability(player, item, val, true);
    }

    @Deprecated
    public boolean consumeDurability(ItemStack item, int val, boolean checkbound) {
        return consumeDurability(null, item, val, checkbound);
    }

    public boolean consumeDurability(@Nullable Player player, ItemStack item, int val, boolean checkbound) {
        if (val == 0) return true;
        int durability;
        ItemMeta itemMeta = item.getItemMeta();
        if (getMaxDurability() != -1) {
            ISubItemTagContainer tagContainer = ItemPDC.makeTag(Objects.requireNonNull(itemMeta), TAG_META);
            durability = ItemPDC.computeIfAbsent(tagContainer, TAG_DURABILITY, PersistentDataType.INTEGER, this::getDefaultDurability);
            if (checkbound && (
                    (val > 0 && durability < getDurabilityLowerBound()) ||
                            (val < 0 && durability > getDurabilityUpperBound())
            )) {
                tagContainer.commit();
                item.setItemMeta(itemMeta);
                return false;
            }
            if (durability <= val
                        && hasMarker(Unbreakable.class)
                        && !isCustomItemModel()) {
                tagContainer.commit();
                item.setItemMeta(itemMeta);
                return false;
            }
            durability -= val;
            if (durability > getMaxDurability()) {
                durability = getMaxDurability();
            }
            ItemPDC.set(tagContainer, TAG_DURABILITY, durability);
            tagContainer.commit();
            item.setItemMeta(itemMeta);
            this.updateItem(player, item, true);
        }
        return true;
    }

    public void give(Player player, int count, boolean wear) {
        ItemStack itemStack = toItemStack(player);
        itemStack.setAmount(count);
        if (wear) {
            String itemType = item.name().toUpperCase();
            if (itemType.endsWith("_HELMET")) {
                if (player.getInventory().getHelmet() == null) {
                    player.getInventory().setHelmet(itemStack);
                    return;
                }
            } else if (itemType.endsWith("_CHESTPLATE")) {
                if (player.getInventory().getChestplate() == null) {
                    player.getInventory().setChestplate(itemStack);
                    return;
                }
            } else if (itemType.endsWith("_LEGGINGS")) {
                if (player.getInventory().getLeggings() == null) {
                    player.getInventory().setLeggings(itemStack);
                    return;
                }
            } else if (itemType.endsWith("_BOOTS")) {
                if (player.getInventory().getBoots() == null) {
                    player.getInventory().setBoots(itemStack);
                    return;
                }
            }
        }
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(itemStack);
        for (ItemStack o : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), o);
        }
    }

    public boolean hasMarker(Class<? extends Marker> marker) {
        for (Marker p : markers) {
            if (p.getClass().equals(marker)) return true;
        }
        return false;
    }

    public <T extends Marker> List<T> getMarker(Class<T> marker) {
        List<T> list = new ArrayList<>();
        for (Marker p : markers) {
            if (p.getClass().equals(marker)) {
                list.add(marker.cast(p));
            }
        }
        return list;
    }

    public <T extends Condition<?>> List<T> getConditions(Class<T> condition) {
        return conditions.stream().filter(p -> p.getClass().equals(condition)).map(condition::cast).collect(Collectors.toList());
    }

    public <T extends Marker> List<T> getMarker(Class<T> marker, boolean subclass) {
        if (!subclass) return getMarker(marker);
        List<T> list = new ArrayList<>();
        for (Marker p : markers) {
            if (marker.isInstance(p)) {
                list.add(marker.cast(p));
            }
        }
        return list;
    }

    public <T extends Marker> List<T> getMarker(NamespacedKey key, Class<T> marker) {
        List<T> list = new ArrayList<>();
        for (Marker p : markers) {
            if (p.getClass().equals(marker) && getPropertyHolderKey(p).equals(key)) {
                list.add(marker.cast(p));
            }
        }
        return list;
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
        if ("".equals(power.getPlaceholderId())){
            String placeholderId = power.getName() + "-" + getPowers().stream().filter(power1 -> power1.getName().equals(power.getName())).count();
            power.setPlaceholderId(placeholderId);
        }
        powers.add(power);
        keys.put(power, key);
        String placeholderId = power.getPlaceholderId();
        placeholders.put(placeholderId, power);
        if (update) {
            rebuild();
        }
    }

    @Override
    public void removePower(Power power) {
        powers.remove(power);
        keys.remove(power);
        String placeholderId = power.getPlaceholderId();
        if (!"".equals(placeholderId)){
            placeholders.remove(placeholderId, power);
        }
        power.deinit();
        rebuild();
    }

    @Override
    public void addCondition(NamespacedKey key, Condition<?> condition) {
        addCondition(key, condition, true);
    }

    private void addCondition(NamespacedKey key, Condition<?> condition, boolean update) {
        if ("".equals(condition.getPlaceholderId())){
            String placeholderId = condition.getName() + "-" + getConditions().stream().filter(power1 -> power1.getName().equals(condition.getName())).count();
            condition.setPlaceholderId(placeholderId);
        }
        conditions.add(condition);
        keys.put(condition, key);
        String placeholderId = condition.getPlaceholderId();
        placeholders.put(placeholderId, condition);
        if (update) {
            rebuild();
        }
    }

    @Override
    public void removeCondition(Condition<?> condition) {
        conditions.remove(condition);
        keys.remove(condition);
        String placeholderId = condition.getPlaceholderId();
        if (!"".equals(placeholderId)){
            placeholders.remove(placeholderId, condition);
        }
        rebuild();
    }

    public void addMarker(NamespacedKey key, Marker marker) {
        addMarker(key, marker, true);
    }

    private void addMarker(NamespacedKey key, Marker marker, boolean update) {
        if ("".equals(marker.getPlaceholderId())){
            String placeholderId = marker.getName() + "-" + getMarkers().stream().filter(power1 -> power1.getName().equals(marker.getName())).count();
            marker.setPlaceholderId(placeholderId);
        }
        markers.add(marker);
        keys.put(marker, key);
        String placeholderId = marker.getPlaceholderId();
        placeholders.put(placeholderId, marker);
        if (update) {
            rebuild();
        }
    }

    public void removeMarker(Marker marker) {
        markers.remove(marker);
        keys.remove(marker);
        String placeholderId = marker.getPlaceholderId();
        if (!"".equals(placeholderId)){
            placeholders.remove(placeholderId, marker);
        }
        rebuild();
    }

    public Map<String, List<PlaceholderHolder>> checkDuplicatePlaceholderIds(){
        Map<String, List<PlaceholderHolder>> ids = new HashMap<>();
        for (PlaceholderHolder placeholder : getPlaceholdersStream()) {
            String placeholderId = placeholder.getPlaceholderId();
            if ("".equals(placeholderId)) {
                continue;
            }
            List<PlaceholderHolder> placeholderHolders;
            if (ids.containsKey(placeholderId)) placeholderHolders = ids.get(placeholderId);
            else {
                placeholderHolders = new ArrayList<>();
                ids.put(placeholderId, placeholderHolders);
            }
            placeholderHolders.add(placeholder);
        }

        Map<String, List<PlaceholderHolder>> result = new HashMap<>();

        for (Map.Entry<String, List<PlaceholderHolder>> entry : ids.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public List<PlaceholderHolder> getPlaceholdersStream() {
        List<PlaceholderHolder> list = new ArrayList<>(getPowers());
        list.addAll(getConditions());
        list.addAll(getMarkers());
        return list;
    }

    public void addDescription(String str) {
        getDescription().add(ColorHelper.parseColor(str));
        rebuild();
    }

    public void toggleBar() {
        setHasDurabilityBar(!isHasDurabilityBar());
        rebuild();
    }

    public BaseComponent getComponent(CommandSender sender) {
        String locale = RPGItems.plugin.cfg.language;
        if (sender instanceof Player) {
            locale = ((Player) sender).getLocale();
        }
        return getComponent(sender instanceof Player ? (Player) sender : null, locale);
    }

    public BaseComponent getComponent(String locale) {
        return getComponent(null, locale);
    }
    public BaseComponent getComponent(@Nullable Player player, String locale) {
        BaseComponent msg = new TextComponent(getDisplayName());
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/rpgitem " + getName()));
        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_ITEM,
                new BaseComponent[]{new TextComponent(ItemStackUtils.itemToJson(toItemStack(player)))});
        msg.setHoverEvent(hover);
        return msg;
    }

    private <TEvent extends Event, T extends Pimpl, TResult, TReturn> List<T> getPower(List<Power> powers, Trigger<TEvent, T, TResult, TReturn> trigger, Player player, ItemStack stack) {
        return powers.stream()
                .filter(p -> p.getTriggers().contains(trigger) || BaseTriggers.CUSTOM_TRIGGER.filter(p, trigger, stack))
                .map(p -> {
                    Class<? extends Power> cls = p.getClass();
                    Power proxy = Interceptor.create(p, player, stack, trigger);
                    return PowerManager.createImpl(cls, proxy);
                })
                .map(p -> p.cast(trigger.getPowerClass()))
                .collect(Collectors.toList());
    }

    public PlaceholderHolder getPlaceholderHolder(String placeholderId) {
        return placeholders.get(placeholderId);
    }

    public PlaceholderHolder replacePlaceholder(String powerId, PlaceholderHolder placeHolder) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        PlaceholderHolder oldPh = placeholders.get(powerId);
        PlaceholderHolder newPh = null;
        if (oldPh == null){
            return null;
        }
        placeHolder.save(yamlConfiguration);
        if (oldPh instanceof Power){
            int i = powers.indexOf(oldPh);
            if (i == -1){
                throw new IllegalStateException();
            }
            Class<? extends Power> pwClz = ((Power) oldPh).getClass();
            Power pow = PowerManager.instantiate(pwClz);
            pow.init(yamlConfiguration, getName());
            powers.set(i, pow);
            newPh = pow;
        } else if (oldPh instanceof Condition) {
            int i = conditions.indexOf(oldPh);
            if (i == -1){
                throw new IllegalStateException();
            }
            Class<? extends Condition> pwClz = ((Condition<?>) oldPh).getClass();
            Condition<?> pow = PowerManager.instantiate(pwClz);
            pow.init(yamlConfiguration, getName());
            conditions.set(i, pow);
            newPh = pow;
        } else if (oldPh instanceof Marker) {
            int i = markers.indexOf(oldPh);
            if (i == -1){
                throw new IllegalStateException();
            }
            Class<? extends Marker> pwClz = ((Marker) oldPh).getClass();
            Marker pow = PowerManager.instantiate(pwClz);
            pow.init(yamlConfiguration, getName());
            markers.set(i, pow);
            newPh = pow;
        }
        if (newPh == null) {
            return null;
        }

        NamespacedKey remove = keys.remove(oldPh);
        keys.put(newPh, remove);
        placeholders.put(powerId, newPh);
        return newPh;
    }



    public void updateFromTemplate(RPGItem target) throws UnknownPowerException {
        Set<String> templatePlaceHolders = target.getTemplatePlaceHolders();
        Map<String, List<String>> powerMap = new LinkedHashMap<>();
        Map<String, Object> valMap = new LinkedHashMap<>();
        //extract original val from self
        templatePlaceHolders.forEach(s -> {
            String[] split = s.split(":");
            PlaceholderHolder power = getPlaceholderHolder(split[0]);
            String propName = split[1];
            Object origVal;
            try {
                origVal = getPropVal(power.getClass(), propName, power);
                List<String> strings = powerMap.computeIfAbsent(split[0], (str) -> new ArrayList<>());
                strings.add(s);
                valMap.put(s, origVal);
            } catch (IllegalAccessException e) {
                StringWriter sw = new StringWriter();
                try (PrintWriter pw = new PrintWriter(sw)) {
                    e.printStackTrace(pw);
                }
                plugin.getLogger().warning(sw.toString());
                throw new RuntimeException();
            }
        });

        copyFromTemplate(target);

        //replace powers & fill placeholders
        for (PlaceholderHolder power : target.getPlaceholdersStream()) {
            String powerId = power.getPlaceholderId();
            PlaceholderHolder replaced = replacePlaceholder(powerId, power);
            List<String> strings = powerMap.get(powerId);
            if (strings != null) {
                for (String s : strings) {
                    String[] split = s.split(":");
                    String propName = split[1];
                    Object origVal = valMap.get(s);
                    try {
                        setPropVal(replaced.getClass(), propName, replaced, origVal);
                    } catch (IllegalAccessException e) {
                        StringWriter sw = new StringWriter();
                        try (PrintWriter pw = new PrintWriter(sw)) {
                            e.printStackTrace(pw);
                        }
                        plugin.getLogger().warning(sw.toString());
                        throw new RuntimeException();
                    }
                }
            }
        }

        ItemManager.save(this);
    }

    private Object getPropVal(Class<?> aClass, String propName, PlaceholderHolder placeholder) throws IllegalAccessException {
        Field getMethod = getField(aClass, propName);
        getMethod.setAccessible(true);
        return getMethod.get(placeholder);
    }

    private void setPropVal(Class<?> aClass, String propName, PlaceholderHolder placeholder, Object value) throws IllegalAccessException {
        Field getMethod = getField(aClass, propName);
        getMethod.setAccessible(true);
        getMethod.set(placeholder, value);
    }


    private Field getField(Class<?> aClass, String methodName) {
        Field getMethod;
        while (true){
            try{
                getMethod = aClass.getDeclaredField(methodName);
                break;
            }catch (NoSuchFieldException e){
                aClass = aClass.getSuperclass();
            }
            if (aClass == null){
                throw new RuntimeException("invalid placeholder");
            }
        }
        return getMethod;
    }

    private void copyFromTemplate(RPGItem target) throws UnknownPowerException {
        List<Power> powers = new ArrayList<>(getPowers());
        List<Marker> markers =  new ArrayList<>(getMarkers());
        List<Condition<?>> conditions =  new ArrayList<>(getConditions());
        boolean isTemplate = isTemplate();
        Set<String> templates =  new HashSet<>(getTemplates());
        placeholders.clear();
        this.powers.clear();
        this.markers.clear();
        this.conditions.clear();

        //copy other settings,
        YamlConfiguration config = new YamlConfiguration();
        target.save(config);
        this.restore(config);

        this.powers = powers;
        this.markers = markers;
        this.conditions = conditions;
        this.isTemplate = isTemplate;
        this.templates = templates;
        this.rebuildPlaceholder();
    }

    private void rebuildPlaceholder() {
        placeholders.clear();
        for (PlaceholderHolder placeholderHolder : getPlaceholdersStream()) {
            placeholders.put(placeholderHolder.getPlaceholderId(), placeholderHolder);
        }
    }

    public List<String> getTemplatePlaceholders() {
        return new ArrayList<>(templatePlaceholders);
    }

    public void addTrigger(String name, Trigger trigger) {
        triggers.put(name, trigger);
    }

    public void deinit() {
        powers.forEach(Power::deinit);
    }

    public void setArmour(int a) {
        setArmour(a, true);
    }

    public void setArmour(int a, boolean update) {
        armour = a;
        if (update) rebuild();
    }

    public void setArmourProjectile(int a) {
        setArmourProjectile(a, true);
    }

    public void setArmourProjectile(int a, boolean update) {
        armourProjectile = a;
        if (update) rebuild();
    }

    public int getDamageMaxMythic() {
        return damageMaxMythic < 0 ? damageMax : damageMaxMythic;
    }

    private void setDamageMax(int damageMax) {
        this.damageMax = damageMax;
    }

    private void setDamageMaxMythic(int damageMaxMythic) {
        this.damageMaxMythic = damageMaxMythic;
    }

    public int getDamageMinMythic() {
        return damageMinMythic < 0 ? damageMin : damageMinMythic;
    }

    private void setDamageMin(int damageMin) {
        this.damageMin = damageMin;
    }

    private void setDamageMinMythic(int damageMinMythic) {
        this.damageMinMythic = damageMinMythic;
    }

    public void setDamageMythic(int min, int max) {
        setDamageMinMythic(min);
        setDamageMaxMythic(max);
    }

    public void setDamage(int min, int max, boolean command) {
        if (command) {
            damageMinPlayer = min;
            damageMaxPlayer = max;
        }
        setDamageMin(min);
        setDamageMax(max);
        rebuild();
    }

    public void setDamage(int min, int max) {
        setDamage(min, max, false);
    }

    public double getCriticalRate(LivingEntity damager) {
        AtomicDouble rate = new AtomicDouble(getCriticalRate());
        EntityEquipment equipment = damager.getEquipment();
        if (equipment != null) {
            Consumer<RPGItem> func = rpg -> {
                if (rpg.getCriticalArmorRate() != 0) {
                    rate.addAndGet(rpg.getCriticalArmorRate());
                }
            };
            ItemManager.toRPGItem(equipment.getHelmet()).ifPresent(func);
            ItemManager.toRPGItem(equipment.getChestplate()).ifPresent(func);
            ItemManager.toRPGItem(equipment.getLeggings()).ifPresent(func);
            ItemManager.toRPGItem(equipment.getBoots()).ifPresent(func);
            ItemManager.toRPGItem(equipment.getItemInOffHand()).filter(it -> it.getItem().equals(SHIELD)).ifPresent(func);
        }
        return rate.get();
    }

    public double getCriticalBackRate(LivingEntity damager) {
        AtomicDouble rate = new AtomicDouble(getCriticalBackRate());
        EntityEquipment equipment = damager.getEquipment();
        if (equipment != null) {
            Consumer<RPGItem> func = rpg -> {
                if (rpg.getCriticalBackArmorRate() != 0) {
                    rate.addAndGet(rpg.getCriticalBackArmorRate());
                }
            };
            ItemManager.toRPGItem(equipment.getHelmet()).ifPresent(func);
            ItemManager.toRPGItem(equipment.getChestplate()).ifPresent(func);
            ItemManager.toRPGItem(equipment.getLeggings()).ifPresent(func);
            ItemManager.toRPGItem(equipment.getBoots()).ifPresent(func);
            ItemManager.toRPGItem(equipment.getItemInOffHand()).filter(it -> it.getItem().equals(SHIELD)).ifPresent(func);
        }
        return rate.get();
    }

    public double getCriticalAntiRate(LivingEntity entity) {
        AtomicDouble rate = new AtomicDouble(getCriticalAntiRate());
        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            Consumer<RPGItem> func = rpg -> {
                if (rpg.getCriticalBackArmorRate() != 0) {
                    rate.addAndGet(rpg.getCriticalAntiArmorRate());
                }
            };
            ItemManager.toRPGItem(equipment.getHelmet()).ifPresent(func);
            ItemManager.toRPGItem(equipment.getChestplate()).ifPresent(func);
            ItemManager.toRPGItem(equipment.getLeggings()).ifPresent(func);
            ItemManager.toRPGItem(equipment.getBoots()).ifPresent(func);
            ItemManager.toRPGItem(equipment.getItemInOffHand()).filter(it -> it.getItem().equals(SHIELD)).ifPresent(func);
        }
        return rate.get();
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

    public void setDurabilityBound(int min, int max) {
        setDurabilityLowerBound(min);
        setDurabilityUpperBound(max);
    }

    public MessageType getDodgeMessageType() {
        if (dodgeMessageType == null) dodgeMessageType = MessageType.TITLE;
        return dodgeMessageType;
    }

    void setFile(File itemFile) {
        file = itemFile;
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    private void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void setMaxDurability(int newVal) {
        maxDurability = newVal <= 0 ? -1 : newVal;
        setDefaultDurability(maxDurability);
    }

    public String getPermission() {
        return Strings.isNullOrEmpty(permission) ? "rpgitems.item.use." + getName() : permission;
    }

    @Override
    public NamespacedKey getPropertyHolderKey(PropertyHolder power) {
        return Objects.requireNonNull(keys.get(power));
    }

    @Override
    public NamespacedKey removePropertyHolderKey(PropertyHolder power) {
        return Objects.requireNonNull(keys.remove(power));
    }

    public boolean isTemplateOf(String templateName){
        return templates.contains(templateName);
    }

    public void setTemplateOf(String templateName){
        this.templates.add(templateName);
    }

    public void setTemplatePlaceHolders(List<String> placeHolder) {
        this.templatePlaceholders.clear();
        this.templatePlaceholders.addAll(placeHolder);
    }

    public void addTemplatePlaceHolder(String placeHolder){
        this.templatePlaceholders.add(placeHolder);
    }

    public void removeTemplatePlaceHolder(String placeHolder){
        this.templatePlaceholders.remove(placeHolder);
    }

    private Set<String> getTemplatePlaceHolders() {
        return templatePlaceholders;
    }

    public enum DamageMode {
        FIXED,
        VANILLA,
        ADDITIONAL,
        MULTIPLY,
    }

    public enum EnchantMode {
        DISALLOW,
        PERMISSION,
        ALLOW
    }

    public enum AttributeMode {
        FULL_UPDATE, PARTIAL_UPDATE
    }

    public enum BarFormat {
        DEFAULT,
        NUMERIC,
        NUMERIC_MINUS_ONE,
        NUMERIC_HEX,
        NUMERIC_HEX_MINUS_ONE,
        NUMERIC_BIN,
        NUMERIC_BIN_MINUS_ONE,
    }
}
