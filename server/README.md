# TaskManager Mock Server

A Node.js mock server for the TaskManager Android app using json-server.

## Features

- **RESTful API** for task management (GET, POST, PUT, DELETE)
- **Mock authentication** endpoint
- **CORS enabled** for cross-origin requests
- **Network latency simulation** (500ms delay)
- **Ready for deployment** on Render, Heroku, or similar platforms

## Available Endpoints

- `POST /auth/login` - Mock authentication (requires email in body)
- `GET /tasks` - Get all tasks
- `POST /tasks` - Create a new task
- `PUT /tasks/:id` - Update a specific task
- `DELETE /tasks/:id` - Delete a specific task

## Local Development

### Prerequisites

- Node.js 16+ 
- npm

### Quick Start

1. Install dependencies:
```bash
npm install
```

2. Start the server:
```bash
npm start
```

The server will run on `http://localhost:3000`

### Development Mode

For local development with auto-reload:
```bash
npm run dev
```

## Deployment

### Render

1. Push this folder to a GitHub repository
2. Connect to Render and select this repository
3. Set the build command: `npm install`
4. Set the start command: `npm start`
5. Deploy!

### Environment Variables

- `PORT` - Server port (defaults to 3000)

## Data Structure

Tasks follow this structure:
```json
{
  "id": "unique-id",
  "title": "Task title",
  "description": "Task description", 
  "completed": false,
  "dueDate": "2024-12-31T00:00:00Z",
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z"
}
```

## CORS

CORS is enabled for all origins. In production, you may want to restrict this to specific domains.

## License
MIT