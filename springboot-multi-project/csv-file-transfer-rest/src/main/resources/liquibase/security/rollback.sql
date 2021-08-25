delete from group_permission;
delete from user_group;
delete from "USER";
delete from "GROUP";
delete from permission;


delete from databasechangelog where filename='liquibase/security/user/data/insert.xml';
delete from databasechangelog where filename='liquibase/security/group/data/insert.xml';
delete from databasechangelog where filename='liquibase/security/permission/data/insert.xml';
delete from databasechangelog where filename='liquibase/security/user_group/data/insert.xml';
delete from databasechangelog where filename='liquibase/security/group_permission/data/insert.xml';
