package ru.kuznetsov.game.repository;

import ru.kuznetsov.game.model.Clan;
import ru.kuznetsov.game.model.User;

import java.util.Collection;

public interface UserRepository {

    User getUser(long userId);

    User save(User user);

    boolean delete(long userId);

    Collection<User> getAll();



}
