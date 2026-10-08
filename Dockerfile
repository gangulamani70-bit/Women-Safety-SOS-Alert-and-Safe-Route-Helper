FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/target/classes ./classes
RUN mkdir -p /var/data && chown -R 10001:10001 /app /var/data
USER 10001:10001
ENV APP_ENV=production
ENV HOST=0.0.0.0
ENV DATA_DIR=/var/data
EXPOSE 8080
ENTRYPOINT ["java", "-cp", "classes", "womensafety.web.LocalhostServer"]
