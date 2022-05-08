FROM mysql
COPY docker/initialscript /docker-entrypoint-initdb.d
COPY docker/db/custom.cnf /etc/mysql/conf.d/