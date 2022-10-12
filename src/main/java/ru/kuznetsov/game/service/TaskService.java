package ru.kuznetsov.game.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import ru.kuznetsov.game.model.Clan;

// Еще один такой сервис
// какой-то сервис с заданиями
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TaskService {

    private final ClanService clanService;

    void completeTask(long clanId, long taskId) {
        // ...
//        if (success) {
        int gold = -10;
        Clan clan = clanService.getClan(clanId);
//        clan.changeGold(gold);
        clanService.save(clan);
//        }
    }
}
