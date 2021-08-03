package com.mapperDTO.exception;

public class CollectionEmptyException extends RuntimeException{
    public CollectionEmptyException(){
        super("Collection is empty");
    }
}
