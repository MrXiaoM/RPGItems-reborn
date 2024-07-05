package think.rpgitems.commands;

import org.bukkit.command.CommandSender;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Completion;
import think.rpgitems.power.PowerManager;
import think.rpgitems.power.RPGCommandReceiver;
import think.rpgitems.power.UnknownExtensionException;
import think.rpgitems.power.trigger.Trigger;
import think.rpgitems.utils.nyaacore.Pair;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

import static think.rpgitems.commands.AdminCommands.*;

@SuppressWarnings({"rawtypes"})
public class TriggerCommands extends RPGCommandReceiver {
    private final RPGItems plugin;

    public TriggerCommands(RPGItems plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "trigger";
    }

    @Completion("")
    public List<String> addCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.addAll(ItemManager.itemNames());
                break;
            case 2:
                break;
            case 3:
                completeStr.addAll(Trigger.keySet());
                break;
            default:
                RPGItem item = getItem(arguments.nextString(), sender);
                arguments.nextString();
                String base = arguments.nextString();
                Trigger trigger = Trigger.get(base);
                if (trigger != null) {
                    return resolveProperties(sender, item, trigger.getClass(), trigger.getNamespacedKey(), arguments.getRawArgs()[arguments.getRawArgs().length - 1], arguments, true);
                }
        }
        return filtered(arguments, completeStr);
    }

    @SubCommand(value = "add", tabCompleter = "addCompleter")
    public void add(CommandSender sender, Arguments args) {
        if (readOnly(sender)) return;
        String itemStr = args.next();
        String powerStr = args.next();
        if (itemStr == null || itemStr.equals("help")) {
            msgs(sender, "manual.trigger.description");
            msgs(sender, "manual.trigger.usage");
            return;
        }
        String name = args.next();
        RPGItem item = getItem(itemStr, sender);
        Trigger base = Trigger.get(powerStr);
        if (base == null) {
            msgs(sender, "message.trigger.unknown_base", powerStr, String.join(", ", Trigger.keySet()));
            return;
        }
        Trigger trigger = base.copy(name);
        trigger.setItem(item);
        try {
            item.addTrigger(name, setPropertyHolder(sender, args, base.getClass(), trigger, true));
            ItemManager.refreshItem();
            ItemManager.save(item);
            msgs(sender, "message.trigger.ok");
        } catch (Exception e) {
            if (e instanceof CommandException) {
                throw (CommandException) e;
            }
            plugin.getLogger().log(Level.WARNING, "Error adding trigger " + powerStr + " to item " + itemStr + " " + item, e);
            msgs(sender, "internal.error.command_exception");
        }
    }

    @Completion("")
    public List<String> propCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1: {
                completeStr.addAll(ItemManager.itemNames());
                break;
            }
            case 2: {
                RPGItem item = getItem(arguments.nextString(), sender);
                for (int i = 0; i < item.getTriggers().size(); i++) {
                    completeStr.add(String.valueOf(i));
                }
                break;
            }
            default: {
                RPGItem item = getItem(arguments.nextString(), sender);
                Trigger trigger = item.getTriggers().get(arguments.nextString());
                Trigger base = Trigger.get(trigger.getBase());
                if (base != null) {
                    return resolveProperties(sender, item, base.getClass(), base.getNamespacedKey(), arguments.getRawArgs()[arguments.getRawArgs().length - 1], arguments, false);
                }
                break;
            }
        }
        return filtered(arguments, completeStr);
    }

    @SubCommand(value = "prop", tabCompleter = "propCompleter")
    public void prop(CommandSender sender, Arguments args) throws IllegalAccessException {
        if (readOnly(sender)) return;
        RPGItem item = getItem(args.nextString(), sender);
        if (args.top() == null) {
            item.getTriggers().forEach((n, t) -> showTrigger(sender, item, t));
            return;
        }
        String name = args.nextString();
        try {
            Trigger trigger = item.getTriggers().get(name);
            if (trigger == null) {
                msgs(sender, "message.trigger.unknown", name);
                return;
            }
            if (args.top() == null) {
                showTrigger(sender, item, trigger);
                return;
            }
            setPropertyHolder(sender, args, trigger.getClass(), trigger, false);
            item.rebuild();
            ItemManager.refreshItem();
            ItemManager.save(item);
            msgs(sender, "message.trigger.change");
        } catch (UnknownExtensionException e) {
            msgs(sender, "message.error.unknown.extension", e.getName());
        }
    }

    public static void showTrigger(CommandSender sender, RPGItem item, Trigger trigger) {
        msgs(sender, "message.trigger.show", trigger.name(), trigger.getBase(), trigger.getLocalizedName(sender), trigger.getNamespacedKey().toString());
        PowerManager.getProperties(item.getPropertyHolderKey(trigger)).forEach(
                (name, prop) -> showProp(sender, trigger.getNamespacedKey(), prop.getValue(), trigger)
        );
    }

    @Completion("")
    public List<String> removeCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1: {
                completeStr.addAll(ItemManager.itemNames());
                break;
            }
            case 2: {
                RPGItem item = getItem(arguments.nextString(), sender);
                completeStr.addAll(item.getTriggers().keySet());
                break;
            }
        }
        return filtered(arguments, completeStr);
    }

    @SubCommand(value = "remove", tabCompleter = "removeCompleter")
    public void remove(CommandSender sender, Arguments args) {
        if (readOnly(sender)) return;
        RPGItem item = getItem(args.nextString(), sender);
        String name = args.nextString();
        Trigger trigger = item.getTriggers().get(name);
        if (trigger == null) {
            msgs(sender, "message.trigger.unknown", name);
            return;
        }
        item.getTriggers().remove(name);
        msgs(sender, "message.trigger.removed");
    }

    @SubCommand("list")
    public void list(CommandSender sender, Arguments args) {
        int perPage = RPGItems.plugin.cfg.powerPerPage;
        String nameSearch = args.argString("n", args.argString("name", ""));
        Set<String> triggers = Trigger.keySet();
        if (triggers.isEmpty()) {
            msgs(sender, "message.marker.not_found", nameSearch);
            return;
        }
        Stream<String> stream = triggers.stream();
        Pair<Integer, Integer> maxPage = getPaging(triggers.size(), perPage, args);
        int page = maxPage.getValue();
        int max = maxPage.getKey();
        stream = stream
                         .skip((long) (page - 1) * perPage)
                         .limit(perPage);
        msgs(sender, "message.trigger.page-header", page, max);

        stream.forEach(
                trigger -> {
                    Trigger base = Trigger.valueOf(trigger);
                    msgs(sender, "message.trigger.key", trigger);
                    msgs(sender, "message.trigger.description", PowerManager.getDescription(base.getNamespacedKey(), null));
                    PowerManager.getProperties(base.getClass()).forEach(
                            (name, mp) -> showProp(sender, base.getNamespacedKey(), mp.getValue(), null)
                    );
                    msgs(sender, "message.line_separator");
                });
        msgs(sender, "message.trigger.page-footer", page, max);
    }
}
