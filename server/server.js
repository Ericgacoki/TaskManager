const jsonServer = require('json-server');

const server = jsonServer.create();

// Use in-memory database for Vercel serverless environment
// Data persists during function lifetime but resets on cold-starts
const db = {
  tasks: []
};

const router = jsonServer.router(db);
const middlewares = jsonServer.defaults({
  noCors: false
});

const PORT = process.env.PORT || 3000;

// Enable CORS for all requests
server.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Content-Length, X-Requested-With');
  
  if (req.method === 'OPTIONS') {
    res.sendStatus(200);
  } else {
    next();
  }
});

// Add custom middleware
server.use(middlewares);

// Custom auth route
server.post('/auth/login', (req, res) => {
  const { email } = req.body;
  
  if (email) {
    res.json({
      token: 'mock-token-' + Date.now(),
      user: {
        email: email,
        id: 1
      }
    });
  } else {
    res.status(400).json({
      error: 'Email is required'
    });
  }
});

// Add a delay to simulate network latency (optional)
server.use((req, res, next) => {
  setTimeout(next, 500);
});

// Use default router
server.use(router);

server.listen(PORT, '0.0.0.0', () => {
  console.log(`TaskManager Mock Server is running on port ${PORT}`);
  console.log(`Available endpoints:`);
  console.log(`  POST /auth/login`);
  console.log(`  GET  /tasks`);
  console.log(`  POST /tasks`);
  console.log(`  PUT  /tasks/:id`);
  console.log(`  DELETE /tasks/:id`);
});

module.exports = server;