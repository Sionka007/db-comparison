CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabela kategorii produktów
CREATE TABLE IF NOT EXISTS product_category (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100),
    description VARCHAR(500),
    parent_category_id UUID,
    level INT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (parent_category_id) REFERENCES product_category(id)
);

-- Tabela marek
CREATE TABLE IF NOT EXISTS brand (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100),
    description VARCHAR(500),
    website VARCHAR(255),
    logo_url VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Rozszerzona tabela klientów
CREATE TABLE IF NOT EXISTS customer (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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
    last_login_date TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Rozszerzona tabela produktów
CREATE TABLE IF NOT EXISTS product (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255),
    description VARCHAR(1000),
    price NUMERIC(10,2),
    stock_quantity INT,
    category_id UUID,
    brand_id UUID,
    weight NUMERIC(10,2),
    dimensions VARCHAR(50),
    sku VARCHAR(50) UNIQUE,
    barcode VARCHAR(50),
    is_available BOOLEAN DEFAULT true,
    min_stock_level INT,
    max_stock_level INT,
    rating NUMERIC(2,1),
    review_count INT DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES product_category(id),
    FOREIGN KEY (brand_id) REFERENCES brand(id)
);

-- Rozszerzona tabela zamówień
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID,
    order_number VARCHAR(50) UNIQUE,
    status VARCHAR(20) DEFAULT 'NEW',
    total_amount NUMERIC(10,2),
    shipping_address_street VARCHAR(255),
    shipping_address_city VARCHAR(100),
    shipping_address_postal_code VARCHAR(10),
    shipping_address_country VARCHAR(100),
    shipping_method VARCHAR(50),
    shipping_cost NUMERIC(10,2),
    payment_method VARCHAR(50),
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    discount_code VARCHAR(50),
    discount_amount NUMERIC(10,2),
    tax_amount NUMERIC(10,2),
    notes TEXT,
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    tracking_number VARCHAR(100),
    order_date TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(id)
);

-- Rozszerzona tabela elementów zamówienia
CREATE TABLE IF NOT EXISTS order_item (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID,
    product_id UUID,
    quantity INT,
    unit_price NUMERIC(10,2),
    discount_amount NUMERIC(10,2),
    tax_rate NUMERIC(5,2),
    tax_amount NUMERIC(10,2),
    total_amount NUMERIC(10,2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);

-- Tabela recenzji produktów
CREATE TABLE IF NOT EXISTS product_review (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID,
    customer_id UUID,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(255),
    comment TEXT,
    is_verified BOOLEAN DEFAULT false,
    helpful_votes INT DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (customer_id) REFERENCES customer(id)
);

-- Tabela ruchów magazynowych
CREATE TABLE IF NOT EXISTS inventory_movement (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID,
    movement_type VARCHAR(20),
    quantity INT,
    reference_type VARCHAR(50),
    reference_id UUID,
    notes TEXT,
    created_at TIMESTAMP,
    created_by UUID,
    FOREIGN KEY (product_id) REFERENCES product(id)
);
