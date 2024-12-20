package think.rpgitems;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import de.tr7zw.changeme.nbtapi.NBT;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.Nullable;
import think.rpgitems.api.IFactorDefiner;
import think.rpgitems.commands.AdminCommands;
import think.rpgitems.commands.UserCommands;
import think.rpgitems.data.Font;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.*;
import think.rpgitems.power.trigger.BaseTriggers;
import think.rpgitems.support.MythicSupport;
import think.rpgitems.support.PlaceholderSupport;
import think.rpgitems.support.ProtocolListener;
import think.rpgitems.support.WGSupport;
import think.rpgitems.utils.cast.PluginUtils;
import think.rpgitems.utils.nms.NMS;
import think.rpgitems.utils.nyaacore.NyaaCoreLoader;
import think.rpgitems.utils.prompt.PromptManager;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static think.rpgitems.utils.cast.PluginUtils.stackTraceToString;

public final class RPGItems extends JavaPlugin implements PluginMessageListener {

    @lombok.Getter
    private static int version;
    @lombok.Getter
    private static int serial;
    @lombok.Getter
    private static String serverMCVersion;
    private static boolean hasNetherite;
    private static boolean hasProtocolLib;
    private static boolean isPaper;
    public static boolean isNetheriteAvailable()  {
        return hasNetherite;
    }
    public static boolean protocolLibAvailable() {
        return hasProtocolLib;
    }
    public static boolean isPaper() {
        return isPaper;
    }

    public static Logger logger;
    public static RPGItems plugin;
    private static BukkitAudiences adventure;
    public static BukkitAudiences adv() {
        return adventure;
    }

    List<Plugin> managedPlugins = new ArrayList<>();
    public Configuration cfg;
    public GuiManager gui;
    public PromptManager prompt;
    private final NyaaCoreLoader nyaaCoreLoader = new NyaaCoreLoader(this);
    public final List<Runnable> disableHook = new ArrayList<>();
    public final MagicValues magic = new MagicValues(this);

    @SuppressWarnings({"unchecked", "JavaReflectionInvocation"})
    public static <T> T forceWrap(final PowerPlain obj, final Class<T> implInterface) {
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            if (!method.getName().equals("fire") && args != null && args[0] instanceof Player && args[1] instanceof RPGItem && args[2] instanceof ItemStack) {
                return obj.getClass().getDeclaredMethod("fire", Player.class, RPGItem.class, ItemStack.class).invoke(obj, args[0], args[1], args[2]);
            }
            return obj.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(obj, args);
        };
        return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{implInterface}, invocationHandler);
    }
    @SuppressWarnings({"unchecked", "JavaReflectionInvocation"})
    private static <T> T getWrapper(final PowerPlain obj, final Class<T> implInterface, final String delegateMethod) {
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            if (!method.getName().equals(delegateMethod)) {
                return obj.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(obj, args);
            } else {
                return obj.getClass().getDeclaredMethod("fire", Player.class, RPGItem.class, ItemStack.class).invoke(obj, args[0], args[1], args[2]);
            }
        };
        return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{implInterface}, invocationHandler);
    }

    public List<Plugin> ext() {
        return Collections.unmodifiableList(managedPlugins);
    }

    @Override
    public void onLoad() {
        plugin = this;
        NMS.init(logger = this.getLogger());
        NBT.preloadApi();
        nyaaCoreLoader.onLoad();

        String versionDesc = getDescription().getVersion();
        Pattern serialPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        Matcher serialMatcher = serialPattern.matcher(versionDesc);

        if (serialMatcher.matches()) {
            version = Integer.parseInt(serialMatcher.group(1)) * 100 + Integer.parseInt(serialMatcher.group(2));
            serial = Integer.parseInt(serialMatcher.group(3));
        }

        isPaper = PluginUtils.isClassPresent("com.destroystokyo.paper.event.player.PlayerArmorChangeEvent");

        Matcher m = Pattern.compile("\\(MC: (1.[0-9.]+)\\)").matcher(Bukkit.getVersion());
        serverMCVersion = m.find() ? m.group(1) : NMS.getVersion();

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

    private static <T extends Pimpl> void registerFallbackAdapter(Class<T> powerClass, String method) {
        PowerManager.registerAdapter(PowerPlain.class, powerClass, p -> getWrapper(p, powerClass, method));
    }

    private static <T extends Pimpl> void registerFallbackAdapter(Class<T> powerInterface) {
        if (!powerInterface.isInterface()) return;
        for (Method method : powerInterface.getDeclaredMethods()) {
            Class<?>[] args = method.getParameterTypes();
            if (args.length < 3) continue;
            if (args[0].isAssignableFrom(Player.class)
            && args[1].isAssignableFrom(RPGItem.class)
            && args[2].isAssignableFrom(ItemStack.class)) {
                registerFallbackAdapter(powerInterface, method.getName());
                break;
            }
        }
    }

    void loadPowers() {
        PowerManager.clear();
        logger.log(Level.INFO, "Loading powers...");
        BaseTriggers.load();

        Lists.newArrayList( // register power fallback adapters
                PowerAttachment.class, PowerBowShoot.class, PowerEntity.class,
                PowerHit.class, PowerHitTaken.class, PowerHurt.class, PowerLeftClick.class,
                PowerLivingEntity.class, PowerLocation.class, PowerMainhandItem.class,
                PowerOffhandClick.class, PowerOffhandItem.class, PowerProjectileHit.class,
                PowerProjectileLaunch.class, PowerRightClick.class, PowerSneak.class, PowerSneaking.class,
                PowerSprint.class, PowerTick.class
        ).forEach(RPGItems::registerFallbackAdapter);

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
                for (RPGItem rpg : ItemManager.getEquipments(entity).values()) {
                    add(appearTimes, rpg);
                }
                if (appearTimes.isEmpty()) return null;

                String factor = cfg.factorConfig.getConflictOverride(appearTimes.keySet());
                if (factor != null) return factor;

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
                    if (plugin != null) {
                        if (!PluginUtils.isClassPresent("io.papermc.paper.plugin.storage.ServerPluginProviderStorage")) {
                            String message = String.format("Loading %s", plugin.getDescription().getFullName());
                            plugin.getLogger().info(message);
                            plugin.onLoad();
                        }
                        managedPlugins.add(plugin);
                        logger.info("Loaded extension: " + plugin.getName());
                    }
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
        plugin = this;
        if (plugin.cfg.version.startsWith("0.") && Double.parseDouble(plugin.cfg.version) < 0.5) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "======================================");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You current version of RPGItems config is not supported.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Please run your server with latest version of RPGItems 3.5 before update.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "======================================");
            throw new IllegalStateException();
        }
        hasNetherite = Material.getMaterial("NETHERITE_INGOT") != null;
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
            PluginCommand command = getCommand("rpgitem");
            if (command != null) command.setExecutor((sender, cmd, label, args) -> {
                sender.sendMessage(ChatColor.RED + "======================================");
                sender.sendMessage(ChatColor.RED + "RPGItems plugin requires Spigot API, Please make sure you are using Spigot.");
                sender.sendMessage(ChatColor.RED + "======================================");
                return true;
            });
            return;
        }

        gui = new GuiManager(this);
        prompt = new PromptManager(this);
        magic.onEnable();

        new AdminCommands(this).registerToBukkit(getCommand("rpgitem"));
        new UserCommands(this).registerToBukkit(getCommand("rpgitems"));

        if (getServer().getPluginManager().isPluginEnabled("MythicMobs")) {
            MythicSupport.registerEvents(this);
        }
        hasProtocolLib = getServer().getPluginManager().isPluginEnabled("ProtocolLib");
        if (hasProtocolLib) {
            new ProtocolListener(this);
        }
        ServerLoadListener serverLoadListener = new ServerLoadListener();
        if (getServer().getOnlinePlayers().isEmpty()) {
            getServer().getPluginManager().registerEvents(serverLoadListener, this);
        }
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        managedPlugins.forEach(getServer().getPluginManager()::enablePlugin);
        if (!getServer().getOnlinePlayers().isEmpty()) {
            serverLoadListener.load();
        }
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
            load();
        }
        public void load() {
            HandlerList.unregisterAll(this);
            getServer().getPluginManager().registerEvents(new Events(), RPGItems.this);
            WGSupport.init(RPGItems.this);
            PlaceholderSupport.init(RPGItems.this);
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
        unregisterCommand("rpgitem");
        unregisterCommand("rpgitems");
        this.getServer().getScheduler().cancelTasks(plugin);
        ItemManager.unload();
        for (Plugin plugin : managedPlugins) {
            PluginUtils.unload(plugin);
        }
        managedPlugins.clear();
        nyaaCoreLoader.onDisable();
        if (gui != null) gui.onDisable();
        for (Runnable runnable : disableHook) {
            runnable.run();
        }
        disableHook.clear();
    }

    private void unregisterCommand(String name) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(null);
            command.setTabCompleter(null);
        }
    }

    /**
     * 保存资源到 RPGItems 文件夹
     * @param ext 资源所在插件实例
     * @param name 文件名
     * @return 保存失败时返回 false，保存成功或文件已存在时返回 true
     */
    public static boolean saveResource(JavaPlugin ext, String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            InputStream resource = ext.getResource(name);
            return resource != null && save(file, resource);
        }
        return true;
    }

    private static boolean save(File file, InputStream resource) {
        try (FileOutputStream os = new FileOutputStream(file)) {
            try (resource) {
                byte[] buffer = new byte[1024 * 10];
                int len;
                while ((len = resource.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning(stackTraceToString(e));
        }
        return false;
    }
}
