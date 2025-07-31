-- Script SQL para crear la base de datos y tabla de productos
-- Ejecutar en PostgreSQL

-- Crear la base de datos (ejecutar como superusuario)
-- CREATE DATABASE comercio_db;

-- Conectar a la base de datos comercio_db antes de ejecutar las siguientes líneas

-- Crear la tabla products
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    category VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_active ON products(active);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);

-- Insertar algunos datos de ejemplo
INSERT INTO products (name, description, price, stock, category) VALUES
('Laptop Dell XPS 13', 'Laptop ultraportátil con procesador Intel i7, 16GB RAM, 512GB SSD', 1299.99, 10, 'Electrónicos'),
('iPhone 15 Pro', 'Smartphone Apple con chip A17 Pro, 128GB de almacenamiento', 999.99, 25, 'Electrónicos'),
('Camiseta Nike Dri-Fit', 'Camiseta deportiva de alta calidad con tecnología Dri-Fit', 29.99, 50, 'Ropa'),
('Zapatillas Adidas Ultraboost', 'Zapatillas para correr con tecnología Boost', 179.99, 30, 'Calzado'),
('Libro: Clean Code', 'Libro sobre programación y mejores prácticas de desarrollo', 39.99, 15, 'Libros'),
('Café Premium Gourmet', 'Café de origen colombiano, tostado artesanal', 24.99, 100, 'Alimentación'),
('Monitor Samsung 27"', 'Monitor 4K UHD de 27 pulgadas para oficina y gaming', 299.99, 8, 'Electrónicos'),
('Mochila Deportiva', 'Mochila resistente al agua con múltiples compartimentos', 49.99, 40, 'Accesorios');

-- Comentarios en las columnas para documentación
COMMENT ON TABLE products IS 'Tabla para almacenar información de productos del e-commerce';
COMMENT ON COLUMN products.id IS 'Identificador único del producto';
COMMENT ON COLUMN products.name IS 'Nombre del producto';
COMMENT ON COLUMN products.description IS 'Descripción detallada del producto';
COMMENT ON COLUMN products.price IS 'Precio del producto en la moneda base';
COMMENT ON COLUMN products.stock IS 'Cantidad disponible en inventario';
COMMENT ON COLUMN products.category IS 'Categoría del producto';
COMMENT ON COLUMN products.active IS 'Indica si el producto está activo (disponible para venta)';
COMMENT ON COLUMN products.created_at IS 'Fecha y hora de creación del registro';
COMMENT ON COLUMN products.updated_at IS 'Fecha y hora de última actualización del registro';
