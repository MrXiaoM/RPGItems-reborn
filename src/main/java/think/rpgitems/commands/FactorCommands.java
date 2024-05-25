package think.rpgitems.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.RPGCommandReceiver;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import java.util.ArrayList;
import java.util.List;

import static think.rpgitems.commands.AdminCommands.*;

public class FactorCommands extends RPGCommandReceiver {
    private final RPGItems plugin;

    public FactorCommands(RPGItems plugin, I18n i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "factor";
    }

    @SubCommand(value = "set", tabCompleter = "setCompleter")
    public void set(CommandSender sender, Arguments args){
        if (plugin.cfg.readonly) {
            sender.sendMessage(ChatColor.YELLOW + "[RPGItems] Read-Only.");
            return;
        }
        RPGItem item = getItem(args.nextString(), sender);
        String value = args.nextString(null);
        if (value != null) {
            if (value.equalsIgnoreCase("none")) value = "";
            item.setFactor(value);
            msgs(sender, "message.factor.set", item.getName(), item.getFactor());
            ItemManager.refreshItem();
            ItemManager.save(item);
        } else {
            msgs(sender, "message.factor.get", item.getName(), item.getFactor());
        }
    }

    private List<String> setCompleter(CommandSender sender, Arguments arguments){
        List<String> completeStr = new ArrayList<>();
        if (arguments.remains() == 1) {
            completeStr.addAll(ItemManager.itemNames());
        }
        return filtered(arguments, completeStr);
    }
}
