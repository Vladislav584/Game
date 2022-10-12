package ru.kuznetsov.game.repository;

import ru.kuznetsov.game.model.Transaction;

import java.util.List;

public interface TransactionRepository {
    List<Transaction> getAllTransactionsByClanId(long idClan);

    boolean saveAll(List<Transaction> transactions);
}
