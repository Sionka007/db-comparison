# Schemat bazy danych

## Różnice między MySQL i PostgreSQL
- MySQL używa `BINARY(16)` dla ID
- PostgreSQL używa `UUID` z rozszerzeniem `uuid-ossp`
- MySQL używa `DATETIME` dla znaczników czasu
- PostgreSQL używa `TIMESTAMP`
- MySQL używa `DECIMAL` dla liczb zmiennoprzecinkowych
- PostgreSQL używa `NUMERIC`

## Tabele

### Customer (Klient)
```sql
- id: BINARY(16)/UUID PRIMARY KEY
- first_name: VARCHAR(100)
- last_name: VARCHAR(100)
- email: VARCHAR(255) UNIQUE NOT NULL
- phone_number: VARCHAR(20)
- date_of_birth: DATE
- address_street: VARCHAR(255)
- address_city: VARCHAR(100)
- address_postal_code: VARCHAR(10)
- address_country: VARCHAR(100)
- status: VARCHAR(20) DEFAULT 'ACTIVE'
- loyalty_points: INT DEFAULT 0
- newsletter_subscription: BOOLEAN DEFAULT false
- last_login_date: DATETIME/TIMESTAMP
- created_at: DATETIME/TIMESTAMP
- updated_at: DATETIME/TIMESTAMP
```

### Product Category (Kategoria produktu)
```sql
- id: BINARY(16)/UUID PRIMARY KEY
- name: VARCHAR(100)
- description: VARCHAR(500)
- parent_category_id: BINARY(16)/UUID -> FOREIGN KEY (product_category)
- level: INT
- is_active: BOOLEAN DEFAULT true
- created_at: DATETIME/TIMESTAMP
- updated_at: DATETIME/TIMESTAMP
```

### Brand (Marka)
```sql
- id: BINARY(16)/UUID PRIMARY KEY
- name: VARCHAR(100)
- description: VARCHAR(500)
- website: VARCHAR(255)
- logo_url: VARCHAR(255)
- is_active: BOOLEAN DEFAULT true
- created_at: DATETIME/TIMESTAMP
- updated_at: DATETIME/TIMESTAMP
```

### Product (Produkt)
```sql
- id: BINARY(16)/UUID PRIMARY KEY
- name: VARCHAR(255)
- description: VARCHAR(1000)
- price: DECIMAL/NUMERIC(10,2)
- stock_quantity: INT
- category_id: BINARY(16)/UUID -> FOREIGN KEY (product_category)
- brand_id: BINARY(16)/UUID -> FOREIGN KEY (brand)
- weight: DECIMAL/NUMERIC(10,2)
- dimensions: VARCHAR(50)
- sku: VARCHAR(50) UNIQUE
- barcode: VARCHAR(50)
- is_available: BOOLEAN DEFAULT true
- min_stock_level: INT
- max_stock_level: INT
- rating: DECIMAL/NUMERIC(2,1)
- review_count: INT DEFAULT 0
- created_at: DATETIME/TIMESTAMP
- updated_at: DATETIME/TIMESTAMP
```

### Order (Zamówienie)
```sql
- id: BINARY(16)/UUID PRIMARY KEY
- customer_id: BINARY(16)/UUID -> FOREIGN KEY (customer)
- order_number: VARCHAR(50) UNIQUE
- status: VARCHAR(20) DEFAULT 'NEW'
- total_amount: DECIMAL/NUMERIC(10,2)
- shipping_address_street: VARCHAR(255)
- shipping_address_city: VARCHAR(100)
- shipping_address_postal_code: VARCHAR(10)
- shipping_address_country: VARCHAR(100)
- shipping_method: VARCHAR(50)
- shipping_cost: DECIMAL/NUMERIC(10,2)
- payment_method: VARCHAR(50)
- payment_status: VARCHAR(20) DEFAULT 'PENDING'
- discount_code: VARCHAR(50)
- discount_amount: DECIMAL/NUMERIC(10,2)
- tax_amount: DECIMAL/NUMERIC(10,2)
- notes: TEXT
- estimated_delivery_date: DATE
- actual_delivery_date: DATE
- tracking_number: VARCHAR(100)
- order_date: DATETIME/TIMESTAMP
- created_at: DATETIME/TIMESTAMP
- updated_at: DATETIME/TIMESTAMP
```

### Order Item (Pozycja zamówienia)
```sql
- id: BINARY(16)/UUID PRIMARY KEY
- order_id: BINARY(16)/UUID -> FOREIGN KEY (orders)
- product_id: BINARY(16)/UUID -> FOREIGN KEY (product)
- quantity: INT
- unit_price: DECIMAL/NUMERIC(10,2)
- discount_amount: DECIMAL/NUMERIC(10,2)
- tax_rate: DECIMAL/NUMERIC(5,2)
- tax_amount: DECIMAL/NUMERIC(10,2)
- total_amount: DECIMAL/NUMERIC(10,2)
- created_at: DATETIME/TIMESTAMP
- updated_at: DATETIME/TIMESTAMP
```

### Product Review (Recenzja produktu)
```sql
- id: BINARY(16)/UUID PRIMARY KEY
- product_id: BINARY(16)/UUID -> FOREIGN KEY (product)
- customer_id: BINARY(16)/UUID -> FOREIGN KEY (customer)
- rating: INT CHECK (rating BETWEEN 1 AND 5)
- title: VARCHAR(255)
- comment: TEXT
- is_verified: BOOLEAN DEFAULT false
- helpful_votes: INT DEFAULT 0
- created_at: DATETIME/TIMESTAMP
- updated_at: DATETIME/TIMESTAMP
```

### Inventory Movement (Ruch magazynowy)
```sql
- id: BINARY(16)/UUID PRIMARY KEY
- product_id: BINARY(16)/UUID -> FOREIGN KEY (product)
- movement_type: VARCHAR(20)
- quantity: INT
- reference_type: VARCHAR(50)
- reference_id: BINARY(16)/UUID
- notes: TEXT
- created_at: DATETIME/TIMESTAMP
- created_by: BINARY(16)/UUID
```

## Relacje między tabelami

1. `Product Category` -> `Product Category` (self-referential, parent-child)
2. `Product Category` -> `Product` (one-to-many)
3. `Brand` -> `Product` (one-to-many)
4. `Customer` -> `Order` (one-to-many)
5. `Order` -> `Order Item` (one-to-many)
6. `Product` -> `Order Item` (one-to-many)
7. `Product` -> `Product Review` (one-to-many)
8. `Customer` -> `Product Review` (one-to-many)
9. `Product` -> `Inventory Movement` (one-to-many)

## Ważne indeksy
- `customer.email` (UNIQUE)
- `product.sku` (UNIQUE)
- `orders.order_number` (UNIQUE)
- `product_review.rating` (z CHECK CONSTRAINT między 1 a 5)
