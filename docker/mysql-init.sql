CREATE TABLE IF NOT EXISTS customer (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID(), 1)),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    date_of_birth DATE,
    address_street VARCHAR(255),
    address_city VARCHAR(100),
    address_postal_code VARCHAR(10),
    address_country VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    loyalty_points INT DEFAULT 0,
    newsletter_subscription BOOLEAN DEFAULT false,
    last_login_date DATETIME,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS product_category (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID(), 1)),
    name VARCHAR(100),
    description VARCHAR(500),
    parent_category_id BINARY(16),
    level INT,
    is_active BOOLEAN DEFAULT true,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (parent_category_id) REFERENCES product_category(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS brand (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID(), 1)),
    name VARCHAR(100),
    description VARCHAR(500),
    website VARCHAR(255),
    logo_url VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS product (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID(), 1)),
    name VARCHAR(255),
    description VARCHAR(1000),
    price DECIMAL(10,2),
    stock_quantity INT,
    category_id BINARY(16),
    brand_id BINARY(16),
    weight DECIMAL(10,2),
    dimensions VARCHAR(50),
    sku VARCHAR(50) UNIQUE,
    barcode VARCHAR(50),
    is_available BOOLEAN DEFAULT true,
    min_stock_level INT,
    max_stock_level INT,
    rating DECIMAL(2,1),
    review_count INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (category_id) REFERENCES product_category(id),
    FOREIGN KEY (brand_id) REFERENCES brand(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID(), 1)),
    customer_id BINARY(16),
    order_number BINARY(16) UNIQUE,
    status VARCHAR(20) DEFAULT 'NEW',
    total_amount DECIMAL(10,2),
    shipping_address_street VARCHAR(255),
    shipping_address_city VARCHAR(100),
    shipping_address_postal_code VARCHAR(10),
    shipping_address_country VARCHAR(100),
    shipping_method VARCHAR(50),
    shipping_cost DECIMAL(10,2),
    payment_method VARCHAR(50),
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    discount_code VARCHAR(50),
    discount_amount DECIMAL(10,2),
    tax_amount DECIMAL(10,2),
    notes TEXT,
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    tracking_number VARCHAR(100),
    order_date DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (customer_id) REFERENCES customer(id)
);


CREATE TABLE IF NOT EXISTS order_item (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID(), 1)),
    order_id BINARY(16),
    product_id BINARY(16),
    quantity INT,
    unit_price DECIMAL(10,2),
    discount_amount DECIMAL(10,2),
    tax_rate DECIMAL(5,2),
    tax_amount DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE IF NOT EXISTS product_review (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID(), 1)),
    product_id BINARY(16),
    customer_id BINARY(16),
    rating INT CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(255),
    comment TEXT,
    is_verified BOOLEAN DEFAULT false,
    helpful_votes INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE TABLE IF NOT EXISTS inventory_movement (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID(), 1)),
    product_id BINARY(16),
    movement_type VARCHAR(20),
    quantity INT,
    reference_type VARCHAR(50),
    reference_id BINARY(16),
    notes TEXT,
    created_at DATETIME,
    created_by BINARY(16),
    FOREIGN KEY (product_id) REFERENCES product(id)
);
