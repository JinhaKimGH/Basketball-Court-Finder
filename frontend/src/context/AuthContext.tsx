import React, { createContext, useState, useEffect } from "react";

const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;

interface User {
    displayName: string,
    email: string,
}

interface AuthState {
    isLoggedIn: boolean;
    user: User | null;
}

interface AuthContextType extends AuthState {
    checkAuth: () => Promise<void>;
    logout: () => void;
    setAuthState: React.Dispatch<React.SetStateAction<AuthState>>;
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
        }).then(
            (res) => {
                if (res.ok) {
                    return res.json();
                } else {
                    setAuthState({isLoggedIn: false, user: null})
                }
            }
        ).then((data) => {
            setAuthState({isLoggedIn: true, user: data});
        }).catch((error) => {
            console.error("Error checking auth: ", error);
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
        });
        setAuthState({ isLoggedIn: false, user: null });
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