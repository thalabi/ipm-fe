Postgres:

create schema and user as follow:

create schema ipm;
CREATE USER ipm WITH PASSWORD 'ipm';
ALTER SCHEMA ipm OWNER TO ipm;
 
drop schema and user as follows:

drop owned by ipm;
drop user ipm;


-- backup and restore

to backup schema:

pg_dump -U postgres -n ipm -f -c  -f ipm.bak postgres

to restore schema:

drop schema ipm cascade;

psql -U postgres -f ipm.bak


