# BooK

This is a simple booking system made in GluonHQ's JavaFX 8. Avaiable for Android, Windows, iOS and Mac OSX.
This project contains a server that holds inlogg, username and booking meetings.

## How it looks like
Consider this as just a template for a simple booking system. Here you can change the text, pictures
and other things that fits your needs. 



## Features

```
* Email verification of booking meeting
* Email verification of canceled meeting
* Using GMail SMTP server to send message to the members
* Spring Boot with Spring Security and JPA and Hibernate
* Made in JavaFX = Avaiable for booth Android and Iphone
```

## How to use

Step 1: Install the following software

```
* MySQL
* OpenJDK 8
* OpenJFX 8
```

After the installations of the MySQL server, you ned to create a MySQL user for the file application.properties.
The user need to have granted rights for creating a database and tables.

Step 2: Configure this properties file and the Java file

```
application.properties
mail.properties
schedule.properties
HTTPClient.java // Located at the BooK app
```

Step 3: Run the server with
```
gradlew bootRun // For Windows
./gradlew bootRun // For Linux
```

Step 4: Run the mobile application with
```
gradlew run// For Windows
./gradlew run// For Linux
```

Step 5: Generate the mobile application for android or iOS. See link below.
https://docs.gluonhq.com/getting-started/#introduction


