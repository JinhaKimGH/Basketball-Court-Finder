import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';
import { AuthProvider } from './context/AuthContext';
import { BrowserRouter } from "react-router-dom";
import { ChakraProvider, defaultSystem } from '@chakra-ui/react';

// Create config to force light mode
const config = {
  ...defaultSystem,
  initialColorMode: 'light',
  useSystemColorMode: false,
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <BrowserRouter basename="/Basketball-Court-Finder">
    <ChakraProvider value={config}>
      <AuthProvider>
        <App />
      </AuthProvider>
    </ChakraProvider>
  </BrowserRouter>
)
