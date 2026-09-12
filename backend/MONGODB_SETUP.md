# MongoDB local para MyOrbit

El backend usa MongoDB y lee la conexion desde `MONGODB_URI`. Sin una variable definida, usa:

```text
mongodb://localhost:27017/myorbit
```

La base de datos se llama `myorbit` y las tareas quedan en la coleccion `tasks`.

## Opcion 1: MongoDB Community local

1. Completa la instalacion de MongoDB Community Server para Windows. El instalador abierto por Winget puede requerir confirmacion administrativa.
2. Mantiene habilitada la opcion de instalar MongoDB como servicio de Windows.
3. Comprueba que el servicio exista y este iniciado:

```powershell
Get-Service MongoDB
Start-Service MongoDB
```

4. Inicia el backend:

```bash
cd backend
./mvnw spring-boot:run
```

## Opcion 2: MongoDB Atlas personal

1. Crea un cluster gratuito en MongoDB Atlas.
2. Crea un usuario de base de datos y permite temporalmente tu IP actual en Network Access.
3. Copia la cadena de conexion `mongodb+srv://...` y define la variable solo en tu terminal:

```bash
export MONGODB_URI='mongodb+srv://USUARIO:CONTRASENA@CLUSTER/myorbit?retryWrites=true&w=majority'
./mvnw spring-boot:run
```

No guardes la URI de Atlas ni contrasenas dentro de `application.properties` o en Git.

## Verificacion

Con el backend iniciado, crea una tarea en la interfaz o ejecuta:

```bash
curl --request POST http://localhost:8080/api/tasks \
  --header "Content-Type: application/json" \
  --data '{"title":"Prueba MongoDB","due":"Hoy","priority":"media","tag":"Personal"}'
```

Luego valida que quedo persistida:

```bash
curl "http://localhost:8080/api/tasks?userId=demo-user"
```