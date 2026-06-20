FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV APP_STORAGE_UPLOAD_DIR=/opt/pageturn/uploads
ENV APP_STORAGE_PUBLIC_DIR=/opt/pageturn/public
ENV JAVA_TOOL_OPTIONS=--enable-native-access=ALL-UNNAMED

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system pageturn \
    && useradd --system --gid pageturn --create-home --home-dir /app pageturn \
    && mkdir -p /opt/pageturn/uploads /opt/pageturn/public \
    && chown -R pageturn:pageturn /app /opt/pageturn

COPY --from=build /workspace/target/*.jar /app/app.jar

USER pageturn

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
