package think.rpgitems.power.impl;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.I18n;
import think.rpgitems.event.BeamEndEvent;
import think.rpgitems.event.BeamHitBlockEvent;
import think.rpgitems.event.BeamHitEntityEvent;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.*;
import think.rpgitems.utils.LightContext;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

import static think.rpgitems.Events.*;

@Meta(defaultTrigger = {"PROJECTILE_HIT"}, generalInterface = {
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
}, implClass = Explosion.Impl.class)
public class Explosion extends BasePower {

    @Property
    public int distance = 20;

    @Property
    public double chance = 20;


    @Property(alias = "power")
    public float explosionPower = 4.0f;

    @Property
    public int cost = 0;

    /**
     * Cost of this power
     */
    public int getCost() {
        return cost;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public String getName() {
        return "explosion";
    }

    @Override
    public String displayText() {
        return I18n.formatDefault("power.explosion", getChance(), getExplosionPower());
    }

    /**
     * Chance of triggering this power
     */
    public double getChance() {
        return chance;
    }

    public float getExplosionPower() {
        return explosionPower;
    }

    public class Impl implements PowerLeftClick, PowerRightClick, PowerPlain, PowerHit, PowerProjectileHit, PowerLocation, PowerBeamHit, PowerLivingEntity {

        @Override
        public PowerResult<Void> leftClick(Player player, RPGItem item, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, item, stack);
        }

        @Override
        public PowerResult<Void> fire(Player player, RPGItem item, ItemStack stack) {
            Block targetBlock = player.getTargetBlock(null, getDistance());
            if (targetBlock == null) return PowerResult.noop();
            return fire(player, item, stack, targetBlock.getLocation());
        }

        @Override
        public PowerResult<Void> fire(Player player, RPGItem item, ItemStack stack, Location location) {
            if (ThreadLocalRandom.current().nextDouble(100) < getChance()) {
                if (!item.consumeDurability(player, stack, getCost())) return PowerResult.cost();
                LightContext.putTemp(player.getUniqueId(), DAMAGE_SOURCE, getPower().getNamespacedKey().toString());
                LightContext.putTemp(player.getUniqueId(), SUPPRESS_MELEE, false);
                LightContext.putTemp(player.getUniqueId(), DAMAGE_SOURCE_ITEM, stack);
                if (location.getWorld() == null) return PowerResult.fail();
                boolean explosion = location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), getExplosionPower(), false, false, player);
                LightContext.clear();
                return explosion ? PowerResult.ok() : PowerResult.fail();
            }
            return PowerResult.noop();
        }

        @Override
        public PowerResult<Void> rightClick(Player player, RPGItem item, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, item, stack);
        }

        @Override
        public PowerResult<Double> hit(Player player, RPGItem item, ItemStack stack, LivingEntity entity, double damage, EntityDamageByEntityEvent event) {
            Location location = entity.getLocation();
            Location start = player.getLocation();
            if (start.distanceSquared(location) >= getDistance() * getDistance()) {
                player.sendMessage(I18n.formatDefault("message.too.far"));
                return PowerResult.noop();
            }
            return fire(player, item, stack, location).with(damage);
        }

        @Override
        public PowerResult<Void> projectileHit(Player player, RPGItem item, ItemStack stack, ProjectileHitEvent event) {
            Projectile hit = event.getEntity();
            Location location = hit.getLocation();
            Location start = player.getLocation();
            if (start.distanceSquared(location) >= getDistance() * getDistance()) {
                player.sendMessage(I18n.formatDefault("message.too.far"));
                return PowerResult.noop();
            }
            return fire(player, item, stack, location);
        }

        @Override
        public Power getPower() {
            return Explosion.this;
        }

        @Override
        public PowerResult<Double> hitEntity(Player player, RPGItem item, ItemStack stack, LivingEntity entity, double damage, BeamHitEntityEvent event) {
            Location location = entity.getLocation();
            return fire(player, item, stack, location).with(damage);
        }

        @Override
        public PowerResult<Void> hitBlock(Player player, RPGItem item, ItemStack stack, Location location, BeamHitBlockEvent event) {
            return fire(player, item, stack, location);
        }

        @Override
        public PowerResult<Void> beamEnd(Player player, RPGItem item, ItemStack stack, Location location, BeamEndEvent event) {
            return fire(player, item, stack, location);
        }

        @Override
        public PowerResult<Void> fire(Player player, RPGItem item, ItemStack stack, LivingEntity entity, @Nullable Double value) {
            Block targetBlock = entity.getTargetBlock(null, getDistance());
            if (targetBlock == null) return PowerResult.noop();
            return fire(player, item, stack, targetBlock.getLocation());
        }
    }
}
