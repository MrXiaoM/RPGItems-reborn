package think.rpgitems.utils.nyaacore.configuration.annotation;

import think.rpgitems.utils.nyaacore.configuration.Getter;
import think.rpgitems.utils.nyaacore.configuration.ISerializable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Serializer {
    Class<? extends Getter<ISerializable>> value();
}
