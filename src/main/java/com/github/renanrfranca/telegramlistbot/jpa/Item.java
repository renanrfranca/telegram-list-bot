package com.github.renanrfranca.telegramlistbot.jpa;

import javax.persistence.*;

@Entity
public class Item {
    @Id
    @GeneratedValue
    private long id;
    private String description;
    @ManyToOne
    @JoinColumn(name="list_id")
    private List list;

    public Item() {

    }

    public Item(String description, List list) {
        this.description = description;
        this.list = list;
    }

    public List getList() {
        return this.list;
    }

    public String getDescription() {
        return description;
    }
}
