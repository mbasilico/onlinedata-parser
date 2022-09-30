# onlinedata-url-resolver

Service that transforms an input URL in Its original form, unshort URLs.

### Service Stack
This service uses:

* Java 11
* Scala 2.13.8 
* Play Framework 2.8.x 
* Doobie 1.0.0-RC1
* Apache httpcomponents
* SBT

### Run a local database
This service use a PostgreSQL database.
Inside database folder execute:

docker-compose up

Create an .env file in project root with the following EnvVars:

DB_URL=jdbc:postgresql://localhost:5432/url_resolver

DB_USER=docker

DB_PASSWORD=docker


### Running the service

sbt run

### Executing the service

curl --location --request POST 'http://localhost:9000/resolve' \
--header 'Content-Type: text/plain' \
--data-raw 'https://t.co/V0jvJVOLBu'

