package com.mapperDTO.exception;

public class FieldNotDTOException extends RuntimeException {
    public FieldNotDTOException(String name) {
        super(String.format("Field %s is not marked as @DTO", name));
    }
}
