package ru.kuznetsov.game.dto;

import lombok.Data;

@Data
public class JoinUserToClanRequest {

    private long userId;
    private long clanId;

}
