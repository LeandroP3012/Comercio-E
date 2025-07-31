package org.example;

import org.example.config.DatabaseConfig;
import org.example.dao.ProductDAO;
import org.example.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Clase principal para demostrar el uso de conexiones PostgreSQL
 * en un proyecto de e-commerce
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("=== Iniciando aplicación E-Commerce ===");
        
        try {
            // Probar la conexión a la base de datos
            if (DatabaseConfig.testConnection()) {
                logger.info("✅ Conexión a PostgreSQL establecida correctamente");
                
                // Crear instancia del DAO
                ProductDAO productDAO = new ProductDAO();
                
                // Demostrar operaciones CRUD
                demonstrateCRUDOperations(productDAO);
                
            } else {
                logger.error("❌ No se pudo establecer conexión con la base de datos");
                logger.error("Por favor verifica:");
                logger.error("1. Que PostgreSQL esté ejecutándose");
                logger.error("2. Que la base de datos 'comercio_db' exista");
                logger.error("3. Que las credenciales en database.properties sean correctas");
                logger.error("4. Que la tabla 'products' esté creada (ejecuta init_products.sql)");
            }
            
        } catch (Exception e) {
            logger.error("Error en la aplicación", e);
        } finally {
            // Cerrar el pool de conexiones al finalizar
            DatabaseConfig.closeDataSource();
            logger.info("=== Aplicación finalizada ===");
        }
    }
    
    /**
     * Demuestra las operaciones CRUD básicas
     */
    private static void demonstrateCRUDOperations(ProductDAO productDAO) {
        logger.info("\n=== Demostrando operaciones CRUD ===");
        
        try {
            // 1. Mostrar todos los productos
            logger.info("\n1. Listando todos los productos:");
            List<Product> allProducts = productDAO.findAll();
            allProducts.forEach(product -> 
                logger.info("  - {} | ${} | Stock: {}", 
                    product.getName(), product.getPrice(), product.getStock()));
            
            // 2. Buscar por categoría
            logger.info("\n2. Buscando productos en categoría 'Electrónicos':");
            List<Product> electronics = productDAO.findByCategory("Electrónicos");
            electronics.forEach(product -> 
                logger.info("  - {} | ${}", product.getName(), product.getPrice()));
            
            // 3. Crear un nuevo producto
            logger.info("\n3. Creando un nuevo producto:");
            Product newProduct = new Product(
                "Tablet Samsung Galaxy Tab S9",
                "Tablet premium con pantalla AMOLED de 11 pulgadas",
                new BigDecimal("649.99"),
                15,
                "Electrónicos"
            );
            
            Long newId = productDAO.save(newProduct);
            logger.info("  ✅ Producto creado con ID: {}", newId);
            
            // 4. Buscar el producto recién creado
            logger.info("\n4. Buscando el producto recién creado:");
            Optional<Product> foundProduct = productDAO.findById(newId);
            if (foundProduct.isPresent()) {
                Product product = foundProduct.get();
                logger.info("  - Encontrado: {} | ${} | ID: {}", 
                    product.getName(), product.getPrice(), product.getId());
                
                // 5. Actualizar el producto
                logger.info("\n5. Actualizando el precio del producto:");
                product.setPrice(new BigDecimal("599.99"));
                boolean updated = productDAO.update(product);
                if (updated) {
                    logger.info("  ✅ Precio actualizado a ${}", product.getPrice());
                }
                
                // 6. Actualizar solo el stock
                logger.info("\n6. Actualizando stock del producto:");
                boolean stockUpdated = productDAO.updateStock(newId, 20);
                if (stockUpdated) {
                    logger.info("  ✅ Stock actualizado a 20 unidades");
                }
            }
            
            // 7. Buscar por rango de precios
            logger.info("\n7. Buscando productos entre $100 y $500:");
            List<Product> priceRange = productDAO.findByPriceRange(
                new BigDecimal("100"), new BigDecimal("500"));
            priceRange.forEach(product -> 
                logger.info("  - {} | ${}", product.getName(), product.getPrice()));
            
            // 8. Buscar por nombre parcial
            logger.info("\n8. Buscando productos que contienen 'Samsung':");
            List<Product> samsungProducts = productDAO.findByName("Samsung");
            samsungProducts.forEach(product -> 
                logger.info("  - {} | ${}", product.getName(), product.getPrice()));
            
            // 9. Mostrar estadísticas
            logger.info("\n9. Estadísticas:");
            long totalProducts = productDAO.countActiveProducts();
            logger.info("  - Total de productos activos: {}", totalProducts);
            
            // 10. Mostrar estadísticas del pool de conexiones
            logger.info("\n10. Estado del pool de conexiones:");
            DatabaseConfig.printPoolStats();
            
        } catch (Exception e) {
            logger.error("Error durante las operaciones CRUD", e);
        }
    }
}
