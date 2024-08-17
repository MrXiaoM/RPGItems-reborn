/*
 * Copyright (C) SainttX <http://sainttx.com>
 * Copyright (C) contributors
 *
 * This file is part of Auctions.
 *
 * Auctions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Auctions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Auctions.  If not, see <http://www.gnu.org/licenses/>.
 */
package think.rpgitems.utils.nyaacore.utils;

import org.bukkit.Bukkit;
import think.rpgitems.power.PlaceholderHolder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReflectionUtils {
    /*
     * The server version string to location NMS & OBC classes
     */
    private static String versionString;

    /**
     * Gets the version string for NMS &amp; OBC class paths
     *
     * @return The version string of OBC and NMS packages
     */
    public static String getVersion() {
        if (versionString == null) {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            versionString = name.substring(name.lastIndexOf('.') + 1) + ".";
        }

        return versionString;
    }

    public static Field getField(Class<?> aClass, String methodName) {
        Field getMethod;
        while (true){
            try{
                getMethod = aClass.getDeclaredField(methodName);
                break;
            }catch (NoSuchFieldException e){
                aClass = aClass.getSuperclass();
            }
            if (aClass == null){
                throw new RuntimeException("invalid placeholder");
            }
        }
        return getMethod;
    }

    public static Object getPropVal(Class<?> aClass, String propName, PlaceholderHolder placeholder) throws IllegalAccessException {
        Field getMethod = getField(aClass, propName);
        getMethod.setAccessible(true);
        return getMethod.get(placeholder);
    }

    public static void setPropVal(Class<?> aClass, String propName, PlaceholderHolder placeholder, Object value) throws IllegalAccessException {
        Field getMethod = getField(aClass, propName);
        getMethod.setAccessible(true);
        getMethod.set(placeholder, value);
    }

    /**
     * get all declared fields in a class and its' super class.
     *
     * @param clz target class
     * @return a List of Field objects declared by clz.
     * @since 7.2
     */
    public static List<Field> getAllFields(Class<?> clz) {
        List<Field> fields = new ArrayList<>();
        return getAllFields(clz, fields);
    }

    private static List<Field> getAllFields(Class<?> clz, List<Field> list) {
        Collections.addAll(list, clz.getDeclaredFields());

        Class<?> supClz = clz.getSuperclass();
        if (supClz == null) {
            return list;
        } else {
            return getAllFields(supClz, list);
        }
    }
}
