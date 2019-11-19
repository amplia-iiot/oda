FROM openjdk:8-alpine3.9

LABEL author="Amplia Soluciones" name="OpenGate Device Agent" version="3.30.5"

# Transfer embedded JRE
#ADD embedded-jdk-4.8.3.tar.bz2 /opt/jre8

# Transfer ODA
ADD target/mqtt-3.30.5-SNAPSHOT.tar.bz2 /opt/oda

# Prepare Java environment variables
#ENV JAVA_HOME /opt/jre8
#ENV PATH $JAVA_HOME/bin:$PATH

# Set ODA dir as working directory
WORKDIR /opt/oda

# Run ODA
ENTRYPOINT bin/run.sh