# spring-boot example
This is a small RESTFul application

## REST API

Application
Create a JVM based backend application using REST. That contains the following
endpoints:
GET /api/stocks (get a list of stocks)
POST /api/stocks (create a stock)
GET /api/stocks/1 (get one stock from the list)
PATCH /api/stocks/1 (update the price of a single stock)
DELETE/api/stocks/1 (delete a single stock)
The initial list of stocks should be created on application start-up. Use a database that is
most appropriate for this use-case (Hint: Use Docker and provide user instructions).
The stock object contains at least the following fields:
• ID;
• name (String);
• currentPrice (Amount);
• lastUpdate (Timestamp).
Configure the GET /api/stocks endpoint to support request pagination (the number of stocks
per page must be configurable).
Each endpoint must be compliant with the HTTP/1.1 and REST standards.
Use Spring Boot to build and test this application.
