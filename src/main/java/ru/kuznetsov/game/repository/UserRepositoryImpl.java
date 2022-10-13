package ru.kuznetsov.game.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kuznetsov.game.controller.UserController;
import ru.kuznetsov.game.model.Clan;
import ru.kuznetsov.game.model.User;
import ru.kuznetsov.game.service.ClanService;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UserRepositoryImpl implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);

    private final DataSource dataSource;
    private final ClanService clanService;

    @Override
    public User getUser(long userId) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select name, wallet, clan_id from USERS where id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    User user = new User();
                    user.setId(userId);
                    while (rs.next()) {
                        user.setName(rs.getString("name"));
                        user.setWallet(rs.getString("wallet"));
                        user.setClan(clanService.getClan(rs.getLong("clan_id")));
                    }
                    return user;
                }
            }
        } catch (SQLException e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        } else {
            return update(user);
        }
    }

    private User insert(User user) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into USERS(name, wallet, clan_id) values(?,?,?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, user.getName());
                pstmt.setString(2, user.getWallet());
                Clan clan = user.getClan();
                if (clan == null) {
                    pstmt.setNull(3, Types.INTEGER);
                } else {
                    pstmt.setLong(3, clan.getId());
                }
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        long pk = rs.getLong(1);
                        user.setId(pk);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        }
        return user;
    }

    private User update(User user) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "UPDATE USERS SET name=?, wallet=?, clan_id=? WHERE id=?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, user.getName());
                pstmt.setString(2, user.getWallet());
                Clan clan = user.getClan();
                if (clan == null) {
                    pstmt.setNull(3, Types.INTEGER);
                } else {
                    pstmt.setLong(3, clan.getId());
                }
                pstmt.setLong(4, user.getId());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        }
        return user;
    }

    public boolean delete(long userId) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "DELETE from USERS WHERE id=?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, userId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Exception: ", e);
            return false;
        }
        return true;
    }

    @Override
    public Collection<User> getAll() {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select * from USERS";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    Map<Long, User> userMap = new HashMap<>();
                    while (rs.next()) {
                        long userId = rs.getLong("id");
                        User user = userMap.computeIfAbsent(userId, aLong -> new User());
                        user.setId(userId);
                        user.setName(rs.getString("name"));
                        user.setWallet(rs.getString("wallet"));
                        long clanId = rs.getLong("clan_id");
                        if (clanId > 0) {
                            user.setClan(clanService.getClan(clanId));
                        }
                    }
                    return userMap.values();
                }
            }
        } catch (SQLException e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        }
    }

}
