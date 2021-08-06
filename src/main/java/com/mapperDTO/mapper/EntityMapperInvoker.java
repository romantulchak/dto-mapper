package com.mapperDTO.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.mapperDTO.mapper.ClassUtility.newInstanceOfType;

/***
 * Use this class to map your Entity to DTO
 * @param <E> Entity class
 * @param <D> DTO class
 */

@Component
public final class EntityMapperInvoker<E, D> {

    private EntityMapper entityMapper;

    /**
     * Convert entity to DTO class
     * @param entity entity class (model)
     * @param dtoClass DTO class
     * @param classToCheck class that using in @{@link com.mapperDTO.annotation.MapToDTO} mapClass property
     * @return
     */
    @SuppressWarnings("unchecked")
    public D entityToDTO(E entity, Class<?> dtoClass, Class<?> classToCheck) {
        D dto = (D) newInstanceOfType(dtoClass);
        Field[] dtoFields = dto.getClass().getDeclaredFields();
        List<Field> entityFields = new LinkedList<>(Arrays.asList(entity.getClass().getDeclaredFields()));
        entityMapper.setClassToCheck(classToCheck);
        entityMapper.handleFields(entity, entityFields, dto, dtoFields);
        return dto;
    }

    @Autowired
    public void setEntityMapper(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }
}
