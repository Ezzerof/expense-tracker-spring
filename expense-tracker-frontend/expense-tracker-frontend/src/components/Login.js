import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { saveAuthToken } from '../utils/storage';


const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [isHovered, setIsHovered] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
    
        const basicAuth = 'Basic ' + btoa(`${username}:${password}`);
    
        try {
            const response = await fetch('http://localhost:8080/api/v1/auth/sign-in', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': basicAuth
                },
                body: JSON.stringify({ username, password })
            });
    
            if (response.status === 200) {
                saveAuthToken(username, password); 
                navigate('/calendar');
            } else {
                alert('Login failed!');
            }
        } catch (error) {
            console.error('Error during login:', error);
            alert('An error occurred during login');
        }
    };
    

    return (
        <div className="login-container" style={containerStyle}>
            <div className="login-card" style={cardStyle}>
                <h3 className="text-center mb-4" style={titleStyle}>Login</h3>
                <form onSubmit={handleLogin}>
                    <div className="form-group mb-3">
                        <label htmlFor="username" style={labelStyle}>Username</label>
                        <input
                            type="text"
                            className="form-control"
                            id="username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            style={inputStyle}
                        />
                    </div>
                    <div className="form-group mb-3">
                        <label htmlFor="password" style={labelStyle}>Password</label>
                        <input
                            type="password"
                            className="form-control"
                            id="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            style={inputStyle}
                        />
                    </div>
                    <button
                        type="submit"
                        className="btn btn-primary w-100"
                        style={isHovered ? buttonHoverStyle : buttonStyle}
                        onMouseEnter={() => setIsHovered(true)} 
                        onMouseLeave={() => setIsHovered(false)} 
                    >
                        Login
                    </button>
                    <p className="text-center mt-3" style={footerTextStyle}>
                        Don't have an account? <a href="/register" style={linkStyle}>Register</a>
                    </p>
                </form>
            </div>
        </div>
    );
};

const containerStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '100vh',
    backgroundImage: 'linear-gradient(to bottom right, #f0f2f5, #dfe6e9)',
    padding: '20px',
    borderRadius: '10px',
    '@media (max-width: 768px)': {
        padding: '20px', 
        maxWidth: '90%', 
    },
};

const cardStyle = {
    width: '90%',
    maxWidth: '700px',
    padding: '60px',
    borderRadius: '20px',
    boxShadow: '0 6px 16px rgba(0, 0, 0, 0.2)',
    backgroundColor: '#ffffff',
    margin: 'auto',
};

const titleStyle = {
    fontWeight: 'bold',
    color: '#333',
    fontSize: '2.5rem', 
    marginBottom: '30px',
    textAlign: 'center',
};

const labelStyle = {
    fontWeight: '500',
    color: '#555',
    fontSize: '1.5rem', 
};

const buttonStyle = {
    borderRadius: '8px',
    backgroundColor: '#007bff',
    border: 'none',
    padding: '15px 0', 
    fontWeight: 'bold',
    color: '#fff',
    cursor: 'pointer',
    fontSize: '1.5rem', 
    transition: 'background-color 0.3s', 
};

const inputStyle = {
    borderRadius: '8px',
    border: '1px solid #ccc',
    padding: '16px',
    width: '100%',
    fontSize: '1.4rem',
    marginBottom: '15px',
};

const buttonHoverStyle = {
    ...buttonStyle, 
    backgroundColor: '#0056b3',
};

const footerTextStyle = {
    fontSize: '1.5rem',
    textAlign: 'center',
    marginTop: '20px',
};

const linkStyle = {
    color: '#007bff',
    textDecoration: 'none',
};

export default Login;
