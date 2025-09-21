FROM openjdk:17-oracle

# 使用通配符匹配动态版本的JAR文件
ADD /target/ByrSki-*.jar /ByrSki-backend.jar
LABEL maintainer="YaphetLee"
LABEL version="DYNAMIC"
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=Asia/Shanghai
ENTRYPOINT ["java","-jar","/ByrSki-backend.jar"]