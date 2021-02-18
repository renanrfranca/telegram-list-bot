package com.github.renanrfranca.telegramlistbot.jpa;

import javax.persistence.*;

@Entity
public class List {
    @Id
    @GeneratedValue
    private long id;
    private String title;
    private String description;

    @OneToMany(mappedBy = "list")
    private java.util.List<Item> itens;

    @ManyToOne
    @JoinColumn(name="chat_id")
    private Chat chat;

    public List() {}

    public List(Chat chat, String title, String description) {
        this.chat = chat;
        this.title = title;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
