import React, { useState } from 'react';
import fetchAPI from '../utils/apiClient';

const DeleteConfirmationModal = ({ onClose, onConfirm, transaction }) => {
    return (
        <div style={deleteModalOverlayStyle}>
            <div style={deleteModalStyle}>
                <h3>Delete Transaction</h3>
                <p>
                    Do you want to delete <strong>all occurrences</strong> of this transaction or just the <strong>selected one</strong>?
                </p>
                <div style={deleteButtonContainerStyle}>
                    <button
                        style={deleteAllButtonStyle}
                        onClick={() => onConfirm('ALL')}
                    >
                        Delete All Occurrences
                    </button>
                    <button
                        style={deleteSingleButtonStyle}
                        onClick={() => onConfirm('SINGLE')}
                    >
                        Delete Only This Occurrence
                    </button>
                    <button
                        style={cancelButtonStyle}
                        onClick={onClose}
                    >
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    );
};


const deleteButtonContainerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    marginTop: '20px',
    gap: '10px',
};

const deleteAllButtonStyle = {
    backgroundColor: '#007bff',
    color: '#fff',
    border: 'none',
    padding: '10px 15px',
    borderRadius: '5px',
    cursor: 'pointer',
    flex: 1,
    transition: 'background 0.3s',
    textAlign: 'center',
};

const deleteSingleButtonStyle = {
    backgroundColor: '#28a745',
    color: '#fff',
    border: 'none',
    padding: '10px 15px',
    borderRadius: '5px',
    cursor: 'pointer',
    flex: 1,
    transition: 'background 0.3s',
    textAlign: 'center',
};

const cancelButtonStyle = {
    backgroundColor: '#dc3545',
    color: '#fff',
    border: 'none',
    padding: '10px 15px',
    borderRadius: '5px',
    cursor: 'pointer',
    flex: 1,
    transition: 'background 0.3s',
    textAlign: 'center',
};

const deleteModalOverlayStyle = {
    position: 'fixed',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1100,
};

const deleteModalStyle = {
    backgroundColor: '#fff',
    padding: '20px',
    borderRadius: '8px',
    width: '600px',
    textAlign: 'center',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.2)',
    zIndex: 1200,
};

export default DeleteConfirmationModal;
