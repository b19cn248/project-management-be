# Giai đoạn xây dựng (Build stage)
FROM eclipse-temurin:21-jdk AS build
RUN apt-get update && apt-get install -y maven
WORKDIR /app

# Sao chép pom.xml trước để tận dụng cache của Docker
COPY pom.xml .
# Tải các dependency trước (tùy chọn, giúp tăng tốc nếu pom.xml ít thay đổi)
RUN mvn dependency:go-offline -B

# Sao chép mã nguồn và build project
COPY src ./src
RUN mvn package -DskipTests

# Giai đoạn chạy (Runtime stage)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]