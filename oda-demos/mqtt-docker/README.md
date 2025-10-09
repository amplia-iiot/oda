# Instrucciones

## Construir imagen Docker
Esto debe hacerse en la carpeta /opt/Repositories/oda/oda-demos/mqtt-docker

Es necesario haber hecho un mvn clean package para que exista la carpeta target

```properties
docker build --tag 'oda-4.12.7-snapshot' .
```

## Arrancar imagen Docker

-v indica en que carpeta local se va a montar el directorio persist de ODA

-v carpetaLocal:/opt/opengate/oda/persist (dentro del docker oda está en /opt/opengate/oda)

```properties
docker run -d  -v /home/ruben-romano/dev/oda-docker-4.12.7-snapshot:/opt/opengate/oda/persist --name oda-4.12.7-snapshot oda-4.12.7-snapshot
```