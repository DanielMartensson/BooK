# BooK

This is a simple booking system made in GluonHQ's JavaFX 8. Avaiable for Android, Windows, iOS and Mac OSX.
This project contains a server that holds inlogg, username and booking meetings.

## How it looks like
Consider this as just a template for a simple booking system. Here you can change the text, pictures
and other things that fits your needs. It's currently a mix between Swedish and English, but use Google Translate 
if you don't understand. Swedish and English has the same gramar and logic. 

Login:

![a](https://github.com/DanielMartensson/BooK/blob/master/Pictures/Login.PNG?raw=true)

Create new member:

![a](https://github.com/DanielMartensson/BooK/blob/master/Pictures/CreateNewMember.PNG?raw=true)

Success to create a new member:

![a](https://github.com/DanielMartensson/BooK/blob/master/Pictures/SuccessNewMember.PNG)

Create new meeting:

![a](https://github.com/DanielMartensson/BooK/blob/master/Pictures/Meeting.PNG?raw=true)

Menu:

![a](https://github.com/DanielMartensson/BooK/blob/master/Pictures/Menu.PNG?raw=true)

Trying to edit my self by using my own user. I'm have onlu authorized with the role USER in Spring Security.

![a](https://github.com/DanielMartensson/BooK/blob/master/Pictures/TryingToEditMember.PNG?raw=true)

## Features

```
* Email verification of booking meeting
* Email verification of canceled meeting
* Using GMail SMTP server to send message to the members
* Spring Boot with Spring Security and JPA and Hibernate
* Made in JavaFX = Avaiable for booth Android and Iphone
* Admin have rights to change roles(ADMIN/USER), password and username for other users
* Auto deleting old meetings that are over 1 week old
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

Step 2: Configure this properties file and the Java file. 

```
application.properties
mail.properties
schedule.properties
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


