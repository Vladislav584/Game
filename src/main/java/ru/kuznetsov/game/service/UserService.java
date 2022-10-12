package ru.kuznetsov.game.service;

import ru.kuznetsov.game.model.User;

import java.util.Collection;

public interface UserService {

    User getUser(long userId);

    Collection<User> getUsers();

    User createUser(User user);

    boolean deleteUser(long userId);

    boolean save(User user);

}
