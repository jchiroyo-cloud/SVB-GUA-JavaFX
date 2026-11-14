# SVB-GUA — Sistema de Venta Local de Boletos

Aplicación de escritorio Java Swing + PostgreSQL para la venta de entradas a conciertos y eventos musicales. Proyecto organizado con Maven y JDBC.

## Funciones incluidas
- Inicio de sesión con roles `administrador` y `vendedor`.
- Gestión de artistas, conciertos, localidades e inventario (agregar, consultar, editar y eliminar).
- Venta de varios boletos en una transacción, con validación y descuento automático del inventario.
- Registro de ventas y detalle de cada venta.
- Reportes de ventas recientes, por concierto, artista, vendedor e inventario disponible.
- Restricciones, claves foráneas e índices en PostgreSQL.

## Requisitos
- Java JDK 17 o superior.
- Maven 3.8 o superior.
- PostgreSQL instalado y en ejecución en el puerto **5432**.

## 1. Crear la base de datos
Abre pgAdmin o `psql` y conéctate al servidor PostgreSQL con el usuario `postgres`. Ejecuta el archivo `sql/crear_base_datos.sql` una sola vez. Después selecciona la base `svb_gua` y ejecuta `sql/estructura.sql`.

Configuración esperada por defecto:
- Host: `localhost`
- Puerto: `5432`
- Base de datos: `svb_gua`
- Usuario PostgreSQL: `postgres`
- Contraseña PostgreSQL: `123456`

Si la contraseña de tu servidor PostgreSQL es diferente, establece la variable de entorno `SVB_DB_PASSWORD` antes de iniciar. También se pueden configurar `SVB_DB_URL` y `SVB_DB_USER`.

## 2. Usuarios de acceso inicial
Los crea `sql/estructura.sql`:
- Administrador: usuario `admin`, contraseña `123456`.
- Vendedor: usuario `vendedor`, contraseña `123456`.

La contraseña de acceso a la aplicación es independiente de la contraseña del servidor PostgreSQL. Cambia las contraseñas iniciales antes de usarlo con datos reales.

## 3. Abrir el proyecto
1. Extrae el ZIP completo en una carpeta.
2. Abre la carpeta `SVB-GUA` en Visual Studio Code o IntelliJ IDEA.
3. Confirma que Java 17+ y Maven están instalados.
4. En la terminal, dentro de la carpeta del proyecto, ejecuta:

```bash
mvn clean package
java -jar target/svb-gua-1.0.0.jar
```

En Windows también puedes ejecutar `INICIAR_WINDOWS.bat`.

## 4. Primer uso sugerido
1. Inicia sesión como `admin`.
2. Registra artistas.
3. Registra conciertos. La fecha debe escribirse como `AAAA-MM-DD HH:MM:SS`, por ejemplo `2026-12-15 19:30:00`.
4. Revisa las localidades iniciales: General, Preferencial y VIP.
5. Crea el inventario de boletos por concierto y localidad, con precio, cantidad total y cantidad disponible iguales al inicio.
6. Abre **Vender boletos**, selecciona el inventario y la cantidad, y confirma la venta.
7. Consulta la pestaña **Reportes**.

## Notas
- El script crea las tablas si todavía no existen. No elimina información existente.
- No se puede eliminar un artista/localidad/usuario si existen registros relacionados que lo impiden; es una protección de integridad.
- Las ventas se guardan en una transacción: si falla el registro de algún detalle, se revierte toda la operación.
- La contraseña se almacena como hash SHA-256 en la base de datos; para producción se recomienda un algoritmo dedicado de contraseñas como Argon2id o bcrypt.
