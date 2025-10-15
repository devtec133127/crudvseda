-- Läuft automatisch beim ersten Start des Postgres-Containers
-- Erstellt separate DBs je „Service“
CREATE DATABASE loans;
CREATE DATABASE inventory;
CREATE DATABASE accounting;
CREATE DATABASE reporting;

-- Optional: je DB Extensions (z. B. für JSONB/UUID; jsonb ist in Postgres core)
\connect loans
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\connect inventory
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\connect accounting
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\connect reporting
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
