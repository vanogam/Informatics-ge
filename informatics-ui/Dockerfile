FROM node:18-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

RUN npm run build

FROM nginx:alpine

COPY --from=0 /app/build/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY cert/nginx-selfsigned.crt /etc/ssl/certs/nginx-selfsigned.crt
COPY cert/nginx-selfsigned.key /etc/ssl/private/nginx-selfsigned.key

EXPOSE 80
EXPOSE 443

CMD ["nginx", "-g", "daemon off;"]