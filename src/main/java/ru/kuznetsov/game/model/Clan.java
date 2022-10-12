package ru.kuznetsov.game.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Clan {

    private Long id;
    private String name;
    private int gold;//логичнее использовать тип данных с плавающй точкой. но оставим как в примере

    @Setter(AccessLevel.PRIVATE)
    private Map<Long, User> users = new ConcurrentHashMap<>();

    public void joinInClan(User user) {
        synchronized (this) {
            users.putIfAbsent(user.getId(), user);
            user.setClan(this);
        }
    }

    public void leaveClan(User user) {
        synchronized (this) {
            users.remove(user.getId());
            user.setClan(null);
        }
    }

    public Collection<User> getUsers() {
        return users.values();
    }

    public int changeGold(long userId, int diff) {
        synchronized (this) {
            gold = gold + diff;
            return gold;
        }
    }

}
