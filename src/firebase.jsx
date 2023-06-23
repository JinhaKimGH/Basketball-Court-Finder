import { initializeApp } from "firebase/app";
import {getAuth} from "firebase/auth"
import {firebaseValues} from "./config.jsx"

const firebaseConfig = {
  apiKey: firebaseValues.apiKey,
  authDomain: firebaseValues.authDomain,
  projectId: firebaseValues.projectId,
  storageBucket: firebaseValues.storageBucket,
  messagingSenderId: firebaseValues.messagingSenderId,
  appId: firebaseValues.appId
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

export { auth };
export default app;