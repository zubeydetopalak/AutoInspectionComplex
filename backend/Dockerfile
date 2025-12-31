# --- Build Stage ---
# Maven imajı kullanarak derleme yapıyoruz. 
# Bu imaj Docker Hub'da mevcuttur.
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Önce sadece pom.xml'i kopyalayıp bağımlılıkları indiriyoruz (Cache için önemli)
COPY pom.xml .
# (İsteğe bağlı) RUN mvn dependency:go-offline

# Kaynak kodları kopyalayıp derliyoruz
COPY src ./src
RUN mvn clean package -DskipTests

# --- Run Stage ---
# HATA BURADAYDI: 'openjdk:17-slim' yerine 'eclipse-temurin:17-jre' kullanıyoruz.
# JRE (Java Runtime Environment) sadece çalıştırma dosyalarını içerdiği için boyutu küçüktür.
FROM eclipse-temurin:17-jre
WORKDIR /app

# Build aşamasından jar dosyasını alıyoruz
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]