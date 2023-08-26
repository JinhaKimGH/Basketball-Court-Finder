# 🏀 Basketball-Court-Finder

This app finds basketball courts around a given location, and allows users to rate them.

This app gets basketball courts, its data, and creates the map using the OpenStreetMap (OSM) API, geocodes and reverse geocodes using the Nominatim API, and utilizes Firebase for a database.

## 🔗 Website Link

You can access this app [here](https://jinhakimgh.github.io/Basketball-Court-Finder)!

## 🪄 Features

Users can:

- Log in
- Leave Reviews
- Search for basketball courts within a range of an address
- View the name of the court, distance from the address, number of hoops, rating, and surface type

## 📸 Screenshots

![Search Results](/screenshots/searchresults.png)
![Court Information](/screenshots/courtinfo.png)

## Environment Variables

To run this project locally, you will need to add the following variables after creating a firebase database to a config.jsx file.

`firebaseValues.apiKey` -> [apiKey for firebase](https://firebase.google.com/docs/web/setup)

`firebaseValues.authDomain` -> [Value created with firebase setup](https://firebase.google.com/docs/web/setup)

`firebaseValues.projectId` -> [Value created with firebase setup](https://firebase.google.com/docs/web/setup)

`firebaseValues.storageBucket` -> [Value created with firebase setup](https://firebase.google.com/docs/web/setup)

`firebaseValues.messagingSenderId` -> [Value created with firebase setup](https://firebase.google.com/docs/web/setup)

`firebaseValues.appId` -> [Value created with firebase setup](https://firebase.google.com/docs/web/setup)

## Run Locally

Clone the project

```bash
  git clone git@github.com:JinhaKimGH/Basketball-Court-Finder.git
```

Go to the project directory

```bash
  cd Basketball-Court-Finder
```

Install dependencies

```bash
  npm install
```

Start the server

```bash
  npm run dev
```

## 🔗 Links

[![linkedin](https://img.shields.io/badge/linkedin-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/jinha-kim/)

## License

[MIT](https://choosealicense.com/licenses/mit/)
