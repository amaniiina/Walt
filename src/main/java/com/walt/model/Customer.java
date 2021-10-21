package com.walt.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Customer extends  NamedEntity{

    @ManyToOne
    City city;
    String address;

    public Customer(){}

    public Customer(String name, City city, String address) {
        super(name);
        this.city = city;
        this.address = address;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "city=" + city +
                ", address='" + address + '\'' +
                '}';
    }
}
