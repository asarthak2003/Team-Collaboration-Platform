import React, { createContext, useState, useEffect, useContext } from "react";
import api from "../services/api";

const AuthContext = createContext(undefined);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);


    // Check if user session already exists in localStorage on application startup
    useEffect(() => {
        const storedUser = localStorage.getItem("user");
        const storedToken = localStorage.getItem("token");

        if (storedUser && storedToken) {
            setUser(JSON.parse(storedUser))
        }
        setLoading(false);
    }, []);

    // Login handler connecting to Spring Boot auth endpoint
    const login = async (email, password) => {
        try {
            const response = await api.post("/api/auth/login", { email, password });
            const { token, userId, name, role } = response.data;
            const userData = { id: userId, name, email, role };

            localStorage.setItem("token", token);
            localStorage.setItem("user", JSON.stringify(userData));

            setUser(userData);
            return { success: true };
        } catch (error) {
            const message = error.response?.data?.error || 'Invalid credentials or login failed';
            return { success: false, error: message };
        }
    };

    // Register handler connecting to Spring Boot auth endpoint
    const register = async (name, email, password, roleName) => {
        try {
            const response = await api.post("/api/auth/register", { name, email, password, roleName });
            return { success: true };
        } catch (error) {
            const message = error.response?.data?.error || 'Registration failed';
            return { success: false, error: message };
        }
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
    };

    const updateUser = (userData) => {
        localStorage.setItem("user", JSON.stringify(userData));
        setUser(userData);
    };

    return (
        <AuthContext.Provider value={{ user, loading, login, register, logout, updateUser }}>
            {children}
        </AuthContext.Provider>
    );
};

// Custom hook to access the authentication context
export const useAuth = () => {
    return useContext(AuthContext);
};