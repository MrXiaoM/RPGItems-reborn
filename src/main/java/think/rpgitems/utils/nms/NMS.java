package think.rpgitems.utils.nms;

import org.bukkit.Bukkit;
import think.rpgitems.utils.nms.legacy.LegacyEntityTools;
import think.rpgitems.utils.nms.legacy.LegacyNBTTagTools;
import think.rpgitems.utils.nms.legacy.LegacyStackTools;
import think.rpgitems.utils.nms.v1_19_R3.EntityTools_v1_19_R3;
import think.rpgitems.utils.nms.v1_19_R3.NBTTagTools_v1_19_R3;
import think.rpgitems.utils.nms.v1_19_R3.StackTools_v1_19_R3;
import think.rpgitems.utils.nms.v1_20_R1.EntityTools_v1_20_R1;
import think.rpgitems.utils.nms.v1_20_R1.NBTTagTools_v1_20_R1;
import think.rpgitems.utils.nms.v1_20_R1.StackTools_v1_20_R1;
import think.rpgitems.utils.nms.v1_20_R2.EntityTools_v1_20_R2;
import think.rpgitems.utils.nms.v1_20_R2.NBTTagTools_v1_20_R2;
import think.rpgitems.utils.nms.v1_20_R2.StackTools_v1_20_R2;
import think.rpgitems.utils.nms.v1_20_R3.EntityTools_v1_20_R3;
import think.rpgitems.utils.nms.v1_20_R3.NBTTagTools_v1_20_R3;
import think.rpgitems.utils.nms.v1_20_R3.StackTools_v1_20_R3;

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
            boolean flag = false;
            if (starts(v, "R1_19_")) {
                stackTools = new StackTools_v1_19_R3();
                entityTools = new EntityTools_v1_19_R3();
                nbtTools = new NBTTagTools_v1_19_R3();
                flag = true;
            } else if (equals(v, "R1_20_1")) {
                stackTools = new StackTools_v1_20_R1();
                entityTools = new EntityTools_v1_20_R1();
                nbtTools = new NBTTagTools_v1_20_R1();
                flag = true;
            } else if (equals(v, "R1_20_2")) {
                stackTools = new StackTools_v1_20_R2();
                entityTools = new EntityTools_v1_20_R2();
                nbtTools = new NBTTagTools_v1_20_R2();
                flag = true;
            } else if (equals(v, "R1_20_3")) {
                stackTools = new StackTools_v1_20_R3();
                entityTools = new EntityTools_v1_20_R3();
                nbtTools = new NBTTagTools_v1_20_R3();
                flag = true;
            }
            if (flag) {
                logger.info("Your server version " + v + " is supported! Enjoy!");
            } else {
                logger.warning("Your server version " + v + " is not supported!");
                logger.warning("Now you are running in Legacy Mode. Some functions may not work.");
                stackTools = new LegacyStackTools();
                entityTools = new LegacyEntityTools();
                nbtTools = new LegacyNBTTagTools(); // TODO: 找个库来处理 nbt
            }
            return flag;
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                t.printStackTrace(pw);
            }
            logger.warning(sw.toString());
        }
        return false;
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

    public static boolean starts(String v, String... versions) {
        for (String s : versions) {
            if (v.startsWith(s)) return true;
        }
        return false;
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
            versionString = name.substring(name.lastIndexOf('.') + 1) + ".";
        }

        return versionString;
    }
}
