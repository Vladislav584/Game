package ru.kuznetsov.game.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "clan")
public class User {

    private Long id;
    private String name;
    private String wallet;
    @JsonIgnore
    private Clan clan;

    public long getClanId() {
        return clan.getId();
    }

}
