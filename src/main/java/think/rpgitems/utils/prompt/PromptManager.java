package think.rpgitems.utils.prompt;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import think.rpgitems.RPGItems;
import think.rpgitems.utils.ColorHelper;

import java.util.HashMap;
import java.util.Map;

public class PromptManager implements Listener {
    RPGItems plugin;
    Map<String, IPrompt> prompts = new HashMap<>();

    public PromptManager(RPGItems plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void send(CommandSender sender, String msg) {
        sender.sendMessage(ColorHelper.parseColor(msg));
    }

    public void runPrompt(IPrompt prompt) {
        if (prompt == null)
            return;
        if (prompts.containsKey(prompt.getPlayer().getName()))
            return;
        prompts.put(prompt.getPlayer().getName(), prompt);
        prompt.startPrompt();
    }

    public boolean isPrompting(Player player) {
        return isPrompting(player.getName());
    }

    public boolean isPrompting(String player) {
        return prompts.containsKey(player);
    }

    public void cancelPrompt(Player player) {
        if (!isPrompting(player))
            return;
        prompts.get(player.getName()).cancelPrompt();
        prompts.remove(player.getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        if (isPrompting(player)) {
            IPrompt prompt = prompts.get(player.getName());
            String chatMsg = e.getMessage();
            // 同步
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (chatMsg.equalsIgnoreCase(prompt.getCancelKey())) {
                    prompt.cancelPrompt();
                    prompts.remove(player.getName());
                } else if (prompt.putPromptResult(chatMsg)) {
                    prompt.finishPrompt();
                    prompts.remove(player.getName());
                }
            });
            e.setCancelled(true);
            e.setFormat("");
            e.setMessage("");
        }
    }
}
