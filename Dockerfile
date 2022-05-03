FROM eclipse-temurin:17-jre-alpine

EXPOSE 9090
ENV DB_HOSTNAME db
COPY target/dcsa_bkg-*.jar .
CMD java -jar dcsa_bkg-*.jar
