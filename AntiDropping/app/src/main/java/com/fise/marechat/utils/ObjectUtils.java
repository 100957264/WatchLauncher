package com.fise.marechat.utils;

import java.lang.reflect.Field;

/**
 * 对象反射相关工具类
 */
public class ObjectUtils {

    /**
     * 设置对象指定字段值
     * @param obj
     * @param fieldName
     * @param fieldValue
     * @param <T>
     * @return
     */
    public static <T> boolean setObjField(T obj, String fieldName, Object fieldValue) {
        if (null == obj)
            return false;

        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);

            Class<?> clazz = f.getType();
            if (clazz == int.class) {
                f.setInt(obj, ((Integer)fieldValue).intValue());
            } else if (clazz == long.class) {
                f.setLong(obj, ((Long)fieldValue).longValue());
            } else if (clazz == float.class) {
                f.setFloat(obj, ((Float)fieldValue).floatValue());
            } else if (clazz == double.class) {
                f.setDouble(obj, ((Double)fieldValue).doubleValue());
            } else if (clazz == boolean.class) {
                f.setBoolean(obj, ((Boolean)fieldValue).booleanValue());
            } else if (clazz == char.class) {
                f.setChar(obj, ((Character)fieldValue).charValue());
            } else if (clazz == byte.class) {
                f.setByte(obj, ((Byte)fieldValue).byteValue());
            } else if (clazz == short.class) {
                f.setShort(obj, ((Short)fieldValue).shortValue());
            } else if (null == fieldValue || clazz.isAssignableFrom(fieldValue.getClass())) {
                f.set(obj, fieldValue);
            } else {
                return false;
            }

            return true;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return false;
    }
}
