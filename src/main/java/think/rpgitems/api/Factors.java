package think.rpgitems.api;

import think.rpgitems.RPGItems;

public class Factors {
    public static void registerFactorDefiner(IFactorDefiner definer) {
        RPGItems.plugin.cfg.factorConfig.addDefiner(definer);
    }
}
