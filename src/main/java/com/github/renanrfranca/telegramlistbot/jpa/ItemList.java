package com.github.renanrfranca.telegramlistbot.jpa;

import javax.persistence.*;
import java.util.List;

@Entity
public class ItemList {
    @Id
    @GeneratedValue
    private Long id;
    private String title;

    @OneToMany(mappedBy = "list")
    private List<Item> itens;

    @ManyToOne
    @JoinColumn(name="chat_id")
    private Chat chat;

    public ItemList() {}

    public ItemList(Chat chat, String title) {
        this.chat = chat;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

}
