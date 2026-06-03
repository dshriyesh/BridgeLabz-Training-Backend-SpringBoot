package com.fundoonotes.exception;
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String m){
        super(m);
    }
}
