package think.rpgitems.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.RPGCommandReceiver;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import java.util.ArrayList;
import java.util.List;

import static think.rpgitems.commands.AdminCommands.*;

public class MythicCommands extends RPGCommandReceiver {
    private final RPGItems plugin;

    public MythicCommands(RPGItems plugin, I18n i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "mythic";
    }

    @SubCommand(value = "skillDamageAdd", tabCompleter = "skillDamageAddCompleter")
    public void skillDamageAdd(CommandSender sender, Arguments arguments){
        if (plugin.cfg.readonly) {
            sender.sendMessage(ChatColor.YELLOW + "[RPGItems] Read-Only.");
            return;
        }
        RPGItem item = getItem(arguments.nextString(), sender);
        String type = arguments.nextString();
        if (type.equalsIgnoreCase("damage")) {
            Double rate = arguments.nextDouble((Double) null);
            if (rate != null) {
                item.setCriticalRate(rate);
                msgs(sender, "message.mythic.skill-damage.damage.set", item.getName(), item.getCriticalRate());
                ItemManager.save(item);
                return;
            }
        } else if (type.equalsIgnoreCase("multiple")) {
            Double damage = arguments.nextDouble((Double) null);
            if (damage != null) {
                item.setCriticalDamage(damage);
                msgs(sender, "message.mythic.skill-damage.multiple.set", item.getName(), item.getCriticalDamage());
                ItemManager.save(item);
                return;
            }
        }
        msgs(sender, "message.mythic.skill-damage.get", item.getName(), item.getCriticalRate(), item.getCriticalDamage(), item.getCriticalMultiple());
    }

    private List<String> skillDamageAddCompleter(CommandSender sender, Arguments arguments){
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1 -> completeStr.addAll(ItemManager.itemNames());
            case 2 -> {
                completeStr.add("damage");
                completeStr.add("multiple");
            }
        }
        return filtered(arguments, completeStr);
    }
}
