import { initializeApp } from "firebase/app";
import {getAuth} from "firebase/auth"

const firebaseConfig = {
  apiKey: "AIzaSyD6qtBhLAnOopFhoBkERnZ3R7TN_I2hj1g",
  authDomain: "basketball-court-finder-6dc25.firebaseapp.com",
  projectId: "basketball-court-finder-6dc25",
  storageBucket: "basketball-court-finder-6dc25.appspot.com",
  messagingSenderId: "1062044207640",
  appId: "1:1062044207640:web:368d26c0174397a393916d"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

export { auth };
export default app;