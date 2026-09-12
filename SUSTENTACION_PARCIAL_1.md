# Guía de Sustentación - Primer Parcial (Backend API MyOrbit)

Este documento contiene la estructura, justificación técnica y el paso a paso para la demostración en vivo del avance del backend.

---

## 1. Justificación Arquitectónica y Técnica (1 minuto)

### ¿Por qué Arquitectura por Funcionalidades (*Package by Feature*) en lugar de MVC Clásico (*Package by Layer*)?
* **Alta Cohesión y Bajo Acoplamiento:** En lugar de tener carpetas globales (`controllers/`, `services/`, `models/`, `repositories/`), el código se agrupa por dominio de negocio (`tasks/`, `users/`, `habits/`).
* **Preparado para Microservicios:** Permite desacoplar y extraer fácilmente un módulo (por ejemplo, `tasks` o `users`) hacia un microservicio independiente en el futuro sin refactorizaciones complejas.
* **Mantenibilidad:** Toda la lógica, contratos (DTOs), entidades y persistencia de una característica residen en el mismo paquete.

### Resumen del Stack Backend
* **Lenguaje & Framework:** Java 21 + Spring Boot (Spring Data MongoDB, Spring Security Crypto).
* **Base de Datos:** MongoDB (Modelo NoSQL orientado a documentos).
* **Seguridad:** Tokens de sesión persistidos (Stateful Tokens) + Hashing de contraseñas con **BCrypt**.

---

## 2. Conexión a Base de Datos (30 segundos)

Archivos a mostrar al evaluador:
1. **Configuración:** `backend/src/main/resources/application.properties`
   ```properties
   spring.application.name=MyOrbit
   spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/myorbit}
   ```
2. **Entidad:** `backend/src/main/java/com/app/MyOrbit/tasks/Task.java` (`@Document(collection = "tasks")`).
3. **Repositorio:** `backend/src/main/java/com/app/MyOrbit/tasks/TaskRepository.java` (`extends MongoRepository<Task, String>`).

---

## 3. Demostración en Postman (Paso a Paso)

### Paso 1: Registro o Inicio de Sesión (Auth API)
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/auth/login` (o `/register` si es usuario nuevo)
* **Headers:** `Content-Type: application/json`
* **Body (raw / JSON):**
  ```json
  {
    "email": "estudiante@correo.com",
    "password": "password123"
  }
  ```
* **Respuesta esperada (`200 OK` o `201 Created`):**
  ```json
  {
    "token": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "usr-1234",
    "name": "Estudiante",
    "email": "estudiante@correo.com"
  }
  ```
* **Qué explicar:** *"El módulo de autenticación valida las credenciales con BCrypt y emite un token de sesión (UUID) que se almacena en la colección `user_sessions`. Este token protegerá las consultas posteriores."*

---

### Paso 2: Crear una Tarea (Tasks API)
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/tasks`
* **Headers:**
  * `Authorization`: `Bearer <TOKEN_OBTENIDO>`
  * `Content-Type`: `application/json`
* **Body (raw / JSON):**
  ```json
  {
    "title": "Entrega de avance Parcial 1",
    "priority": "alta",
    "tag": "Universidad",
    "due": "2026-09-15"
  }
  ```
* **Respuesta esperada (`201 Created`):** Objeto JSON de la tarea con su `id` generado.
* **Qué explicar:** *"El backend intercepta el token, identifica al usuario autenticado y guarda la tarea vinculada a su `userId`."*

---

### Paso 3: Agregar Subtarea Anidada
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/tasks/<ID_DE_LA_TAREA>/subtasks`
* **Headers:**
  * `Authorization`: `Bearer <TOKEN_OBTENIDO>`
  * `Content-Type`: `application/json`
* **Body (raw / JSON):**
  ```json
  {
    "title": "Demostración de endpoints en Postman"
  }
  ```
* **Respuesta esperada (`200 OK`):** Tarea actualizada con la subtarea embebida dentro del array `subtasks`.
* **Qué explicar:** *"Aprovechamos el modelo NoSQL de MongoDB para almacenar el árbol de subtareas embebido dentro del documento principal de la tarea sin requerir tablas intermedias."*

---

### Paso 4: Listar Tareas del Usuario Autenticado
* **Método:** `GET`
* **URL:** `http://localhost:8080/api/tasks`
* **Headers:**
  * `Authorization`: `Bearer <TOKEN_OBTENIDO>`
* **Respuesta esperada (`200 OK`):** Lista JSON conteniendo únicamente las tareas del usuario.
* **Qué explicar:** *"El endpoint asegura el aislamiento multiusuario: un usuario solo puede consultar y modificar sus propios recursos."*

---

### Paso 5: Completar Tarea (Regla de Negocio en Cascada)
* **Método:** `PATCH`
* **URL:** `http://localhost:8080/api/tasks/<ID_DE_LA_TAREA>/status`
* **Headers:**
  * `Authorization`: `Bearer <TOKEN_OBTENIDO>`
  * `Content-Type`: `application/json`
* **Body (raw / JSON):**
  ```json
  {
    "done": true
  }
  ```
* **Respuesta esperada (`200 OK`):** Tarea con `done: true` y todas sus subtareas actualizadas a `done: true`.
* **Qué explicar:** *"El `TaskService` ejecuta la regla de negocio que propaga automáticamente el estado completado a todas las subtareas asociadas."*

---

## 4. Verificación en Base de Datos (MongoDB Compass / mongosh) (30 segundos)

1. Abrir MongoDB Compass conectado a `mongodb://localhost:27017`.
2. Mostrar base de datos `myorbit`:
   * Colección `users`: Usuarios registrados con hash de contraseña.
   * Colección `user_sessions`: Sesiones activas con sus tokens.
   * Colección `tasks`: Documentos de tareas con estructura jerárquica de subtareas.

---

## 5. Checklist de Preparación Previa
- [ ] Servicio de MongoDB activo en puerto `27017`.
- [ ] Servidor backend Spring Boot ejecutándose en puerto `8080`.
- [ ] Postman configurado con la colección de peticiones y variables/headers listos.
- [ ] MongoDB Compass abierto y conectado para la evidencia de persistencia.
