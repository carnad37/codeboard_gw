package com.hhs.codeboard.gw.expt;

public class AlreadyHasAuthTokenException extends RuntimeException{

    public AlreadyHasAuthTokenException(String message) {
        super(message);
    }
}
