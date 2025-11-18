# Word Memory Trainer

A full-stack application designed to help users memorize words, featuring user registration and login, word list viewing, hard word management, review tracking, and more.

## Technology Stack

- Frontend: Vue 3 + Vite  
- Backend: Spring Boot (Java)  
- Database: JPA + MySQL (default configuration)

## Features

- User registration and login  
- View word lists by study day  
- Manage personal hard words (add/remove)  
- Track review status and review count  
- View personal review history  
- Import and export word data (CSV format)

## Installation and Setup

### Frontend

1. Navigate to the frontend directory: `cd FrontEnd/wordbook-frontend`  
2. Install dependencies: `npm install`  
3. Start the development server: `npm run dev`

### Backend

1. Navigate to the backend directory: `cd Word`  
2. Build with Maven: `mvn clean package`  
3. Start the application: `java -jar target/*.jar`

## API Documentation

### User Management
- `POST /api/user/register` - Register a new user  
- `POST /api/user/login` - Log in a user  
- `GET /api/user/{username}` - Retrieve user information

### Word Management
- `GET /api/words` - Retrieve all words  
- `GET /api/words/day/{day}` - Retrieve words by study day  
- `POST /api/words/import` - Import words (CSV file)  
- `GET /api/words/export/day/{day}` - Export words as CSV

### Hard Words
- `GET /api/hard-words/{username}` - Retrieve user's hard words  
- `POST /api/hard-words/add` - Add a hard word  
- `POST /api/hard-words/remove` - Remove a hard word

### Review Records
- `GET /api/review-record/user/{username}` - Retrieve user's review records  
- `POST /api/review-record/save` - Save a review record  
- `DELETE /api/review-record/{id}` - Delete a review record

## License

This project is licensed under the MIT License. See the LICENSE file in the root directory for details.

## Contribution Guidelines

Pull requests are welcome! Please follow these steps:
1. Fork the project  
2. Create a new branch (`git checkout -b feature/new-feature`)  
3. Commit your changes (`git commit -m 'Add new feature'`)  
4. Push to the branch (`git push origin feature/new-feature`)  
5. Open a Pull Request

We recommend running the full test suite and ensuring consistent code style before submitting a PR.