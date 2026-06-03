package com.fundoonotes.exception;
public class NoteNotFoundException extends RuntimeException
{
    public NoteNotFoundException(String m)
    {
        super(m);
    }
}
