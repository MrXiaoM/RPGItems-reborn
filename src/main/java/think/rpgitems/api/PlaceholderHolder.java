package think.rpgitems.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PlaceholderHolder {
    @Nullable
    String onPlaceholderRequest(Player player, @NotNull String params);
}
