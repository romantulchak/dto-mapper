package com.mapperDTO.exception;

public class ClassDefaultConstructorException extends RuntimeException {
    public ClassDefaultConstructorException(String typeName) {
        super(String.format("Class %s does no default constructor without any parameters", typeName));
    }
}
