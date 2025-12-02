# Hot Properties - Real Estate Management System

## Project Overview

**Hot Properties** is a comprehensive real estate management web application developed for SE 452 (Object-Oriented Software Development). The system connects property buyers with real estate agents through an intuitive platform that facilitates property listings, searches, and communication between parties.

Built with Spring Boot and following object-oriented design principles, this application demonstrates enterprise-level software architecture including role-based access control, RESTful API design, and database persistence using JPA/Hibernate with MySQL.

### What the Application Does

Hot Properties serves as a multi-user real estate marketplace where:
- **Agents** can list, manage, and update property details with multiple images
- **Buyers** can browse properties, save favorites, and contact agents
- **Admins** can manage user accounts and oversee agent registrations
- All users can communicate through an integrated messaging system

## Technologies Used

- **Java 11+** - Core programming language
- **Spring Boot 3.x** - Application framework with embedded Tomcat server
- **Spring Security** - Authentication and role-based authorization
- **Spring Data JPA** - Database persistence layer
- **Hibernate** - ORM framework
- **MySQL** - Relational database
- **Thymeleaf** - Server-side template engine
- **Maven** - Dependency management and build automation
- **JWT** - Token-based authentication
- **HTML/CSS/JavaScript** - Frontend technologies

## Installation Instructions

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- Git

### Database Setup

1. Install MySQL and create a database:
```sql
CREATE DATABASE se452_hotproperties;
CREATE USER 'user1'@'localhost' IDENTIFIED BY 'user1';
GRANT ALL PRIVILEGES ON se452_hotproperties.* TO 'user1'@'localhost';
FLUSH PRIVILEGES;
```

2. The application will automatically create tables on first run (configured with `spring.jpa.hibernate.ddl-auto=update`)

### Setup Steps

1. Clone the repository:
```bash
git clone <repository-url>
cd <project-directory>
```

2. Build the project using Maven:
```bash
./mvnw clean install
```
   Or on Windows:
```cmd
mvnw.cmd clean install
```

3. The dependencies will be automatically downloaded and the project will be compiled.

## Usage

### Running the Application

**Option 1: Using IntelliJ IDEA**
- Open the project in IntelliJ IDEA
- Locate the main class (annotated with `@SpringBootApplication`)
- Right-click the class and select "Run" or click the green run button
- Application starts on `http://localhost:8080`

**Option 2: Using Maven (Command Line)**
```cmd
mvnw.cmd spring-boot:run
```

**Option 3: Running as JAR**
```cmd
mvnw.cmd clean package
java -jar target/hot_properties-0.0.1-SNAPSHOT.jar
```

### Accessing the Application

Once running, access the application at `http://localhost:8080` through your web browser

## User Roles and Capabilities

The application implements three distinct user roles, each with specific permissions and functionalities:

### 1. BUYER Role
Buyers are property seekers who can browse and interact with listings.

**Capabilities:**
- **Browse Properties**: Search and filter properties by location, price range, and size
- **View Property Details**: Access detailed information including images, descriptions, and agent contact info
- **Manage Favorites**: Save interesting properties to a favorites list for quick access
- **Send Messages**: Contact agents directly about specific properties
- **View Message History**: Access all conversations with agents
- **Profile Management**: Update personal information (name, email, password)

**Typical User Flow:**
1. Register as a buyer or log in
2. Browse available properties using filters (location, price, size)
3. View detailed property information and image galleries
4. Save properties to favorites list
5. Send inquiry messages to property agents
6. Review agent responses in the messages section

### 2. AGENT Role
Agents are real estate professionals who list and manage properties.

**Capabilities:**
- **Add Properties**: Create new property listings with title, price, location, size, and description
- **Upload Images**: Attach multiple photos to each property listing
- **Manage Listings**: View all properties they've listed in one dashboard
- **Edit Properties**: Update property details and add/remove images
- **Delete Properties**: Remove listings that are no longer available
- **Receive Messages**: Get inquiries from interested buyers
- **Respond to Buyers**: Reply to buyer questions through the messaging system
- **Profile Management**: Maintain agent profile information

**Typical User Flow:**
1. Admin creates agent account
2. Agent logs in and navigates to property management
3. Adds new property with details and images
4. Views and responds to buyer inquiries in messages section
5. Updates or removes properties as needed
6. Manages multiple property listings from central dashboard

### 3. ADMIN Role
Administrators oversee the platform and manage user accounts.

**Capabilities:**
- **Create Agent Accounts**: Register new real estate agents in the system
- **Manage Agents**: View, edit, or remove agent profiles
- **User Oversight**: Monitor system users and resolve issues
- **System Administration**: Access to administrative functions and reports
- **Profile Management**: Maintain administrator account details

**Typical User Flow:**
1. Admin logs in with elevated privileges
2. Creates new agent accounts when requested
3. Manages existing agent profiles
4. Monitors system activity and user management

## Application Walkthrough

### Getting Started

1. **Landing Page** (`/`)
   - Public-facing homepage with application overview
   - Login and registration links

2. **User Registration** (`/register`)
   - New buyers can self-register
   - Provides first name, last name, email, and password
   - Automatically assigned BUYER role

3. **Login** (`/login`)
   - All users authenticate with email and password
   - JWT-based session management
   - Redirects to role-specific dashboard

### Main Application Features

#### For Buyers

**Browse Properties** (`/properties/list`)
- Grid view of all available properties
- Filter options: location, price range (min/max), property size
- Each listing shows: image, title, price, location, size
- Click to view full details

**Property Details** (`/properties/view/{id}`)
- Image gallery with lightbox view
- Complete property information
- Agent contact details
- "Add to Favorites" button
- "Contact Agent" messaging option

**Favorites** (`/favorites`)
- Personal collection of saved properties
- Quick access to interesting listings
- Remove properties from favorites

**Messages** (`/messages/buyer`)
- Inbox of all conversations with agents
- View message threads by property
- Send and receive messages

#### For Agents

**Property Management** (`/properties/manage`)
- Dashboard showing all agent's listings
- Quick edit/delete actions for each property
- "Add New Property" button

**Add Property** (`/properties/add`)
- Form with fields: title, price, location, zip code, size, description
- Multi-file upload for property images
- Validation ensures all required fields are filled

**Edit Property** (`/properties/edit/{id}`)
- Modify existing property details
- Add new images or remove existing ones
- Update information in real-time

**Agent Messages** (`/messages/agent`)
- View all buyer inquiries
- Organized by property and buyer
- Respond to questions directly

#### For Admins

**Agent Management** (`/agents/manage`)
- List of all registered agents
- Edit agent information
- Remove agent accounts if needed

**Create Agent** (`/agents/create`)
- Registration form for new agents
- Sets up agent account with proper permissions

### Common Features (All Users)

**Dashboard** (`/dashboard`)
- Role-specific landing page after login
- Quick links to relevant features
- User greeting and navigation menu

**Profile** (`/profile`)
- View current user information
- Email, name, role display

**Edit Profile** (`/editprofile`)
- Update first name and last name
- Change password
- Email cannot be modified (used as unique identifier)

### Technical Features

- **Image Management**: Properties support multiple images stored in `uploads/` directory
- **Security**: Role-based access control using `@PreAuthorize` annotations
- **Validation**: Server-side form validation with error handling
- **Flash Messages**: Success/error notifications for user actions
- **Responsive Design**: CSS-based styling for different screen sizes
- **Database Persistence**: All data stored in MySQL with JPA/Hibernate


## Features

### Core Functionality
- **Multi-Role System**: Three distinct user types (Buyer, Agent, Admin) with role-based permissions
- **Property Management**: Full CRUD operations for real estate listings
- **Image Upload System**: Multiple image support per property with organized file storage
- **Advanced Search**: Filter properties by location, price range, and size
- **Favorites System**: Buyers can bookmark properties of interest
- **Messaging System**: Direct communication channel between buyers and agents
- **User Authentication**: Secure JWT-based login with Spring Security
- **Profile Management**: Users can update their personal information

### Technical Highlights
- **RESTful API Design**: Clean endpoint structure following REST principles
- **Spring Boot Integration**: Leverages auto-configuration and dependency injection
- **Object-Oriented Design**: Implements OOP concepts including:
  - Encapsulation (entity classes with private fields)
  - Inheritance (role hierarchy)
  - Polymorphism (service interfaces)
  - Abstraction (DTOs and service layers)
- **Repository Pattern**: Data access through Spring Data JPA repositories
- **Service Layer**: Business logic separated from controllers
- **DTO Pattern**: Data Transfer Objects for clean API contracts
- **Exception Handling**: Custom exceptions with proper error messages
- **Transaction Management**: ACID compliance with `@Transactional` annotations
- **Validation**: Bean validation with Jakarta Validation annotations

## Project Information

- **Course**: SE 452 - Object-Oriented Software Development
- **Project Name**: Hot Properties
- **Description**: Real Estate Management System
- **Build Tool**: Maven ([pom.xml](pom.xml))
- **Main Application**: `HotPropertiesApplication.java`
- **Database**: MySQL (`se452_hotproperties`)
- **Default Port**: 8080

### Project Structure
```
src/main/java/edu/final_project/hot_properties/
├── controllers/          # HTTP request handlers
│   ├── AgentController
│   ├── DashboardController
│   ├── MessageController
│   ├── ProfileController
│   ├── login/LoginController
│   └── property/
│       ├── PropertyController
│       └── FavoriteController
├── entities/            # JPA entity classes
│   ├── User
│   ├── Role
│   ├── Property
│   ├── PropertyImage
│   ├── Message
│   └── Favorite
├── repositories/        # Data access interfaces
├── services/           # Business logic layer
├── dtos/              # Data Transfer Objects
└── exceptions/        # Custom exception classes

src/main/resources/
├── application.properties    # Configuration
├── static/                  # CSS, images
└── templates/              # Thymeleaf HTML templates
    ├── agent/
    ├── dashboard/
    ├── login/
    ├── messages/
    ├── Profile/
    └── property/
```

## Development

### Building from Source

```bash
./mvnw clean package
```


### IDE Support

The project includes configuration for:
- IntelliJ IDEA (`.idea/` directory)
- Visual Studio Code (`.vscode/settings.json`)

## License

This project is developed for academic purposes as part of the SE 452 course curriculum.

---
