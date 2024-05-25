package think.rpgitems.commands;

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
        if (readOnly(sender)) return;
        RPGItem item = getItem(arguments.nextString(), sender);
        String type = arguments.nextString();
        if (type.equalsIgnoreCase("damage")) {
            Double damage = arguments.nextDouble((Double) null);
            if (damage != null) {
                item.setMythicSkillDamage(damage);
                msgs(sender, "message.mythic.skill-damage.damage.set", item.getName(), item.getMythicSkillDamage());
                ItemManager.save(item);
                return;
            }
        } else if (type.equalsIgnoreCase("multiple")) {
            Double multiple = arguments.nextDouble((Double) null);
            if (multiple != null) {
                item.setMythicSkillDamageMultiple(multiple);
                msgs(sender, "message.mythic.skill-damage.multiple.set", item.getName(), item.getMythicSkillDamageMultiple());
                ItemManager.save(item);
                return;
            }
        } else if (type.equalsIgnoreCase("critical")) {
            String subType = arguments.nextString();
            if (subType.equalsIgnoreCase("rate")) {
                Double rate = arguments.nextDouble((Double) null);
                if (rate != null) {
                    item.setMythicSkillCriticalRate(rate);
                    msgs(sender, "message.mythic.skill-critical-damage.rate.set", item.getName(), item.getMythicSkillCriticalRate());
                    ItemManager.save(item);
                    return;
                }
            } else if (subType.equalsIgnoreCase("damage")) {
                Double damage = arguments.nextDouble((Double) null);
                if (damage != null) {
                    item.setMythicSkillCriticalDamage(damage);
                    msgs(sender, "message.mythic.skill-critical-damage.damage.set", item.getName(), item.getMythicSkillCriticalDamage());
                    ItemManager.save(item);
                    return;
                }
            } else if (subType.equalsIgnoreCase("multiple")) {
                Double multiple = arguments.nextDouble((Double) null);
                if (multiple != null) {
                    item.setMythicSkillCriticalDamageMultiple(multiple);
                    msgs(sender, "message.mythic.skill-critical-damage.multiple.set", item.getName(), item.getMythicSkillCriticalDamageMultiple());
                    ItemManager.save(item);
                    return;
                }
            }
            msgs(sender, "message.mythic.skill-critical-damage.get", item.getName(), item.getMythicSkillCriticalRate(), item.getMythicSkillCriticalDamage(), item.getMythicSkillCriticalDamageMultiple());
            return;
        }
        msgs(sender, "message.mythic.skill-damage.get", item.getName(), item.getMythicSkillDamage(), item.getMythicSkillDamageMultiple());
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
