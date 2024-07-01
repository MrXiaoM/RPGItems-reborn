package think.rpgitems.utils.nyaacore.utils;

import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class RayTraceUtils {

    public static List<LivingEntity> rayTraceEntities(Player player, float distance) {
        return rayTraceEntities(player, distance, not(player).and(canInteract()));
    }

    public static List<LivingEntity> rayTraceEntities(LivingEntity player, float distance, Predicate<Entity> predicate) {
        List<LivingEntity> result = new ArrayList<>();
        Vector start = player.getEyeLocation().toVector();
        Vector end = start.clone().add(player.getEyeLocation().getDirection().multiply(distance));
        for (Entity e : player.getWorld().getNearbyEntities(player.getEyeLocation(), distance, distance, distance, predicate)) {
            if (e instanceof LivingEntity && e.isValid()) {
                Optional<Vector> hit = clip(e.getBoundingBox(), start, end);
                if (hit.isPresent()) {
                    result.add((LivingEntity) e);
                }
            }
        }
        return result;
    }
    public static Optional<Vector> clip(BoundingBox box, Vector var0, Vector var1) {
        double[] var2 = new double[]{1.0};
        double var3 = var1.getX() - var0.getX();
        double var5 = var1.getY() - var0.getY();
        double var7 = var1.getZ() - var0.getZ();
        boolean var9 = a(box, var0, var2, var3, var5, var7);
        if (var9) {
            return Optional.empty();
        } else {
            double var10 = var2[0];
            return Optional.of(var0.add(new Vector(var10 * var3, var10 * var5, var10 * var7)));
        }
    }

    private static boolean a(BoundingBox box, Vector var1, double[] var2, double var4, double var6, double var8) {
        double a = box.getMinX();
        double b = box.getMinY();
        double c = box.getMinZ();
        double d = box.getMaxX();
        double e = box.getMaxY();
        double f = box.getMaxZ();
        boolean var3 = true;
        if (var4 > 1.0E-7) {
            var3 = a(var2, var3, var4, var6, var8, a, b, e, c, f, var1.getX(), var1.getY(), var1.getZ());
        } else if (var4 < -1.0E-7) {
            var3 = a(var2, var3, var4, var6, var8, d, b, e, c, f, var1.getX(), var1.getY(), var1.getZ());
        }

        if (var6 > 1.0E-7) {
            var3 = a(var2, var3, var6, var8, var4, b, c, f, a, d, var1.getY(), var1.getZ(), var1.getX());
        } else if (var6 < -1.0E-7) {
            var3 = a(var2, var3, var6, var8, var4, e, c, f, a, d, var1.getY(), var1.getZ(), var1.getX());
        }

        if (var8 > 1.0E-7) {
            var3 = a(var2, var3, var8, var4, var6, c, a, d, b, e, var1.getZ(), var1.getX(), var1.getY());
        } else if (var8 < -1.0E-7) {
            var3 = a(var2, var3, var8, var4, var6, f, a, d, b, e, var1.getZ(), var1.getX(), var1.getY());
        }

        return var3;
    }

    private static boolean a(double[] var0, boolean var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, double var16, double var19, double var21, double var23) {
        double var25 = (var8 - var19) / var2;
        double var27 = var21 + var25 * var4;
        double var29 = var23 + var25 * var6;
        if (0.0 < var25 && var25 < var0[0] && var10 - 1.0E-7 < var27 && var27 < var12 + 1.0E-7 && var14 - 1.0E-7 < var29 && var29 < var16 + 1.0E-7) {
            var0[0] = var25;
            return false;
        } else {
            return var1;
        }
    }

    public static Predicate<Entity> not(Entity e) {
        return entity -> !entity.getUniqueId().equals(e.getUniqueId());
    }

    public static Predicate<Entity> canInteract() {
        return input -> {
            if (input instanceof Player && ((Player) input).getGameMode() == GameMode.SPECTATOR) {
                return false;
            }
            return input instanceof LivingEntity && ((LivingEntity) input).isCollidable();
        };
    }

    public static Entity getTargetEntity(Player p) {
        return getTargetEntity(p, getDistanceToBlock(p, p.getGameMode() == GameMode.CREATIVE ? 6.0F : 4.5F));
    }

    public static float getDistanceToBlock(LivingEntity entity, float maxDistance) {
        RayTraceResult r = entity.getWorld().rayTraceBlocks(entity.getEyeLocation(), entity.getEyeLocation().getDirection(), maxDistance);
        if (r != null) {
            return (float) entity.getEyeLocation().distance(r.getHitPosition().toLocation(entity.getWorld()));
        }
        return maxDistance;
    }

    public static Entity getTargetEntity(LivingEntity entity, float maxDistance) {
        RayTraceResult r = entity.getWorld().rayTraceEntities(entity.getEyeLocation(), entity.getEyeLocation().getDirection(), maxDistance,
                e -> (e instanceof LivingEntity || e instanceof ItemFrame) &&
                        !(e instanceof LivingEntity && !((LivingEntity) e).isCollidable()) &&
                        e.getUniqueId() != entity.getUniqueId() &&
                        !(e instanceof Player && ((Player) e).getGameMode() == GameMode.SPECTATOR));
        if (r != null) {
            return r.getHitEntity();
        }
        return null;
    }
}
