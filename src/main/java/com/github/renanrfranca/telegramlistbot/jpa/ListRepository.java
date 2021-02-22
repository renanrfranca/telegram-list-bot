package com.github.renanrfranca.telegramlistbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListRepository extends JpaRepository<ItemList, Long> {
    List<ItemList> findAllByChatId(Long chatId);
}
