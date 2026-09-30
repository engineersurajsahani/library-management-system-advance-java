# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -e dependency:go-offline
COPY src ./src
RUN mvn -q clean package

# ---------- Runtime stage ----------
FROM tomcat:10.1-jre17-temurin
ENV JAVA_TOOL_OPTIONS="-Xmx512m"

# Remove default apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR as ROOT application
COPY --from=build /build/target/library.war /usr/local/tomcat/webapps/ROOT.war

# SQLite data volume — DB file survives redeploys
VOLUME ["/data"]
ENV LMS_DB_PATH=/data/lms.db

EXPOSE 8080
CMD ["catalina.sh", "run"]
