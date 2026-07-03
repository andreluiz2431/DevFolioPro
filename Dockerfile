# ==========================================
# STAGE 1: Build Kotlin Multiplatform Web App
# ==========================================
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Copy the entire project with proper permissions
COPY --chown=gradle:gradle . .

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
