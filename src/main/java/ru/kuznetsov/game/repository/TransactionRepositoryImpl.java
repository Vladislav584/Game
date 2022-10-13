package ru.kuznetsov.game.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kuznetsov.game.controller.UserController;
import ru.kuznetsov.game.model.Transaction;
import ru.kuznetsov.game.service.ClanService;
import ru.kuznetsov.game.service.UserService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TransactionRepositoryImpl implements TransactionRepository {

    private static final Logger log = LoggerFactory.getLogger(TransactionRepositoryImpl.class);
    private final DataSource dataSource;
    private final ClanService clanService;
    private final UserService userService;

    @Override
    public List<Transaction> getAllTransactionsByClanId(long idClan) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select * from TRANSACTION t where t.clan_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, idClan);
                try (ResultSet rs = pstmt.executeQuery()) {
                    List<Transaction> transactions = new ArrayList<>();
                    while (rs.next()) {
                        Transaction transaction = new Transaction();
                        transaction.setId(rs.getLong("id"));
                        transaction.setInnerClan(clanService.getClan(rs.getLong("clan_id")));
                        transaction.setUser(userService.getUser(rs.getLong("user_id")));
                        transaction.setDiffValue(rs.getInt("diff_gold_value"));
                        transaction.setGoldValue(rs.getInt("gold_value"));
                        transaction.setCreateDate(rs.getDate("create_date"));
                        transactions.add(transaction);
                    }
                    return transactions;
                }
            }
        } catch (SQLException e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    //only insert
    @Override
    public boolean saveAll(List<Transaction> transactions) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into TRANSACTION(clan_id, user_id, diff_gold_value, gold_value) values(?,?,?,?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (Transaction transaction : transactions) {
                    pstmt.setLong(1, transaction.getInnerClan().getId());
                    pstmt.setLong(2, transaction.getUser().getId());
                    pstmt.setLong(3, transaction.getDiffValue());
                    pstmt.setLong(4, transaction.getGoldValue());
                    pstmt.addBatch();
                }
                int i[] = pstmt.executeBatch();
                return true;//fixme
            }
        } catch (SQLException e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        }
    }
}
