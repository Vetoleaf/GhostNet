-- Create "persons" table
CREATE TABLE IF NOT EXISTS persons (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    telephone_number VARCHAR(15) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Create "ghost_nets" table
CREATE TABLE IF NOT EXISTS ghost_nets (
    id SERIAL PRIMARY KEY,
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL,
    size INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    person_id BIGINT REFERENCES persons(id) NOT NULL,
    rescue_person_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
-- Create "rescue_persons" table
CREATE TABLE IF NOT EXISTS rescue_persons (
  id INT8 NOT NULL DEFAULT unique_rowid(),
  name VARCHAR(255) NULL,
  telephone_number VARCHAR(255) NULL,
  "role" VARCHAR(255) NULL,
  status VARCHAR(255) NULL,
  netz_id INT8 NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Insert into "persons" table
INSERT INTO persons (name, telephone_number, role, created_at)
VALUES
    ('John', '016162397981', 'meldende Person', '2023-12-10T13:35:01.236339'),
    ('Emma', '016164397982', 'meldende Person', '2023-12-10T13:40:10.249024'),
    ('Michael', '016162397983', 'meldende Person', '2023-12-10T13:45:01.236339'),
    ('Sophia', '016164397984', 'meldende Person', '2023-12-10T13:50:10.249024'),
    ('Daniel', '016162397985', 'meldende Person', '2023-12-10T13:55:01.236339'),
    ('Olivia', '016164397986', 'meldende Person', '2023-12-10T14:00:10.249024'),
    ('David', '016162397987', 'meldende Person', '2023-12-10T14:05:01.236339'),
    ('Mia', '016164397988', 'meldende Person', '2023-12-10T14:10:10.249024'),
    ('Lucas', '016162397989', 'meldende Person', '2023-12-10T14:15:01.236339'),
    ('Ella', '016164397990', 'meldende Person', '2023-12-10T14:20:10.249024')
RETURNING id;

-- Insert into "ghost_nets" table using the generated person IDs
INSERT INTO ghost_nets (latitude, longitude, size, status, person_id, rescue_person_id, created_at)
VALUES
    ('29.792984', '-59.326035', '30', 'gemeldet', (SELECT id FROM persons WHERE name = 'John'), NULL, '2023-12-10T13:35:01.236339'),
    ('-11.576907', '74.356916', '70', 'gemeldet', (SELECT id FROM persons WHERE name = 'Emma'), NULL, '2023-12-10T13:40:10.249024'),
    ('20.910134', '139.791203', '35', 'gemeldet', (SELECT id FROM persons WHERE name = 'Michael'), NULL, '2023-12-10T13:45:01.236339'),
    ('72.695571', '-63.547602', '75', 'gemeldet', (SELECT id FROM persons WHERE name = 'Sophia'), NULL, '2023-12-10T13:50:10.249024'),
    ('71.170032', '43.398759', '40', 'gemeldet', (SELECT id FROM persons WHERE name = 'Daniel'), NULL, '2023-12-10T13:55:01.236339'),
    ('34.139088', '18.524754', '80', 'gemeldet', (SELECT id FROM persons WHERE name = 'Olivia'), NULL, '2023-12-10T14:00:10.249024'),
    ('33.555129', '124.063926', '45', 'gemeldet', (SELECT id FROM persons WHERE name = 'David'), NULL, '2023-12-10T14:05:01.236339'),
    ('37.835819', '5.156459', '85', 'gemeldet', (SELECT id FROM persons WHERE name = 'Mia'), NULL, '2023-12-10T14:10:10.249024'),
    ('42.478174', '49.686783', '90', 'gemeldet', (SELECT id FROM persons WHERE name = 'Lucas'), NULL, '2023-12-10T14:15:01.236339'),
    ('59.303758', '-92.892116', '95', 'gemeldet', (SELECT id FROM persons WHERE name = 'Ella'), NULL, '2023-12-10T14:20:10.249024');


