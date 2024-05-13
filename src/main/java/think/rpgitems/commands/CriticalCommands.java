package think.rpgitems.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.RPGCommandReceiver;
import think.rpgitems.utils.nyaacore.Message;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import java.util.ArrayList;
import java.util.List;

import static think.rpgitems.commands.AdminCommands.*;

public class CriticalCommands extends RPGCommandReceiver {
    private final RPGItems plugin;

    public CriticalCommands(RPGItems plugin, I18n i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "critical";
    }

    @SubCommand(value = "normal", tabCompleter = "normalCompleter")
    public void normal(CommandSender sender, Arguments arguments){
        if (plugin.cfg.readonly) {
            sender.sendMessage(ChatColor.YELLOW + "[RPGItems] Read-Only.");
            return;
        }
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
        }
        msgs(sender, "message.critical.normal.get", item.getName(), item.getCriticalRate(), item.getCriticalDamage(), item.getCriticalMultiple());
    }

    private List<String> normalCompleter(CommandSender sender, Arguments arguments){
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1 -> completeStr.addAll(ItemManager.itemNames());
            case 2 -> {
                completeStr.add("rate");
                completeStr.add("damage");
                completeStr.add("multiple");
            }
        }
        return filtered(arguments, completeStr);
    }

    @SubCommand(value = "back", tabCompleter = "normalCompleter")
    public void back(CommandSender sender, Arguments arguments){
        if (plugin.cfg.readonly) {
            sender.sendMessage(ChatColor.YELLOW + "[RPGItems] Read-Only.");
            return;
        }
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
        }
        msgs(sender, "message.critical.back.get", item.getName(), item.getCriticalBackRate(), item.getCriticalBackDamage(), item.getCriticalBackMultiple());
    }

    @SubCommand(value = "anti", tabCompleter = "typeCompleter")
    public void anti(CommandSender sender, Arguments arguments){
        if (plugin.cfg.readonly) {
            sender.sendMessage(ChatColor.YELLOW + "[RPGItems] Read-Only.");
            return;
        }
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

    private List<String> antiCompleter(CommandSender sender, Arguments arguments){
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1 -> completeStr.addAll(ItemManager.itemNames());
            case 2 -> {
                completeStr.add("rate");
            }
        }
        return filtered(arguments, completeStr);
    }
}
