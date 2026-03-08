# Informatics-ge

ინსტრუქცია:

დაბილდვა:
პროექტის ფოლდერში გაუშვით კომანდი:
mvn clean install

გაშვება:
პროექტის ფოლდერში გაუშვით კომანდი:
docker-compose up --build

`.env` ფაილში საჭიროა შემდეგი ველების არსებობა:
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `INFORMATICS_HOME_DIR` (მაგ: `/home/informatics/prod` ან `/home/informatics/dev`)
- `INFORMATICS_CERTS_DIR` (მაგ: `/home/informatics/prod/cert`)

`INFORMATICS_HOME_DIR` გამოიყენება როგორც base path docker compose-ში და application properties-ში.
config ფაილი კონტეინერში mountდება აქედან:
`/opt/app/external-config/application.properties`.

SSL სერტიფიკატებისთვის `INFORMATICS_CERTS_DIR` დირექტორია mountდება `informatics-ui` კონტეინერში (`/etc/nginx/certs`) და უნდა შეიცავდეს:
- `tls.crt`
- `tls.key`