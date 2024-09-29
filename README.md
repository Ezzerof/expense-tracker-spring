# Expense Tracker Spring Boot Application

## Overview
The **Expense Tracker** is a Spring Boot application that allows users to manage their expenses and incomes effectively. Users can add, update, delete, and retrieve their transactions (both expenses and incomes) with various features like categorization, sorting, pagination, and more.

## Features
- **User Authentication**: Secure user registration and login using Spring Security.
- **Expense and Income Management**: Create, update, delete, and retrieve expenses and incomes.
- **Transaction Categorization**: Transactions can be categorized for easier tracking.
- **Pagination and Sorting**: Retrieve transactions with pagination and sorting capabilities.
- **Data Validation**: Ensures accurate data entry for all transactions.
- **Entity Relationships**: Each transaction is linked to a specific user.

## Technologies Used
- **Backend**: Java, Spring Boot, Spring Data JPA, Spring Security
- **Database**: MySQL
- **Build Tool**: Maven
- **Testing**: JUnit, Mockito
- **Tools**: Postman for API testing
- **Authentication**: Spring Security with BCryptPasswordEncoder

## Prerequisites
- **Java 17** or higher
- **Maven 3.6+**
- **MySQL** (ensure the database is running on your machine or a server)
- **Postman** (for testing the APIs, if required)

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/Ezzerof/expense-tracker-spring.git
cd expense-tracker-spring
```
### 2. Configure the Database
- Create a MySQL database named `expense_tracker`.
- Update the `application.properties` file with your MySQL credentials:
  ```
  spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker
  spring.datasource.username=your_username
  spring.datasource.password=your_password
  spring.jpa.hibernate.ddl-auto=update
```
