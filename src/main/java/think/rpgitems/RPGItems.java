package think.rpgitems;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.Nullable;
import think.rpgitems.api.IFactorDefiner;
import think.rpgitems.commands.AdminCommands;
import think.rpgitems.item.RPGItem;
import think.rpgitems.utils.nms.NMS;
import think.rpgitems.utils.nyaacore.NyaaCoreLoader;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import think.rpgitems.data.Font;
import think.rpgitems.item.ItemManager;
import think.rpgitems.power.*;
import think.rpgitems.power.trigger.BaseTriggers;
import think.rpgitems.power.trigger.Trigger;
import think.rpgitems.support.WGSupport;
import think.rpgitems.utils.cast.PluginUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RPGItems extends JavaPlugin implements PluginMessageListener {

    private static int version;
    private static int serial;
    private static String serverMCVersion;

    public static Logger logger;
    public static RPGItems plugin;

    List<Plugin> managedPlugins = new ArrayList<>();
    public Configuration cfg;
    private final NyaaCoreLoader nyaaCoreLoader = new NyaaCoreLoader(this);


    public static int getVersion() {
        return version;
    }

    public static int getSerial() {
        return serial;
    }

    public static String getServerMCVersion() {
        return serverMCVersion;
    }

    @SuppressWarnings({"unchecked", "JavaReflectionInvocation"})
    private static <T> T getWrapper(final PowerPlain obj, final Class<T> implInterface, final String delegateMethod) {
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            if (!method.getName().equals(delegateMethod)) {
                return obj.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(obj, args);
            } else {
                return obj.getClass().getDeclaredMethod("fire", Player.class, ItemStack.class).invoke(obj, args[0], args[1]);
            }
        };
        return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{implInterface}, invocationHandler);
    }
    @Override
    public void onLoad() {
        plugin = this;
        NMS.init(logger = this.getLogger());
        nyaaCoreLoader.onLoad();

        String versionDesc = getDescription().getVersion();
        Pattern serialPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        Matcher serialMatcher = serialPattern.matcher(versionDesc);

        if (serialMatcher.matches()) {
            version = Integer.parseInt(serialMatcher.group(1)) * 100 + Integer.parseInt(serialMatcher.group(2));
            serial = Integer.parseInt(serialMatcher.group(3));
        }

        serverMCVersion = Bukkit.getMinecraftVersion(); // Paper

        cfg = new Configuration(this);
        cfg.load();
        cfg.enabledLanguages.forEach(lang -> new I18n(this, lang));
        cfg.enabledLanguages.forEach(lang ->
                PowerManager.addDescriptionResolver(RPGItems.plugin, lang, (power, property) -> {
                    I18n i18n = I18n.getInstance(lang);
                    if (property == null) {
                        String powerKey = "properties." + power.getKey() + ".main_description";
                        return i18n.getFormatted(powerKey);
                    }
                    String key = "properties." + power.getKey() + "." + property;
                    if (i18n.hasKey(key)) {
                        return i18n.getFormatted(key);
                    }
                    String baseKey = "properties.base." + property;
                    if (i18n.hasKey(baseKey)) {
                        return i18n.getFormatted(baseKey);
                    }
                    return null;
                }));
        loadPowers();
        saveDefaultConfig();
        Font.load();
        WGSupport.load();
        loadExtensions();
    }

    public void reload() {
        plugin.cfg = new Configuration(plugin);
        plugin.cfg.load();
        plugin.cfg.enabledLanguages.forEach(lang -> new I18n(plugin, lang));
        plugin.loadPowers();
        WGSupport.reload();
        plugin.loadExtensions();
        plugin.managedPlugins.forEach(Bukkit.getPluginManager()::enablePlugin);
        ItemManager.reload(plugin);
    }

    void loadPowers() {
        PowerManager.clear();
        logger.log(Level.INFO, "Loading powers...");
        new BaseTriggers();
        PowerManager.registerAdapter(PowerPlain.class, PowerOffhandClick.class, p -> getWrapper(p, PowerOffhandClick.class, "offhandClick"));
        PowerManager.registerAdapter(PowerPlain.class, PowerSprint.class, p -> getWrapper(p, PowerSprint.class, "sprint"));
        PowerManager.registerAdapter(PowerPlain.class, PowerSneak.class, p -> getWrapper(p, PowerSneak.class, "sneak"));
        PowerManager.registerAdapter(PowerPlain.class, PowerAttachment.class, p -> getWrapper(p, PowerAttachment.class, "attachment"));
        PowerManager.registerConditions(RPGItems.plugin, Power.class.getPackage().getName() + ".cond");
        PowerManager.registerPowers(RPGItems.plugin, Power.class.getPackage().getName() + ".impl");
        PowerManager.registerMarkers(RPGItems.plugin, Power.class.getPackage().getName() + ".marker");
        PowerManager.registerModifiers(RPGItems.plugin, Power.class.getPackage().getName() + ".propertymodifier");
        logger.log(Level.INFO, "Powers loaded.");
    }

    public void loadExtensions() {
        cfg.factorConfig.clearDefiner();
        cfg.factorConfig.addDefiner(new IFactorDefiner() {
            @Override
            public @Nullable String define(LivingEntity entity) {
                EntityEquipment equipment = entity.getEquipment();
                if (equipment == null) return null;
                Map<String, Integer> appearTimes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                add(appearTimes, ItemManager.toRPGItem(equipment.getHelmet()).orElse(null));
                add(appearTimes, ItemManager.toRPGItem(equipment.getChestplate()).orElse(null));
                add(appearTimes, ItemManager.toRPGItem(equipment.getLeggings()).orElse(null));
                add(appearTimes, ItemManager.toRPGItem(equipment.getBoots()).orElse(null));
                add(appearTimes, ItemManager.toRPGItem(equipment.getItemInOffHand()).orElse(null));
                add(appearTimes, ItemManager.toRPGItem(equipment.getItemInMainHand()).orElse(null));
                String factor = null;
                int max = 0;
                for (Map.Entry<String, Integer> entry : appearTimes.entrySet()) {
                    if (entry.getValue() > max) {
                        factor = entry.getKey();
                    }
                }
                return factor;
            }

            private void add(Map<String, Integer> appearTimes, RPGItem rpg) {
                if (rpg == null) return;
                if (rpg.getFactor() == null || rpg.getFactor().trim().isEmpty()) return;
                int times = appearTimes.getOrDefault(rpg.getFactor(), 0);
                appearTimes.put(rpg.getFactor(), times + 1);
            }
        });
        for (Plugin plugin : plugin.managedPlugins) {
            String name = plugin.getName();
            PluginUtils.unload(plugin);
            if (Bukkit.getPluginManager().getPlugin(name) != null) {
                getLogger().warning("Plugin " + name + " unload failed!");
            }
        }
        plugin.managedPlugins.clear();
        File extDir = new File(plugin.getDataFolder(), "ext");
        if (extDir.isDirectory() || extDir.mkdirs()) {
            File[] files = extDir.listFiles((d, n) -> n.endsWith(".jar"));
            if (files == null) return;
            for (File file : files) {
                try {
                    Plugin plugin = Bukkit.getPluginManager().loadPlugin(file);
                    if (!PluginUtils.isClassPresent("io.papermc.paper.plugin.storage.ServerPluginProviderStorage")) {
                        String message = String.format("Loading %s", plugin.getDescription().getFullName());
                        plugin.getLogger().info(message);
                        plugin.onLoad();
                    }
                    managedPlugins.add(plugin);
                    logger.info("Loaded extension: " + plugin.getName());
                } catch (InvalidPluginException | InvalidDescriptionException e) {
                    logger.log(Level.SEVERE, "Error loading extension: " + file.getName(), e);
                }
            }
        } else {
            logger.severe("Error creating extension directory ./ext");
        }
    }

    @Override
    public void onEnable() {
        Trigger.stopAcceptingRegistrations();
        plugin = this;
        if (plugin.cfg.version.startsWith("0.") && Double.parseDouble(plugin.cfg.version) < 0.5) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "======================================");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You current version of RPGItems config is not supported.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Please run your server with latest version of RPGItems 3.5 before update.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "======================================");
            throw new IllegalStateException();
        }
        nyaaCoreLoader.onEnable();

        String implementationVersion = Bukkit.class.getPackage().getImplementationVersion();
        //may null in test environment
        if (implementationVersion != null && implementationVersion.startsWith("git-Bukkit-")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "======================================");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "RPGItems plugin requires Spigot API, Please make sure you are using Spigot.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "======================================");
        }
        try {
            Bukkit.spigot();
        } catch (Throwable e) {
            getCommand("rpgitem").setExecutor((sender, command, label, args) -> {
                sender.sendMessage(ChatColor.RED + "======================================");
                sender.sendMessage(ChatColor.RED + "RPGItems plugin requires Spigot API, Please make sure you are using Spigot.");
                sender.sendMessage(ChatColor.RED + "======================================");
                return true;
            });
        }
        AdminCommands adminCommandHandler = new AdminCommands(this, I18n.getInstance(cfg.language));
        UserHandler userCommandHandler = new UserHandler(this, I18n.getInstance(cfg.language));
        getCommand("rpgitem").setExecutor(adminCommandHandler);
        getCommand("rpgitem").setTabCompleter(adminCommandHandler);
        getCommand("rpgitems").setExecutor(userCommandHandler);
        getCommand("rpgitems").setTabCompleter(userCommandHandler);
        getServer().getPluginManager().registerEvents(new ServerLoadListener(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        managedPlugins.forEach(Bukkit.getPluginManager()::enablePlugin);
    }

    @Override
    @SuppressWarnings({"all"})
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (subChannel.equals("RPGItems")) {
            short len = in.readShort();
            byte[] bytes = new byte[len];
            in.readFully(bytes);

            try (DataInputStream msgIn = new DataInputStream(new ByteArrayInputStream(bytes))) {
                String command = msgIn.readUTF();
                logger.info("Received BungeeCord command: " + command);
                if (command.equalsIgnoreCase("reload") && cfg.readonly) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rpgitem reload");
                }
            } catch (Throwable t){
                t.printStackTrace();
            }
        }
    }
    private class ServerLoadListener implements Listener {
        @EventHandler
        public void onServerLoad(ServerLoadEvent event) {
            HandlerList.unregisterAll(this);
            getServer().getPluginManager().registerEvents(new Events(), RPGItems.this);
            WGSupport.init(RPGItems.this);
            logger.info("Loading RPGItems...");
            ItemManager.load(RPGItems.this);
            logger.info("Done");
            new Ticker().runTaskTimer(RPGItems.this, 0, 0);
        }
    }

    @Override
    public void onDisable() {
        WGSupport.unload();
        HandlerList.unregisterAll(plugin);
        getCommand("rpgitem").setExecutor(null);
        getCommand("rpgitem").setTabCompleter(null);
        this.getServer().getScheduler().cancelTasks(plugin);
        ItemManager.unload();
        for (Plugin plugin : managedPlugins) {
            PluginUtils.unload(plugin);
        }
        managedPlugins.clear();
        nyaaCoreLoader.onDisable();
    }
}
