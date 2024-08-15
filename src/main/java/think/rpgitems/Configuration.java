package think.rpgitems;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import think.rpgitems.data.Factor;
import think.rpgitems.data.FactorConfig;
import think.rpgitems.utils.nyaacore.Pair;
import think.rpgitems.utils.nyaacore.configuration.PluginConfigure;
import org.bukkit.plugin.java.JavaPlugin;
import think.rpgitems.item.RPGItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Configuration extends PluginConfigure {
    private final RPGItems plugin;

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    public Configuration(RPGItems plugin) {
        this.plugin = plugin;
    }

    @Serializable
    public String language = "zh_CN";

    @Serializable
    public String version = "1.0";

    @Serializable(name = "general.enabled_languages")
    public List<String> enabledLanguages = Stream.of("en_US", "zh_CN").collect(Collectors.toList());

    @Serializable(name = "general.spu_endpoint")
    public String spuEndpoint = null;

    @Serializable(name = "general.items_dir_redirect")
    public String itemsDirRedirect = null;

    @Serializable(name = "general.readonly")
    public boolean readonly = false;
    @Serializable(name = "general.reload-notice-readonly-server")
    public boolean readonlyReloadNotice = true;

    @Serializable(name = "command.list.item_per_page", alias = "itemperpage")
    public int itemPerPage = 9;

    @Serializable(name = "command.list.power_per_page")
    public int powerPerPage = 5;

    @Serializable(name = "support.world_guard.enable", alias = "support.worldguard")
    public boolean useWorldGuard = true;

    @Serializable(name = "support.world_guard.force_refresh", alias = "support.wgforcerefresh")
    public boolean wgForceRefresh = false;

    @Serializable(name = "support.world_guard.disable_in_no_pvp")
    public boolean wgNoPvP = true;

    @Serializable(name = "support.world_guard.show_warning")
    public boolean wgShowWarning = true;

    @Serializable(name = "support.placeholder_api.enable")
    public boolean usePlaceholderAPI = true;

    @Serializable(name = "support.protocol_lib.enable")
    public boolean useProtocolLib = true;

    @Serializable(name = "support.protocol_lib.auto_replace_armor_material_to_netherite_or_diamond")
    public boolean plAutoReplaceArmorMaterial = true;

    @Serializable(name = "general.give_perms", alias = "give-perms")
    public boolean givePerms = false;

    @Serializable(name = "gist.token", alias = "githubToken")
    public String githubToken = "";

    @Serializable(name = "gist.publish", alias = "publishGist")
    public boolean publishGist = true;

    @Serializable(name = "item.defaults.numeric_bar", alias = "numericBar")
    public boolean numericBar = false;

    @Serializable(name = "item.defaults.force_bar", alias = "forceBar")
    public boolean forceBar = false;

    @Serializable(name = "item.defaults.license")
    public String defaultLicense = "All Right Reserved";

    @Serializable(name = "item.defaults.enchant_mode")
    public RPGItem.EnchantMode defaultEnchantMode = RPGItem.EnchantMode.DISALLOW;

    @Serializable(name = "item.defaults.allow_anvil_enchant")
    public boolean allowAnvilEnchant = true;

    @Serializable(name = "item.defaults.note")
    public String defaultNote;

    @Serializable(name = "item.defaults.author")
    public String defaultAuthor;

    @Serializable(name = "stone.max_count")
    public int stoneMaxCount = 3;

    @Serializable(name = "general.item.fs_lock")
    public boolean itemFsLock = false;

    @Serializable(name = "general.item.show_loaded")
    public boolean itemShowLoaded = false;

    // enable for better performance
    // note: all new given items will not stack
    // and can not be used in trades!
    @Serializable(name = "general.item.item_stack_uuid")
    public boolean itemStackUuid = true;

    @SuppressWarnings("unused")
    @Serializable(name = "unused.locale_inv", alias = {"general.locale_inv", "localeInv"})
    public boolean oldLocaleInv = false;

    @Serializable(name = "item.quality")
    public Map<String, String> qualityPrefixes = new HashMap<>();
    {
        qualityPrefixes.put("trash", "&7");
        qualityPrefixes.put("normal", "&f");
        qualityPrefixes.put("rare", "&b");
        qualityPrefixes.put("epic", "&3");
        qualityPrefixes.put("legendary", "&e");
    }

    @Serializable(name = "item.magic.total")
    public Map<String, Integer> magicProperties = new HashMap<>();
    {
        magicProperties.put("default", 50);
    }

    @Serializable(name = "item.magic.bossbar.color")
    public BarColor magicColor = BarColor.GREEN;

    @Serializable(name = "item.magic.bossbar.style")
    public BarStyle magicStyle = BarStyle.SEGMENTED_10;

    @Serializable(name = "factor_config")
    public FactorConfig factorConfig = new FactorConfig();
    {
        factorConfig.addFactor(new Factor("machine", "&bMachine", mapOf(
                Pair.of("creature", "damage * 1.2"),
                Pair.of("supernatural", "damage * 0.8"),

                Pair.of("weak", "damage * 1.8")
        )));
        factorConfig.addFactor(new Factor("creature", "&eCreature", mapOf(
                Pair.of("supernatural", "damage * 1.2"),
                Pair.of("machine", "damage * 0.8"),

                Pair.of("weak", "damage * 1.8")
        )));
        factorConfig.addFactor(new Factor("supernatural", "&dSuper Natural", mapOf(
                Pair.of("machine", "damage * 1.2"),
                Pair.of("creature", "damage * 0.8"),

                Pair.of("weak", "damage * 1.8")
        )));
        factorConfig.addFactor(new Factor("weak", "&cWeak", mapOf(
                Pair.of("machine", "damage * 0.2"),
                Pair.of("creature", "damage * 0.2"),
                Pair.of("supernatural", "damage * 0.2")
        )));

        factorConfig.addConflictOverride(List.of("machine", "creature"), "weak");
        factorConfig.addConflictOverride(List.of("machine", "supernatural"), "weak");
        factorConfig.addConflictOverride(List.of("creature", "supernatural"), "weak");
    }

    @SafeVarargs
    public static <V> Map<String, V> mapOf(Pair<String, V>... pairs) {
        Map<String, V> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Pair<String, V> pair : pairs) {
            map.put(pair.getKey(), pair.getValue());
        }
        return map;
    }
}
