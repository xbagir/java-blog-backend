FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package

FROM tomcat:10.1-jre21
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /app/target/blog-backend.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
