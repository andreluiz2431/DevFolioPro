const express = require('express');
const path = require('path');
const app = express();
const port = process.env.PORT || 3000;

// Serve static assets from the compiled KMP Web output directory
app.use(express.static(path.join(__dirname, 'web/build/dist/js/productionExecutable')));

// Fallback all requests to index.html to support single-page client-side navigation
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'web/build/dist/js/productionExecutable', 'index.html'));
});

app.listen(port, () => {
  console.log(`DevFolio Pro Web Portfolio successfully listening on port ${port}`);
});
