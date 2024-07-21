FROM node:18 AS build

WORKDIR /app
RUN apt-get update

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build -- --output-path=dist/rtg-ng --configuration production

FROM nginx:latest
COPY --from=build /app/dist/rtg-ng /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
