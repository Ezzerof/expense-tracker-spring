import React, { useState } from 'react';

const SavingsModal = ({ onClose, onSaveSavings, currentSavings }) => {
    const [newSavings, setNewSavings] = useState(currentSavings);

    const handleSave = () => {
        if (!isNaN(newSavings)) {
            onSaveSavings(parseFloat(newSavings));
        }
    };

    return (
        <div style={modalOverlayStyle}>
            <div style={modalStyle}>
                <h3>Change Savings</h3>
                <input
                    type="number"
                    placeholder="Enter new savings"
                    value={newSavings}
                    onChange={(e) => setNewSavings(e.target.value)}
                />
                <div style={buttonContainerStyle}>
                    <button onClick={handleSave}>Save</button>
                    <button onClick={onClose}>Close</button>
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

export default SavingsModal;
