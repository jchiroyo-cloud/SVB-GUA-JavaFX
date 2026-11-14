CREATE EXTENSION IF NOT EXISTS pgcrypto;
-- Ejecutar dentro de la base svb_gua. Script idempotente.
CREATE TABLE IF NOT EXISTS usuarios (
 id_usuario SERIAL PRIMARY KEY,
 nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
 contrasena_hash VARCHAR(255) NOT NULL,
 nombre_completo VARCHAR(100) NOT NULL,
 rol VARCHAR(20) NOT NULL CHECK (rol IN ('administrador','vendedor')),
 fecha_creacion TIMESTAMP DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS artistas (
 id_artista SERIAL PRIMARY KEY,
 nombre_artistico VARCHAR(120) NOT NULL,
 genero_musical VARCHAR(50) NOT NULL,
 pais_origen VARCHAR(50)
);
CREATE TABLE IF NOT EXISTS conciertos (
 id_concierto SERIAL PRIMARY KEY,
 id_artista INTEGER NOT NULL REFERENCES artistas(id_artista) ON DELETE RESTRICT,
 titulo_evento VARCHAR(150) NOT NULL,
 fecha_concierto TIMESTAMP NOT NULL,
 recinto VARCHAR(120) NOT NULL,
 estado VARCHAR(20) NOT NULL DEFAULT 'programado' CHECK (estado IN ('programado','activo','finalizado','cancelado'))
);
CREATE TABLE IF NOT EXISTS localidades (
 id_localidad SERIAL PRIMARY KEY,
 nombre VARCHAR(50) UNIQUE NOT NULL
);
CREATE TABLE IF NOT EXISTS inventario_boletos (
 id_inventario SERIAL PRIMARY KEY,
 id_concierto INTEGER NOT NULL REFERENCES conciertos(id_concierto) ON DELETE CASCADE,
 id_localidad INTEGER NOT NULL REFERENCES localidades(id_localidad) ON DELETE RESTRICT,
 precio NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
 cantidad_total INTEGER NOT NULL CHECK (cantidad_total >= 0),
 cantidad_disponible INTEGER NOT NULL CHECK (cantidad_disponible >= 0 AND cantidad_disponible <= cantidad_total),
 CONSTRAINT unq_concierto_localidad UNIQUE (id_concierto,id_localidad)
);
CREATE TABLE IF NOT EXISTS ventas (
 id_venta SERIAL PRIMARY KEY,
 id_vendedor INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
 fecha_venta TIMESTAMP DEFAULT NOW(),
 total_venta NUMERIC(10,2) NOT NULL CHECK (total_venta >= 0)
);
CREATE TABLE IF NOT EXISTS detalle_ventas (
 id_detalle SERIAL PRIMARY KEY,
 id_venta INTEGER NOT NULL REFERENCES ventas(id_venta) ON DELETE CASCADE,
 id_inventario INTEGER NOT NULL REFERENCES inventario_boletos(id_inventario) ON DELETE RESTRICT,
 cantidad INTEGER NOT NULL CHECK (cantidad > 0),
 precio_unitario NUMERIC(10,2) NOT NULL CHECK (precio_unitario >= 0),
 subtotal NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0)
);
CREATE INDEX IF NOT EXISTS idx_conciertos_artista ON conciertos(id_artista);
CREATE INDEX IF NOT EXISTS idx_inventario_concierto ON inventario_boletos(id_concierto);
CREATE INDEX IF NOT EXISTS idx_ventas_vendedor ON ventas(id_vendedor);
CREATE INDEX IF NOT EXISTS idx_detalle_venta ON detalle_ventas(id_venta);
-- Usuarios iniciales: contraseña temporal para ambos: 123456
INSERT INTO usuarios(nombre_usuario,contrasena_hash,nombre_completo,rol)
VALUES ('admin', encode(digest('123456','sha256'),'hex'),'Administrador del sistema','administrador')
ON CONFLICT(nombre_usuario) DO NOTHING;
INSERT INTO usuarios(nombre_usuario,contrasena_hash,nombre_completo,rol)
VALUES ('vendedor', encode(digest('123456','sha256'),'hex'),'Vendedor de taquilla','vendedor')
ON CONFLICT(nombre_usuario) DO NOTHING;
INSERT INTO localidades(nombre) VALUES ('General'),('Preferencial'),('VIP') ON CONFLICT(nombre) DO NOTHING;
