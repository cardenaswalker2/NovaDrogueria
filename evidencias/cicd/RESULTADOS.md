# 📊 REGISTRO OFICIAL DE RESULTADOS CI/CD - NOVADROGUERIA

## 🏢 Proyecto
- **Nombre del Proyecto**: NovaDrogueria (Sistema Integral Farmacéutico Backend Spring Boot + Frontend App)
- **Java**: OpenJDK 17.0.16 (Temurin-17.0.16+8)
- **Spring Boot**: 3.4.2
- **Maven**: Apache Maven 3.9.11
- **Base de Datos**: MongoDB 8.0 / 7.0 con soporte de Replica Set (`rs0`) para transacciones ACID multi-documento (`MongoTransactionManager`).

---

## 🌿 Git
- **Repositorio Remoto**: [https://github.com/cardenaswalker2/NovaDrogueria.git](https://github.com/cardenaswalker2/NovaDrogueria.git)
- **Rama Principal**: `main`
- **Ramas de Trabajo / Taller**: `chore/taller-cicd`, `feature/endpoint-estado`
- **Commit Base CI**: `06d8293` (*fix: corrige assertion para recuperar estado VERDE del pipeline*)
- **Commit Feature**: `391feff` (*feat: agrega endpoint de estado GET /api/estado y pruebas automatizadas*)

---

## 🚀 CI / CD (GitHub Actions)
- **Workflow Archivo**: `.github/workflows/ci.yml`
- **Disparadores (Triggers)**:
  - `push` en `main`, `develop`, `chore/**`, `feature/**`
  - `pull_request` en `main`
- **Ejecuciones Reales Verificadas**:
  1. **Run ID `33325603850`**: Primer despliegue de CI en `chore/taller-cicd` -> **SUCCESS (Verde)**
  2. **Run ID `33325679422`**: Demostración de fallo controlado por test unitario incorrecto -> **FAILURE (Rojo)**
  3. **Run ID `33325737975`**: Recuperación tras corrección de assertion -> **SUCCESS (Verde)**
  4. **Run ID `33325811965`**: Pipeline ejecutado en rama `main` tras incorporación -> **SUCCESS (Verde)**
  5. **Run ID `33325847415`**: Pipeline de rama `feature/endpoint-estado` con endpoint nuevo -> **SUCCESS (Verde)**

---

## 🧪 Tests
- **Total de Tests Ejecutados**: 26 pruebas automáticas (100% pasando, 0 fallos, 0 errores, 0 omitidos).
- **Pruebas Creadas para el Taller**:
  1. **Test de Lógica de Negocio / Service (`ProductServiceTest.java`)**:
     - `testGetProductByIdSuccess()`: Búsqueda y validación de entidad.
     - `testGetProductByIdNotFound()`: Manejo de excepción `ResourceNotFoundException`.
     - `testCreateProductSuccess()`: Creación con asignación de timestamps.
     - `testCreateProductDuplicateSlugThrowsException()`: Validación de slug duplicado (`BusinessRuleException`).
     - `testCreateProductNegativePriceThrowsException()`: Validación de precio negativo.
     - `testCreateProductNegativeStockThrowsException()`: Validación de inventario negativo.
     - `testDeactivateOrDeleteProduct()`: Borrado lógico seguro (*Soft Delete*).
  2. **Test de Controller con MockMvc (`ProductApiControllerTest.java`)**:
     - `testSearchProductsReturnsList()`: Endpoint `GET /api/productos/buscar?q=ibuprofeno`.
     - `testSearchProductsEmptyQuery()`: Validación de cadenas en blanco.
  3. **Test de Endpoint de Estado (`StatusApiControllerTest.java`)**:
     - `testGetEstadoReturnsUp()`: Endpoint `GET /api/estado` con respuesta HTTP 200 y JSON estructurado.

---

## 📊 JaCoCo (Cobertura de Código)
- **Herramienta**: `org.jacoco:jacoco-maven-plugin:0.8.12`
- **Ubicación del Reporte**: `target/site/jacoco/index.html`
- **Métricas Reales**:
  - Cobertura Global de Instrucciones: **29%** (1,772 de 6,018 instrucciones)
  - Cobertura Global de Ramas (Branches): **25%** (103 de 399 ramas)
  - Cobertura de Líneas: **29%** (353 de 1,201 líneas)
  - Cobertura de Métodos: **37%** (120 de 320 métodos)
  - Cobertura en Utilitarios (`com.example.demo.util`): **88%**
  - Cobertura en Seguridad (`com.example.demo.security`): **80%**
  - Cobertura en Servicios (`com.example.demo.service`): **37%**

---

## 📦 Artifacts
- **Nombre**: `reporte-jacoco`
- **Ruta Generada**: `target/site/jacoco`
- **Disponibilidad**: Publicado automáticamente en cada ejecución del workflow de GitHub Actions con retención de 14 días.

---

## 🔐 Seguridad y Secretos
- **GitHub Actions Secret**: `APP_ENV_DEMO`
- **Valor Configurado de Prueba**: `workshop-2026`
- **Protección**: Gestionado como secreto enmascarado en GitHub Secrets (`${{ secrets.APP_ENV_DEMO }}`), validado en el pipeline mediante comprobación de presencia y hash SHA-256 sin imprimir en consola.
- **Fuga de Secretos**: Ninguna. `.gitignore` protege exhaustivamente variables locales (`.env`, `application-local.properties`, `target/`, etc.).

---

## 🔒 Branch Protection
- **Rama Protegida**: `main`
- **Regla Configurada**: *Require status checks to pass before merging* asociado al job `Build, Test & JaCoCo Coverage`.

---

## 🔀 Pull Request
- **Rama Origen**: `feature/endpoint-estado`
- **Rama Destino**: `main`
- **URL de Comparación / PR**: [https://github.com/cardenaswalker2/NovaDrogueria/compare/main...feature/endpoint-estado](https://github.com/cardenaswalker2/NovaDrogueria/compare/main...feature/endpoint-estado)
- **Estado de Validación**: Pipeline verde comprobado en la rama feature antes de la integración.

---

## 🛠️ Problemas Reales Identificados y Solución Técnica

1. **Requerimiento de Transacciones MongoDB (Replica Set en CI y Local)**:
   - *Causa*: `ReservationService` implementa `@Transactional` a través de `MongoTransactionManager`. En instancias standalone de MongoDB las transacciones multi-documento fallan con `UncategorizedMongoDbException: This MongoDB deployment does not support retryable writes`.
   - *Solución*: Se inició MongoDB con la bandera `--replSet rs0` e inicialización de replica set, y en el workflow de GitHub Actions se configuró un contenedor Docker `mongo:7.0` con inicialización automática de `rs0`, permitiendo que las pruebas de integración se ejecuten al 100% de manera idéntica en CI y en local.

2. **Aislamiento en Pruebas Unitarias de Controladores Web**:
   - *Causa*: Inicialmente `@MockBean` sobre `MongoTemplate` en un contexto completo `@SpringBootTest` interfería con la inicialización automática de los repositorios Spring Data en `DataSeeder`.
   - *Solución*: Se implementó la arquitectura recomendada con `MockMvcBuilders.standaloneSetup()`, logrando pruebas puramente desacopladas, ultrarrápidas y sin dependencias de base de datos externa.
