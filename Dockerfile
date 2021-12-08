FROM java:8
WORKDIR /
ADD out/artifacts/javasocket_jar/javasocket.jar javasocket.jar
EXPOSE 6000
CMD java -jar javasocket.jar