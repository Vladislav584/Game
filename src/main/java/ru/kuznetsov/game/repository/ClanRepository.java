package ru.kuznetsov.game.repository;

import ru.kuznetsov.game.model.Clan;

import java.util.Collection;

public interface ClanRepository {

    Clan getClan(long clanId);

    Clan save(Clan clan);

    Clan delete(long clanId);

    Collection<Clan> getAll();
}
