package think.rpgitems.commands;

import org.bukkit.command.CommandSender;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Completion;
import think.rpgitems.power.RPGCommandReceiver;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import java.util.ArrayList;
import java.util.List;

import static think.rpgitems.commands.AdminCommands.*;

public class CriticalCommands extends RPGCommandReceiver {
    public CriticalCommands(RPGItems plugin) {
        super(plugin);
    }

    @Override
    public String getHelpPrefix() {
        return "critical";
    }

    @SubCommand(value = "normal", tabCompleter = "normalCompleter")
    public void normal(CommandSender sender, Arguments arguments){
        if (readOnly(sender)) return;
        RPGItem item = getItem(arguments.nextString(), sender);
        String type = arguments.nextString();
        if (type.equalsIgnoreCase("rate")) {
            Double rate = arguments.nextDouble((Double) null);
            if (rate != null) {
                item.setCriticalRate(rate);
                msgs(sender, "message.critical.normal.rate.set", item.getName(), item.getCriticalRate());
                ItemManager.save(item);
                return;
            }
        } else if (type.equalsIgnoreCase("damage")) {
            Double damage = arguments.nextDouble((Double) null);
            if (damage != null) {
                item.setCriticalDamage(damage);
                msgs(sender, "message.critical.normal.damage.set", item.getName(), item.getCriticalDamage());
                ItemManager.save(item);
                return;
            }
        } else if (type.equalsIgnoreCase("multiple")) {
            Double multiple = arguments.nextDouble((Double) null);
            if (multiple != null) {
                item.setCriticalMultiple(multiple);
                msgs(sender, "message.critical.normal.multiple.set", item.getName(), item.getCriticalMultiple());
                ItemManager.save(item);
                return;
            }
        } else if (type.equalsIgnoreCase("armorRate")) {
            Double rate = arguments.nextDouble((Double) null);
            if (rate != null) {
                item.setCriticalArmorRate(rate);
                msgs(sender, "message.critical.normal.armor-rate.set", item.getName(), item.getCriticalArmorRate());
                ItemManager.save(item);
                return;
            }
        }
        msgs(sender, "message.critical.normal.get", item.getName(), item.getCriticalRate(), item.getCriticalDamage(), item.getCriticalMultiple(), item.getCriticalArmorRate());
    }

    @Completion("")
    private List<String> normalCompleter(CommandSender sender, Arguments arguments){
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1: {
                completeStr.addAll(ItemManager.itemNames());
                break;
            }
            case 2: {
                completeStr.add("rate");
                completeStr.add("damage");
                completeStr.add("multiple");
                break;
            }
        }
        return filtered(arguments, completeStr);
    }

    @SubCommand(value = "back", tabCompleter = "normalCompleter")
    public void back(CommandSender sender, Arguments arguments){
        if (readOnly(sender)) return;
        RPGItem item = getItem(arguments.nextString(), sender);
        String type = arguments.nextString();
        if (type.equalsIgnoreCase("rate")) {
            Double rate = arguments.nextDouble((Double) null);
            if (rate != null) {
                item.setCriticalBackRate(rate);
                msgs(sender, "message.critical.back.rate.set", item.getName(), item.getCriticalBackRate());
                ItemManager.save(item);
                return;
            }
        } else if (type.equalsIgnoreCase("damage")) {
            Double damage = arguments.nextDouble((Double) null);
            if (damage != null) {
                item.setCriticalBackDamage(damage);
                msgs(sender, "message.critical.back.damage.set", item.getName(), item.getCriticalBackDamage());
                ItemManager.save(item);
                return;
            }
        } else if (type.equalsIgnoreCase("multiple")) {
            Double multiple = arguments.nextDouble((Double) null);
            if (multiple != null) {
                item.setCriticalBackMultiple(multiple);
                msgs(sender, "message.critical.back.multiple.set", item.getName(), item.getCriticalBackMultiple());
                ItemManager.save(item);
                return;
            }
        } else if (type.equalsIgnoreCase("armorRate")) {
            Double rate = arguments.nextDouble((Double) null);
            if (rate != null) {
                item.setCriticalBackArmorRate(rate);
                msgs(sender, "message.critical.back.armor-rate.set", item.getName(), item.getCriticalBackArmorRate());
                ItemManager.save(item);
                return;
            }
        }
        msgs(sender, "message.critical.back.get", item.getName(), item.getCriticalBackRate(), item.getCriticalBackDamage(), item.getCriticalBackMultiple(), item.getCriticalBackArmorRate());
    }

    @SubCommand(value = "anti", tabCompleter = "antiCompleter")
    public void anti(CommandSender sender, Arguments arguments){
        if (readOnly(sender)) return;
        RPGItem item = getItem(arguments.nextString(), sender);
        String type = arguments.nextString();
        if (type.equalsIgnoreCase("rate")) {
            Double rate = arguments.nextDouble((Double) null);
            if (rate != null) {
                item.setCriticalAntiRate(rate);
                msgs(sender, "message.critical.anti.rate.set", item.getName(), item.getCriticalAntiRate());
                ItemManager.save(item);
                return;
            }
        }
        msgs(sender, "message.critical.anti.get", item.getName(), item.getCriticalAntiRate());
    }

    @Completion("")
    private List<String> antiCompleter(CommandSender sender, Arguments arguments){
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1: {
                completeStr.addAll(ItemManager.itemNames());
                break;
            }
            case 2: {
                completeStr.add("rate");
                break;
            }
        }
        return filtered(arguments, completeStr);
    }
}
