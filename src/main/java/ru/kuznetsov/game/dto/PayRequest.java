package ru.kuznetsov.game.dto;

import lombok.Data;

@Data
public class PayRequest {

    private long idUser;
    private long idClan;
    private int replenishment;

}
