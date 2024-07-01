package think.rpgitems.commands;

import com.udojava.evalex.Expression;
import org.bukkit.command.CommandSender;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.data.FactorModifier;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Completion;
import think.rpgitems.power.RPGCommandReceiver;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static think.rpgitems.commands.AdminCommands.*;

public class FactorCommands extends RPGCommandReceiver {
    private final RPGItems plugin;

    public FactorCommands(RPGItems plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "factor";
    }

    @SubCommand(value = "value", tabCompleter = "valueCompleter")
    public void value(CommandSender sender, Arguments args){
        if (readOnly(sender)) return;
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

    enum FactorModifierType {
        attack, defend
    }

    @SubCommand(value = "modifier", tabCompleter = "modifierCompleter")
    public void modifier(CommandSender sender, Arguments args) {
        if (readOnly(sender)) return;
        RPGItem item = getItem(args.nextString(), sender);
        String value = args.nextString(null);
        if ("set".equalsIgnoreCase(value)) {
            String factor = args.nextString();
            if (!plugin.cfg.factorConfig.getFactorList().contains(factor)) {
                msgs(sender, "message.factor.modifier.warn_factor_not_found", factor);
            }
            FactorModifierType type = args.nextEnum(FactorModifierType.class);
            String expression = consumeString(args);
            try {
                Expression exp = new Expression(expression)
                        .with("damage", BigDecimal.valueOf(114));
                exp.eval();
            } catch (Throwable t) {
                msgs(sender, "message.factor.modifier.wrong_expression");
                return;
            }
            FactorModifier modifier = item.getFactorModifiers().get(factor);
            if (modifier == null) modifier = new FactorModifier(factor);
            switch (type) {
                case attack: {
                    modifier.setAttack(expression);
                    break;
                }
                case defend: {
                    modifier.setDefend(expression);
                    break;
                }
            }
            item.getFactorModifiers().put(factor, modifier);
            String typeString = I18n.getFormatted(sender, "message.factor.modifier.set.type." + type.name().toLowerCase());
            msgs(sender, "message.factor.modifier.set.set", item.getName(), typeString, factor, expression);
            return;
        } else if ("test".equalsIgnoreCase(value)) {
            String factor = args.nextString();
            FactorModifier modifier = item.getFactorModifiers().get(factor);
            if (modifier == null) {
                msgs(sender, "message.factor.modifier.not_found", item.getName(), factor);
                return;
            }
            FactorModifierType type = args.nextEnum(FactorModifierType.class);
            double damage = args.nextDouble();
            double finalDamage;
            switch (type) {
                case attack: finalDamage = modifier.attack(damage); break;
                case defend: finalDamage = modifier.defend(damage); break;
                default: finalDamage = damage; break;
            }
            String typeString = I18n.getFormatted(sender, "message.factor.modifier.set.type." + type.name().toLowerCase());
            msgs(sender, "message.factor.modifier.test", item.getName(), typeString, factor, damage, finalDamage);
            return;
        } else if ("remove".equalsIgnoreCase(value)) {
            String factor = args.nextString();
            FactorModifier modifier = item.getFactorModifiers().remove(factor);
            if (modifier != null) {
                msgs(sender, "message.factor.modifier.remove.success", item.getName(), factor);
            } else {
                msgs(sender, "message.factor.modifier.not_found", item.getName(), factor);
            }
            return;
        } else if ("list".equalsIgnoreCase(value)) {
            Map<String, FactorModifier> modifiers = item.getFactorModifiers();
            if (modifiers.isEmpty()) {
                msgs(sender, "message.factor.modifier.list.empty", item.getName());
            } else {
                msgs(sender, "message.factor.modifier.list.header", item.getName(), modifiers.size());
                for (FactorModifier modifier : modifiers.values()) {
                    msgs(sender, "message.factor.modifier.list.target", modifier.getTarget());
                    msgs(sender, "message.factor.modifier.list.type.attack", modifier.getAttack());
                    msgs(sender, "message.factor.modifier.list.type.defend", modifier.getDefend());
                }
            }
            return;
        }
        printHelp(sender, args);
    }

    @Completion("")
    private List<String> valueCompleter(CommandSender sender, Arguments arguments){
        List<String> completeStr = new ArrayList<>();
        if (arguments.remains() == 1) {
            completeStr.addAll(ItemManager.itemNames());
        }
        return filtered(arguments, completeStr);
    }
    @Completion("")
    private List<String> modifierCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 0: break;
            case 1: {
                completeStr.addAll(ItemManager.itemNames());
                break;
            }
            case 2: {
                completeStr.add("set");
                completeStr.add("remove");
                completeStr.add("list");
                completeStr.add("test");
                break;
            }
            case 3: {
                RPGItem item = getItem(arguments.getRawArgs()[2], sender);
                String operate = arguments.getRawArgs()[3];
                if ("set".equalsIgnoreCase(operate)) {
                    completeStr.addAll(plugin.cfg.factorConfig.getFactorList());
                    completeStr.removeIf(item.getFactorModifiers()::containsKey);
                } else if ("remove".equalsIgnoreCase(operate) || "test".equalsIgnoreCase(operate)) {
                    completeStr.addAll(item.getFactorModifiers().keySet());
                }
                break;
            }
            case 4: {
                String operate = arguments.getRawArgs()[3];
                if ("set".equalsIgnoreCase(operate) || "test".equalsIgnoreCase(operate)) {
                    completeStr.add("attack");
                    completeStr.add("defend");
                }
                break;
            }
            default: { // remains >= 5
                String operate = arguments.getRawArgs()[3];
                if ("set".equalsIgnoreCase(operate)) {
                    completeStr.add("damage + 1.0");
                    completeStr.add("damage * (1.0 + 0.05)");
                } else if ("test".equalsIgnoreCase(operate)) {
                    completeStr.add("114");
                    completeStr.add("514");
                }
                break;
            }
        }
        return filtered(arguments, completeStr);
    }
}
