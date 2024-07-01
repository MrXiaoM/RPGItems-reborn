package think.rpgitems;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;
import think.rpgitems.api.IGui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager implements Listener {
    final Map<UUID, IGui> playersGui = new HashMap<>();
    public final RPGItems plugin;
    public GuiManager(RPGItems plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGui(IGui gui) {
        Player player = gui.getPlayer();
        if (player == null) return;
        player.closeInventory();
        playersGui.put(player.getUniqueId(), gui);
        Inventory inv = gui.newInventory();
        if (inv != null) player.openInventory(inv);
    }

    public void onDisable() {
        for (Map.Entry<UUID, IGui> entry : playersGui.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;
            entry.getValue().onClose(player.getOpenInventory());
            player.closeInventory();
            player.sendTitle(
                    I18n.getFormatted(player, "message.gui.disable-title.title"),
                    I18n.getFormatted(player, "message.gui.disable-title.subtitle"),
                    10, 30, 10);
        }
        playersGui.clear();
    }

    @Nullable
    public IGui getOpeningGui(Player player) {
        return playersGui.get(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        IGui remove = playersGui.remove(player.getUniqueId());
        if (remove != null) {
            remove.onClose(player.getOpenInventory());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (playersGui.containsKey(player.getUniqueId())) {
            playersGui.get(player.getUniqueId()).onClick(event.getAction(), event.getClick(), event.getSlotType(),
                    event.getRawSlot(), event.getCurrentItem(), event.getCursor(), event.getView(), event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (playersGui.containsKey(player.getUniqueId())) {
            playersGui.get(player.getUniqueId()).onDrag(event.getView(), event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        IGui remove = playersGui.remove(player.getUniqueId());
        if (remove != null) {
            remove.onClose(event.getView());
        }
    }
}
