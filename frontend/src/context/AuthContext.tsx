import React, { createContext, useState, useEffect } from "react";

const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;

interface User {
    displayName: string,
    email: string,
}

export interface AuthState {
    isLoggedIn: boolean;
    user: User | null;
}

interface AuthContextType extends AuthState {
    checkAuth: () => Promise<void>;
    logout: () => void;
    setAuthState: (newState: AuthState | ((prevState: AuthState) => AuthState)) => void;
}

export const AuthContext = createContext<AuthContextType | null> (null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => { 
    const [authState, setAuthState] = useState<AuthState>({
        isLoggedIn: false,
        user: null
    });


    // Checks authentication status
    const checkAuth = async () => {
      fetch(`${baseApiUrl}/api/users`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Pragma': 'no-cache',
          'Expires': '0',
          'Authorization': `Bearer ${sessionStorage.getItem("BCFtoken")}`
        }
      }).then(
        (res) => {
          if (res.ok) {
            return res.json().then(data => {
              setAuthState({isLoggedIn: true, user: data});
            });
          } else {
            setAuthState({isLoggedIn: false, user: null});
          }
        }
      ).catch((error) => {
        console.error("Error checking auth: ", error);
        setAuthState({isLoggedIn: false, user: null});
        // TODO: Replace with logging later
      });
    }

    // Logout function
    const logout = async () => {
      fetch(`${baseApiUrl}/api/users/logout`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
      }).then(
        (res) => {
          if (res.ok) {
            setAuthState({ isLoggedIn: false, user: null });
          } else {
            throw new Error(`HTTP error! status: ${res.status}`);
          }
        }).catch((error) => {
        console.error("Error logging out: ", error);
        // TODO: Replace with logging later
      });
    }

    // CHeck on page/app load
    useEffect(() => {
      checkAuth();
    }, []);

    return (
        <AuthContext.Provider value={{ ...authState, checkAuth, logout, setAuthState }}>
            {children}
        </AuthContext.Provider>
    )
}