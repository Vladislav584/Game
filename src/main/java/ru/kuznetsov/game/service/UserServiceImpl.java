package ru.kuznetsov.game.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import ru.kuznetsov.game.model.Clan;
import ru.kuznetsov.game.model.User;
import ru.kuznetsov.game.repository.UserRepository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UserServiceImpl implements UserService {

    private final static Map<Long, User> CACHE = new ConcurrentHashMap<>();

    private final UserRepository userRepository;

    @Override
    public User getUser(long userId) {
        return CACHE.computeIfAbsent(userId, integer -> userRepository.getUser(userId));
    }

    @Override
    public Collection<User> getUsers() {
        Collection<User> users = userRepository.getAll();
        users.forEach(it -> CACHE.putIfAbsent(it.getId(), it));
        return users;
    }

    @Override
    public User createUser(User user) {
        User savedUser = userRepository.save(user);
        CACHE.putIfAbsent(savedUser.getId(), savedUser);//это безопасно, т.к. есть констраинт на уровне БД
        return savedUser;
    }

    @Override
    public boolean deleteUser(long userId) {
        return userRepository.delete(userId);
    }

    @Override
    public boolean save(User user) {
        userRepository.save(user);
        return true;
    }
}
