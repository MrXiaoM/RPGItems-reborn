package think.rpgitems.gui.editor;

public class Enums {

    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        return valueOf(enumType, name, null);
    }

    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name, T nullValue) {
        for (T t : enumType.getEnumConstants()) {
            if (t.name().equalsIgnoreCase(name))
                return t;
        }
        return nullValue;
    }

    public static Object valueOfForce(Class<?> enumType, String name) {
        return valueOfForce(enumType, name, null);
    }

    public static Object valueOfForce(Class<?> enumType, String name, Object nullValue) {
        for (Object t : enumType.getEnumConstants()) {
            if (((Enum<?>) t).name().equalsIgnoreCase(name))
                return t;
        }
        return nullValue;
    }
}
