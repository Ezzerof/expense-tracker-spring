import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Register = () => {
    const [firstName, setFirstName] = useState('');
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isHovered, setIsHovered] = useState(false);
    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();

        const response = await fetch('http://localhost:8080/api/v1/auth/sign-up', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ firstName, username, email, password })
        });

        if (response.status === 201) {
            alert('Registration successful! Please log in.');
            navigate('/'); 
        } else {
            alert('Registration failed!');
        }
    };

    return (
        <div className="register-container" style={containerStyle}>
            <div className="register-card" style={cardStyle}>
                <h3 className="text-center mb-4" style={titleStyle}>Register</h3>
                <form onSubmit={handleRegister}>
                    <div className="form-group mb-3">
                        <label htmlFor="firstName" style={labelStyle}>First Name</label>
                        <input
                            type="text"
                            className="form-control"
                            id="firstName"
                            value={firstName}
                            onChange={(e) => setFirstName(e.target.value)}
                            required
                            style={inputStyle}
                        />
                    </div>
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
                        <label htmlFor="email" style={labelStyle}>Email</label>
                        <input
                            type="email"
                            className="form-control"
                            id="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
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
                        Register
                    </button>
                    <p className="text-center mt-3">
                        Already have an account? <a href="/" style={linkStyle}>Login</a>
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
    height: '100vh',
    backgroundColor: '#f8f9fa',
};

const cardStyle = {
    width: '100%',
    maxWidth: '400px',
    padding: '20px',
    borderRadius: '10px',
    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
    backgroundColor: '#fff',
};

const titleStyle = {
    fontWeight: 'bold',
    color: '#333',
};

const labelStyle = {
    fontWeight: '500',
    color: '#555',
};

const inputStyle = {
    borderRadius: '8px',
    border: '1px solid #ccc',
    padding: '10px',
    width: '100%',
};

const buttonStyle = {
    borderRadius: '8px',
    backgroundColor: '#007bff',
    border: 'none',
    padding: '12px 0',
    fontWeight: 'bold',
    color: '#fff',
    cursor: 'pointer',
};

const buttonHoverStyle = {
    ...buttonStyle, 
    backgroundColor: '#0056b3',
};

const linkStyle = {
    color: '#007bff',
    textDecoration: 'none',
};

export default Register;
