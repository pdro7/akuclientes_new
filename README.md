# AkuClientes - Sistema de Gestión de Clientes

Sistema de gestión de clientes desarrollado en Spring Boot con funcionalidad de búsqueda avanzada usando Elasticsearch.

## 🚀 Características

- **Gestión de Clientes**: Almacena información de tutores y sus hijos
- **Búsqueda Avanzada**: Búsqueda de texto completo con Elasticsearch
- **Filtros Dinámicos**: Filtrado por ciudad, departamento, y rango de edad
- **Facetas**: Contadores automáticos por categorías
- **Importación de Datos**: Soporte para archivos Excel/CSV
- **API REST**: Endpoints completos para integración

## 🛠️ Tecnologías

- **Backend**: Spring Boot 3.5.4
- **Base de Datos**: PostgreSQL 16
- **Búsqueda**: Elasticsearch 8.14.0
- **Java**: OpenJDK 17
- **Contenedores**: Docker & Docker Compose
- **Build**: Maven

## 📋 Prerrequisitos

- Java 17 o superior
- Docker y Docker Compose
- Maven (incluido como wrapper)

## 🚀 Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone https://github.com/pdro7/akuclientes_new.git
cd akuclientes_new
```

### 2. Iniciar servicios con Docker
```bash
docker-compose up -d
```

Esto iniciará:
- PostgreSQL en el puerto 5432
- Elasticsearch en el puerto 9200

### 3. Crear base de datos
```bash
docker exec -it postgres_akumaya psql -U akumaya -c "CREATE DATABASE IF NOT EXISTS akumaya;"
```

### 4. Ejecutar la aplicación
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

La aplicación estará disponible en `http://localhost:8080`

## 📊 Importación de Datos

El sistema incluye datos de ejemplo en formato CSV. Para importar:

1. La aplicación detecta automáticamente el archivo `clientes_data.xlsx` al iniciar
2. Los datos se importan automáticamente en la base de datos y Elasticsearch
3. Se procesarán 91 registros de clientes con información completa

## 🔍 API Endpoints

### Búsqueda de Clientes
```http
GET /api/clientes/search?q={texto}&ciudad={ciudad}&departamento={depto}&edadMin={min}&edadMax={max}&page={page}&size={size}
```

**Parámetros:**
- `q`: Texto de búsqueda (nombres de tutor, hijo, o referencia)
- `ciudad`: Filtro por ciudad
- `departamento`: Filtro por departamento
- `edadMin`/`edadMax`: Rango de edad del hijo
- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 10)

**Ejemplo de respuesta:**
```json
{
  "content": [
    {
      "id": 1,
      "nombreTutor": "Juan Pérez",
      "nombreHijo": "Santiago Pérez",
      "edadHijo": 8,
      "ciudad": "Bogotá",
      "departamento": "Cundinamarca",
      "comoNosConocio": "Redes sociales"
    }
  ],
  "totalElements": 8,
  "totalPages": 1,
  "facets": {
    "ciudades": {"Bogotá": 5, "Medellín": 2, "Cali": 1},
    "departamentos": {"Cundinamarca": 5, "Antioquia": 2, "Valle": 1},
    "rangosEdad": {"6-8": 3, "9-11": 4, "12-14": 1}
  }
}
```

### Ejemplos de Búsqueda

1. **Búsqueda por nombre:**
   ```
   GET /api/clientes/search?q=Santiago
   ```
   Retorna 8 resultados con "Santiago" en nombres

2. **Búsqueda con filtros:**
   ```
   GET /api/clientes/search?q=Juan&ciudad=Bogotá&edadMin=8&edadMax=12
   ```

3. **Solo filtros (sin texto):**
   ```
   GET /api/clientes/search?departamento=Cundinamarca&page=0&size=20
   ```

## 🏗️ Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/akumaya/akuclientes/
│   │   ├── config/          # Configuración de Elasticsearch
│   │   ├── controller/      # Controladores REST
│   │   ├── domain/          # Entidades JPA y documentos ES
│   │   ├── repository/      # Repositorios JPA y ES
│   │   ├── service/         # Lógica de negocio
│   │   └── util/           # Utilidades y helpers
│   └── resources/
│       ├── application.yml  # Configuración principal
│       └── application-*.yml # Perfiles específicos
├── test/                   # Tests unitarios
└── docker-compose.yml      # Servicios Docker
```

## 🔧 Configuración

### Perfiles Disponibles
- `local`: Para desarrollo local con Docker
- `aws`: Para despliegue en AWS

### Variables de Entorno Principales
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/akumaya
    username: akumaya
    password: akumaya123
  
  elasticsearch:
    uris: http://localhost:9200
```

## ✅ Funcionalidades Principales

### ✨ Búsqueda de Texto Mejorada
- Búsqueda fuzzy en nombres de tutores e hijos
- Búsqueda en campo "Cómo nos conoció"
- Implementación con `multi_match` de Elasticsearch para máxima precisión

### 🎯 Filtros y Facetas
- Filtrado por ciudad y departamento
- Filtros de rango de edad
- Facetas dinámicas con contadores automáticos
- Paginación completa

### 📈 Casos de Uso Validados
- ✅ Búsqueda "Santiago" → 8 resultados
- ✅ Búsqueda "Juan" → 12 resultados  
- ✅ Filtros combinados funcionan correctamente
- ✅ Facetas actualizadas dinámicamente

## 🐳 Docker

Servicios incluidos en `docker-compose.yml`:
- **PostgreSQL**: Base de datos principal
- **Elasticsearch**: Motor de búsqueda

```bash
# Iniciar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

## 🧪 Testing

```bash
# Ejecutar tests
./mvnw test

# Ejecutar con perfil específico
./mvnw test -Dspring.profiles.active=test
```

## 📝 Notas Importantes

### Búsqueda de Texto
La funcionalidad de búsqueda por texto ha sido **completamente reparada**. La implementación anterior usando `Criteria.matches()` no funcionaba correctamente. La nueva implementación usa:

```java
// Query Elasticsearch nativa para búsqueda multi-campo
String queryJson = String.format("""
    {
      "multi_match": {
        "query": "%s",
        "fields": ["nombreTutor", "nombreHijo", "comoNosConocio"]
      }
    }
    """, q.replace("\"", "\\\""));
```

### Datos de Ejemplo
El sistema incluye 91 registros reales de clientes importados desde CSV, incluyendo:
- Nombres de tutores y niños
- Edades, ciudades y departamentos
- Información de referencia

## 🤝 Contribución

1. Fork del proyecto
2. Crear rama para feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit de cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT.

## 🚀 Estado del Proyecto

- ✅ **Búsqueda por texto**: Completamente funcional
- ✅ **Filtros**: Todos operativos
- ✅ **Facetas**: Implementadas y funcionales
- ✅ **Importación de datos**: Automática al inicio
- ✅ **API REST**: Endpoints completos y documentados

---

**Desarrollado con Spring Boot + Elasticsearch para búsquedas avanzadas de clientes** 🔍