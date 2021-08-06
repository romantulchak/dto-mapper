package com.mapperDTO.exception;

public class CreateNewInstanceException extends RuntimeException{
    public CreateNewInstanceException(String className){
        super(String.format("Cannot create new instance of class %s", className));
    }
}
