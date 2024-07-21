# Stage 1: Build the frontend
FROM node:18 AS build

WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application code
COPY . .

# Build the application for production
RUN npm run build -- --output-path=dist/rtg-ng --configuration production

# Stage 2: Serve the frontend with nginx
FROM nginx:alpine

# Copy the build output to the nginx html directory
COPY --from=build /app/dist/rtg-ng /usr/share/nginx/html

# Copy the nginx configuration file
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
