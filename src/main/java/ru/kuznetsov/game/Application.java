package ru.kuznetsov.game;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kuznetsov.game.config.InjectConfig;
import ru.kuznetsov.game.controller.GameController;
import ru.kuznetsov.game.controller.UserController;
import ru.kuznetsov.game.exception.ParseConfigException;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static spark.Spark.*;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        Properties appProps;
        try (InputStream inputStream = Application.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            // так же, возможно, имеет смысл читать конфиг непосредственно рядом с JAR,
            // но опустим этот момент для упрощения
            appProps = new Properties();
            if (inputStream == null) {
                return;
            }
            appProps.load(inputStream);
        } catch (IOException e) {
            log.error("Exception: ", e);
            throw new ParseConfigException("Config not found");
        }
        DataSource dataSource = createDataSource(appProps);
        liquibaseStart(dataSource);
        Injector injector = Guice.createInjector(new InjectConfig(dataSource));
        GameController gameController = injector.getInstance(GameController.class);
        UserController userController = injector.getInstance(UserController.class);
        port(Integer.parseInt(appProps.getProperty("rest.port")));
        get("/clans", gameController.getClans());
        get("/clan/:id", gameController.getClanById());
        post("/clan", gameController.createClan());
        delete("/delete-clan/:id", gameController.deleteClan());
        put("/top-up-gold", gameController.topUpGoldClan());
        get("/transactions/:clan-id", gameController.getTransactions());
        get("/users", userController.getUsers());
        get("/user/:id", userController.getUserById());
        post("/user", userController.createUser());
        put("/add-user-to-clan", userController.joinUserToClan());
        delete("/delete-user/:id", userController.deleteUser());
    }

    //затюнить можно под нужды
    private static DataSource createDataSource(Properties appProps) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(appProps.getProperty("datasource.url"));
        config.setUsername(appProps.getProperty("datasource.user"));
        config.setPassword(appProps.getProperty("datasource.password"));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        return new HikariDataSource(config);
    }

    private static void liquibaseStart(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new liquibase.Liquibase("liquibase/changelog-master.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
        } catch (SQLException | LiquibaseException e) {
            log.error("Exception: ", e);
            throw new IllegalStateException("DB not initialize");
        }
    }

}
