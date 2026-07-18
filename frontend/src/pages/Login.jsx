import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate, Link } from "react-router-dom";
import { Layers, Mail, Lock, Loader2, AlertCircle } from 'lucide-react'

function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        const result = await login(email, password);

        setIsLoading(false);

        if (result.success) {
            navigate('/chat');
        } else {
            setError(result.message);
        }
    };

    return (
        <div className="flex justify-center items-center min-h-screen bg-gradient-to-br from-indigo-950 via-indigo-900 to-gray-900">
            <div className="bg-indigo-950/70 backdrop-blur-md border border-indigo-800/50 rounded-2xl p-8 max-w-md w-full shadow-2xl">

            </div>
        </div>
    )
}