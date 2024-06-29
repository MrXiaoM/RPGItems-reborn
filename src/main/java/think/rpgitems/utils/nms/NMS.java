package think.rpgitems.utils.nms;

import org.bukkit.Bukkit;
import think.rpgitems.utils.nms.legacy.LegacyEntityTools;
import think.rpgitems.utils.nms.legacy.LegacyNBTTagTools;
import think.rpgitems.utils.nms.legacy.LegacyStackTools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class NMS {
    Logger logger;
    IStackTools stackTools;
    IEntityTools entityTools;
    INBTTagTools nbtTools;
    private static String versionString;
    private static NMS inst = null;
    private NMS(Logger logger) {
        this.logger = logger;
    }

    public static boolean init(Logger logger) {
        if (inst != null) return true;
        inst = new NMS(logger);
        return inst.init();
    }
    public boolean init() {
        try {
            String v = getVersion();
            if (load(v)) {
                logger.info("Your server version " + v + " is supported! Enjoy!");
                return true;
            } else {
                logger.warning("Your server version " + v + " is not supported!");
                logger.warning("Now you are running in Legacy Mode. Some functions may not work.");
                stackTools = new LegacyStackTools();
                entityTools = new LegacyEntityTools();
                nbtTools = new LegacyNBTTagTools(); // TODO: 找个库来处理 nbt
            }
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                t.printStackTrace(pw);
            }
            logger.warning(sw.toString());
        }
        return false;
    }

    public boolean load(String nms) throws ReflectiveOperationException {
        try {
            String root = "think.rpgitems.utils.nms." + nms + ".";
            Class<?> classStackTools = Class.forName(root + "StackTools_" + nms);
            Class<?> classEntityTools = Class.forName(root + "EntityTools_" + nms);
            Class<?> classNBTTagTools = Class.forName(root + "NBTTagTools_" + nms);
            stackTools = (IStackTools) classStackTools.getConstructor().newInstance();
            entityTools = (IEntityTools) classEntityTools.getConstructor().newInstance();
            nbtTools = (INBTTagTools) classNBTTagTools.getConstructor().newInstance();
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static IStackTools stackTools() {
        return inst.stackTools;
    }

    public static IEntityTools entityTools() {
        return inst.entityTools;
    }

    public static INBTTagTools nbtTools() {
        return inst.nbtTools;
    }

    public static boolean equals(String v, String... versions) {
        for (String s : versions) {
            if (v.equals(s)) return true;
        }
        return false;
    }

    public static String getVersion() {
        if (versionString == null) {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            String v = name.substring(name.lastIndexOf('.') + 1);
            String bukkitVersion = Bukkit.getBukkitVersion();
            if (v.startsWith("v1_")) {
                versionString = v;
            } else if (bukkitVersion.contains("-")) {
                String ver = bukkitVersion.substring(0, bukkitVersion.indexOf('-'));
                versionString = switch (ver) {
                    case "1.20.5", "1.20.6" -> "v1_20_R4";
                    default -> "v" + ver.replaceFirst("\\.", "_").replace(".", "_R");
                };
            } else {
                throw new IllegalStateException("Unsupported new server version " + Bukkit.getBukkitVersion());
            }
        }

        return versionString;
    }
}
