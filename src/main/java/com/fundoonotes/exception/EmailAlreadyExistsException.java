package com.fundoonotes.exception;
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String m){
        super(m);
    }
}
