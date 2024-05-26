package think.rpgitems.commands;

import org.bukkit.command.CommandSender;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Completion;
import think.rpgitems.power.RPGCommandReceiver;
import think.rpgitems.utils.nyaacore.Message;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import java.util.ArrayList;
import java.util.List;

import static think.rpgitems.commands.AdminCommands.*;

public class MetaCommands extends RPGCommandReceiver {
    private final RPGItems plugin;

    public MetaCommands(RPGItems plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "meta";
    }

    //temporary implementation, will replace with generic function
    //all prop in RPGItem will be able to modify here.
    @SubCommand(value = "quality", tabCompleter = "qualityCompleter")
    public void onQuality(CommandSender sender, Arguments arguments){
        if (readOnly(sender)) return;
        RPGItem item = getItem(arguments.nextString(), sender);
        String quality = arguments.nextString();
        item.setQuality(quality);
        if (!plugin.cfg.qualityPrefixes.containsKey(quality)){
            new Message("").append(I18n.formatDefault("command.meta.quality.warn_quality_not_exists", quality));
        }
        ItemManager.save(item);
    }

    @Completion("")
    private List<String> qualityCompleter(CommandSender sender, Arguments arguments){
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1 -> completeStr.addAll(ItemManager.itemNames());
            case 2 -> completeStr.addAll(plugin.cfg.qualityPrefixes.keySet());
        }
        return filtered(arguments, completeStr);
    }

    @SubCommand(value = "type", tabCompleter = "typeCompleter")
    public void onType(CommandSender sender, Arguments arguments){
        if (readOnly(sender)) return;
        RPGItem item = getItem(arguments.nextString(), sender);
        String type = arguments.nextString();
        item.setType(type);
        ItemManager.save(item);
    }

    @Completion("")
    private List<String> typeCompleter(CommandSender sender, Arguments arguments){
        List<String> completeStr = new ArrayList<>();
        if (arguments.remains() == 1) {
            completeStr.addAll(ItemManager.itemNames());
        }
        return filtered(arguments, completeStr);
    }
}
