
FROM openjdk:21-ea

WORKDIR /usrapp/bin

ENV PORT 35000

COPY /target/classes /usrapp/bin/classes
COPY /target/dependency /usrapp/bin/dependency

CMD ["java", "-cp", "./classes:./dependency/*", "Arep.Lab07.Application"]