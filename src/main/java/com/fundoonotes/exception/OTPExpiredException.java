package com.fundoonotes.exception;

public class OTPExpiredException extends RuntimeException {
    public OTPExpiredException(String m)
    {
        super(m);
    }
}
