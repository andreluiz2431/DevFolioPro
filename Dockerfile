# ==========================================
# STAGE 1: Build Kotlin Multiplatform Web App
# ==========================================
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Install wget and unzip to download and extract Gradle
RUN apt-get update && apt-get install -y wget unzip && rm -rf /var/lib/apt/lists/*

# Download and install Gradle 9.3.1
RUN wget -q https://services.gradle.org/distributions/gradle-9.3.1-bin.zip \
    && unzip -q gradle-9.3.1-bin.zip -d /opt \
    && rm gradle-9.3.1-bin.zip

ENV PATH=$PATH:/opt/gradle-9.3.1/bin

# Copy the entire project
COPY . .

# Build the JS Browser Production Distribution
# This runs dead-code elimination (DCE), code optimization and bundling
RUN gradle :web:jsBrowserProductionDistribution --no-daemon --stacktrace

# ==========================================
# STAGE 2: Run Production Node.js Server
# ==========================================
FROM node:18-alpine AS runner
WORKDIR /app

# Copy the build outputs from the builder stage
COPY --from=builder /app/web/build/dist/js/productionExecutable ./web/build/dist/js/productionExecutable
COPY --from=builder /app/server.js ./server.js
COPY --from=builder /app/package.json ./package.json

# Install production dependencies
RUN npm install --production

# Expose the dynamic port used by Railway
EXPOSE 3000

# Start the Express server
CMD ["node", "server.js"]
