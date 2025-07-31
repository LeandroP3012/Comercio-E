package org.example.dao;

import org.example.model.Product;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DAO para operaciones CRUD con la tabla products
 */
public class ProductDAO extends BaseDAO {
    
    private static final String TABLE_NAME = "products";
    
    /**
     * Obtiene todos los productos activos
     */
    public List<Product> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE active = true ORDER BY name";
        return executeQuery(sql, this::mapResultSetToProduct);
    }
    
    /**
     * Busca un producto por su ID
     */
    public Optional<Product> findById(Long id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        List<Product> products = executeQuery(sql, this::mapResultSetToProduct, id);
        return products.isEmpty() ? Optional.empty() : Optional.of(products.get(0));
    }
    
    /**
     * Busca productos por categoría
     */
    public List<Product> findByCategory(String category) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE category = ? AND active = true ORDER BY name";
        return executeQuery(sql, this::mapResultSetToProduct, category);
    }
    
    /**
     * Busca productos por nombre (búsqueda parcial)
     */
    public List<Product> findByName(String name) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE LOWER(name) LIKE LOWER(?) AND active = true ORDER BY name";
        return executeQuery(sql, this::mapResultSetToProduct, "%" + name + "%");
    }
    
    /**
     * Busca productos en un rango de precios
     */
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE price BETWEEN ? AND ? AND active = true ORDER BY price";
        return executeQuery(sql, this::mapResultSetToProduct, minPrice, maxPrice);
    }
    
    /**
     * Guarda un nuevo producto
     */
    public Long save(Product product) {
        String sql = "INSERT INTO " + TABLE_NAME + 
                    " (name, description, price, stock, category, active, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        
        Long id = executeInsertWithGeneratedKey(sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.isActive(),
                Timestamp.valueOf(product.getCreatedAt()),
                Timestamp.valueOf(product.getUpdatedAt())
        );
        
        product.setId(id);
        return id;
    }
    
    /**
     * Actualiza un producto existente
     */
    public boolean update(Product product) {
        String sql = "UPDATE " + TABLE_NAME + 
                    " SET name = ?, description = ?, price = ?, stock = ?, category = ?, active = ?, updated_at = ? " +
                    "WHERE id = ?";
        
        product.setUpdatedAt(LocalDateTime.now());
        
        int rowsAffected = executeUpdate(sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.isActive(),
                Timestamp.valueOf(product.getUpdatedAt()),
                product.getId()
        );
        
        return rowsAffected > 0;
    }
    
    /**
     * Actualiza el stock de un producto
     */
    public boolean updateStock(Long productId, Integer newStock) {
        String sql = "UPDATE " + TABLE_NAME + " SET stock = ?, updated_at = ? WHERE id = ?";
        int rowsAffected = executeUpdate(sql, newStock, Timestamp.valueOf(LocalDateTime.now()), productId);
        return rowsAffected > 0;
    }
    
    /**
     * Elimina un producto (eliminación lógica)
     */
    public boolean delete(Long id) {
        String sql = "UPDATE " + TABLE_NAME + " SET active = false, updated_at = ? WHERE id = ?";
        int rowsAffected = executeUpdate(sql, Timestamp.valueOf(LocalDateTime.now()), id);
        return rowsAffected > 0;
    }
    
    /**
     * Elimina un producto físicamente de la base de datos
     */
    public boolean deletePhysically(Long id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        int rowsAffected = executeUpdate(sql, id);
        return rowsAffected > 0;
    }
    
    /**
     * Obtiene el total de productos activos
     */
    public long countActiveProducts() {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE active = true";
        List<Long> results = executeQuery(sql, rs -> rs.getLong(1));
        return results.isEmpty() ? 0 : results.get(0);
    }
    
    /**
     * Mapea un ResultSet a un objeto Product
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setCategory(rs.getString("category"));
        product.setActive(rs.getBoolean("active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            product.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            product.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return product;
    }
}
