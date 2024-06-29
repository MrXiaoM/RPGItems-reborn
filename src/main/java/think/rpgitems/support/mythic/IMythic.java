package think.rpgitems.support.mythic;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public interface IMythic extends Listener {
    String getVersion();
    boolean castSkill(Player player, String spell);
}
