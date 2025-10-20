CREATE TABLE worker_displacement_notification (
    id BIGSERIAL PRIMARY KEY,  -- PostgreSQL uses BIGSERIAL instead of AUTO_INCREMENT
    filed_number VARCHAR(50),
    
    -- Foreign Keys (No Duplication)
    worker_affiliate_id BIGINT NOT NULL,
    employer_affiliate_id BIGINT NOT NULL, 
    displacement_department_id INTEGER,
    displacement_municipality_id BIGINT,
    
    -- Displacement-specific data only
    displacement_start_date DATE NOT NULL,
    displacement_end_date DATE NOT NULL,
    displacement_reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVO',
    
    -- Audit
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT,
    updated_date TIMESTAMP,
    updated_by_user_id BIGINT,
    
    -- Foreign Key Constraints
    CONSTRAINT fk_worker_affiliate 
        FOREIGN KEY (worker_affiliate_id) REFERENCES affiliate(id_affiliate),
    CONSTRAINT fk_employer_affiliate 
        FOREIGN KEY (employer_affiliate_id) REFERENCES affiliate(id_affiliate),
    CONSTRAINT fk_displacement_department 
        FOREIGN KEY (displacement_department_id) REFERENCES tmp_departamentos(id_departamento),
    CONSTRAINT fk_displacement_municipality 
        FOREIGN KEY (displacement_municipality_id) REFERENCES tmp_municipality(id_municipio)
);


-- Worker Displacement filed number sequence
CREATE SEQUENCE IF NOT EXISTS consecutive_worker_displacement_seq
  INCREMENT BY 1
  MINVALUE 1
  START WITH 1
  NO CYCLE
  CACHE 1;

CREATE TABLE historico_cargues_masivos (
    id BIGSERIAL PRIMARY KEY,
    fecha_cargue TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20),
    id_empleador BIGINT,
    nombre_archivo VARCHAR(255),
    cantidad_registros INT,
    cantidad_errores INT,
    usuario_cargue VARCHAR(100),
    archivo_cargado BYTEA,
    archivo_errores BYTEA,
    CONSTRAINT fk_id_empleador
        FOREIGN KEY (id_empleador) REFERENCES affiliate(id_affiliate)
);