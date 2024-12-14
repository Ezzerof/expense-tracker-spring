import React, { useState } from 'react';

const SavingsModal = ({ onClose, onSaveSavings, currentSavings }) => {
    const [additionalSavings, setAdditionalSavings] = useState(0); 

    const handleSave = () => {
        if (!isNaN(additionalSavings)) {
            onSaveSavings(parseFloat(additionalSavings)); 
        }
    };

    return (
        <div style={modalOverlayStyle}>
            <div style={modalStyle}>
                <h5>Add Savings Left</h5>
                <input
                    type="number"
                    style={inputStyle}
                    placeholder="Enter additional savings"
                    value={additionalSavings}
                    onChange={(e) => setAdditionalSavings(e.target.value)}
                />
                <div style={buttonContainerStyle}>
                    <button style={closeButtonStyle} onClick={onClose}>Close</button>
                    <button style={saveButtonStyle} onClick={handleSave}>Save</button>
                </div>
            </div>
        </div>
    );
};


const modalOverlayStyle = {
    position: 'fixed',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: '1001',
};

const modalStyle = {
    backgroundColor: '#fff',
    padding: '20px',
    borderRadius: '8px',
    width: '300px',
    textAlign: 'center',
    boxShadow: '0 2px 10px rgba(0, 0, 0, 0.2)',
};

const buttonContainerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    marginTop: '20px',
};

const inputStyle = {
    padding: '10px',
    width: '100%',
    marginBottom: '20px',
    borderRadius: '4px',
    border: '1px solid #ddd',
};

const closeButtonStyle = {
    backgroundColor: '#6c757d',
    color: '#fff',
    padding: '8px 16px',
    borderRadius: '5px',
    cursor: 'pointer',
    border: 'none',
};

const saveButtonStyle = {
    backgroundColor: '#007bff',
    color: '#fff',
    padding: '8px 16px',
    borderRadius: '5px',
    cursor: 'pointer',
    border: 'none',
};

export default SavingsModal;
