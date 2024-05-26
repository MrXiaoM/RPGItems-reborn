package think.rpgitems;

import think.rpgitems.utils.nyaacore.LanguageRepository;
import think.rpgitems.utils.nyaacore.utils.HexColorUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.Map;

public class I18n extends LanguageRepository {
    private static final Map<String, I18n> instances = new HashMap<>();
    private final RPGItems plugin;
    private final String lang;

    protected Map<String, String> map = new HashMap<>();

    public I18n(RPGItems plugin, String lang) {
        instances.put(lang.toLowerCase(), this);
        this.plugin = plugin;
        this.lang = lang;
        loadResourceLanguage(lang);
        save(lang + ".template");
        loadLocalLanguage(lang + ".custom");
    }

    /**
     * Save language file back to disk using given file name
     */
    public void save(String fileName) {
        Plugin plugin = getPlugin();
        File localLangFile = new File(plugin.getDataFolder(), fileName + ".yml");
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            for (String key : map.keySet()) {
                yaml.set(key, map.get(key));
            }
            yaml.save(localLangFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Cannot save language file: " + fileName + ".yml");
        }
    }

    /**
     * add all language items from section into language map recursively
     * overwrite existing items
     * The '&' will be transformed to color code.
     *
     * @param section        source section
     * @param prefix         used in recursion to determine the proper prefix
     */
    private static void loadLanguageSection(Map<String, String> map, ConfigurationSection section, String prefix) {
        if (map == null || section == null || prefix == null) return;
        for (String key : section.getKeys(false)) {
            String path = prefix + key;
            if (section.isString(key)) {
                map.put(path, HexColorUtils.hexColored(section.getString(key)));
            } else if (section.isList(key)) {
                String s = String.join("\n&r", section.getStringList(key));
                map.put(path, HexColorUtils.hexColored(s));
            } else if (section.isConfigurationSection(key)) {
                loadLanguageSection(map, section.getConfigurationSection(key), path + ".");
            }
        }
    }


    // helper function to load language map
    private static void loadResourceMap(Plugin plugin, String codeName,
                                        Map<String, String> targetMap) {
        if (plugin == null || codeName == null || targetMap == null) throw new IllegalArgumentException();
        InputStream stream = plugin.getResource("lang/" + codeName + ".yml");
        if (stream != null) {
            YamlConfiguration section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
            loadLanguageSection(targetMap, section, "");
        }
    }

    // helper function to load language map
    private static void loadLocalMap(Plugin plugin, String codeName,
                                     Map<String, String> targetMap) {
        if (plugin == null || codeName == null || targetMap == null) throw new IllegalArgumentException();
        if (Boolean.parseBoolean(System.getProperty("nyaautils.i18n.refreshLangFiles", "false"))) return;
        File langFile = new File(plugin.getDataFolder(), codeName + ".yml");
        if (langFile.exists() && langFile.isFile()) {
            YamlConfiguration section = YamlConfiguration.loadConfiguration(langFile);
            loadLanguageSection(targetMap, section, "");
        }
    }

    /**
     * Load specified resource language map
     */
    protected void loadResourceLanguage(String fileName) {
        loadResourceMap(getPlugin(), fileName, map);
    }

    /**
     * Load specified local language map
     */
    protected void loadLocalLanguage(String fileName) {
        loadLocalMap(getPlugin(), fileName, map);
    }

    public static I18n getInstance(CommandSender sender) {
        return getInstance((sender instanceof Player) ? ((Player) sender).getLocale() : RPGItems.plugin.cfg.language);
    }

    public String format(String key, Object... args) {
        return getFormatted(key, args);
    }

    public static String formatDefault(String key, Object... args) {
        return getInstance(RPGItems.plugin.cfg.language).getFormatted(key, args);
    }

    public static I18n getInstance(String lang) {
        return instances.getOrDefault(lang.toLowerCase(), instances.get(RPGItems.plugin.cfg.language.toLowerCase()));
    }

    public static String getFormatted(CommandSender sender, String key, Object... para) {
        I18n instance = getInstance(sender);
        return instance.getFormatted(key, para);
    }

    /**
     * Get the language item then format with `para` by {@link String#format(String, Object...)}
     */
    @Override
    public String getFormatted(String key, Object... para) {
        String val = map.get(key);
        if (val == null) {
            return super.getFormatted(key, para);
        } else {
            try {
                return String.format(val, para);
            } catch (IllegalFormatConversionException e) {
                StringWriter sw = new StringWriter();
                try (PrintWriter pw = new PrintWriter(sw)) {
                    e.printStackTrace(pw);
                }
                getPlugin().getLogger().warning(sw.toString());
                getPlugin().getLogger().warning("Corrupted language key: " + key);
                getPlugin().getLogger().warning("val: " + val);
                StringBuilder keyBuilder = new StringBuilder();
                for (Object obj : para) {
                    keyBuilder.append("#<").append(obj.toString()).append(">");
                }
                String params = keyBuilder.toString();
                getPlugin().getLogger().warning("params: " + params);
                return "CORRUPTED_LANG<" + key + ">" + params;
            }
        }
    }

    @Override
    public boolean hasKey(String key) {
        if (map.containsKey(key)) return true;
        return super.hasKey(key);
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    protected String getLanguage() {
        return lang;
    }
}
