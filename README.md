# 🛒 Spring Boot E-Commerce Backend API

> A robust, minimal e-commerce backend built with Spring Boot and MongoDB, featuring a complete order lifecycle and a simulated payment gateway with asynchronous webhook handling.

**Author**: T. Abdul Kalam Azad

## 🚀 Features

- **Product Management**: Create, retrieve, and manage product inventory.
- **Shopping Cart**: Add items, view cart, and auto-merge quantities for existing items.
- **Order System**: Convert cart items into persistent orders with total calculation.
- **Mock Payment Gateway**: Simulates a real-world payment processor with a 3-second delay and asynchronous Webhook callbacks.
- **Status Automation**: Orders automatically update from `CREATED` → `PAID` upon successful webhook receipt.

## 🛠️ Tech Stack

| Component      | Version          |
| -------------- | ---------------- |
| **Language**   | Java 17          |
| **Framework**  | Spring Boot 3.2+ |
| **Database**   | MongoDB          |
| **Build Tool** | Maven            |
| **Testing**    | Postman          |

---

## ⚙️ Setup & Installation

### 1. Prerequisites

- Java 17 (JDK) installed.
- MongoDB installed and running locally on port `27017`.
- Maven installed.

### 2. Configuration

The application is pre-configured to run on localhost. Check `src/main/resources/application.yaml`:

```yaml
server:
  port: 8080
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/ecommerce_db
payment:
  razorpay:
    enabled: false # Set to true if using real Razorpay
```

### 3. Run the Application

Open the project terminal and run:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

---

## 🧪 API Documentation

### 1. Products

#### Create Product

```http
POST /api/products
Content-Type: application/json

{
  "name": "Gaming Laptop",
  "description": "High Performance",
  "price": 50000.0,
  "stock": 10
}
```

**Response:**

```json
{
  "id": "product_123",
  "name": "Gaming Laptop",
  "description": "High Performance",
  "price": 50000.0,
  "stock": 10
}
```

---

### 2. Cart

#### Add Item to Cart

```http
POST /api/cart/add
Content-Type: application/json

{
  "userId": "user123",
  "productId": "{PRODUCT_ID}",
  "quantity": 1
}
```

#### Get Cart

```http
GET /api/cart/{userId}
```

**Response:**

```json
{
  "userId": "user123",
  "items": [
    {
      "productId": "product_123",
      "productName": "Gaming Laptop",
      "quantity": 1,
      "price": 50000.0
    }
  ],
  "totalItems": 1,
  "totalAmount": 50000.0
}
```

---

### 3. Orders

#### Create Order (Converts Cart to Order)

```http
POST /api/orders
Content-Type: application/json

{
  "userId": "user123"
}
```

**Response:**

```json
{
  "id": "order_456",
  "userId": "user123",
  "totalAmount": 50000.0,
  "status": "CREATED",
  "createdAt": "2024-01-19T10:30:00Z"
}
```

**Note:** Response returns an `id` (Order ID) with status `CREATED`.

#### Get Order Details

```http
GET /api/orders/{orderId}
```

---

### 4. Payments (Mock Service)

#### Initiate Payment

```http
POST /api/payments/create
Content-Type: application/json

{
  "orderId": "{ORDER_ID}",
  "amount": 50000.0
}
```

**Response:**

```json
{
  "id": "payment_789",
  "orderId": "{ORDER_ID}",
  "amount": 50000.0,
  "status": "PENDING",
  "createdAt": "2024-01-19T10:31:00Z"
}
```

Response status will be `PENDING`. The system simulates a bank delay.

---

## 🔄 Mock Payment Logic

To demonstrate Asynchronous Processing and Webhooks without external dependencies:

1. **User initiates payment** via `/api/payments/create`
2. **System saves payment** as `PENDING`
3. **Background thread waits** for 3 seconds
4. **Thread calls webhook** endpoint (`/api/webhooks/payment`) with a `SUCCESS` payload
5. **Webhook Controller** verifies payment and updates Order status to `PAID`

### Webhook Flow Diagram

```
Client Request
    ↓
POST /api/payments/create
    ↓
Payment saved (PENDING)
    ↓
Background Thread (Wait 3s)
    ↓
Webhook Callback
/api/webhooks/payment
    ↓
Order Status Updated (PAID)
    ↓
Payment Status Updated (SUCCESS)
```

---

## 📊 Database Schema

### ER Diagram

```
erDiagram
    USER ||--o{ CART_ITEM : "has"
    USER ||--o{ ORDER : "places"
    PRODUCT ||--o{ CART_ITEM : "in"
    PRODUCT ||--o{ ORDER_ITEM : "ordered"
    ORDER ||--|{ ORDER_ITEM : "contains"
    ORDER ||--|| PAYMENT : "has"

    USER {
        string id PK
        string username
    }
    PRODUCT {
        string id PK
        string name
        double price
        int stock
    }
    CART_ITEM {
        string id PK
        string userId FK
        string productId FK
        int quantity
    }
    ORDER {
        string id PK
        string userId FK
        double totalAmount
        string status
    }
    PAYMENT {
        string id PK
        string orderId FK
        double amount
        string status
        string paymentId
    }
```

---

## 📂 Project Structure

```
com.example.ecommerce
├── config              # RestTemplate Configuration
├── controller          # REST API Endpoints
├── dto                 # Data Transfer Objects (Requests/Responses)
├── model               # MongoDB Documents
├── repository          # Data Access Layer (MongoRepository)
├── service             # Business Logic
└── webhook             # Payment Callback Handler
```

---

## ✅ Testing Checklist (Walkthrough)

Follow these steps to verify the complete flow in Postman:

### Step 1: Create Products

- [ ] Send `POST` request to create a **Laptop** (price: 50000)
- [ ] Send `POST` request to create a **Mouse** (price: 1000)
- [ ] Note down the Product IDs

### Step 2: Add to Cart

- [ ] Add 1 Laptop to cart for user1
- [ ] Add 2 Mice to cart for user1

### Step 3: Verify Cart

- [ ] Send `GET` request to `/api/cart/user1`
- [ ] Verify items are listed correctly
- [ ] Expected total: 52000 (50000 + 2000)

### Step 4: Place Order

- [ ] Send `POST` request to `/api/orders` with user1
- [ ] Verify response status is `CREATED`
- [ ] Verify total amount is 52000
- [ ] Copy the returned `orderId`

### Step 5: Pay for Order

- [ ] Send `POST` request to `/api/payments/create` with the orderId
- [ ] Verify response status is `PENDING`

### Step 6: Wait for Webhook Processing

- [ ] Wait for 3-5 seconds (Mock Service Delay)
- [ ] System automatically processes the webhook callback

### Step 7: Verify Payment

- [ ] Send `GET` request to `/api/orders/{orderId}`
- [ ] **Result**: Status should now be `PAID` ✓

---

## 🐛 Troubleshooting

| Issue                     | Solution                                     |
| ------------------------- | -------------------------------------------- |
| MongoDB Connection Failed | Ensure MongoDB is running on port 27017      |
| Port 8080 Already in Use  | Change `server.port` in `application.yaml`   |
| Build Fails               | Run `mvn clean install`                      |
| Payment Not Processing    | Check application logs for async task status |

---

## 📝 Notes

- The payment gateway is **mocked** and does not process real transactions
- Webhooks are handled asynchronously using Spring's `@Async` annotation
- All data is stored in MongoDB (no persistent storage across restarts unless MongoDB is configured)
- CORS is enabled for localhost testing

---

# By Abdul Kalam Azad
