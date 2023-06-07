Postgres:

create schema and user as follow:

create schema ipm;
CREATE USER ipm WITH PASSWORD 'ipm';
ALTER SCHEMA ipm OWNER TO ipm;
 
drop schema and user as follows:

drop owned by ipm;
drop user ipm;
