package com.armdoctor.exceptions;

public class UserValidationException extends ReflectiveOperationException{
    public UserValidationException (String message) {super(message);}
}
