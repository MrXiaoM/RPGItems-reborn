package think.rpgitems.power.impl;

import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.utils.nyaacore.Message;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import think.rpgitems.commands.AdminCommands;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.power.*;

import java.util.logging.Level;

import static think.rpgitems.power.Utils.checkCooldown;

@Meta(defaultTrigger = "RIGHT_CLICK", generalInterface = {
        PowerLeftClick.class,
        PowerRightClick.class,
        PowerPlain.class,
        PowerSneak.class,
        PowerLivingEntity.class,
        PowerSprint.class,
        PowerHurt.class,
        PowerHit.class,
        PowerHitTaken.class,
        PowerBowShoot.class,
        PowerBeamHit.class,
        PowerLocation.class
}, implClass = Economy.Impl.class, note = "Requires Vault plugin and a Vault-Compatible economy plugin")
public class Economy extends BasePower {

    private static net.milkbowl.vault.economy.Economy eco;

    @Property
    public int cooldown = 0;

    @Property
    public double amountToPlayer;

    @Property
    public boolean showFailMessage;

    @Property
    public boolean abortOnFailure = true;

    @Property
    public boolean requireHurtByEntity = true;

    @Override
    public void init(ConfigurationSection section, String itemName) {
        super.init(section, itemName);
        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> provider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (provider != null) {
            eco = provider.getProvider();
        } else {
            RPGItems.plugin.getLogger().log(Level.SEVERE, "Vault Economy not found");
            throw new AdminCommands.CommandException("message.error.economy");
        }
    }

    @Override
    public String getName() {
        return "economy";
    }

    @Override
    public String displayText() {
        return I18n.formatDefault(getAmountToPlayer() > 0 ? "power.economy.deposit" : "power.economy.withdraw", eco.format(Math.abs(getAmountToPlayer())), (double) getCooldown() / 20d);
    }

    public double getAmountToPlayer() {
        return amountToPlayer;
    }

    /**
     * Cooldown time of this power
     */
    public int getCooldown() {
        return cooldown;
    }

    public boolean isAbortOnFailure() {
        return abortOnFailure;
    }

    public boolean isRequireHurtByEntity() {
        return requireHurtByEntity;
    }

    public boolean isShowFailMessage() {
        return showFailMessage;
    }

    public class Impl implements PowerRightClick, PowerLeftClick, PowerPlain, PowerHit, PowerHurt, PowerHitTaken, PowerBowShoot {


        @Override
        public PowerResult<Double> takeHit(Player target, RPGItem item, ItemStack stack, double damage, EntityDamageEvent event) {
            if (!isRequireHurtByEntity() || event instanceof EntityDamageByEntityEvent) {
                return fire(target, item, stack).with(damage);
            }
            return PowerResult.noop();
        }

        @Override
        public PowerResult<Void> fire(Player player, RPGItem item, ItemStack stack) {
            if (!checkCooldown(item, getPower(), player, getCooldown(), true, true))
                return isAbortOnFailure() ? PowerResult.abort() : PowerResult.cd();
            EconomyResponse economyResponse;
            if (getAmountToPlayer() > 0) {
                economyResponse = eco.depositPlayer(player, getAmountToPlayer());
            } else {
                economyResponse = eco.withdrawPlayer(player, -getAmountToPlayer());
            }
            if (economyResponse.transactionSuccess()) {
                return PowerResult.ok();
            }
            if (isShowFailMessage()) {
                new Message(economyResponse.errorMessage).send(player);
            }
            return isAbortOnFailure() ? PowerResult.abort() : PowerResult.fail();
        }

        @Override
        public Power getPower() {
            return Economy.this;
        }

        @Override
        public PowerResult<Void> hurt(Player target, RPGItem item, ItemStack stack, EntityDamageEvent event) {
            if (!isRequireHurtByEntity() || event instanceof EntityDamageByEntityEvent) {
                return fire(target, item, stack);
            }
            return PowerResult.noop();
        }

        @Override
        public PowerResult<Void> leftClick(Player player, RPGItem item, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, item, stack);
        }

        @Override
        public PowerResult<Void> rightClick(Player player, RPGItem item, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, item, stack);
        }

        @Override
        public PowerResult<Float> bowShoot(Player player, RPGItem item, ItemStack stack, EntityShootBowEvent event) {
            return fire(player, item, stack).with(event.getForce());
        }

        @Override
        public PowerResult<Double> hit(Player player, RPGItem item, ItemStack stack, LivingEntity entity, double damage, EntityDamageByEntityEvent event) {
            return fire(player, item, stack).with(damage);
        }
    }
}
