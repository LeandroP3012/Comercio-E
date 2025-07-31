package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Configuración de la base de datos PostgreSQL
 * Utiliza HikariCP como pool de conexiones para mejor rendimiento
 * Lee la configuración desde el archivo database.properties
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    private static HikariDataSource dataSource;
    private static Properties dbProperties;
    
    static {
        loadProperties();
        initializeDataSource();
    }
    
    /**
     * Carga las propiedades de configuración desde el archivo database.properties
     */
    private static void loadProperties() {
        dbProperties = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                logger.warn("No se encontró el archivo database.properties, usando valores por defecto");
                setDefaultProperties();
                return;
            }
            dbProperties.load(input);
            logger.info("Propiedades de base de datos cargadas correctamente");
        } catch (IOException e) {
            logger.error("Error al cargar propiedades de base de datos", e);
            setDefaultProperties();
        }
    }
    
    /**
     * Establece valores por defecto si no se puede cargar el archivo de propiedades
     */
    private static void setDefaultProperties() {
        dbProperties.setProperty("db.url", "jdbc:postgresql://localhost:5432/comercio_db");
        dbProperties.setProperty("db.username", "postgres");
        dbProperties.setProperty("db.password", "Pr0J3ctoSoft25");
        dbProperties.setProperty("db.driver", "org.postgresql.Driver");
        dbProperties.setProperty("db.pool.maximum-pool-size", "10");
        dbProperties.setProperty("db.pool.minimum-idle", "2");
        dbProperties.setProperty("db.pool.connection-timeout", "30000");
        dbProperties.setProperty("db.pool.idle-timeout", "600000");
        dbProperties.setProperty("db.pool.max-lifetime", "1800000");
    }
    
    /**
     * Inicializa el pool de conexiones HikariCP
     */
    private static void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            
            // Configuración básica de conexión
            config.setJdbcUrl(dbProperties.getProperty("db.url"));
            config.setUsername(dbProperties.getProperty("db.username"));
            config.setPassword(dbProperties.getProperty("db.password"));
            config.setDriverClassName(dbProperties.getProperty("db.driver"));
            
            // Configuración del pool de conexiones
            config.setMaximumPoolSize(Integer.parseInt(dbProperties.getProperty("db.pool.maximum-pool-size", "10")));
            config.setMinimumIdle(Integer.parseInt(dbProperties.getProperty("db.pool.minimum-idle", "2")));
            config.setConnectionTimeout(Long.parseLong(dbProperties.getProperty("db.pool.connection-timeout", "30000")));
            config.setIdleTimeout(Long.parseLong(dbProperties.getProperty("db.pool.idle-timeout", "600000")));
            config.setMaxLifetime(Long.parseLong(dbProperties.getProperty("db.pool.max-lifetime", "1800000")));
            
            // Configuraciones adicionales para PostgreSQL
            config.addDataSourceProperty("cachePrepStmts", 
                dbProperties.getProperty("db.cache-prep-stmts", "true"));
            config.addDataSourceProperty("prepStmtCacheSize", 
                dbProperties.getProperty("db.prep-stmt-cache-size", "250"));
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 
                dbProperties.getProperty("db.prep-stmt-cache-sql-limit", "2048"));
            
            dataSource = new HikariDataSource(config);
            
            logger.info("Pool de conexiones inicializado correctamente");
            logger.info("URL de base de datos: {}", maskPassword(dbProperties.getProperty("db.url")));
            
        } catch (Exception e) {
            logger.error("Error al inicializar el pool de conexiones", e);
            throw new RuntimeException("No se pudo inicializar la conexión a la base de datos", e);
        }
    }
    
    /**
     * Enmascara información sensible para los logs
     */
    private static String maskPassword(String url) {
        return url.replaceAll("password=[^&]*", "password=***");
    }
    
    /**
     * Obtiene una conexión del pool
     * @return Connection objeto de conexión a la base de datos
     * @throws SQLException si hay error al obtener la conexión
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("El pool de conexiones no está inicializado");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Obtiene el DataSource
     * @return DataSource
     */
    public static DataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * Cierra el pool de conexiones
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Pool de conexiones cerrado");
        }
    }
    
    /**
     * Verifica si la conexión a la base de datos está disponible
     * @return true si la conexión es exitosa, false en caso contrario
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            boolean isValid = connection != null && !connection.isClosed() && connection.isValid(5);
            if (isValid) {
                logger.info("Conexión a la base de datos exitosa");
            } else {
                logger.warn("La conexión a la base de datos no es válida");
            }
            return isValid;
        } catch (SQLException e) {
            logger.error("Error al probar la conexión", e);
            return false;
        }
    }
    
    /**
     * Obtiene información del estado del pool de conexiones
     */
    public static void printPoolStats() {
        if (dataSource != null) {
            logger.info("Estado del pool - Conexiones activas: {}, Conexiones inactivas: {}, Total: {}", 
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections());
        }
    }
}
