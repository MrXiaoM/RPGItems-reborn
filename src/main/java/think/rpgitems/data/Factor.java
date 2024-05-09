package think.rpgitems.data;

import java.util.Map;

public class Factor {
    public final String id;
    public final String name;
    public final Map<String, String> damageTo;

    public Factor(String id, String name, Map<String, String> damageTo) {
        this.id = id;
        this.name = name;
        this.damageTo = damageTo;
    }
}

