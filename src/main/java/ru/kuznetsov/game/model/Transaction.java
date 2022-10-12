package ru.kuznetsov.game.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
@RequiredArgsConstructor
public class Transaction {

    private Long id;
    @JsonIgnore
    private Clan innerClan;
    private User user;
    private int diffValue;
    private int goldValue;
    private Date createDate;

    public Long getClanId() {
        return innerClan.getId();
    }

}
