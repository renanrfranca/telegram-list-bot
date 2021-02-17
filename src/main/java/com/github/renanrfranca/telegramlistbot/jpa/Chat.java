package com.github.renanrfranca.telegramlistbot.jpa;

import javax.persistence.*;

@Entity
public class Chat {
    @Id
    private Long id;

    @OneToMany(mappedBy = "chat")
    private java.util.List<List> lists;

    public Chat(Long id) {
        this.id = id;
    }
}
