import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Register = () => {
    const [firstName, setFirstName] = useState('');
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isHovered, setIsHovered] = useState(false);
    const [errors, setErrors] = useState({});
    const navigate = useNavigate();

    const validateFirstName = (name) => /^[a-zA-Z]+$/.test(name);
    const validateUsername = (name) => /^[a-zA-Z\d]{5,15}$/.test(name);
    const validateEmail = (email) => /^[A-Za-z0-9+_.-]+@(.+)$/.test(email);
    const validatePassword = (password) => password.length >= 8;

    const handleRegister = async (e) => {
        e.preventDefault();
        const newErrors = {};
    
        if (!validateFirstName(firstName)) newErrors.firstName = 'First name must be between 2 and 50 characters and contain only letters.';
        if (!validateUsername(username)) newErrors.username = 'Username must be 5-15 characters with letters and numbers.';
        if (!validateEmail(email)) newErrors.email = 'Enter a valid email address.';
        if (!validatePassword(password)) newErrors.password = 'Password must be at least 8 characters long.';
    
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }
    
        try {
            const response = await fetch('http://localhost:8080/api/v1/auth/sign-up', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ firstName, username, email, password }),
            });
    
            if (response.status === 201) {
                alert('Registration successful! Please log in.');
                navigate('/');
            } else if (response.status === 400) {
                const result = await response.json();
                const backendErrors = {};
    
                result.errors?.forEach((error) => {
                    const field = error.propertyPath || 'general';
                    const message = error.interpolatedMessage || 'Invalid input';
                    backendErrors[field] = message;
                });
    
                setErrors(backendErrors);
            } else {
                alert('Registration failed! Please try again.');
            }
        } catch (error) {
            console.error('Error during registration:', error);
            alert('An error occurred during registration');
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
                            onChange={(e) => {
                                setFirstName(e.target.value);
                                if (errors.firstName) setErrors((prev) => ({ ...prev, firstName: '' }));
                            }}
                            required
                            style={errors.firstName ? { ...inputStyle, ...errorStyle } : inputStyle}
                        />
                        <small style={tipStyle}>Must be 2-50 characters and contain only letters.</small>
                        {errors.firstName && <small style={errorTextStyle}>{errors.firstName}</small>}
                    </div>

                    <div className="form-group mb-3">
                        <label htmlFor="username" style={labelStyle}>Username</label>
                        <input
                            type="text"
                            className="form-control"
                            id="username"
                            value={username}
                            onChange={(e) => {
                                setUsername(e.target.value);
                                if (errors.username) setErrors((prev) => ({ ...prev, username: '' }));
                            }}
                            required
                            style={errors.username ? { ...inputStyle, ...errorStyle } : inputStyle}
                        />
                        <small style={tipStyle}>Must be 5-15 characters and contain only letters and numbers.</small>
                        {errors.username && <small style={errorTextStyle}>{errors.username}</small>}
                    </div>

                    <div className="form-group mb-3">
                        <label htmlFor="email" style={labelStyle}>Email</label>
                        <input
                            type="email"
                            className="form-control"
                            id="email"
                            value={email}
                            onChange={(e) => {
                                setEmail(e.target.value);
                                if (errors.email) setErrors((prev) => ({ ...prev, email: '' }));
                            }}
                            required
                            style={errors.email ? { ...inputStyle, ...errorStyle } : inputStyle}
                        />
                        <small style={tipStyle}>Enter a valid email address.</small>
                        {errors.email && <small style={errorTextStyle}>{errors.email}</small>}
                    </div>

                    <div className="form-group mb-3">
                        <label htmlFor="password" style={labelStyle}>Password</label>
                        <input
                            type="password"
                            className="form-control"
                            id="password"
                            value={password}
                            onChange={(e) => {
                                setPassword(e.target.value);
                                if (errors.password) setErrors((prev) => ({ ...prev, password: '' }));
                            }}
                            required
                            style={errors.password ? { ...inputStyle, ...errorStyle } : inputStyle}
                        />
                        <small style={tipStyle}>Must be at least 8 characters long.</small>
                        {errors.password && <small style={errorTextStyle}>{errors.password}</small>}
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
                    <p className="text-center mt-3" style={footerTextStyle}>
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
    marginBottom: '5px',
};

const errorStyle = {
    border: '1px solid red',
    backgroundColor: '#ffe6e6',
};

const errorTextStyle = {
    color: 'red',
    fontSize: '0.9rem',
    marginTop: '5px',
};


const buttonHoverStyle = {
    ...buttonStyle,
    backgroundColor: '#0056b3',
};

const linkStyle = {
    color: '#007bff',
    textDecoration: 'none',
};

const footerTextStyle = {
    fontSize: '1.5rem',
    textAlign: 'center',
    marginTop: '20px',
};

const tipStyle = {
    color: '#6c757d',
    fontSize: '0.85rem',
    marginTop: '3px',
};

export default Register;
