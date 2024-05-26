package think.rpgitems.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.PlaceholderHolder;
import think.rpgitems.power.Property;
import think.rpgitems.power.RPGCommandReceiver;
import think.rpgitems.power.UnknownPowerException;
import think.rpgitems.utils.nyaacore.Message;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.BadCommandException;
import think.rpgitems.utils.nyaacore.cmdreceiver.CommandReceiver;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static think.rpgitems.commands.AdminCommands.readOnly;

public class TemplateCommands extends RPGCommandReceiver {
    public TemplateCommands(RPGItems plugin) {
        super(plugin);
    }

    @Override
    public String getHelpPrefix() {
        return "template";
    }

    @SubCommand("create")
    public void onCreate(CommandSender sender, Arguments arguments){
        if (readOnly(sender)) return;
        String itemName = arguments.nextString();
        List<String> placeHolder = new ArrayList<>();
        while (arguments.remains() > 0){
            String s = arguments.nextString();
            placeHolder.add(s);
        }

        RPGItem rpgItem = ItemManager.getItem(itemName).orElseThrow(BadCommandException::new);
        List<String> badPlaceHolders = checkPlaceHolder(rpgItem, placeHolder);
        if (!badPlaceHolders.isEmpty()){
            badPlaceHolders.forEach(s -> sendBadMsg(sender, s));
            return;
        }
        rpgItem.setTemplate(true);
        rpgItem.setTemplatePlaceHolders(placeHolder);
        ItemManager.save(rpgItem);
        new Message("").append(I18n.getInstance(sender).format("command.template.create.success", itemName)).send(sender);
    }

    @SubCommand("delete")
    public void onDelete(CommandSender sender, Arguments arguments){
        if (readOnly(sender)) return;
        String itemName = arguments.nextString();
        RPGItem rpgItem = ItemManager.getItem(itemName).orElseThrow(BadCommandException::new);
        rpgItem.setTemplate(false);
        new Message("").append(I18n.getInstance(sender).format("command.template.delete.success", itemName)).send(sender);
    }

    @SubCommand("apply")
    public void onApply(CommandSender sender, Arguments arguments){
        if (readOnly(sender)) return;
        String itemName = arguments.nextString();
        RPGItem target = ItemManager.getItem(itemName).orElseThrow(BadCommandException::new);
        boolean isTemplate = target.isTemplate();
        I18n i18n = I18n.getInstance(sender);
        if (!isTemplate){
            new Message("").append(i18n.format("command.template.not_template", itemName)).send(sender);
            return;
        }
        Set<RPGItem> toUpdate = new HashSet<>();
        ItemManager.items().stream().filter(rpgItem -> rpgItem.isTemplateOf(itemName))
                .forEach(rpgItem -> {
                    try {
                        rpgItem.updateFromTemplate(target);
                    } catch (UnknownPowerException e) {
                        StringWriter sw = new StringWriter();
                        try (PrintWriter pw = new PrintWriter(sw)) {
                            e.printStackTrace(pw);
                        }
                        logger.warning(sw.toString());
                        sender.sendMessage("error applying template: " + e);
                    }
                    toUpdate.add(rpgItem);
                });
        toUpdate.forEach(ItemManager::save);
        new Message("").append(i18n.format("command.template.apply.success", itemName)).send(sender);
    }

    @SubCommand("placeholder")
    PlaceholderCommands placeholderCommands = new PlaceholderCommands(RPGItems.plugin);

    public static class PlaceholderCommands extends CommandReceiver{

        /**
         * @param plugin for logging purpose only
         */
        public PlaceholderCommands(Plugin plugin) {
            super(plugin);
        }

        @SubCommand("add")
        public void onAdd(CommandSender sender, Arguments arguments){
            if (readOnly(sender)) return;
            String itemName = arguments.nextString();

            RPGItem rpgItem = ItemManager.getItem(itemName).orElse(null);
            if (rpgItem == null) {
                new Message("").append(I18n.getInstance(sender).format("message.item.cant.find")).send(sender);
                return;
            }

            List<String> toAdd = new ArrayList<>();
            while (arguments.remains() > 0){
                toAdd.add(arguments.nextString());
            }

            String name = rpgItem.getName();
            if (!rpgItem.isTemplate()){
                new Message("").append(I18n.getInstance(sender).format("command.template.placeholder.not_template", name)).send(sender);
                return;
            }
            List<String> badPlaceHolders = checkPlaceHolder(rpgItem, toAdd);
            if (!badPlaceHolders.isEmpty()){
                badPlaceHolders.forEach(s -> sendBadMsg(sender, s));
                return;
            }
            toAdd.forEach(s -> {
                rpgItem.addTemplatePlaceHolder(s);
                new Message("").append(I18n.getInstance(sender).format("command.template.placeholder.add.success", name, s)).send(sender);
            });

        }

        @SubCommand("remove")
        public void onRemove(CommandSender sender, Arguments arguments){
            if (readOnly(sender)) return;
            String itemName = arguments.nextString();
            String toRemove = arguments.nextString();
            RPGItem rpgItem = ItemManager.getItem(itemName).orElse(null);
            if (rpgItem == null) {
                new Message("").append(I18n.getInstance(sender).format("message.item.cant.find")).send(sender);
                return;
            }
            String name = rpgItem.getName();
            if (!rpgItem.isTemplate()){
                new Message("").append(I18n.getInstance(sender).format("command.template.placeholder.not_template", name)).send(sender);
                return;
            }
            rpgItem.removeTemplatePlaceHolder(toRemove);
            new Message("").append(I18n.getInstance(sender).format("command.template.placeholder.remove.success", name)).send(sender);

        }

        @SubCommand("list")
        public void onList(CommandSender sender, Arguments arguments){
            String itemName = arguments.nextString();
            RPGItem rpgItem = ItemManager.getItem(itemName).orElse(null);
            if (rpgItem == null) {
                new Message("").append(I18n.getInstance(sender).format("message.item.cant.find")).send(sender);
                return;
            }
            String name = rpgItem.getName();
            if (!rpgItem.isTemplate()){
                new Message("").append(I18n.getInstance(sender).format("command.template.placeholder.not_template", name)).send(sender);
                return;
            }
            new Message("").append(I18n.getInstance(sender).format("command.template.placeholder.itemName", name)).send(sender);
            rpgItem.getTemplatePlaceholders().forEach(placeholderHolder -> new Message("").append(I18n.getInstance(sender).format("command.template.placeholder.info", placeholderHolder)).send(sender));
        }

        @Override
        public String getHelpPrefix() {
            return null;
        }
    }


    private static void sendBadMsg(CommandSender sender, String s) {
        I18n i18n = I18n.getInstance(sender);
        new Message("").append(i18n.format("command.template.bad_placeholder", s));
    }

    /**
     * check syntax of placeholders
     * &lt;placeholderId:propName&gt;
     * @return bad placeholders
     */
    private static List<String> checkPlaceHolder(RPGItem rpgItem, List<String> placeHolder) {
        List<String> ret = new ArrayList<>();
        placeHolder.forEach(s ->{
            try{
                String[] split = s.split(":", 3);
                String powerid;
                String propName;
                if (split.length != 2){
                    ret.add(s);
                    return;
                }
                powerid = split[0];
                propName = split[1];
                PlaceholderHolder power = rpgItem.getPlaceholderHolder(powerid);
                Class<?> aClass = power.getClass();
                while (aClass != null){
                    Field declaredField;
                    try{
                        declaredField = aClass.getDeclaredField(propName);
                    }catch (Exception e){
                        declaredField = null;
                    }
                    //if found field with name [propName] and this field has @Property
                    //it's a valid placeholder
                    if (declaredField != null && declaredField.getAnnotation(Property.class) != null){
                        return;
                    }
                    aClass = aClass.getSuperclass();
                }
                ret.add(s);
            } catch (Exception e){
                ret.add(s);
            }
        });
        return ret;
    }

    public static void msgs(CommandSender target, String template, Object... args) {
        I18n i18n = I18n.getInstance(target);
        target.sendMessage(i18n.getFormatted(template, args));
    }

}
