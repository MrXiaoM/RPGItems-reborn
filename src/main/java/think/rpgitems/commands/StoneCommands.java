package think.rpgitems.commands;

import org.bukkit.command.CommandSender;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGStone;
import think.rpgitems.power.Completion;
import think.rpgitems.power.RPGCommandReceiver;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import java.util.ArrayList;
import java.util.List;

import static think.rpgitems.commands.AdminCommands.*;

public class StoneCommands extends RPGCommandReceiver {
    public StoneCommands(RPGItems plugin) {
        super(plugin);
    }

    @Override
    public String getHelpPrefix() {
        return "stone";
    }

    @SubCommand(value = "rate", tabCompleter = "stoneCompleter")
    public void rateCommand(CommandSender sender, Arguments arguments){
        if (readOnly(sender)) return;
        RPGStone stone = getStone(arguments.nextString(), sender);
        if (arguments.remains() < 1) {
            msgs(sender, "message.stone.rate.get", stone.getName(), stone.getSuccessRate());
            return;
        }
        double rate = arguments.nextDouble();
        if (rate > 1 || rate < 0) {
            msgs(sender, "message.error.double.limit", 0d, 1d);
        }
        stone.setSuccessRate(rate);
        msgs(sender, "message.stone.rate.set", stone.getName(), stone.getSuccessRate());
    }

    @Completion("")
    private List<String> stoneCompleter(CommandSender sender, Arguments arguments){
        List<String> completeStr = new ArrayList<>();
        if (arguments.remains() == 1) {
            completeStr.addAll(ItemManager.stoneNames());
        }
        return filtered(arguments, completeStr);
    }
}
