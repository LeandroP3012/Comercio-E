package org.example.dao;

import org.example.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase base para operaciones de base de datos
 * Proporciona métodos comunes para operaciones CRUD
 */
public abstract class BaseDAO {
    protected static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);
    
    /**
     * Ejecuta una consulta SELECT y devuelve una lista de resultados
     * @param sql consulta SQL
     * @param mapper función para mapear ResultSet a objeto
     * @param params parámetros de la consulta
     * @return lista de resultados
     */
    protected <T> List<T> executeQuery(String sql, ResultSetMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            // Establecer parámetros
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapper.map(resultSet));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error ejecutando consulta: " + sql, e);
            throw new RuntimeException("Error en consulta de base de datos", e);
        }
        
        return results;
    }
    
    /**
     * Ejecuta una operación de inserción, actualización o eliminación
     * @param sql consulta SQL
     * @param params parámetros de la consulta
     * @return número de filas afectadas
     */
    protected int executeUpdate(String sql, Object... params) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            // Establecer parámetros
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            return statement.executeUpdate();
            
        } catch (SQLException e) {
            logger.error("Error ejecutando actualización: " + sql, e);
            throw new RuntimeException("Error en operación de base de datos", e);
        }
    }
    
    /**
     * Ejecuta una inserción y devuelve la clave generada
     * @param sql consulta SQL de inserción
     * @param params parámetros de la consulta
     * @return ID generado
     */
    protected Long executeInsertWithGeneratedKey(String sql, Object... params) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Establecer parámetros
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La inserción falló, no se afectó ninguna fila");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("La inserción falló, no se obtuvo el ID");
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error ejecutando inserción: " + sql, e);
            throw new RuntimeException("Error en inserción de base de datos", e);
        }
    }
    
    /**
     * Ejecuta una operación dentro de una transacción
     * @param operation operación a ejecutar
     */
    protected void executeInTransaction(DatabaseOperation operation) {
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            operation.execute(connection);
            
            connection.commit();
            
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Error al hacer rollback", rollbackEx);
                }
            }
            logger.error("Error en transacción", e);
            throw new RuntimeException("Error en transacción de base de datos", e);
            
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Error al cerrar conexión", e);
                }
            }
        }
    }
    
    /**
     * Interface para mapear ResultSet a objetos
     */
    @FunctionalInterface
    protected interface ResultSetMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }
    
    /**
     * Interface para operaciones de base de datos
     */
    @FunctionalInterface
    protected interface DatabaseOperation {
        void execute(Connection connection) throws SQLException;
    }
}
