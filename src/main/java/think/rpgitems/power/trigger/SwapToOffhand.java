package think.rpgitems.power.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.PowerMainhandItem;
import think.rpgitems.power.PowerResult;

class SwapToOffhand extends Trigger<PlayerSwapHandItemsEvent, PowerMainhandItem, Boolean, Boolean> {
    SwapToOffhand() {
        super(PlayerSwapHandItemsEvent.class, PowerMainhandItem.class, Boolean.class, Boolean.class, "SWAP_TO_OFFHAND");
    }
    public SwapToOffhand(String name) {
        super(name, "SWAP_TO_OFFHAND", PlayerSwapHandItemsEvent.class, PowerMainhandItem.class, Boolean.class, Boolean.class);
    }

    @Override
    public Boolean def(Player player, ItemStack i, PlayerSwapHandItemsEvent event) {
        return true;
    }

    @Override
    public Boolean next(Boolean a, PowerResult<Boolean> b) {
        if (!b.isOK()) return a;
        Boolean r = b.data();
        if (r == null) return a;
        return r && a;
    }

    @Override
    public PowerResult<Boolean> warpResult(PowerResult<Void> overrideResult, PowerMainhandItem power, Player player, ItemStack i, PlayerSwapHandItemsEvent event) {
        return overrideResult.with(true);
    }

    @Override
    public PowerResult<Boolean> run(RPGItem item, PowerMainhandItem power, Player player, ItemStack i, PlayerSwapHandItemsEvent event) {
        return power.swapToOffhand(player, item, i, event);
    }
}
