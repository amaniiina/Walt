<B>For this exercise you MUST use the following technologies:</b>

Java 8  (lambda expressions, stream etc.) <br>
Maven 3.6.* <br>
Spring Boot 2.3.5.RELEASE <br>
Spring data <br>
Junit test 4.* <br>

You will need to implement a Walt app system.

<B>Walt is an on-line food delivery service.</b>

Walt works with different restaurants, each restaurant has it: Name City Address

The company has driver list, for each driver the system saves: Name City 

When customers order, they must provide: Name, City, Address, Time of delivery, Restaurant to deliver from. They can only choose restaurants in the city they live in.

1. Write a service to create a delivery and assign an available driver for delivery, driver should be picked if he/she lives in the same city of the restaurant & customer, he/she has no other delivery at the same time. If more than one driver is available assign it to the least busy driver according to the driver history

2. When assigning a delivery, save the distance from the restaurant to the customer, for this purpose the distance will be random number between 0-20 Km

3. Walt want to rank the drivers based on their total distance they did for deliveries. Please provide a detailed report to display the drivers name and the total distance of delivery order by total distance in descending order

4. Provide same report as No. 3 in specific city

5. Add Junit tests to test your work

<b>Assumptions:</b>

* Each drive takes a full hour – it will start and end in a full hour (no need to calculate minutes)
* Since distance is randomized on delivery, deliveries from same restaurant to same customer can be different in each delivery
* If no available driver in city – provide proper error message
* When a customer place an order, you can assume the Restaurant exist in the system, customer may or may not exist 

<b>Important notes:</b>
1. Make sure to import the project as Maven project
2. WaltTest is configured with basic data, you may use it
3. Model, Repositories are implemented, you may add any additional class, repository or service
4. Just to be clear, you are required to implement the interface: WaltService

Good-luck!
