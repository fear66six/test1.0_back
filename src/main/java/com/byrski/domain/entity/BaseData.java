package com.byrski.domain.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Consumer;

/**
 * 基础数据接口，提供将自身数据转换为视图对象的通用方法。
 */
public interface BaseData {

    /**
     * 将自身数据转换为指定类型的视图对象，并允许自定义转换逻辑。
     *
     * @param clazz     目标视图对象的类类型。
     * @param consumer  自定义转换逻辑，接受目标视图对象作为参数。
     * @param <V>       目标视图对象的类型参数。
     * @return          转换后的视图对象。
     */
    default <V> V asViewObject(Class<V> clazz, Consumer<V> consumer) {
        V v = this.asViewObject(clazz);
        consumer.accept(v);
        return v;
    }

    /**
     * 将自身数据转换为指定类型的视图对象。
     * 通过反射机制将自身对象的字段值复制到目标视图对象中。
     *
     * @param clazz 目标视图对象的类类型。
     * @param <V>   目标视图对象的类型参数。
     * @return      转换后的视图对象。
     */
    default <V> V asViewObject(Class<V> clazz) {
        try {
            Field[] declaredFields = clazz.getDeclaredFields();
            Constructor<V> constructor = clazz.getConstructor();
            V v = constructor.newInstance();
            for (Field declaredField : declaredFields) {
                convert(declaredField, v);
            }
            return v;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 将源对象字段的值复制到目标对象字段中。
     *
     * @param field 目标对象的字段。
     * @param vo    目标对象。
     */
    private void convert(Field field, Object vo) {
        try {
            Field source = this.getClass().getDeclaredField(field.getName());
            field.setAccessible(true);
            source.setAccessible(true);
            field.set(vo, source.get(this));
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
            //忽略异常，可能字段名不一致
        }
    }
}
