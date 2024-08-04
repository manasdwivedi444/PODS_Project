FROM eclipse-temurin:17-jdk-focal
 
WORKDIR /app
 
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
 
COPY src ./src
 
ENTRYPOINT ["./mvnw", "compile" , "exec:java"]
CMD ["-Dexec.args=8083"]
