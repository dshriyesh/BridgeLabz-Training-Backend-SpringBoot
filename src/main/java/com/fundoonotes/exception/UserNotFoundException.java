package com.fundoonotes.exception;
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String m){
        super(m);
    }
}
