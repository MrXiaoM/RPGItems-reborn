package think.rpgitems.utils.cast;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import think.rpgitems.RPGItems;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.*;

public class PluginUtils {

    /**
     * Thanks to <a href="https://github.com/TheBlackEntity/PlugManX/blob/a82a91323495e265fe7cf9e3170978ada799da0e/src/main/java/com/rylinaux/plugman/util/PaperPluginUtil.java#L523-L695">PlugManX</a>
     * @param plugin plugin to unload
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void unload(Plugin plugin) {
        String name = plugin.getName();
        PluginManager pluginManager = Bukkit.getPluginManager();

        SimpleCommandMap commandMap;

        List<Plugin> plugins;

        Map<String, Plugin> names;
        Map<String, Command> commands;
        Map<Event, SortedSet<RegisteredListener>> listeners = null;

        HandlerList.unregisterAll(plugin);
        pluginManager.disablePlugin(plugin);

        try {
            Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            plugins = (List<Plugin>) pluginsField.get(pluginManager);

            Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

            try {
                Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                listenersField.setAccessible(true);
                listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
            } catch (Exception ignored) {
            }

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            commands = (Map<String, Command>) knownCommandsField.get(commandMap);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            RPGItems.plugin.getLogger().warning(stackTraceToString(e));
            return;
        }

        if (listeners != null)
            for (SortedSet<RegisteredListener> set : listeners.values())
                set.removeIf(value -> value.getPlugin() == plugin);

        for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Command> entry = it.next();
            if (entry.getValue() instanceof PluginCommand) {
                PluginCommand c = (PluginCommand) entry.getValue();
                if (c.getPlugin() == plugin) {
                    c.unregister(commandMap);
                    it.remove();
                }
            } else try {
                Field pluginField = Arrays.stream(entry.getValue().getClass().getDeclaredFields()).filter(field -> Plugin.class.isAssignableFrom(field.getType())).findFirst().orElse(null);
                if (pluginField != null) {
                    Plugin owningPlugin;
                    try {
                        pluginField.setAccessible(true);
                        owningPlugin = (Plugin) pluginField.get(entry.getValue());
                        if (owningPlugin.getName().equalsIgnoreCase(plugin.getName())) {
                            entry.getValue().unregister(commandMap);
                            it.remove();
                        }
                    } catch (IllegalAccessException e) {
                        RPGItems.plugin.getLogger().warning(stackTraceToString(e));
                    }
                }
            } catch (IllegalStateException e) {
                if (e.getMessage().equalsIgnoreCase("zip file closed")) {
                    entry.getValue().unregister(commandMap);
                    it.remove();
                }
            }
        }


        plugins.remove(plugin);
        names.remove(name);

        // Attempt to close the classloader to unlock any handles on the plugin's jar file.
        ClassLoader cl = plugin.getClass().getClassLoader();
        if (cl instanceof URLClassLoader) {
            try {
                Field pluginField = cl.getClass().getDeclaredField("plugin");
                pluginField.setAccessible(true);
                pluginField.set(cl, null);

                Field pluginInitField = cl.getClass().getDeclaredField("pluginInit");
                pluginInitField.setAccessible(true);
                pluginInitField.set(cl, null);

            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                RPGItems.plugin.getLogger().warning(stackTraceToString(ex));
            }

            try {
                ((URLClassLoader) cl).close();
            } catch (IOException ex) {
                RPGItems.plugin.getLogger().warning(stackTraceToString(ex));
            }
        }

        try {
            Class paper = Class.forName("io.papermc.paper.plugin.manager.PaperPluginManagerImpl");
            Object paperPluginManagerImpl = paper.getMethod("getInstance").invoke(null);

            Field instanceManagerField = paperPluginManagerImpl.getClass().getDeclaredField("instanceManager");
            instanceManagerField.setAccessible(true);
            Object instanceManager = instanceManagerField.get(paperPluginManagerImpl);

            Field lookupNamesField = instanceManager.getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            Map<String, Object> lookupNames = (Map<String, Object>) lookupNamesField.get(instanceManager);

            Method disableMethod = instanceManager.getClass().getMethod("disablePlugin", Plugin.class);
            disableMethod.setAccessible(true);
            disableMethod.invoke(instanceManager, plugin);

            lookupNames.remove(plugin.getName().toLowerCase());

            Field pluginListField = instanceManager.getClass().getDeclaredField("plugins");
            pluginListField.setAccessible(true);
            List<Plugin> pluginList = (List<Plugin>) pluginListField.get(instanceManager);
            pluginList.remove(plugin);

        } catch (Exception ignore) {
        } // Paper most likely not loaded

        // Will not work on processes started with the -XX:+DisableExplicitGC flag, but lets try it anyway.
        // This tries to get around the issue where Windows refuses to unlock jar files that were previously loaded into the JVM.
        System.gc();
        RPGItems.plugin.getLogger().info("Extension " + name + " has been unloaded.");
    }

    public static String stackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
        }
        return sw.toString();
    }

    public static boolean isClassPresent(String cls) {
        try {
            Class.forName(cls);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
