const express = require('express');
const path = require('path');
const fs = require('fs');
const app = express();
const port = process.env.PORT || 3000;

// Middleware for parsing JSON and URL-encoded data
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Path to the local licenses database file
const LICENSES_FILE = path.join(__dirname, 'licenses.json');

// Helper to load licenses from local storage
function loadLicenses() {
  try {
    if (fs.existsSync(LICENSES_FILE)) {
      const data = fs.readFileSync(LICENSES_FILE, 'utf8');
      return JSON.parse(data);
    }
  } catch (error) {
    console.error('Error loading licenses database:', error);
  }
  return {};
}

// Helper to save licenses to local storage
function saveLicenses(licenses) {
  try {
    fs.writeFileSync(LICENSES_FILE, JSON.stringify(licenses, null, 2), 'utf8');
    return true;
  } catch (error) {
    console.error('Error saving licenses database:', error);
    return false;
  }
}

// Serve static assets from the compiled KMP Web output directory
app.use(express.static(path.join(__dirname, 'web/build/dist/js/productionExecutable')));

// API Route: Check if a UID has an active license
app.get('/api/licenses/:uid', (req, res) => {
  const { uid } = req.params;
  const licenses = loadLicenses();
  
  if (licenses[uid] && licenses[uid].status === 'approved') {
    return res.json({
      uid: uid,
      unlocked: true,
      licenseType: 'premium',
      licenseCode: `LIC-MP-CURSOS-${uid.slice(-6).toUpperCase()}`,
      updatedAt: licenses[uid].updatedAt,
      paymentId: licenses[uid].paymentId
    });
  }
  
  return res.json({
    uid: uid,
    unlocked: false,
    licenseType: 'base'
  });
});

// API Route: Unlock a license manually (Admin)
app.post('/api/licenses/manual', (req, res) => {
  const { uid, email, secret } = req.body;
  const adminSecret = process.env.ADMIN_SECRET || 'devfolio_secret_token_123';
  
  if (secret !== adminSecret) {
    return res.status(403).json({ error: 'Unauthorized manual activation' });
  }
  
  if (!uid) {
    return res.status(400).json({ error: 'Missing uid field' });
  }
  
  const licenses = loadLicenses();
  licenses[uid] = {
    status: 'approved',
    email: email || 'manual_activation@devfolio.pro',
    paymentId: 'MANUAL_' + Date.now(),
    updatedAt: new Date().toISOString()
  };
  
  if (saveLicenses(licenses)) {
    return res.json({ success: true, message: `License unlocked for UID: ${uid}` });
  } else {
    return res.status(500).json({ error: 'Failed to write license' });
  }
});

// Webhook Route: Receive notifications from Mercado Pago
app.post('/webhook/mercadopago', async (req, res) => {
  console.log('Received Mercado Pago Webhook notification:', JSON.stringify(req.body));
  console.log('Query parameters:', JSON.stringify(req.query));

  // Determine the payment ID and event type from query or body
  let paymentId = req.query['data.id'] || (req.body.data && req.body.data.id);
  let actionType = req.query.type || req.body.type;

  // Mercado Pago sends different event signatures depending on API version
  if (!paymentId && req.body.action) {
    if (req.body.action.startsWith('payment.') && req.body.data) {
      paymentId = req.body.data.id;
      actionType = 'payment';
    }
  }

  // If the notification is not about a payment, or doesn't have an ID, we acknowledge it and stop
  if (!paymentId || actionType !== 'payment') {
    console.log(`Skipping non-payment webhook notification: action=${actionType}, id=${paymentId}`);
    return res.status(200).send('Event received');
  }

  const accessToken = process.env.MERCADO_PAGO_ACCESS_TOKEN;
  if (!accessToken) {
    console.error('CRITICAL: MERCADO_PAGO_ACCESS_TOKEN environment variable is not defined on the server.');
    return res.status(500).send('Server environment misconfiguration');
  }

  try {
    console.log(`Fetching payment details from Mercado Pago API for ID: ${paymentId}...`);
    
    const response = await fetch(`https://api.mercadopago.com/v1/payments/${paymentId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error(`Mercado Pago API responded with error status ${response.status}:`, errorText);
      return res.status(400).send('Failed to verify payment with Mercado Pago');
    }

    const paymentData = await response.json();
    console.log(`Payment details fetched successfully. Status: ${paymentData.status}, ExtRef: ${paymentData.external_reference}`);

    // If payment is approved, we unlock the features for the user
    if (paymentData.status === 'approved') {
      const uid = paymentData.external_reference;
      
      if (uid) {
        const licenses = loadLicenses();
        licenses[uid] = {
          status: 'approved',
          email: paymentData.payer?.email || 'unknown@example.com',
          paymentId: paymentId.toString(),
          updatedAt: new Date().toISOString()
        };
        
        if (saveLicenses(licenses)) {
          console.log(`SUCCESS: License successfully unlocked and recorded for user UID: ${uid}`);
        } else {
          console.error(`ERROR: Failed to save license to database for UID: ${uid}`);
        }
      } else {
        console.warn(`WARNING: Payment was approved but 'external_reference' was missing or empty. Payer email: ${paymentData.payer?.email}`);
      }
    } else {
      console.log(`Payment ID ${paymentId} status is ${paymentData.status} (not approved yet). No license was unlocked.`);
    }

    // Always respond with a 200 OK so Mercado Pago knows we processed the notification
    return res.status(200).send('Notification processed');
  } catch (error) {
    console.error('Error processing Mercado Pago Webhook:', error);
    return res.status(500).send('Internal Server Error processing Webhook');
  }
});

// Fallback all other requests to index.html to support single-page client-side navigation
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'web/build/dist/js/productionExecutable', 'index.html'));
});

app.listen(port, () => {
  console.log(`DevFolio Pro Web Portfolio successfully listening on port ${port}`);
});

