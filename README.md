# SVB-GUA JavaFX — Gestión y venta de boletos para conciertos

Nueva versión JavaFX + Maven basada en la estructura y el esquema SQL del proyecto SVB-GUA enviado por el usuario. El proyecto Swing original no se modifica.

## Requisitos
- JDK 17 o superior (recomendado JDK 21).
- Apache NetBeans 30.
- PostgreSQL y base de datos `svb_gua`.
- Conexión a Internet la primera vez que Maven descargue JavaFX y el driver JDBC.

## Abrir en NetBeans
1. Extrae el ZIP en una carpeta normal, por ejemplo `Documentos/SVB-GUA-JavaFX`.
2. En NetBeans selecciona **File > Open Project** y elige la carpeta `SVB-GUA-JavaFX` (la que contiene `pom.xml`).
3. Espera que Maven descargue las dependencias.
4. Clic derecho al proyecto y selecciona **Clean and Build**.
5. Ejecuta con **Run Project**. Si NetBeans pregunta por la clase principal, usa `com.svb.gua.MainApp`.

## Base de datos
El archivo `sql/estructura.sql` contiene el esquema de la base de datos y crea usuarios iniciales si no existen:
- Usuario: `admin`, contraseña temporal: `123456`
- Usuario: `vendedor`, contraseña temporal: `123456`

Ejecuta el script dentro de la base `svb_gua` solamente si todavía necesitas crear las tablas. No lo ejecutes a ciegas sobre una base con datos importantes; haz antes una copia de seguridad.

Por defecto la aplicación se conecta a `jdbc:postgresql://localhost:5433/svb_gua`, usuario `postgres`, contraseña `123456`, para mantener la configuración identificada en el proyecto original. Si tu PostgreSQL usa otros datos, configura estas variables de entorno antes de abrir NetBeans:
- `SVB_DB_URL` (por ejemplo `jdbc:postgresql://localhost:5432/svb_gua`)
- `SVB_DB_USER`
- `SVB_DB_PASSWORD`

## Funciones incluidas en esta versión inicial
- Inicio de sesión y roles administrador/vendedor.
- Panel con resumen y ventas recientes.
- Mantenimiento básico de artistas, conciertos, localidades, inventario y usuarios para administrador.
- Venta transaccional de boletos con validación de inventario.
- Reportes de ventas recientes, ventas por concierto, artista, vendedor e inventario disponible.

## Importante
Esta es una primera migración funcional de interfaz y módulos, no una garantía de que conecte en cualquier equipo sin ajustar la base de datos local. Debe probarse en tu computadora contra tu PostgreSQL real. La conexión a la base se valida al iniciar sesión. Conserva siempre el proyecto original `SVB-GUA` como respaldo.
