package ru.kuznetsov.game.controller;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kuznetsov.game.dto.ClanRequest;
import ru.kuznetsov.game.dto.PayRequest;
import ru.kuznetsov.game.exception.PersistentException;
import ru.kuznetsov.game.model.Clan;
import ru.kuznetsov.game.model.Transaction;
import ru.kuznetsov.game.service.ClanService;
import ru.kuznetsov.game.service.TransactionService;
import ru.kuznetsov.game.service.UserAddGoldService;
import spark.Route;

import java.util.List;

import static ru.kuznetsov.game.util.Constants.APPLICATION_JSON;
import static ru.kuznetsov.game.util.Constants.OBJECT_MAPPER;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    private final ClanService clanService;
    private final UserAddGoldService userAddGoldService;
    private final TransactionService transactionService;

    public Route getClans() {
        return (request, response) -> {
            response.type(APPLICATION_JSON);
            return OBJECT_MAPPER.writeValueAsString(clanService.getClans());
        };
    }

    public Route getClanById() {
        return (request, response) -> {
            long id = Long.parseLong(request.params(":id"));
            response.type(APPLICATION_JSON);
            return OBJECT_MAPPER.writeValueAsString(clanService.getClan(id));
        };
    }

    public Route createClan() {
        return (request, response) -> {
            ClanRequest clanRequest = OBJECT_MAPPER.convertValue(request.body(), ClanRequest.class);
            response.type(APPLICATION_JSON);
            try {
                Clan clan = clanService.createClan(clanRequest);
                return OBJECT_MAPPER.writeValueAsString(clan);
            } catch (Exception e) {
                log.error("Exception: ", e);
                response.status(500);
                return "{ errorMessage: " + e.getMessage() + " }";
            }
        };
    }

    public Route deleteClan() {
        return (request, response) -> {
            long id = Long.parseLong(request.params(":id"));
            response.type(APPLICATION_JSON);
            try {
                return OBJECT_MAPPER.writeValueAsString(clanService.deleteClan(id));
            } catch (Exception e) {
                log.error("Exception: ", e);
                response.status(500);
                return "{ errorMessage: " + e.getMessage() + " }";
            }
        };
    }

    public Route topUpGoldClan() {
        return (request, response) -> {
            PayRequest payRequest = OBJECT_MAPPER.convertValue(request.body(), PayRequest.class);
            response.type(APPLICATION_JSON);
            try {
                userAddGoldService.addGoldToClan(payRequest);
            } catch (PersistentException e) {
                log.error("Exception: ", e);
                response.status(500);
                return "{ errorMessage: \"Get error when save a clan\" }";
            }
            return OBJECT_MAPPER.writeValueAsString(clanService.getClans());
        };
    }

    public Route getTransactions() {
        return (request, response) -> {
            long idClan = Long.parseLong(request.params(":clan-id"));
            response.type(APPLICATION_JSON);
            try {
                List<Transaction> transactions = transactionService.getAllTransactionsByClanId(idClan);
                return OBJECT_MAPPER.writeValueAsString(transactions);
            } catch (Exception e) {
                log.error("Exception: ", e);
                response.status(500);
                return "{ errorMessage: " + e.getMessage() + " }";
            }
        };
    }

}
