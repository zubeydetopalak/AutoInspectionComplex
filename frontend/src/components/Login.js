import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const response = await axios.post('/api/login', {
                username,
                password
            });

            const user = response.data;

            if (user && user.role) {
                // Store user info if needed (e.g., localStorage)
                localStorage.setItem('user', JSON.stringify(user));

                if (user.role === 'SECRETARY') {
                    navigate('/secretary-dashboard');
                } else if (user.role === 'INSPECTOR') {
                    navigate('/inspector-dashboard');
                } else {
                    setError('Unknown role: ' + user.role);
                }
            } else {
                setError('Login failed: No role returned');
            }

        } catch (err) {
            console.error(err);
            setError('Login failed. Please check your credentials.');
        }
    };

    return (
        <div className="flex-center">
            <div className="card">
                <div className="card-header">
                    <h2 className="card-title">Login</h2>
                    <p className="card-subtitle">Sign in to your account</p>
                </div>
                {error && <div className="error-message">{error}</div>}
                <form onSubmit={handleLogin}>
                    <div className="form-group">
                        <label className="form-label">Username</label>
                        <input
                            className="form-input"
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Password</label>
                        <input
                            className="form-input"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    <button type="submit" className="btn btn-primary">
                        Sign In
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Login;
