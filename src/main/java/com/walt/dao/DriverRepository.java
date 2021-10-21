package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Driver;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends CrudRepository<Driver,Long> {
    List<Driver> findAllDriversByCity(City city);

    Driver findByName(String name);
}
