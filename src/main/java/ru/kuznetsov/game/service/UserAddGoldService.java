package ru.kuznetsov.game.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import ru.kuznetsov.game.dto.PayRequest;
import ru.kuznetsov.game.exception.PersistentException;
import ru.kuznetsov.game.model.Clan;

// Так же у нас есть ряд сервисов похожих на эти.
// Их суть в том, что они добавляют(или уменьшают) золото в казне клана
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UserAddGoldService { // пользователь добавляет золото из собственного кармана

    private final ClanService clanService;

    public void addGoldToClan(PayRequest payRequest) {
        Clan clan = clanService.getClan(payRequest.getIdClan());
        clan.changeGold(payRequest.getIdUser(), payRequest.getReplenishment());
        clan.changeGold(payRequest.getIdUser(), payRequest.getReplenishment() * 5);//имитация деятельности -
        // несколько раз меняем баланс (разные транзакции в одной бизнес логике)
        boolean isSave = clanService.save(clan);
        if (!isSave) {
            throw new PersistentException("Failed to add gold to the clan");
        }
    }
}
