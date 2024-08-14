package think.rpgitems.power.impl;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import think.rpgitems.Events;
import think.rpgitems.I18n;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.*;
import think.rpgitems.utils.ColorHelper;

import java.util.function.Function;
import java.util.function.Supplier;

@Meta(
        defaultTrigger = {"RIGHT_CLICK"},
        generalInterface = {PowerPlain.class},
        implClass = ArrowWithItem.Impl.class
)
public class ArrowWithItem extends BasePower {
    @Property(
            order = 0
    )
    public int cooldown = 0;
    @Property
    public int cost = 0;
    @Property
    public String item = "";
    @Property
    public Sound sound = Sound.ENTITY_ARROW_SHOOT;
    @Property
    public boolean fire = false;
    @Property
    public boolean noGravity = false;
    @Property
    public boolean explode = false;
    @Property
    public boolean explodeFire = false;

    public Sound getSound() {
        return sound;
    }

    public boolean isFire() {
        return this.fire;
    }
    public boolean isNoGravity() {
        return noGravity;
    }
    public boolean isExplode() {
        return explode;
    }

    public boolean isExplodeFire() {
        return explodeFire;
    }

    Function<ItemStack, Boolean> check = null;
    Supplier<String> checkName = null;

    public int getCost() {
        return this.cost;
    }



    public String getName() {
        return "arrow_item";
    }

    public String displayText() {
        load();
        return I18n.formatDefault("power.arrow-item", displayItem(), (double) this.getCooldown() / 20.0);
    }

    public String displayItem() {
        return checkName != null ? checkName.get() : item;
    }

    @Override
    public void init(ConfigurationSection section, String itemName) {
        super.init(section, itemName);
        load();
    }

    public void load() {
        if (check != null && checkName != null) return;
        if (item.startsWith("rpgitem:")) {
            String rpgName = item.substring(8);
            RPGItem rpgItem = ItemManager.getItem(rpgName).orElse(null);
            if (rpgItem != null) {
                String name = rpgItem.getDisplayName();
                checkName = () -> name;
                check = it -> ItemManager.toRPGItem(it).map(rpg -> rpg.getName().equals(rpgName)).orElse(false);
            } else checkName = () -> rpgName;
        } else {
            Material m = Material.matchMaterial(item);
            if (m != null) {
                String name = "<translate:" + new ItemStack(m).getTranslationKey() + ">";
                checkName = () -> name;
                check = it -> it.getType().equals(m);
            }
        }
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public Material consumeArrow(Player p) {
        load();
        if (check == null) return null;
        PlayerInventory inv = p.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && check.apply(item)) {
                Material display = item.getType();
                int amount = item.getAmount() - 1;
                if (amount > 0) item.setAmount(amount);
                else item = null;
                inv.setItem(i, item);
                return display;
            }
        }

        return null;
    }

    public class Impl implements PowerRightClick, PowerLeftClick, PowerPlain {
        public PowerResult<Void> rightClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return this.fire(player, stack);
        }

        public PowerResult<Void> fire(Player player, ItemStack stack) {
            RPGItem item = ItemManager.toRPGItem(stack).orElse(null);
            if (item == null) return PowerResult.fail();
            if (!Utils.checkCooldown(item, this.getPower(), player, getCooldown(), true, true)) {
                return PowerResult.cd();
            } else if (!item.consumeDurability(player, stack, getCost())) {
                return PowerResult.cost();
            } else {
                Material displayItem = consumeArrow(player);
                if (displayItem == null) {
                    ColorHelper.parseAndSend(player, I18n.formatDefault("properties.arrow_item.no_arrow", displayItem()));
                    return PowerResult.fail();
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0F, 1.0F);
                Events.registerRPGProjectile(stack, player);
                Snowball arrow = player.launchProjectile(Snowball.class);
                arrow.addScoreboardTag("rgi_projectile");
                arrow.setShooter(player);
                arrow.setItem(new ItemStack(displayItem));
                if (isFire()) arrow.setFireTicks(114514);
                if (isNoGravity()) arrow.setGravity(false);
                if (isExplode()) {
                    arrow.addScoreboardTag("rgi_projectile_explode" + (isExplodeFire() ? "_fire" : ""));
                }
                Events.autoRemoveProjectile(arrow.getEntityId());
                arrow.setPersistent(false);
                return PowerResult.ok();
            }
        }

        public Power getPower() {
            return ArrowWithItem.this;
        }

        public PowerResult<Void> leftClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return this.fire(player, stack);
        }
    }
}
