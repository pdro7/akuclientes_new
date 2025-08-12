CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS clientes (
                                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hora_registro TIMESTAMPTZ NOT NULL,
    nombre_tutor VARCHAR(200) NOT NULL,
    ciudad VARCHAR(120),
    departamento VARCHAR(120),
    nombre_hijo VARCHAR(200),
    edad_hijo INT,
    como_nos_conocio VARCHAR(200),
    acepta_newsletter BOOLEAN NOT NULL
    );
