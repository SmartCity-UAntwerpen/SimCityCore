FROM java:8
COPY . /home
WORKDIR /home
EXPOSE 8080
EXPOSE 1883
EXPOSE 8083
EXPOSE 8089
EXPOSE 12201
CMD java -Djava.security.egd=file:/dev/./urandom -jar RobotCityDeployer.jar
