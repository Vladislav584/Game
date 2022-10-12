package ru.kuznetsov.game.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Constants {

    public static final String APPLICATION_JSON = "application/json";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

}
