For postgres create schema and user as follow:

create schema ipm;
CREATE USER ipm WITH PASSWORD 'ipm';
GRANT CONNECT ON DATABASE postgres TO ipm;

ALTER SCHEMA ipm OWNER TO ipm;
 


