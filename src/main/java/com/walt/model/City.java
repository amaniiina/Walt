package com.walt.model;

import javax.persistence.Entity;

@Entity
public class City extends NamedEntity{

    public City(){}

    public City(String name){
        super(name);
    }
}
