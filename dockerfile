FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests -Xmx256m
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx256m", "-jar", "target/fixitapi-0.0.1-SNAPSHOT.jar"]