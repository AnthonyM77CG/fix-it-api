FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests -DMAVEN_OPTS="-Xmx256m"
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx256m", "-jar", "target/fixitapi-0.0.1-SNAPSHOT.jar"]