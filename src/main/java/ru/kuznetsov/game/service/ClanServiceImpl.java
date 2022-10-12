package ru.kuznetsov.game.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kuznetsov.game.controller.UserController;
import ru.kuznetsov.game.dto.ClanRequest;
import ru.kuznetsov.game.model.Clan;
import ru.kuznetsov.game.model.Transaction;
import ru.kuznetsov.game.model.User;
import ru.kuznetsov.game.repository.ClanRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClanServiceImpl implements ClanService {

    private final static Map<Long, Clan> CACHE = new ConcurrentHashMap<>();
    private final ClanRepository clanRepository;
    private final TransactionService transactionService;
    private final UserService userService;

    @Override
    public Collection<Clan> getClans() {
        Collection<Clan> clans = clanRepository.getAll();
        clans.forEach(it -> CACHE.putIfAbsent(it.getId(), it));
        return clans.stream()
                .map(OuterClan::new)
                .collect(Collectors.toList());
    }

    @Override
    public Clan getClan(long clanId) {
        Clan innerClan = CACHE.computeIfAbsent(clanId, integer -> clanRepository.getClan(clanId));
        return new OuterClan(innerClan);
    }

    @Override
    public Clan createClan(ClanRequest clanRequest) {
        Clan clan = new Clan();
        clan.setName(clanRequest.getName());
        clan.setGold(0);
        Clan savedClan = clanRepository.save(clan);
        CACHE.putIfAbsent(savedClan.getId(), savedClan);//это безопасно, т.к. есть констраинт на уровне БД
        return new OuterClan(savedClan);
    }

    @Override
    public boolean deleteClan(long clanId) {
        clanRepository.delete(clanId);
        return true;
    }

    @Override
    public boolean save(Clan clan) {
        if (clan instanceof OuterClan) {
            OuterClan outerClan = (OuterClan) clan;
            synchronized (outerClan.innerClan) {
                if (!outerClan.getName().equals(outerClan.innerClan.getName())) {
                    clanRepository.save(outerClan);
                }
                //fixme: проверить есть ли изменения в наборе юзеров
                outerClan.innerClan.getUsers().forEach(userService::save);

                List<Transaction> transactions = outerClan.getTransactions();
                boolean savedTransactions = transactionService.saveAll(transactions);
                if (savedTransactions) {
                    Map<Clan, List<Transaction>> clanListMap = transactions.stream().collect(Collectors.groupingBy(Transaction::getInnerClan));
                    clanListMap.forEach((cl, tr) -> tr.forEach(it -> cl.changeGold(it.getUser().getId(), it.getDiffValue())));
                    transactions.clear();
                }
                return savedTransactions;
            }
        } else {
            synchronized (clan) {//транзакция на уровне бд еще нужна
                boolean isSave = clanRepository.save(clan) != null;
                clan.getUsers().forEach(userService::save);
                return isSave;
            }
        }
    }


    private class OuterClan extends Clan {

        @JsonIgnore
        private final List<Transaction> transactions = new ArrayList<>();

        @JsonIgnore
        private final Clan innerClan;
        @JsonIgnore
        private String newName;

        public OuterClan(Clan innerClan) {
            this.innerClan = innerClan;
        }

        @Override
        public Long getId() {
            return innerClan.getId();
        }

        @Override
        public String getName() {
            return newName == null ? innerClan.getName() : newName;
        }

        @Override
        public void setName(String name) {
            this.newName = name;
        }

        //мной предполагается, что объект OuterClan не будет шариться между потоками
        //(в сервисы не будут передаваться в другие потоки данный объект, а шариться будет только innerClan),
        //поэтому считаем этот метод атомарной операцией
        @Override
        public int getGold() {
            int actualGold = innerClan.getGold();
            return actualGold + getInnerTransactionValue();
        }

        @Override
        public int changeGold(long userId, int diff) {
            Transaction transaction = new Transaction();
            transaction.setInnerClan(innerClan);
            transaction.setDiffValue(diff);
            transaction.setGoldValue(getGold() + diff);
            transaction.setUser(ClanServiceImpl.this.userService.getUser(userId));
            transactions.add(transaction);
            return getGold();
        }

        @Override
        public void joinInClan(User user) {
            innerClan.joinInClan(user);
        }

        @Override
        public void leaveClan(User user) {
            innerClan.leaveClan(user);
        }

        @Override
        public Collection<User> getUsers() {
            return innerClan.getUsers();
        }

        private List<Transaction> getTransactions() {
            return transactions;
        }

        private int getInnerTransactionValue() {
            return transactions.stream()
                    .map(Transaction::getDiffValue)
                    .mapToInt(Integer::intValue)
                    .sum();
        }

    }
}
