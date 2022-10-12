package ru.kuznetsov.game.config;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import ru.kuznetsov.game.repository.*;
import ru.kuznetsov.game.service.*;

import javax.sql.DataSource;

@RequiredArgsConstructor
public class InjectConfig extends AbstractModule {

    private final DataSource dataSource;

    @Override
    protected void configure() {
        bind(DataSource.class).toInstance(dataSource);
        bind(ClanRepository.class).to(ClanRepositoryImpl.class);
        bind(TransactionRepository.class).to(TransactionRepositoryImpl.class);
        bind(TransactionService.class).to(TransactionServiceImpl.class);
        bind(ClanService.class).to(ClanServiceImpl.class);
        bind(UserAddGoldService.class).asEagerSingleton();
        bind(TaskService.class).asEagerSingleton();
        bind(UserService.class).to(UserServiceImpl.class);
        bind(UserRepository.class).to(UserRepositoryImpl.class);
    }

}
