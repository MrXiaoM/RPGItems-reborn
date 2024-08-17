package think.rpgitems.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Completion;
import think.rpgitems.power.RPGCommandReceiver;
import think.rpgitems.utils.pdc.ItemPDC;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.BadCommandException;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import java.util.Optional;

import static think.rpgitems.commands.AdminCommands.print;
import static think.rpgitems.item.RPGItem.TAG_IS_MODEL;
import static think.rpgitems.item.RPGItem.TAG_META;

public class UserCommands extends RPGCommandReceiver {

    public UserCommands(RPGItems plugin) {
        super(plugin);
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }

    @SubCommand(value = "info", permission = "info")
    @Completion("command")
    public void info(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        RPGItem item = getItem(sender, false);
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        print(sender, item, false);
        PersistentDataContainer tagContainer = itemStack.getItemMeta().getPersistentDataContainer();
        PersistentDataContainer metaTag = ItemPDC.getTag(tagContainer, TAG_META);
        Optional<Boolean> optIsModel = ItemPDC.optBoolean(metaTag, TAG_IS_MODEL);
        if (optIsModel.orElse(false)) {
            msg(p, "message.model.is");
        }
    }

    @SubCommand(value = "tomodel", permission = "tomodel")
    @Completion("command")
    public void toModel(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        RPGItem item = getItem(sender);
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        item.toModel(p, itemStack);
        p.getInventory().setItemInMainHand(itemStack);
        msg(p, "message.model.to");
    }

    private RPGItem getItem(CommandSender sender) {
        return getItem(sender, true);
    }

    private RPGItem getItem(CommandSender sender, boolean ignoreModel) {
        Player p = (Player) sender;
        Optional<RPGItem> item = ItemManager.toRPGItem(p.getInventory().getItemInMainHand(), ignoreModel);
        if (item.isPresent()) {
            return item.get();
        } else {
            throw new BadCommandException("message.error.iteminhand");
        }
    }
}
