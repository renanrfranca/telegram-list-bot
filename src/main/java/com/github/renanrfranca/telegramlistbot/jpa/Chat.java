package com.github.renanrfranca.telegramlistbot.jpa;

import javax.persistence.*;
import java.util.List;

@Entity
public class Chat {
    @Id
    private Long id;

    @OneToMany(mappedBy = "chat")
    private List<ItemList> itemLists;

    public Chat() {}

    public Chat(Long id) {
        this.id = id;
    }
}
