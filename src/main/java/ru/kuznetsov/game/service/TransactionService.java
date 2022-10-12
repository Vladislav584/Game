package ru.kuznetsov.game.service;

import ru.kuznetsov.game.model.Transaction;

import java.util.List;

public interface TransactionService {

    boolean saveAll(List<Transaction> transactions);

    List<Transaction> getAllTransactionsByClanId(long idClan);
}
