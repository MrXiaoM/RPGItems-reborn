package think.rpgitems.utils.nyaacore.cmdreceiver;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import think.rpgitems.I18n;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class CommandReceiver implements CommandExecutor, TabCompleter {

    // All subcommands
    private final Map<String, ISubCommandInfo> subCommands = new HashMap<>();
    // Default subcommand
    private ISubCommandInfo defaultSubCommand = null;
    protected Logger logger;
    /**
     * @param plugin for logging purpose only
     */
    @SuppressWarnings("rawtypes")
    public CommandReceiver(Plugin plugin) {
        if (plugin == null) throw new IllegalArgumentException();
        
        this.logger = plugin.getLogger();
        
        // Collect all methods
        Class cls = getClass();
        Set<Method> allMethods = new HashSet<>();
        while (cls != null) {
            allMethods.addAll(Arrays.asList(cls.getDeclaredMethods()));
            cls = cls.getSuperclass();
        }

        // Collect all fields
        cls = getClass();
        Set<Field> allFields = new HashSet<>();
        while (cls != null) {
            allFields.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        }

        Stream.concat(
                allMethods.stream().map(this::parseSubCommandAnnotation),
                allFields.stream().map(f -> parseSubCommandAnnotation(plugin, f))
        ).forEach(scInfo -> {
            if (scInfo == null) return;
            if (scInfo.name != null) {
                if (subCommands.containsKey(scInfo.name)) {
                    logger.warning("There is a sub command named '" + scInfo.name + "' already existed! Old sub command has been overwritten.");
                }
                subCommands.put(scInfo.name, scInfo);
            }

            if (scInfo.isDefault) {
                if (defaultSubCommand != null) {
                    logger.warning("There is a default sub command of '" + scInfo.instance.getClass().getSimpleName() + "' already existed! Old sub command has been overwritten.");
                }
                defaultSubCommand = scInfo;
            }
        });
    }

    public void registerToBukkit(PluginCommand command) {
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    public boolean registerCommand(ISubCommandInfo command, boolean override) {
        boolean contains = subCommands.containsKey(command.getName());
        if (!override && contains) return false;
        subCommands.put(command.getName(), command);
        return contains;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static CommandReceiver newInstance(Class cls, Object arg1) throws ReflectiveOperationException {
        for (Constructor c : cls.getConstructors()) {
            if (c.getParameterCount() == 2 && c.getParameterTypes()[0].isAssignableFrom(arg1.getClass())) {
                return (CommandReceiver) c.newInstance(arg1);
            }
        }
        throw new NoSuchMethodException("no matching constructor found");
    }

    public static Player asPlayer(CommandSender target) {
        if (target instanceof Player) {
            return (Player) target;
        } else {
            throw new NotPlayerException();
        }
    }

    // Scan recursively into parent class to find annotated methods when constructing

    /**
     * This prefix will be used to locate the correct manual item.
     * If the class is registered to bukkit directly, you should return a empty string.
     * If the class is registered through @SubCommand annotation, you should return the subcommand name.
     * If it's a nested subcommand, separate the prefixes using dot.
     *
     * @return the prefix
     */
    public abstract String getHelpPrefix();

    /**
     * @return should {@link CommandReceiver#acceptCommand(CommandSender, Arguments)} print the default "Success" message after executing a command
     */
    protected boolean showCompleteMessage() {
        return false;
    }

    private SubCommandInfoReflect parseSubCommandAnnotation(Method m) {
        SubCommand scAnno = m.getAnnotation(SubCommand.class);
        if (scAnno == null) return null;

        Class<?>[] params = m.getParameterTypes();
        if (!(params.length == 2 &&
                params[0] == CommandSender.class &&
                params[1] == Arguments.class)) {
            logger.warning(I18n.formatDefault("internal.error.bad_subcommand", m.toString()));
            return null; // incorrect method signature
        }
        m.setAccessible(true);

        Method tabm = null;
        if (!scAnno.tabCompleter().isEmpty()) {
            try {
                tabm = m.getDeclaringClass().getDeclaredMethod(scAnno.tabCompleter(), CommandSender.class, Arguments.class);
                tabm.setAccessible(true);
            } catch (NoSuchMethodException ex) {
                StringWriter sw = new StringWriter();
                try (PrintWriter pw = new PrintWriter(sw)) {
                    ex.printStackTrace(pw);
                }
                logger.warning(sw.toString());
                logger.warning(I18n.formatDefault("internal.error.bad_subcommand", m.toString()));
                return null;
            }
        }

        if (!scAnno.value().isEmpty() && scAnno.isDefaultCommand()) {
            // cannot be both subcommand and default command
            logger.warning(I18n.formatDefault("internal.error.bad_subcommand", m.toString()));
            return null;
        } else if (!scAnno.value().isEmpty()) {
            // subcommand
            String subCommandName = scAnno.value();
            String perm = scAnno.permission().isEmpty() ? null : scAnno.permission();
            return new SubCommandInfoReflect(this, subCommandName, perm, false, m, null, null, false, tabm);
        } else if (scAnno.isDefaultCommand()) {
            // default command
            String perm = scAnno.permission().isEmpty() ? null : scAnno.permission();
            return new SubCommandInfoReflect(this, null, perm, false, m, null, null, true, tabm);
        } else {
            // not subcommand nor default command, remove the annotation
            logger.warning(I18n.formatDefault("internal.error.bad_subcommand", m.toString()));
            return null;
        }
    }

    /**
     * @param plugin for logging purpose only
     */
    private SubCommandInfoReflect parseSubCommandAnnotation(Plugin plugin, Field f) {
        SubCommand scAnno = f.getAnnotation(SubCommand.class);
        if (scAnno == null) return null;

        if (!CommandReceiver.class.isAssignableFrom(f.getType())) {
            logger.warning(I18n.formatDefault("internal.error.bad_subcommand", f.toString()));
            return null; // incorrect field type
        }

        if (!scAnno.tabCompleter().isEmpty()) {
            logger.warning(I18n.formatDefault("internal.error.bad_subcommand", f.toString()));
            return null; // field-based subcommand does not need method-based tabcompletion
        }

        // try to instantiate sub command receiver
        CommandReceiver obj;
        try {
            obj = newInstance(f.getType(), plugin);
            f.setAccessible(true);
            f.set(this, obj);
        } catch (ReflectiveOperationException ex) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                ex.printStackTrace(pw);
            }
            logger.warning(sw.toString());
            logger.warning(I18n.formatDefault("internal.error.bad_subcommand", f.toString()));
            return null;
        }

        if (!scAnno.value().isEmpty() && scAnno.isDefaultCommand()) {
            // cannot be both subcommand and default command
            logger.warning(I18n.formatDefault("internal.error.bad_subcommand", f.toString()));
            return null;
        } else if (!scAnno.value().isEmpty()) {
            // subcommand
            String subCommandName = scAnno.value();
            String perm = scAnno.permission().isEmpty() ? null : scAnno.permission();
            return new SubCommandInfoReflect(this, subCommandName, perm, true, null, f, obj, false, null);
        } else if (scAnno.isDefaultCommand()) {
            // default command
            String perm = scAnno.permission().isEmpty() ? null : scAnno.permission();
            return new SubCommandInfoReflect(this, null, perm, true, null, f, obj, true, null);
        } else {
            // not subcommand nor default command, remove the annotation
            logger.warning(I18n.formatDefault("internal.error.bad_subcommand", f.toString()));
            return null;
        }
    }

    /*
     * Code path looks like this:
     * - Bukkit => CmdRecv:onCommand => CmdRecv:acceptCommand => SubCmdRecv:acceptCommand => SubCmdRecv:commandMethod
     * <p>
     * Determine subcommand method or class and Exception collection.
     * Can be override for finer subcommand routing
     * <p>
     * Subcommand execution search order:
     * 1. {@link CommandReceiver#subCommands}
     * 2. {@link CommandReceiver#defaultSubCommand}
     * 3. {@link CommandReceiver#printHelp(CommandSender, Arguments)}
     */

    public Set<String> getSubCommands() {
        return Collections.unmodifiableSet(subCommands.keySet());
    }

    // Only directly registered command handler need this
    // acceptCommand() will be called directly in subcommand classes
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Arguments cmd = Arguments.parse(args, sender);
        if (cmd == null) return false;
        acceptCommand(sender, cmd);
        return true;
    }

    /*
     * The code path looks like this:
     * - Bukkit => CmdRecv:onTabComplete => CmdRecv:acceptTabComplete => SubCmdRecv:acceptTabComplete => SubCmdRecv:tabCompleteMethod
     * <p>
     * Subcommand tab completion search order:
     * 1. {@link CommandReceiver#subCommands}
     * 2. {@link CommandReceiver#defaultSubCommand}.callTabCompletion
     * 3. default builtin completion logic
     * <p>
     */

    /**
     * @param sender who run the command
     * @param cmd    the command, or part of the command
     */
    public void acceptCommand(CommandSender sender, Arguments cmd) {
        String subCommand = cmd.top();
        try {

            boolean showCompleteMessage;
            try {
                if (subCommand != null && subCommands.containsKey(subCommand)) {
                    cmd.next(); // drop the first parameter
                    showCompleteMessage = subCommands.get(subCommand).showCompleteMessage();
                    subCommands.get(subCommand).callCommand(sender, cmd);
                } else if (defaultSubCommand != null) {
                    showCompleteMessage = defaultSubCommand.showCompleteMessage();
                    defaultSubCommand.callCommand(sender, cmd);
                } else {
                    showCompleteMessage = false;
                    printHelp(sender, cmd);
                }

                if (showCompleteMessage && showCompleteMessage()) {
                    msg(sender, "internal.info.command_complete");
                }
            } catch (ReflectiveOperationException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                else
                    throw new RuntimeException("Failed to invoke subcommand", ex);
            }

        } catch (NotPlayerException ex) {
            msg(sender, "internal.error.not_player");
        } catch (NoItemInHandException ex) {
            msg(sender, ex.isOffHand ? "internal.error.no_item_offhand" : "internal.error.no_item_hand");
        } catch (BadCommandException ex) {
            String msg = ex.getMessage();
            if (msg != null && !msg.isEmpty()) {
                if (ex.objs == null) {
                    msg(sender, msg);
                } else {
                    msg(sender, msg, ex.objs);
                }
            } else {
                msg(sender, "internal.error.invalid_command_arg");
            }
            msg(sender, "internal.info.usage_prompt",
                    getHelpContent(sender, "usage", subCommand));
        } catch (NoPermissionException ex) {
            msg(sender, "internal.error.no_required_permission", ex.getMessage());
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                ex.printStackTrace(pw);
            }
            logger.warning(sw.toString());
            msg(sender, "internal.error.command_exception");
        }
    }

    // Only directly registered command handler need this
    // acceptTabComplete() will be called directly in subcommand classes
    @Override
    public final List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        try {
            Arguments cmd = Arguments.parsePreserveLastBlank(args, sender);
            if (cmd == null) return null;
            return acceptTabComplete(sender, cmd);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * @param sender who run the command
     * @param args   the command, or part of the command
     * @return tab completion candidates
     */
    public List<String> acceptTabComplete(CommandSender sender, Arguments args) {
        String cmd = args.top();
        if (cmd == null) return null;
        boolean isPartial = args.remains() == 1;

        if (isPartial) {
            // ask default command
            // list all matching subcommands
            List<String> ret = null;
            if (defaultSubCommand != null) ret = defaultSubCommand.callTabComplete(sender, args);
            if (ret == null) ret = new ArrayList<>();
            final String cmd_prefix = cmd;
            List<String> subcommands = subCommands.keySet().stream().filter(s -> s.startsWith(cmd_prefix)).sorted().toList();
            ret.addAll(subcommands);
            return ret;
        } else {
            // goto subcommand if exact match found
            // otherwise ask default command
            if (subCommands.containsKey(cmd)) {
                args.next();
                return subCommands.get(cmd).callTabComplete(sender, args);
            } else if (defaultSubCommand != null) {
                return defaultSubCommand.callTabComplete(sender, args);
            } else {
                return null;
            }
        }
    }

    private String getHelpContent(CommandSender sender, String type, String cmd) {
        String prefix = getHelpPrefix().isEmpty() ? "" : (getHelpPrefix() + ".");
        String key = "manual." + prefix + cmd + "." + type;
        I18n i18n = I18n.getInstance(sender);
        if (i18n.hasKey(key)) {
            return i18n.getFormatted(key);
        } else {
            return i18n.getFormatted("manual.no_" + type, cmd);
        }
    }

    @SubCommand("help")
    public void printHelp(CommandSender sender, Arguments args) {
        List<String> cmdList = new ArrayList<>(subCommands.keySet());
        cmdList.sort(Comparator.naturalOrder());
        String format = I18n.getFormatted(sender, "manual.format");
        StringBuilder tmp = new StringBuilder();
        for (String cmd : cmdList) {
            if (!subCommands.get(cmd).hasPermission(sender)) continue;
            String description = getHelpContent(sender, "description", cmd);
            String usage = getHelpContent(sender, "usage", cmd);
            tmp.append("\n").append(format
                    .replace("<description>", description)
                    .replace("<usage>", usage));
        }

        if (defaultSubCommand != null && defaultSubCommand.hasPermission(sender)) {
            String cmd = "<default>";
            String description = getHelpContent(sender, "description", cmd);
            String usage = getHelpContent(sender, "usage", cmd);
            tmp.append("\n").append(format
                    .replace("<description>", description)
                    .replace("<usage>", usage));
        }
        sender.sendMessage(tmp.toString());
    }

    public void msg(CommandSender target, String template, Object... args) {
        target.sendMessage(I18n.getFormatted(target, template, args));
    }

    public static class SubCommandInfoReflect implements ISubCommandInfo {
        final Object instance;
        final String name; // default command can have this be null
        final String permission; // if none then no permission required
        final Method tabCompleter;
        final boolean isField; // isField? field : method;
        final Method method;
        final Field field;
        final CommandReceiver fieldValue;
        final boolean isDefault;

        SubCommandInfoReflect(Object instance, String name, String permission, boolean isField, Method method, Field field, CommandReceiver fieldValue, boolean isDefault, Method tabCompleter) {
            if (name == null && !isDefault) throw new IllegalArgumentException();
            if (isField && !(method == null && field != null && fieldValue != null))
                throw new IllegalArgumentException();
            if (!isField && !(method != null && field == null && fieldValue == null))
                throw new IllegalArgumentException();
            if (isField && tabCompleter != null) {
                throw new IllegalArgumentException();
            }
            this.instance = instance;
            this.name = name;
            this.permission = permission;
            this.isField = isField;
            this.method = method;
            this.field = field;
            this.fieldValue = fieldValue;
            this.isDefault = isDefault;
            this.tabCompleter = tabCompleter;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean showCompleteMessage() {
            return isField;
        }

        @Override
        public void callCommand(CommandSender sender, Arguments args) throws IllegalAccessException, InvocationTargetException {
            if (permission != null && !sender.hasPermission(permission)) {
                throw new NoPermissionException(permission);
            }
            if (isField) {
                fieldValue.acceptCommand(sender, args);
            } else {
                method.invoke(instance, sender, args);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> callTabComplete(CommandSender sender, Arguments args) {
            if (permission != null && !sender.hasPermission(permission)) {
                return null;
            }
            if (isField) {
                return fieldValue.acceptTabComplete(sender, args);
            } else if (tabCompleter != null) {
                try {
                    return (List<String>) tabCompleter.invoke(instance, sender, args);
                } catch (ReflectiveOperationException ex) {
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        public boolean hasPermission(CommandSender sender) {
            if (permission == null) return true;
            return sender.hasPermission(permission);
        }
    }
}
