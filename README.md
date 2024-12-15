# Expense Tracker
#### Video Demo: https://www.youtube.com/watch?v=fbJBENUKO9Y

## Project Overview
Expense Tracker is a web application designed to help users manage their expenses and incomes while providing a clear summary of savings over time. The application is developed with a **Java Spring Boot backend** and a **React frontend**, ensuring a smooth user experience and efficient data handling.

The main features of the Expense Tracker include:
- User Authentication (Registration & Login)
- Managing Expenses and Incomes
- Calendar View to Display Transactions
- Savings Summary Calculated Dynamically
- Support for Adding, Editing, and Deleting Transactions

## Technologies Used
The following technologies and tools are used in this project:

### Backend:
- **Java** (Spring Boot framework)
- **PostgreSQL** (Database)
- **Spring Security** (Authentication & Authorization)
- **Maven** (Build tool)
- **RESTful APIs** for communication with the frontend

### Frontend:
- **React** (JavaScript library for UI development)
- **CSS** for styling
- **Axios** for API communication

### Additional Tools:
- IntelliJ IDEA
- npm & Node.js
- Git & GitHub

## Project Structure
The following sections outline the key directories and files, explaining their purpose:

### Backend (Java Spring Boot)

#### **`src/main/java/com/example/expensetrackerspring`**
- **exceptions/**
    - Contains custom exceptions and a global exception handler for managing errors.
    - Example files:
      - `UserNotFoundException`: Thrown when a user does not exist.
      - `GlobalExceptionHandler`: Centralized error handling for the application.
- **persistence/**
    - **entity/**
        - `Transaction.java`: Represents a financial transaction (income or expense).
        - `User.java`: Represents a user entity for authentication.
        - `UserMonthlySummary.java`: Summarizes monthly savings.
    - **repository/**
        - Interfaces for accessing data from the database.
        - `TransactionRepository.java`: Repository for transaction entities.
        - `UserRepository.java`: Repository for user entities.
- **service/**
    - Contains the business logic for managing users, transactions, and monthly summaries.
    - Files include:
      - `TransactionService.java`: Handles transaction-related operations.
      - `AuthenticationService.java`: Manages user authentication and registration.
- **rest/**
    - Contains REST controllers to expose endpoints for API communication.
    - Files include:
      - `AuthController.java`: Endpoints for user login and registration.
      - `TransactionController.java`: Handles transaction CRUD operations.
    - **payload/**
        - **request/**: DTOs for handling incoming data.
            - `SaveTransactionRequest.java`: Request for saving a new transaction.
            - `SignInRequest.java`: Request for user login.
        - **response/**: DTOs for sending structured responses.
            - `TransactionResponse.java`: Response containing transaction details.
- **security/**
    - Contains security configuration classes.
    - Files:
      - `SecurityConfig.java`: Configures Spring Security for user authentication.
      - `WebConfig.java`: Configures CORS and other web settings.
- **utils/**
    - Utility classes and configurations.
- **resources/**
    - **`application.properties`**: Configurations for database connections, security, etc.

### Frontend (React)

#### **`src` Directory**
- **components/**
    - Contains reusable UI components.
    - Files include:
      - `Calendar.js`: Displays transactions in a calendar view.
      - `Login.js`: Login page component.
      - `Register.js`: Registration page component.
      - `TransactionModal.js`: Modal for adding/editing transactions.
- **utils/**
    - `apiClient.js`: Handles API requests using Axios.
    - `storage.js`: Utility for managing local storage.
- **App.js**
    - Root React component that defines application routes and pages.
- **index.js**
    - Entry point of the React application.
- **App.css** & **index.css**
    - Stylesheets for the React application.
- **package.json**
    - Contains project dependencies and scripts.

## Design Choices
### **Separation of Concerns**
The project follows the **MVC architecture** (Model-View-Controller), ensuring clear separation between data handling, business logic, and UI presentation.

- **Backend**: Handles business logic, data persistence, and API exposure.
- **Frontend**: Focuses solely on user interactions and UI presentation.

### **Custom Exceptions**
Custom exceptions like `UserNotFoundException` and `InvalidCredentialException` were implemented to handle specific error scenarios, ensuring better debugging and cleaner code.

### **Dynamic Savings Calculation**
A notable design decision was to calculate savings dynamically based on income and expense data. This approach ensures that savings remain consistent when transactions are added, edited, or removed.

## How It Works
### User Flow
1. **Registration**: Users create an account via the registration page.
2. **Login**: Users log in using their credentials. Invalid credentials display an error.
3. **Dashboard**:
   - View a calendar showing all transactions for the selected month.
   - Highlighted dates indicate transactions.
   - Add, edit, or delete transactions.
4. **Savings**: Savings are automatically recalculated and displayed based on the user’s transactions.

### Transactions
- **Single Transactions**: End dates are disabled for single transactions.
- **Recurring Transactions**: Transactions repeat until the specified end date.

### Calendar Navigation
- Users can navigate between months and years.
- Transactions dynamically update in the calendar view.

## Running the Project
Follow these steps to run the project locally:

### Backend
1. Clone the repository.
   ```bash
   git clone https://github.com/Ezzerof/expense-tracker-spring.git
   ```
2. Open the backend directory in an IDE like IntelliJ IDEA.
3. Set up the PostgreSQL database and configure credentials in `application.properties`.
4. Run the Spring Boot application using Maven:
   ```bash
   mvn spring-boot:run
   ```

### Frontend
1. Navigate to the frontend directory.
   ```bash
   cd expense-tracker-frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the React application:
   ```bash
   npm start
   ```
4. Open `http://localhost:3000` in your browser.

## Future Enhancements
- Implementing **Charts/Graphs** to visualize expenses and incomes.
- Adding **User Settings** for profile customization.
- Integrating **Email Notifications** for transaction reminders.
- Expanding **Security Features** like two-factor authentication.

## Conclusion
Expense Tracker is a robust and user-friendly application for tracking financial transactions. It demonstrates a strong understanding of full-stack development, integrating modern technologies and best practices. This project is a foundation that can be expanded with additional features, making it a valuable tool for financial management.

---
Thank you for exploring my project. Feedback and contributions are welcome!
