package com.qcard.data.model;

import java.io.Serializable;
import java.util.List;

public class SharableQSet implements Serializable {
    private String name;
    private String description;
    private List<SharableQCard> cards;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SharableQCard> getCards() {
        return cards;
    }

    public void setCards(List<SharableQCard> cards) {
        this.cards = cards;
    }
}
