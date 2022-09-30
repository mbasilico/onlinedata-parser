# onlinedata-parser

Service that get an URL as parameter and returns the data as plain text and metadata such as: keywords, summary and wordCount.

### Service Stack
This service uses:

* Java 11
* Scala 2.13.8 
* Play Framework 2.8.x 
* CoreNLP for natural language processing (Nound detection)
* Apache httpcomponents
* SBT

### Run a local Mongo database
This service use a PostgreSQL database.
Inside database folder execute:

docker-compose up

Create an .env file in project root with the following EnvVars:

MONGODB_URL=

MONGODB_USER=

MONGODB_PASSWORD=


### Running the service

sbt run

### Executing the service

curl --location --request POST 'http://localhost:9000/parse' \
--header 'Content-Type: text/plain' \
--data-raw 'https://t.co/V0jvJVOLBu'

