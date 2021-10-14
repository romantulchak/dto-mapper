package com.mapperDTO.mapper;

import com.mapperDTO.annotation.DTO;
import com.mapperDTO.annotation.MapToDTO;
import com.mapperDTO.exception.CollectionEmptyException;
import com.mapperDTO.exception.CreateClassInstanceException;
import com.mapperDTO.exception.FieldNotDTOException;
import org.hibernate.Hibernate;
import org.hibernate.collection.internal.PersistentBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import static com.mapperDTO.mapper.ClassUtility.newInstanceOfType;


@Component
public final class EntityMapper {

    private final Logger logger = LoggerFactory.getLogger(EntityMapper.class);

    private final Collection<Class<?>> collections = new ArrayList<>();
    private Class<?> classToCheck;

    public EntityMapper() {
        collections.add(List.class);
        collections.add(Set.class);
        collections.add(Map.class);
        collections.add(Queue.class);
        collections.add(Collection.class);
    }


    private <T, R> void handleExistsField(T entity, R dto, List<Field> entityFields, Field field) {
        for (Field entityField : entityFields) {
            try {
                entityField.setAccessible(true);
                if (checkAssociatedField(field, entityField) && checkAnnotation(field, classToCheck)) {
                    logger.debug("field {} match to field {}", field.getName(), entityField.getName());
                    Object value = entityField.get(entity);
                    entityFields.remove(entityField);
                    if (value != null) {
                         field.setAccessible(true);
                        if (isDTOField(field)) {
                            value = setInternalDTOFields(value, field.getType(), field);
                            logger.debug("field {} marked as @DTO", field.getName());
                        }
                        logger.debug("received entity field value {}", value);
                        field.set(dto, value);
                    }
                    break;
                }
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage());
                throw new FieldNotDTOException(field.getName());
            }
        }
    }

    private boolean checkAssociatedField(Field field, Field fieldToCompare) {
        MapToDTO annotation = field.getDeclaredAnnotation(MapToDTO.class);
        if (annotation != null && !annotation.associatedField().isEmpty()) {
            logger.debug("is annotation associatedField {} match to compared field name {}", field.getName(), fieldToCompare.getName());
            return annotation.associatedField().equals(fieldToCompare.getName());
        } else {
            logger.debug("is field name {} match to compared field name {} ?", field.getName(), fieldToCompare.getName());
        }
        return field.getName().equals(fieldToCompare.getName());
    }

    private boolean checkAnnotation(Field field, Class<?> classToCheck) {
        MapToDTO declaredAnnotation = field.getDeclaredAnnotation(MapToDTO.class);
        logger.debug("is field {} declared annotation? ", declaredAnnotation);
        if (declaredAnnotation != null && declaredAnnotation.mapClass().length != 0) {
            List<Class<?>> classes = Arrays.asList(declaredAnnotation.mapClass());
            return field.isAnnotationPresent(MapToDTO.class) && classes.contains(classToCheck);
        }
        return false;
    }

    private boolean isDTOField(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            logger.debug("generic type - {}", genericType.getTypeName());
            Class<?> actualTypeArgument = (Class<?>) genericType.getActualTypeArguments()[0];
            return actualTypeArgument.isAnnotationPresent(DTO.class);
        }
        return field.getType().isAnnotationPresent(DTO.class);
    }

    private <T> Object setInternalDTOFields(T entity, Class<?> type, Field actualField) {
        Collection<?> typeOfCollection = getTypeOfCollection(entity);
        logger.debug("is collection types is null? {}", typeOfCollection == null);
        if (typeOfCollection != null) {
            return mapPersistentBagOfElements(typeOfCollection, type, actualField);
        } else {
            return mapSingleObject(entity, type);
        }
    }


    private <T> boolean checkObjectInstance(T entity, Class<?> obj) {
        return obj.isInstance(entity);
    }

    private <T> Object mapSingleObject(T entity, Class<?> type) {
        List<Field> entityFields = new LinkedList<>(Arrays.asList(entity.getClass().getDeclaredFields()));
        logger.debug("Entity field size - {}", entityFields.size());
        Object internalDto = newInstanceOfType(type);
        List<Field> dtoFields = Arrays.asList(type.getDeclaredFields());
        handleFields(entity, entityFields, internalDto, dtoFields);
        return internalDto;
    }

    private Collection<Object> mapPersistentBagOfElements(Collection<?> collectionElements, Class<?> type, Field actualField) {
        if (Hibernate.isInitialized(collectionElements) && !collectionElements.isEmpty()) {
            ParameterizedType genericType = (ParameterizedType) actualField.getGenericType();
            logger.debug("Collection elements is initialized ? {}", Hibernate.isInitialized(collectionElements));
            logger.debug("Generic type - {}", genericType.getTypeName());
            logger.debug("Collection elements size - {}", collectionElements.size());
            Collection<Object> collection = newCollectionInstance(type);
            type = (Class<?>) genericType.getActualTypeArguments()[0];
            for (Object object : collectionElements) {
                List<Field> entityFields = new LinkedList<>(Arrays.asList(collectionElements
                        .stream()
                        .findFirst()
                        .orElseThrow(CollectionEmptyException::new)
                        .getClass()
                        .getDeclaredFields()));
                Object internalDto = newInstanceOfType(type);
                logger.debug("new internal object type - {}", internalDto.getClass());
                List<Field> dtoFields = Arrays.asList(type.getDeclaredFields());
                handleFields(object, entityFields, internalDto, dtoFields);
                collection.add(internalDto);
            }
            return collection;
        }
        return Collections.emptyList();
    }

    /**
     * Converts an entity field to a DTO field
     *
     * @param entity       entity (model)
     * @param entityFields list of fields that are in Entity or in parent Entity
     * @param dto          instance of DTO class
     * @param dtoFields    list of fields that are in DTO or in parent DTO
     */
    public <T> void handleFields(T entity, List<Field> entityFields, Object dto, List<Field> dtoFields) {
        for (Field field : dtoFields) {
            logger.debug("handle field - {}", field.getName());
            handleExistsField(entity, dto, entityFields, field);
        }
    }


    private Collection<Object> newCollectionInstance(Class<?> clazz) {
        if (clazz.isAssignableFrom(List.class)) {
            return new ArrayList<>();
        } else if (clazz.isAssignableFrom(Set.class)) {
            return new HashSet<>();
        } else if (clazz.isAssignableFrom(Queue.class)) {
            return new PriorityQueue<>();
        }
        throw new CreateClassInstanceException(clazz.getTypeName());
    }

    private <T> Collection<?> getTypeOfCollection(T entity) {
        if (checkObjectInstance(entity, PersistentBag.class)) {
            logger.debug("object is persistent bag");
            return (PersistentBag) entity;
        } else if (checkObjectInstance(entity, List.class)) {
            logger.debug("object is list");
            return (List<?>) entity;
        } else if (checkObjectInstance(entity, Set.class)) {
            logger.debug("object is set");
            return (Set<?>) entity;
        }
        return null;
    }

    public void setClassToCheck(Class<?> classToCheck) {
        this.classToCheck = classToCheck;
    }
}
