# 🏀 Basketball-Court-Finder

This app finds basketball courts around a given location, and allows users to leave reviews.

This app gets basketball courts, its data, and creates the map using the OpenStreetMap (OSM) API, and geocodes and reverse geocodes using the Nominatim API.

## 🔗 Website Link

You can access this app [here](https://jinhakimgh.github.io/Basketball-Court-Finder)!

## 🪄 Features

Users can:

- Log in
- Leave Reviews
- Search for basketball courts within a range of an address
- View and update different court attributes

## 📷 Photos

![](screenshots/home.png)
![](screenshots/reviews.png)

## 🚀 Run Locally

Clone the project

```bash
  git clone git@github.com:JinhaKimGH/Basketball-Court-Finder.git
```

Go to the frontend directory

```bash
  cd Basketball-Court-Finder/frontend
```

Install dependencies

```bash
  npm install
```

Start the server

```bash
  npm run dev
```

Open the backend

```bash
  cd Basketball-Court-Finder/backend
```

Install dependencies

```bash
  mvn install
```

Set Up `application.properties`

Inside `src/main/resources/application.properties`, set these attributes accordingly

```properties
spring.datasource.url = jdbc:mysql://localhost:3306/basketballcourtfinder
spring.application.name=Basketball Court Finder
spring.datasource.username=yourusername
spring.datasource.password=yourpassword

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

jwt.secret=random88characterlongsecret
jwt.expiration=86400000
cors.allowed-origin=http://localhost:5173
spring.profiles.active=dev
```

Build and Run the Application

Using Maven without IntelliJ IDEA:
Click the maven tab in the IDE.

```sh
./mvnw clean install
./mvnw spring-boot:run
```

## 🔗 Links

[![linkedin](https://img.shields.io/badge/linkedin-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/jinha-kim/)

## License

[MIT](https://choosealicense.com/licenses/mit/)
