package com.mapperDTO.mapper;

import com.mapperDTO.exception.ClassDefaultConstructorException;
import com.mapperDTO.exception.CreateClassInstanceException;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public final class ClassUtility {

    private ClassUtility(){}

    /**
     * Create instance of class
     * @param clazz - an instance class that will be created using a simple constructor
     * @return new instance of class
     */
    public static Object newInstanceOfType(Class<?> clazz) {
        try {
            boolean hasDefaultConstructor = Arrays.stream(clazz.getConstructors()).anyMatch(c -> c.getParameterCount() == 0);
            if (hasDefaultConstructor) {
                return clazz.getConstructor().newInstance();
            }
            throw new ClassDefaultConstructorException(clazz.getTypeName());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new CreateClassInstanceException(clazz.getTypeName());
        }
    }
}
