package com.project.InventoryMgtSys.exceptions;

public class NameValueRequiredException extends RuntimeException {
    public NameValueRequiredException(String message){
        super(message);
    }
}
