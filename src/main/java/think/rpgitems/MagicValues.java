package think.rpgitems;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.TreeMap;

public class MagicValues implements Listener {
    final RPGItems plugin;
    Map<String, Integer> magic = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    Map<String, BossBar> bossBars = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    protected MagicValues(RPGItems plugin) {
        this.plugin = plugin;
        plugin.disableHook.add(this::onDisable);
    }

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void onDisable() {
        for (BossBar bar : bossBars.values()) {
            bar.removeAll();
        }
        bossBars.clear();
        magic.clear();
    }

    public void updateBossBar(Player player) {
        int magic = getUserMagic(player);
        int total = getUserTotalMagic(player);
        if (magic >= total) {
            BossBar bossBar = bossBars.remove(player.getName());
            if (bossBar != null) bossBar.removeAll();
            return;
        }
        BossBar bossBar = bossBars.get(player.getName());
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(I18n.getFormatted(player, "message.magic.bar-title", magic, total), plugin.cfg.magicColor, plugin.cfg.magicStyle);
            bossBar.addPlayer(player);
            bossBars.put(player.getName(), bossBar);
        } else {
            bossBar.setTitle(I18n.getFormatted(player, "message.magic.bar-title", magic, total));
        }
        bossBar.setProgress((double)magic / total);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        updateBossBar(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerLeave(e.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        playerLeave(e.getPlayer());
    }

    private void playerLeave(Player player) {
        BossBar bossBar = bossBars.remove(player.getName());
        if (bossBar == null) return;
        bossBar.removeAll();
    }
    public int getUserTotalMagic(Player player) {
        int magic = 0;
        for (Map.Entry<String, Integer> entry : plugin.cfg.magicProperties.entrySet()) {
            if (player.hasPermission("rpgitems.magic." + entry)) {
                magic = entry.getValue();
            } else {
                break;
            }
        }
        return magic;
    }

    public int getUserMagic(Player player) {
        Integer value = magic.get(player.getName());
        return value != null ? value : getUserTotalMagic(player);
    }

    public void setUserMagic(Player player, int value) {
        magic.put(player.getName(), value);
        updateBossBar(player);
    }

    public boolean costMagic(Player player, int value) {
        if (value <= 0) return true;
        int magic = getUserMagic(player);
        if (magic < value) return false;
        setUserMagic(player, magic - value);
        return true;
    }
}
