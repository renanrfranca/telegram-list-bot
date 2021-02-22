package com.github.renanrfranca.telegramlistbot.jpa;

import javax.persistence.*;

@Entity
public class Item {
    @Id
    @GeneratedValue
    private Long id;
    private String description;
    @ManyToOne
    @JoinColumn(name="list_id")
    private ItemList itemList;

    public Item() {}

    public Item(String description, ItemList itemList) {
        this.description = description;
        this.itemList = itemList;
    }

    public ItemList getList() {
        return this.itemList;
    }

    public String getDescription() {
        return description;
    }
}
