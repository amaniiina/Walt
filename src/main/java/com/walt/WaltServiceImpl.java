package com.walt;

import com.walt.dao.CustomerRepository;
import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class WaltServiceImpl implements WaltService {

    public static Date now = new Date(); // For validating delivery date

    @Resource
    private CustomerRepository customerRepo;
    @Resource
    private DriverRepository driverRepo;
    @Resource
    private DeliveryRepository deliveryRepo;

    final long hourInMillis = 1000*60*60;
    final long minDistance = 0;
    final long maxDistance = 20;

    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) {
        // Validate parameters
        checkParameters(customer, restaurant, deliveryTime);

        //  Get drivers in the same city as customer
        List <Driver> driversInCity = getDriversInCity(customer.getCity());

        // Get list of drivers that are free
        List <Driver> freeDrivers = getFreeDrivers(driversInCity, deliveryTime);

        // Find the least busy driver from the free drivers in the city
        Driver leastBusy = leastBusyDriver(freeDrivers);

        // assign delivery, generate random distance between 0-20 from the restaurant to the customer
        Random r = new Random();
        double randDistance = minDistance+(maxDistance-minDistance) * r.nextDouble();
        Delivery delivery = new Delivery(leastBusy, restaurant, customer, deliveryTime, randDistance);

        // add new delivery to database
        deliveryRepo.save(delivery);

        return delivery;
    }

    /**
     * @param customer ordering Customer
     * @param restaurant restaurant to order from
     * @param deliveryTime time when order must reach customer
     */
    private void checkParameters(Customer customer, Restaurant restaurant, Date deliveryTime){
        if(customer==null || restaurant==null || deliveryTime==null){
            throw new RuntimeException("Invalid parameter. One or more are null.");
        }
        // Check if customer is in database, if not add it
        if(customerRepo.findByName(customer.getName()) == null){
            customerRepo.save(customer);
        }
        // Check that customer city == restaurant city
        if(!Objects.equals(customer.getCity().getId(), restaurant.getCity().getId())){
            // error message
            throw new RuntimeException("Restaurant does not exist in the same city as you, " +
                    "please choose a restaurant from your city");
        }
        // Check that deliveryTime > current time
        if(deliveryTime.before(now)){
            // error message
            throw new RuntimeException("Invalid Delivery Time, please choose a valid time.");
        }
    }

    /**
     * @param city chosen city
     * @return list of Drivers in city
     */
    private List<Driver> getDriversInCity(City city){
        List<Driver> driversInCity = driverRepo.findAllDriversByCity(city);
        // if no drivers in city
        if(driversInCity.isEmpty()){
            // error msg
            throw new RuntimeException("Sorry! No available drivers in your city");
        }
        return driversInCity;
    }

    /**
     * @param drivers list of Drivers
     * @param deliveryTime time when order must reach customer
     * @return List of drivers available to deliver at deliveryTime
     */
    private List<Driver> getFreeDrivers(List<Driver> drivers, Date deliveryTime){
        List<Driver> freeDrivers = new ArrayList<>();
        // need to get all deliveries for each driver
        for (Driver driver : drivers) {
            List<Delivery> driverDeliveries= deliveryRepo.findByDriver(driver);

            // if driver has no deliveries in the time we want then he is free
            if(driverDeliveries.stream().noneMatch(delivery ->
                    Math.abs(delivery.getDeliveryTime().getTime() - deliveryTime.getTime() ) < hourInMillis)){
                freeDrivers.add(driver);
            }
        }
        if(freeDrivers.isEmpty()){
            // appropriate msg
            throw new RuntimeException("Sorry! No drivers are currently free in your city, try again later");
        }
        return freeDrivers;
    }

    /**
     * @param drivers list of Drivers
     * @return Driver with the least deliveries
     */
    private Driver leastBusyDriver(List<Driver> drivers){
        // check again if no drivers are available & show appropriate msg
        if(drivers.isEmpty()){
            throw new RuntimeException("Sorry! No drivers are currently free in your city, try again later");
        }
        // if only one driver is available
        else if(drivers.size() == 1){
            return drivers.get(0);
        }
        // if multiple free drivers exist choose driver with the least number of past drives
        AtomicInteger minDrives = new AtomicInteger(Integer.MAX_VALUE);
        AtomicReference<Driver> leastBusy = new AtomicReference<>();
        if(drivers.size() > 1){
            drivers.forEach(driver -> {
                int total = deliveryRepo.findByDriver(driver).size();
                if (total < minDrives.get()){
                    minDrives.set(total);
                    leastBusy.set(driver);
                }
            });
        }
        if(leastBusy.get()==null) throw new RuntimeException("Atomic reference is null");
        return leastBusy.get();
    }

    /**
     * @return detailed report displaying the drivers and the total distance of delivery order by total
     * distance in descending order
     */
    @Override
    public List<DriverDistance> getDriverRankReport() {
        return deliveryRepo.getDriverTotalDistance();
    }

    /**
     * @param city chosen City
     * @return detailed report displaying the drivers and the total distance of delivery order by total
     * distance in descending order in certain city
     */
    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        return deliveryRepo.getDriverTotalDistanceByCity(city);
    }
}
