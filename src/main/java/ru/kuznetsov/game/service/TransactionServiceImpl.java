package ru.kuznetsov.game.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import ru.kuznetsov.game.model.Clan;
import ru.kuznetsov.game.model.Transaction;
import ru.kuznetsov.game.repository.TransactionRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public boolean saveAll(List<Transaction> transactions) {
        Map<Clan, List<Transaction>> clanAndTransactions = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getInnerClan));
        if (clanAndTransactions.entrySet().stream()
                .anyMatch(entry -> entry.getKey().getGold() +
                        entry.getValue().stream()
                                .map(Transaction::getDiffValue)
                                .mapToInt(Integer::intValue).
                                sum()
                        < 0)) {
            throw new IllegalStateException("No negative balance");
        }
        return transactionRepository.saveAll(transactions);
    }

    @Override
    public List<Transaction> getAllTransactionsByClanId(long idClan) {
        return transactionRepository.getAllTransactionsByClanId(idClan);
    }
}
