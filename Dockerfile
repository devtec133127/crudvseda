FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends maven \
 && rm -rf /var/lib/apt/lists/*

COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
