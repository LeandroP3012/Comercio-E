# Proyecto E-Commerce con PostgreSQL

Este proyecto demuestra cómo implementar conexiones a PostgreSQL en una aplicación Java usando Maven, HikariCP como pool de conexiones, y patrones DAO para operaciones de base de datos.

## 🚀 Características

- ✅ Conexión a PostgreSQL usando JDBC
- ✅ Pool de conexiones con HikariCP para mejor rendimiento
- ✅ Patrón DAO (Data Access Object) para separación de capas
- ✅ Manejo de transacciones
- ✅ Logging con SLF4J y Logback
- ✅ Configuración externalizada con archivos de propiedades
- ✅ Ejemplo completo de operaciones CRUD

## 📋 Prerrequisitos

1. **Java 17 o superior**
2. **Maven 3.6 o superior**
3. **PostgreSQL 12 o superior**
4. **Base de datos creada** (ver instrucciones abajo)

## 🔧 Configuración

### 1. Instalar y configurar PostgreSQL

```bash
# En Windows (usando Chocolatey)
choco install postgresql

# O descarga desde: https://www.postgresql.org/download/windows/
```

### 2. Crear la base de datos

```sql
-- Conectar a PostgreSQL como superusuario
psql -U postgres

-- Crear la base de datos
CREATE DATABASE comercio_db;

-- Crear un usuario (opcional)
CREATE USER comercio_user WITH PASSWORD 'tu_password';
GRANT ALL PRIVILEGES ON DATABASE comercio_db TO comercio_user;
```

### 3. Ejecutar el script de inicialización

```bash
# Conectar a la base de datos
psql -U postgres -d comercio_db

# Ejecutar el script SQL
\i src/main/resources/database/init_products.sql
```

### 4. Configurar las credenciales

Edita el archivo `src/main/resources/database.properties`:

```properties
# Configuración de la base de datos PostgreSQL
db.url=jdbc:postgresql://localhost:5432/comercio_db
db.username=tu_usuario
db.password=tu_password
```

## 🏃‍♂️ Ejecución

### Compilar el proyecto

```bash
mvn clean compile
```

### Ejecutar la aplicación

```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

### Ejecutar con Maven directamente

```bash
mvn clean compile exec:java
```

## 📁 Estructura del proyecto

```
src/
├── main/
│   ├── java/
│   │   └── org/example/
│   │       ├── Main.java              # Clase principal con ejemplos
│   │       ├── config/
│   │       │   └── DatabaseConfig.java # Configuración de conexión
│   │       ├── dao/
│   │       │   ├── BaseDAO.java        # Clase base para DAOs
│   │       │   └── ProductDAO.java     # DAO para productos
│   │       └── model/
│   │           └── Product.java        # Modelo de producto
│   └── resources/
│       ├── database.properties         # Configuración de BD
│       └── database/
│           └── init_products.sql       # Script de inicialización
```

## 🔍 Ejemplos de uso

### Conectar a la base de datos

```java
// Probar conexión
if (DatabaseConfig.testConnection()) {
    System.out.println("Conexión exitosa!");
}

// Obtener conexión manual
try (Connection conn = DatabaseConfig.getConnection()) {
    // Usar la conexión...
}
```

### Operaciones CRUD con ProductDAO

```java
ProductDAO productDAO = new ProductDAO();

// Crear producto
Product product = new Product("Laptop", "Descripción", new BigDecimal("999.99"), 10, "Electrónicos");
Long id = productDAO.save(product);

// Buscar por ID
Optional<Product> found = productDAO.findById(id);

// Listar todos
List<Product> products = productDAO.findAll();

// Buscar por categoría
List<Product> electronics = productDAO.findByCategory("Electrónicos");

// Actualizar
product.setPrice(new BigDecimal("899.99"));
productDAO.update(product);

// Eliminar (soft delete)
productDAO.delete(id);
```

## 🛠️ Dependencias principales

- **PostgreSQL JDBC Driver**: Conectividad con PostgreSQL
- **HikariCP**: Pool de conexiones de alto rendimiento
- **SLF4J + Logback**: Sistema de logging
- **Maven**: Gestión de dependencias y build

## 📊 Configuración del pool de conexiones

El pool de conexiones está configurado con los siguientes parámetros por defecto:

- **Máximo de conexiones**: 10
- **Mínimo inactivas**: 2
- **Timeout de conexión**: 30 segundos
- **Timeout inactivo**: 10 minutos
- **Tiempo de vida máximo**: 30 minutos

Puedes modificar estos valores en `database.properties`.

## 🔒 Seguridad

- Las credenciales se mantienen en archivos de configuración externos
- Se usa prepared statements para prevenir SQL injection
- El pool de conexiones maneja la liberación automática de recursos

## 🐛 Solución de problemas

### Error: "Connection refused"
- Verifica que PostgreSQL esté ejecutándose
- Confirma el puerto (por defecto 5432)
- Revisa la configuración del firewall

### Error: "Database does not exist"
- Asegúrate de haber creado la base de datos `comercio_db`
- Verifica el nombre en `database.properties`

### Error: "Authentication failed"
- Confirma usuario y contraseña en `database.properties`
- Verifica permisos del usuario en PostgreSQL

### Error: "Table 'products' doesn't exist"
- Ejecuta el script `init_products.sql`
- Verifica que estés conectado a la base de datos correcta

## 📝 Logs

Los logs se muestran en consola y incluyen:
- Estado de conexiones
- Operaciones de base de datos
- Errores y excepciones
- Estadísticas del pool de conexiones

## 🚀 Próximos pasos

- Agregar más modelos (Usuario, Pedido, etc.)
- Implementar servicios de negocio
- Agregar validaciones
- Implementar API REST
- Agregar tests unitarios e integración

## 📄 Licencia

Este proyecto es de ejemplo educativo y está disponible bajo licencia MIT.
