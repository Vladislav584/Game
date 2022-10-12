package ru.kuznetsov.game.controller;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kuznetsov.game.dto.JoinUserToClanRequest;
import ru.kuznetsov.game.model.Clan;
import ru.kuznetsov.game.model.User;
import ru.kuznetsov.game.service.ClanService;
import ru.kuznetsov.game.service.UserService;
import spark.Route;

import static ru.kuznetsov.game.util.Constants.APPLICATION_JSON;
import static ru.kuznetsov.game.util.Constants.OBJECT_MAPPER;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final ClanService clanService;

    public Route getUsers() {
        return (request, response) -> {
            response.type(APPLICATION_JSON);
            try {
                return OBJECT_MAPPER.writeValueAsString(userService.getUsers());
            } catch (Exception e) {
                log.error("Exception: ", e);
                response.status(500);
                return "{ errorMessage: " + e.getMessage() + " }";
            }
        };
    }

    public Route getUserById() {
        return (request, response) -> {
            long id = Long.parseLong(request.params(":id"));
            response.type(APPLICATION_JSON);
            try {
                return OBJECT_MAPPER.writeValueAsString(userService.getUser(id));
            } catch (Exception e) {
                log.error("Exception: ", e);
                response.status(500);
                return "{ errorMessage: " + e.getMessage() + " }";
            }
        };
    }

    public Route createUser() {
        return (request, response) -> {
            User user = OBJECT_MAPPER.convertValue(request.body(), User.class);
            response.type(APPLICATION_JSON);
            try {
                return OBJECT_MAPPER.writeValueAsString(userService.createUser(user));
            } catch (Exception e) {
                log.error("Exception: ", e);
                response.status(500);
                return "{ errorMessage: " + e.getMessage() + " }";
            }
        };
    }

    public Route joinUserToClan() {
        return (request, response) -> {
            try {
                JoinUserToClanRequest joinUserToClanRequest = OBJECT_MAPPER.convertValue(request.body(), JoinUserToClanRequest.class);
                Clan clan = clanService.getClan(joinUserToClanRequest.getClanId());
                User user = userService.getUser(joinUserToClanRequest.getUserId());
                clan.joinInClan(user);
                boolean isSave = clanService.save(clan);
                if (isSave) {
                    response.type(APPLICATION_JSON);
                    return "";//fixme
                } else {
                    response.status(500);
                    return "{ errorMessage: \"Cannot join user to clan\" }";
                }
            } catch (Exception e) {
                log.error("Exception: ", e);
                response.status(500);
                return "{ errorMessage: " + e.getMessage() + " }";
            }
        };
    }

    public Route deleteUser() {
        return (request, response) -> {
            long id = Long.parseLong(request.params(":id"));
            response.type(APPLICATION_JSON);
            try {
                return OBJECT_MAPPER.writeValueAsString(userService.deleteUser(id));
            } catch (Exception e) {
                log.error("Exception: ", e);
                response.status(500);
                return "{ errorMessage: " + e.getMessage() + " }";
            }
        };
    }
}
