#
# Package stage
#
FROM alpine as build

ADD target/*.zip /oda.zip

RUN unzip /oda.zip -d /

FROM eclipse-temurin:8-jre-noble

WORKDIR /opt/opengate/oda

COPY --from=build /mqtt* ./

ADD entrypoint.sh ./entrypoint.sh
RUN chmod +x ./entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]
