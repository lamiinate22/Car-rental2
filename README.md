
# Car Rental | Web Application

Car Rental is an advanced car rental management system that allows users to book vehicles, track their reservation history, and manage the fleet. The system is designed to offer an intuitive user interface alongside a robust administrative backend, enabling efficient management of reservations, users, and additional rental options. The application also supports user authentication and authorization, ensuring data security and integrity.

## Key Components of the Car Rental System

- **Car Reservation**: Users can browse available cars, select rental dates, and add extra options such as GPS, child seats, or additional drivers. After booking, users receive detailed information about their reservation.
- **User Management**: Administrators can manage users, add new ones, update user data, and delete accounts. The system supports an administrator role with access to additional administrative functionalities.
- **Fleet Management**: Administrators can add new cars to the fleet, update their technical data and availability, and remove cars from the system.
- **Additional Options**: Users can add various extra options to their bookings. Administrators can manage these options, adding new ones, updating existing ones, or removing unnecessary options.

## Application Architecture

The application consists of a front-end and a back-end. The front-end is built with **Vaadin**, which allows for the creation of interactive and dynamic user interfaces without the need for JavaScript. The back-end is based on **Spring Boot**, providing a solid and scalable server-side solution, database management, and RESTful API services.

## Reservation Process

1. **Car Selection**: Users browse available cars and choose the one they want to rent.
2. **Date Selection**: Users select the start and end dates for the rental.
3. **Add Extra Options**: Users choose additional options like GPS or child seats to add to their reservation.
4. **Reservation Confirmation**: After filling in all required fields, users confirm the reservation. The system calculates the total rental price and saves the details to the database.
5. **Reservation Details**: After confirming the booking, users receive detailed information about their reservation displayed on the page.

## List of Technologies and Languages Used

- **Java**: The main programming language used for building the application's logic and database integration.
- **Spring Boot**: A framework used to develop the back-end application. It provides support for creating RESTful APIs, dependency management, and database integration.
- **Vaadin**: A front-end framework used to build dynamic and interactive user interfaces in Java.
- **MySQL**: A database management system used to store application data such as users, reservations, cars, and extra options.
- **Hibernate**: An ORM (Object-Relational Mapping) framework used for mapping Java objects to relational database structures.
- **Maven**: A project management and dependency management tool.
- **Spring Security**: Used to manage user authentication and authorization.
- **Lombok**: A Java library that reduces boilerplate code by automatically generating getters, setters, constructors, etc.

## List of Features

### User Features

- **User Registration**: New users can register by providing their personal and login details.
- **Login**: Registered users can log into the system.
- **View Available Cars**: Users can browse a list of available cars for rent.
- **Car Reservation**: Users can book selected cars for a specific period, choosing rental dates and additional options.
- **View Reservation Details**: After booking, users can view the details of their reservation, including rental dates, total price, and selected extra options.
- **Display Fuel Types and Prices by Region**: Users can view the types of fuel and their prices in different regions.

### Administrative Features

- **User Management**: Administrators can add, edit, and delete users, as well as assign administrative roles.
- **Car Management**: Administrators can add new cars to the fleet, edit technical details of existing cars, and remove cars from the system.
- **Reservation Management**: Administrators can view, edit, and delete reservations, as well as finalize bookings by updating the status of the cars and applying additional charges.

## Additional Features

- **Calculate Total Reservation Price**: The system automatically calculates the total price of the reservation based on the rental period, daily car rate, and prices of selected extra options.
- **Car Status Update**: After the reservation ends, the system automatically updates the car's status, including mileage and availability.
- **Data Security**: The application ensures data security through authorization and authentication mechanisms, as well as encryption of sensitive data.
- **Fuel Type Management**: The system considers various fuel types when calculating reservation costs, based on the latest fuel prices across different regions.
- **Intuitive User Interface**: Thanks to Vaadin, the application offers a modern, intuitive user interface that simplifies system usage for both users and administrators.

## External Integrations

- **Fuel Price Updates**: The system integrates with external services to fetch up-to-date fuel prices, which are used in calculating reservation costs.

## Testing and Documentation

- **Unit and Integration Tests**: The application includes a comprehensive set of unit and integration tests to ensure the correct functioning of all components.
- **API Documentation**: The application is equipped with API documentation, making it easy for developers to understand and integrate with the system.

