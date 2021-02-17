package com.github.renanrfranca.telegramlistbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
