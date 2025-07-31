package org.example.util;

import org.example.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Utilidad para inicializar la base de datos con las tablas necesarias
 */
public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    
    private static final String CREATE_PRODUCTS_TABLE = """
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
        )
        """;
    
    private static final String CREATE_INDEXES = """
        CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
        CREATE INDEX IF NOT EXISTS idx_products_active ON products(active);
        CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
        CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);
        """;
    
    private static final String INSERT_SAMPLE_DATA = """
        INSERT INTO products (name, description, price, stock, category) VALUES
        ('Laptop Dell XPS 13', 'Laptop ultraportátil con procesador Intel i7, 16GB RAM, 512GB SSD', 1299.99, 10, 'Electrónicos'),
        ('iPhone 15 Pro', 'Smartphone Apple con chip A17 Pro, 128GB de almacenamiento', 999.99, 25, 'Electrónicos'),
        ('Camiseta Nike Dri-Fit', 'Camiseta deportiva de alta calidad con tecnología Dri-Fit', 29.99, 50, 'Ropa'),
        ('Zapatillas Adidas Ultraboost', 'Zapatillas para correr con tecnología Boost', 179.99, 30, 'Calzado'),
        ('Libro: Clean Code', 'Libro sobre programación y mejores prácticas de desarrollo', 39.99, 15, 'Libros'),
        ('Café Premium Gourmet', 'Café de origen colombiano, tostado artesanal', 24.99, 100, 'Alimentación'),
        ('Monitor Samsung 27"', 'Monitor 4K UHD de 27 pulgadas para oficina y gaming', 299.99, 8, 'Electrónicos'),
        ('Mochila Deportiva', 'Mochila resistente al agua con múltiples compartimentos', 49.99, 40, 'Accesorios')
        ON CONFLICT DO NOTHING
        """;
    
    public static void main(String[] args) {
        logger.info("=== Inicializando Base de Datos ===");
        
        try {
            initializeDatabase();
            logger.info("✅ Base de datos inicializada correctamente");
        } catch (Exception e) {
            logger.error("❌ Error al inicializar la base de datos", e);
            System.exit(1);
        } finally {
            DatabaseConfig.closeDataSource();
        }
    }
    
    public static void initializeDatabase() throws Exception {
        if (!DatabaseConfig.testConnection()) {
            throw new RuntimeException("No se puede conectar a la base de datos");
        }
        
        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Crear tabla products
            logger.info("Creando tabla products...");
            statement.execute(CREATE_PRODUCTS_TABLE);
            logger.info("✅ Tabla products creada");
            
            // Crear índices
            logger.info("Creando índices...");
            statement.execute(CREATE_INDEXES);
            logger.info("✅ Índices creados");
            
            // Verificar si ya hay datos
            var rs = statement.executeQuery("SELECT COUNT(*) FROM products");
            rs.next();
            int count = rs.getInt(1);
            
            if (count == 0) {
                // Insertar datos de ejemplo
                logger.info("Insertando datos de ejemplo...");
                statement.execute(INSERT_SAMPLE_DATA);
                logger.info("✅ Datos de ejemplo insertados");
            } else {
                logger.info("La tabla ya contiene {} productos", count);
            }
            
        }
    }
}
