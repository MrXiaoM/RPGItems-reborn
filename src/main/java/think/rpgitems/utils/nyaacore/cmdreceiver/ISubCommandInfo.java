package think.rpgitems.utils.nyaacore.cmdreceiver;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface ISubCommandInfo {
    String getName();
    void callCommand(CommandSender sender, Arguments args) throws Exception;
    List<String> callTabComplete(CommandSender sender, Arguments args);
    boolean hasPermission(CommandSender sender);
    default boolean showCompleteMessage() {
        return false;
    }
}
