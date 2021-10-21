package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData(){

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);

        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa, bash);

        createRestaurant(jerusalem, tlv, bash);
    }

    private void createRestaurant(City jerusalem, City tlv, City bash) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");
        Restaurant indian = new Restaurant("indian", bash, "indian restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican, indian));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa, City bash) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");
        Customer cust = new Customer("cust", bash, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach, cust));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata));
    }

    @Test
    public void testBasics(){
        assertEquals(((List<City>) cityRepository.findAll()).size(),4);
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
    }



    ///////////////////////////// INPUT VALIDATION TESTS /////////////////////////////
    /**
     *  Test different customer and restaurant cities
     */
    @Test
    public void testDiffCities(){
        Customer customer = customerRepository.findByName("Beethoven");
        Restaurant restaurant = restaurantRepository.findByName("meat");
        Date deliveryTime = new Date();
        try{
            waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
            Assert.fail("No exception thrown");
        } catch (Exception e){
            assertEquals("Restaurant does not exist in the same city as you, " +
                    "please choose a restaurant from your city", e.getMessage());
        }
    }

    /**
     *  Test invalid delivery time
     */
    @Test()
    public void testInvalidDeliveryTime(){
        Customer customer = customerRepository.findByName("Beethoven");
        Restaurant restaurant = restaurantRepository.findByName("vegan");
        // an hour earlier
        Date deliveryTime = new Date(System.currentTimeMillis() - 60*60*1000);
        try{
            waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
            Assert.fail("No exception thrown");
        } catch (Exception e){
            assertEquals("Invalid Delivery Time, please choose a valid time.",
                    e.getMessage());
        }
    }

    /**
     *  Test customer not in database
     */
    @Test
    public void testNewCustomer(){
        Customer customer = new Customer("Beethoven 22", cityRepository.findByName("Jerusalem"),
                "Ludwig van Beethoven");
        Restaurant restaurant = restaurantRepository.findByName("meat");
        Date deliveryTime =  new Date();
        waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
        assertEquals(7, customerRepository.count());
    }



    ///////////////////////////// ASSIGNING DRIVER TESTS /////////////////////////////
    /**
     *  Test existing drivers in city, see if exception is thrown when no driver is available
     */
    @Test
    public void testDriversInCity(){
        City eilat = new City("Eilat");
        cityRepository.save(eilat);
        Customer bach = new Customer("Bach2", eilat, "Sebastian Bach. Johann");
        customerRepository.save(bach);
        Restaurant mexican = new Restaurant("restauranttt", eilat, "mexican restaurant ");
        restaurantRepository.save(mexican);

        Customer customer = customerRepository.findByName("Bach2");
        Restaurant restaurant = restaurantRepository.findByName("restauranttt");
        Date deliveryTime = new Date();
        try{
            waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
            Assert.fail("No exception thrown");
        } catch (Exception e){
            assertEquals("Sorry! No available drivers in your city", e.getMessage());
        }
    }

    /**
     *  Test multiple orders in the same hour in the same city(tlv), see if exception is thrown when no driver is free
     */ 
    @Test
    public void testFreeDrivers(){
        Customer customer = customerRepository.findByName("Beethoven");
        Restaurant restaurant = restaurantRepository.findByName("vegan");
        // now
        Date deliveryTime1 = new Date();
        // half hour from now
        Date deliveryTime2 = new Date((System.currentTimeMillis() + (int) (60 * 60 * 1000 * 0.5)));
        List<Date> deliveryTimes =  Arrays.asList(deliveryTime1, deliveryTime2, deliveryTime1, deliveryTime2);
        for(int i = 0; i < 4; i++){
            try{
                waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTimes.get(i));
            } catch (Exception e){
                assertEquals("Sorry! No drivers are currently free in your city, try again later", e.getMessage());
                return;
            }
        }
        Assert.fail("No exception thrown");
    }

    /**
     *  Check that the driver with the least past drives is picked ("least busy")
     */
    @Test
    public void testLeastBusyDriver() {
        Customer customer = customerRepository.findByName("Beethoven");
        Restaurant restaurant = restaurantRepository.findByName("vegan");
        // now
        Date deliveryTime1 = new Date();
        // an hour from now
        Date deliveryTime2 = new Date(System.currentTimeMillis() + 60 * 60 * 1000);

        List <Driver> drivers = driverRepository.findAllDriversByCity(customer.getCity());
        Delivery dl1 = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime1);
        Delivery dl2 = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime1);

        drivers = drivers.stream()
                .filter(driver -> driver.getId() != dl1.getDriver().getId() && driver.getId() != dl2.getDriver().getId())
                .collect(Collectors.toList());

        Delivery dl3 = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime2);
        assertEquals(3, deliveryRepository.count());
        assertEquals(1, drivers.size());
        assertEquals(drivers.get(0).getId(), dl3.getDriver().getId());

    }



    ///////////////////////////// DRIVER REPORTS TESTS /////////////////////////////
    /**
     * check report size and returned values
     */
    @Test
    public void testDriverRankReports() {
        Customer customer = customerRepository.findByName("Beethoven");
        Customer customer2 = customerRepository.findByName("Mozart");
        Restaurant restaurant = restaurantRepository.findByName("vegan");
        Restaurant restaurant2 = restaurantRepository.findByName("meat");
        Date deliveryTime1 = new Date();

        // dl1 in tlv, dl2 in jer
        Delivery dl1 = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime1);
        Delivery dl2 = waltService.createOrderAndAssignDriver(customer2, restaurant2, deliveryTime1);

        Driver max_driver_expected = dl1.getDistance() > dl2.getDistance() ? dl1.getDriver() : dl2.getDriver();
        List<DriverDistance> driversDist_actual = deliveryRepository.getDriverTotalDistance();
        assertEquals(2, driversDist_actual.size());
        assertEquals(max_driver_expected.getId(), driversDist_actual.get(0).getDriver().getId());
    }

    /**
     * check report size and returned values in certain city (Jerusalem)
     */
    @Test
    public void testDriverRankReportsByCity() {
        Customer customer = customerRepository.findByName("Beethoven");
        Customer customer2 = customerRepository.findByName("Mozart");
        Restaurant restaurant = restaurantRepository.findByName("vegan");
        Restaurant restaurant2 = restaurantRepository.findByName("meat");
        Date deliveryTime1 = new Date();

        // dl1 in tlv, dl2 & dl3 in jer
        Delivery dl1 = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime1);
        Delivery dl2 = waltService.createOrderAndAssignDriver(customer2, restaurant2, deliveryTime1);
        Delivery dl3 = waltService.createOrderAndAssignDriver(customer2, restaurant2, deliveryTime1);

        City city = cityRepository.findByName("Jerusalem");

        Driver max_driver_expected = dl3.getDistance() > dl2.getDistance() ? dl3.getDriver() : dl2.getDriver();
        List<DriverDistance> driversDist_actual = deliveryRepository.getDriverTotalDistanceByCity(city);
        assertEquals(2, driversDist_actual.size());
        assertEquals(max_driver_expected.getId(), driversDist_actual.get(0).getDriver().getId());
        assertEquals(city.getId(), driversDist_actual.get(0).getDriver().getCity().getId());
        assertEquals(city.getId(), driversDist_actual.get(1).getDriver().getCity().getId());
        driversDist_actual.forEach(System.out::println);
    }

}
