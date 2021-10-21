package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Driver;
import com.walt.model.Delivery;
import com.walt.model.DriverDistance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends CrudRepository<Delivery, Long> {

    List<Delivery> findByDriver(Driver driver);

    @Query("SELECT dl.driver AS driver, SUM(dl.distance) AS totalDistance " +
            "FROM Delivery dl " +
            "GROUP BY dl.driver " +
            "ORDER BY totalDistance DESC ")
    List<DriverDistance> getDriverTotalDistance();

    @Query("SELECT dl.driver AS driver, SUM(dl.distance) AS totalDistance FROM Delivery dl " +
            "where dl.driver.city = ?1 " +
            "GROUP BY dl.driver " +
            "ORDER BY totalDistance DESC")
    List<DriverDistance> getDriverTotalDistanceByCity(City city);

}


