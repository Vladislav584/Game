package ru.kuznetsov.game.exception;

public class ParseConfigException extends RuntimeException {
    public ParseConfigException(String errorMsg) {
        super(errorMsg);
    }
}
