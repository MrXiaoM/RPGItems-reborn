package think.rpgitems.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import think.rpgitems.RPGItems;
import think.rpgitems.power.RPGCommandReceiver;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import static think.rpgitems.commands.AdminCommands.msgs;

public class MagicCommands extends RPGCommandReceiver {
    RPGItems plugin;
    public MagicCommands(RPGItems plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "magic";
    }

    @SubCommand(value = "add")
    public void add(CommandSender sender, Arguments arguments) {
        Player player = arguments.nextPlayer();
        int value = arguments.nextInt();
        boolean isSilent = arguments.nextString("").equals("-s");
        int magic = plugin.magic.getUserMagic(player);
        int total = plugin.magic.getUserTotalMagic(player);
        int newMagic = Math.min(magic + value, total);
        plugin.magic.setUserMagic(player, newMagic);
        if (!isSilent) {
            msgs(sender, "message.magic.add", player.getName(), newMagic - magic);
        }
    }

    @SubCommand(value = "sub")
    public void sub(CommandSender sender, Arguments arguments) {
        Player player = arguments.nextPlayer();
        int value = arguments.nextInt();
        boolean isSilent = arguments.nextString("").equals("-s");
        int magic = plugin.magic.getUserMagic(player);
        int newMagic = Math.max(magic - value, 0);
        plugin.magic.setUserMagic(player, newMagic);
        if (!isSilent) {
            msgs(sender, "message.magic.sub", player.getName(), magic - newMagic);
        }
    }

    @SubCommand(value = "set")
    public void set(CommandSender sender, Arguments arguments) {
        Player player = arguments.nextPlayer();
        int value = arguments.nextInt();
        boolean isSilent = arguments.nextString("").equals("-s");
        int total = plugin.magic.getUserTotalMagic(player);
        int newMagic = Math.min(total, Math.max(value, 0));
        plugin.magic.setUserMagic(player, newMagic);
        if (!isSilent) {
            msgs(sender, "message.magic.set", player.getName(), newMagic);
        }
    }

    @SubCommand(value = "get")
    public void get(CommandSender sender, Arguments arguments) {
        Player player = arguments.nextPlayer();
        int magic = plugin.magic.getUserMagic(player);
        int total = plugin.magic.getUserTotalMagic(player);
        msgs(sender, "message.magic.get", player.getName(), magic, total);
    }
}
