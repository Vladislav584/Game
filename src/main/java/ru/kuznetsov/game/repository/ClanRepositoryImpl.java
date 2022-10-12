package ru.kuznetsov.game.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kuznetsov.game.controller.UserController;
import ru.kuznetsov.game.model.Clan;
import ru.kuznetsov.game.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//@Log4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClanRepositoryImpl implements ClanRepository {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final DataSource dataSource;

    @Override
    public Clan getClan(long clanId) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select c.id as clan_id, c.name as clan_name, COALESCE(t.gold_value, 0) as clan_gold,\n" +
                    "u.id as user_id, u.name as user_name, u.wallet\n" +
                    "from CLAN c \n" +
                    "left join USERS u on c.id = u.clan_id \n" +
                    "left join TRANSACTION t on c.id = t.clan_id\n" +
                    "where c.id=?\n" +
                    "and (t.id = (select max(t1.id) from TRANSACTION t1 where t1.clan_id=c.id) or ISNULL(t.create_date))";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, clanId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    Clan clan = new Clan();
                    while (rs.next()) {
                        clan.setId(rs.getLong("clan_id"));
                        clan.setName(rs.getString("clan_name"));
                        clan.setGold(rs.getInt("clan_gold"));
                        long userId = rs.getLong("user_id");
                        if (userId != 0) {
                            User user = new User();
                            user.setId(userId);
                            user.setName(rs.getString("user_name"));
                            user.setWallet(rs.getString("wallet"));
                            clan.joinInClan(user);
                        }
                    }
                    return clan;
                }
            }
        } catch (SQLException e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Clan save(Clan clan) {
        if (clan.getId() == null) {
            return insert(clan);
        } else {
            return update(clan);
        }
    }

    private Clan insert(Clan clan) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into CLAN(name) values(?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, clan.getName());
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        long pk = rs.getLong(1);
                        clan.setId(pk);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        }
        return clan;
    }

    private Clan update(Clan clan) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "UPDATE CLAN SET name=? WHERE id=?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, clan.getName());
                pstmt.setLong(2, clan.getId());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        }
        return clan;
    }

    public Clan delete(long clanId) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "DELETE from CLAN WHERE id=?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, clanId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Collection<Clan> getAll() {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select c.id as clan_id, c.name as clan_name, COALESCE(t.gold_value, 0) as clan_gold,\n" +
                    "u.id as user_id, u.name as user_name, u.wallet\n" +
                    "from CLAN c \n" +
                    "left join USERS u on c.id = u.clan_id \n" +
                    "left join TRANSACTION t on c.id = t.clan_id\n" +
                    "where t.id = (select max(t1.id) from TRANSACTION t1 where t1.clan_id=c.id) or " +
                    "ISNULL(t.create_date)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    Map<Long, Clan> clanMap = new HashMap<>();
                    while (rs.next()) {
                        long clanId = rs.getLong("clan_id");
                        Clan clan = clanMap.computeIfAbsent(clanId, aLong -> new Clan());
                        clan.setId(clanId);
                        clan.setName(rs.getString("clan_name"));
                        clan.setGold(rs.getInt("clan_gold"));
                        long userId = rs.getLong("user_id");
                        if (userId != 0) {
                            User user = new User();
                            user.setId(userId);
                            user.setName(rs.getString("user_name"));
                            user.setWallet(rs.getString("wallet"));
                            clan.joinInClan(user);
                        }
                    }
                    return clanMap.values();
                }
            }
        } catch (SQLException e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        }
    }
}
