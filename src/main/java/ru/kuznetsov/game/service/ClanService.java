package ru.kuznetsov.game.service;

import ru.kuznetsov.game.dto.ClanRequest;
import ru.kuznetsov.game.model.Clan;

import java.util.Collection;

public interface ClanService {

    Clan getClan(long clanId);

    Collection<Clan> getClans();

    Clan createClan(ClanRequest clanRequest);

    boolean save(Clan clan);

    boolean deleteClan(long clanId);

}
