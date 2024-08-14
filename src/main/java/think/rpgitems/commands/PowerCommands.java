package think.rpgitems.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGBaseHolder;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.*;
import think.rpgitems.power.trigger.Trigger;
import think.rpgitems.utils.nyaacore.Pair;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.BadCommandException;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static think.rpgitems.commands.AdminCommands.*;

public class PowerCommands extends RPGCommandReceiver {
    private final RPGItems plugin;

    public PowerCommands(RPGItems plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "power";
    }

    private static Pair<NamespacedKey, Class<? extends Power>> getPowerClass(CommandSender sender, String powerStr) {
        try {
            NamespacedKey key = PowerManager.parseKey(powerStr);
            Class<? extends Power> cls = PowerManager.getPower(key);
            if (cls == null) {
                msgs(sender, "message.power.unknown", powerStr);
            }
            return Pair.of(key, cls);
        } catch (UnknownExtensionException e) {
            msgs(sender, "message.error.unknown.extension", e.getName());
            return null;
        }
    }

    @Completion("")
    public List<String> addCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1: {
                completeStr.addAll(ItemManager.itemNames());
                break;
            }
            case 2: {
                for (NamespacedKey s : PowerManager.getPowers().keySet()) {
                    Object obj = PowerManager.hasExtension() ? s : s.getKey();
                    completeStr.add(obj.toString());
                }
                break;
            }
            default: {
                RPGBaseHolder holder = getStoneOrItem(arguments.nextString(), sender);
                String last = arguments.getRawArgs()[arguments.getRawArgs().length - 1];
                String powerKey = arguments.nextString();
                Pair<NamespacedKey, Class<? extends Power>> powerClass = getPowerClass(sender, powerKey);
                if (powerClass != null) {
                    return resolveProperties(sender, holder, powerClass.getValue(), powerClass.getKey(), last, arguments, true);
                }
                break;
            }
        }
        return filtered(arguments, completeStr);
    }

    @SubCommand(value = "add", tabCompleter = "addCompleter")
    public void add(CommandSender sender, Arguments args) {
        if (readOnly(sender)) return;
        String itemStr = args.next();
        String powerStr = args.next();
        if (itemStr == null || itemStr.equals("help") || powerStr == null) {
            msgs(sender, "manual.power.add.description");
            msgs(sender, "manual.power.add.usage");
            return;
        }
        RPGBaseHolder holder = getStoneOrItem(itemStr, sender);
        Pair<NamespacedKey, Class<? extends Power>> keyClass = getPowerClass(sender, powerStr);
        if (keyClass == null || keyClass.getValue() == null) return;
        Power power;
        Class<? extends Power> cls = keyClass.getValue();
        NamespacedKey key = keyClass.getKey();
        try {
            power = initPropertyHolder(sender, args, holder, cls);
            holder.addPower(key, power);
            ItemManager.refreshItem();
            holder.save();
            msg(sender, "message.power.ok", powerStr, holder.getPowers().size() - 1);
        } catch (Exception e) {
            if (e instanceof BadCommandException) {
                throw (BadCommandException) e;
            }
            plugin.getLogger().log(Level.WARNING, "Error adding power " + powerStr + " to item " + itemStr + " " + holder, e);
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
                RPGBaseHolder holder = getStoneOrItem(arguments.nextString(), sender);
                for (int i = 0; i < holder.getPowers().size(); i++) {
                    completeStr.add(i + "-" + holder.getPowers().get(i).getNamespacedKey());
                }
                break;
            }
            default: {
                RPGBaseHolder holder = getStoneOrItem(arguments.nextString(), sender);
                Power nextPower = nextPower(holder, sender, arguments);
                return resolveProperties(sender, holder, nextPower.getClass(), nextPower.getNamespacedKey(), arguments.getRawArgs()[arguments.getRawArgs().length - 1], arguments, false);
            }
        }
        return filtered(arguments, completeStr);
    }

    private static Power nextPower(RPGBaseHolder holder, CommandSender sender, Arguments args) {
        String next = args.top();
        if (next.contains("-")) {
            next = args.nextString();
            String p1 = next.split("-", 2)[0];
            String p2 = next.split("-", 2)[1];
            try {
                int nth = Integer.parseInt(p1);
                Power power = holder.getPowers().get(nth);
                if (power == null) {
                    throw new BadCommandException("message.power.unknown", nth);
                }
                Pair<NamespacedKey, Class<? extends Power>> keyClass = getPowerClass(sender, p2);
                if (keyClass == null || !power.getNamespacedKey().equals(keyClass.getKey())) {
                    throw new BadCommandException("message.power.unknown", p2);
                }
                return power;
            } catch (NumberFormatException ignore) {
                Pair<NamespacedKey, Class<? extends Power>> keyClass = getPowerClass(sender, p1);
                if (keyClass == null) {
                    throw new BadCommandException("message.power.unknown", p1);
                }
                try {
                    int nth = Integer.parseInt(p2);
                    Power power = holder.getPower(keyClass.getKey(), keyClass.getValue()).get(nth);
                    if (power == null) {
                        throw new BadCommandException("message.power.unknown", nth);
                    }
                    return power;
                } catch (NumberFormatException ignored) {
                    throw new BadCommandException("message.power.unknown", p2);
                }
            }
        } else {
            int nth = args.nextInt();
            Power power = holder.getPowers().get(nth);
            if (power == null) {
                throw new BadCommandException("message.power.unknown", nth);
            }
            return power;
        }
    }

    @SubCommand(value = "prop", tabCompleter = "propCompleter")
    public void prop(CommandSender sender, Arguments args) throws IllegalAccessException {
        if (readOnly(sender)) return;
        RPGBaseHolder holder = getStoneOrItem(args.nextString(), sender);
        if (args.top() == null) {
            for (int i = 0; i < holder.getPowers().size(); i++) {
                Power power = holder.getPowers().get(i);
                showPower(sender, i, holder, power);
            }
            return;
        }
        try {
            Power power = nextPower(holder, sender, args);
            if (args.top() == null) {
                showPower(sender, holder.getPowers().indexOf(power), holder, power);
                return;
            }
            setPropertyHolder(sender, args, holder, power.getClass(), power, false);
            if (holder instanceof RPGItem) ((RPGItem) holder).rebuild();
            ItemManager.refreshItem();
            holder.save();
            msgs(sender, "message.power.change");
        } catch (UnknownExtensionException e) {
            msgs(sender, "message.error.unknown.extension", e.getName());
        }
    }

    public static void showPower(CommandSender sender, int nth, RPGBaseHolder holder, Power power) {
        msgs(sender, "message.power.show", nth, power.getLocalizedName(sender), power.getNamespacedKey().toString(), power.displayText() == null ? I18n.getInstance(sender).format("message.power.no_display") : power.displayText(), power.getTriggers().stream().map(Trigger::name).collect(Collectors.joining(",")));
        NamespacedKey powerKey = holder.getPropertyHolderKey(power);
        PowerManager.getProperties(powerKey).forEach(
                (name, prop) -> showProp(sender, powerKey, prop.getValue(), power)
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
                RPGBaseHolder holder = getStoneOrItem(arguments.nextString(), sender);
                for (int i = 0; i < holder.getPowers().size(); i++) {
                    completeStr.add(i + "-" + holder.getPowers().get(i).getNamespacedKey());
                }
            }
        }
        return filtered(arguments, completeStr);
    }

    @Completion("")
    public List<String> reorderCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1: {
                completeStr.addAll(ItemManager.itemNames());
                break;
            }
            case 2: {
                RPGBaseHolder holder = getStoneOrItem(arguments.nextString(), sender);
                for (int i = 0; i < holder.getPowers().size(); i++) {
                    completeStr.add(i + "-" + holder.getPowers().get(i).getNamespacedKey());
                }
                break;
            }
            case 3: {
                RPGBaseHolder holder = getStoneOrItem(arguments.nextString(), sender);
                String next = arguments.top();
                int nth;
                if (next.contains("-")) {
                    next = arguments.top();
                    String p1 = next.split("-", 2)[0];
                    String p2 = next.split("-", 2)[1];
                    try {
                        nth = Integer.parseInt(p1);
                    } catch (NumberFormatException ignore) {
                        Pair<NamespacedKey, Class<? extends Power>> keyClass = getPowerClass(sender, p1);
                        if (keyClass == null) {
                            throw new BadCommandException("message.power.unknown", p1);
                        }
                        try {
                            nth = Integer.parseInt(p2);
                        } catch (NumberFormatException ignored) {
                            throw new BadCommandException("message.power.unknown", p2);
                        }
                    }
                } else {
                    nth = Integer.parseInt(arguments.top());
                }
                int finalNth = nth;
                for (int i = 0; i < holder.getPowers().size(); i++) {
                    if (i == finalNth) continue;
                    completeStr.add(i + "-" + holder.getPowers().get(i).getNamespacedKey());
                }
                break;
            }
        }
        return filtered(arguments, completeStr);
    }

    @SubCommand(value = "remove", tabCompleter = "removeCompleter")
    public void remove(CommandSender sender, Arguments args) {
        if (readOnly(sender)) return;
        RPGBaseHolder holder = getStoneOrItem(args.nextString(), sender);
        int nth = -1;
        Power power = nextPower(holder, sender, args);
        try {
            List<Power> powers = holder.getPowers();
            for (int i = 0; i < powers.size(); i++) {
                Power pi = powers.get(i);
                if (power.equals(pi)) {
                    nth = i;
                    break;
                }
            }
            if (nth < 0) {
                msg(sender, "message.num_out_of_range", nth, 0, powers.size());
                return;
            }
            Power power1 = holder.getPowers().get(nth);
            if (power1 == null) {
                msgs(sender, "message.power.unknown", nth);
                return;
            }
            power.deinit();
            holder.getPowers().remove(nth);
            NamespacedKey key = holder.removePropertyHolderKey(power);
            if (holder instanceof RPGItem) ((RPGItem) holder).rebuild();
            holder.save();
            msgs(sender, "message.power.removed", key.toString(), nth);
        } catch (UnknownExtensionException e) {
            msgs(sender, "message.error.unknown.extension", e.getName());
        }
    }

    @SubCommand(value = "reorder", tabCompleter = "reorderCompleter")
    public void reorder(CommandSender sender, Arguments args) {
        if (readOnly(sender)) return;
        RPGBaseHolder holder = getStoneOrItem(args.nextString(), sender);
        int origin = -1;
        int next = -1;
        int size = holder.getPowers().size();
        Power originPower = nextPower(holder, sender, args);
        Power nextPower = nextPower(holder, sender, args);
        List<Power> powers = holder.getPowers();
        for (int i = 0; i < powers.size(); i++) {
            Power pi = powers.get(i);
            if (origin == -1 && originPower.equals(pi)) {
                origin = i;
                continue;
            }
            if (next == -1 && nextPower.equals(pi)){
                next = i;
            }
        }

        if (next < 0 || next >= size) {
            msg(sender, "message.num_out_of_range", next, 0, size);
            return;
        }
        if (origin < 0 || origin >= size) {
            msg(sender, "message.num_out_of_range", origin, 0, size);
            return;
        }
        Power remove = holder.getPowers().remove(origin);
        holder.getPowers().add(next, remove);
        ItemManager.refreshItem();
        holder.save();
        msg(sender, "message.power.reorder", remove.getName(), next);
    }

    @SubCommand("list")
    public void list(CommandSender sender, Arguments args) {
        int perPage = RPGItems.plugin.cfg.powerPerPage;
        String nameSearch = args.argString("n", args.argString("name", ""));
        List<NamespacedKey> powers = new ArrayList<>();
        for (NamespacedKey i : PowerManager.getPowers().keySet()) {
            if (!i.getKey().contains(nameSearch)) continue;
            powers.add(i);
        }
        if (powers.isEmpty()) {
            msgs(sender, "message.power.not_found", nameSearch);
            return;
        }
        powers.sort(Comparator.comparing(NamespacedKey::getKey));
        Stream<NamespacedKey> stream = powers.stream();
        Pair<Integer, Integer> maxPage = getPaging(powers.size(), perPage, args);
        int page = maxPage.getValue();
        int max = maxPage.getKey();
        stream = stream
                .skip((long) (page - 1) * perPage)
                .limit(perPage);
        msgs(sender, "message.power.page-header", page, max);

        stream.forEach(
                power -> {
                    msgs(sender, "message.power.key", power.toString());
                    msgs(sender, "message.power.description", PowerManager.getDescription(power, null));
                    PowerManager.getProperties(power).forEach(
                            (name, mp) -> showProp(sender, power, mp.getValue(), null)
                    );
                    msgs(sender, "message.line_separator");
                });
        msgs(sender, "message.power.page-footer", page, max);
    }
}
