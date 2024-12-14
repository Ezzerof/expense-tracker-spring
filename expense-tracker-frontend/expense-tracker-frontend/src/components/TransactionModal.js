import React, { useState, useEffect } from 'react';
import fetchAPI from '../utils/apiClient';
import { format } from 'date-fns';


const TransactionModal = ({
    onClose,
    onAddTransaction,
    selectedDay,
    setSelectedDay,
    onEditTransaction,
    onDeleteTransaction,
    editingTransaction = null,
}) => {
    const [formState, setFormState] = useState({
        name: '',
        description: '',
        amount: '',
        category: '',
        recurrenceFrequency: 'SINGLE',
        transactionType: 'EXPENSE',
        startDate: '',
        endDate: '',
    });

    const [showTransactions, setShowTransactions] = useState(false);

    useEffect(() => {
        if (selectedDay && selectedDay instanceof Date) {
            const formattedDate = selectedDay.toISOString().split('T')[0]; // Ensure it's a valid Date object
            setFormState((prevState) => ({
                ...prevState,
                startDate: formattedDate,
                endDate: formattedDate,
            }));
        } else if (selectedDay?.day) {
            // Handle if `selectedDay` is not a Date but has day/month/year
            const today = new Date();
            const formattedDate = `${today.getFullYear()}-${String(selectedDay.month + 1).padStart(2, '0')}-${String(selectedDay.day).padStart(2, '0')}`;
            setFormState((prevState) => ({
                ...prevState,
                startDate: formattedDate,
                endDate: formattedDate,
            }));
        }
        if (editingTransaction) {
            setFormState({
                name: editingTransaction.name || '',
                description: editingTransaction.description || '',
                amount: editingTransaction.amount || '',
                category: editingTransaction.category || '',
                startDate: editingTransaction.startDate || '',
                endDate: editingTransaction.endDate || '',
                recurrenceFrequency: editingTransaction.recurrenceFrequency || 'SINGLE',
                transactionType: editingTransaction.transactionType || 'EXPENSE',
            });
        } else if (selectedDay && !formState.startDate && !formState.endDate) {
            // Only set the start and end dates if they are not already set
            const formattedDate = selectedDay.date?.toISOString().split('T')[0] || '';
            setFormState((prevState) => ({
                ...prevState,
                startDate: formattedDate,
                endDate: formattedDate,
            }));
        }
    }, [selectedDay, editingTransaction, formState.startDate, formState.endDate]);
    
    
    

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormState((prevState) => ({ ...prevState, [name]: value }));
    };

    const handleSave = async () => {
        const {
            name,
            amount,
            category,
            startDate,
            endDate,
            recurrenceFrequency,
            transactionType,
            description,
        } = formState;
    
        if (!name || !amount || !category || !startDate || !recurrenceFrequency) {
            alert('Please fill in all the required fields.');
            return;
        }
    
        const transaction = {
            ...editingTransaction, // Include existing transaction details if editing
            name,
            amount: parseFloat(amount),
            category,
            startDate: format(new Date(startDate), 'yyyy-MM-dd'),
            endDate: endDate ? format(new Date(endDate), 'yyyy-MM-dd') : null,
            recurrenceFrequency,
            transactionType,
            description,
        };
    
        try {
            if (editingTransaction) {
                await onEditTransaction(transaction); // Trigger edit callback
            } else {
                await onAddTransaction(transaction); // Trigger add callback
            }
            onClose(); // Close modal after successful operation
        } catch (error) {
            console.error('Error saving transaction:', error);
        }
    };
    

    return (
        <div style={modalOverlayStyle}>
            <div style={modalStyle}>
                <h3>{editingTransaction ? 'Edit Transaction' : 'Add Transaction'}</h3>
                <div style={formStyle}>
                    <label>Name</label>
                    <input
                        type="text"
                        name="name"
                        value={formState.name}
                        onChange={handleInputChange}
                        placeholder="Transaction name"
                    />
                    <label>Description</label>
                    <input
                        type="text"
                        name="description"
                        value={formState.description}
                        onChange={handleInputChange}
                        placeholder="Description"
                    />
                    <label>Amount</label>
                    <input
                        type="number"
                        name="amount"
                        value={formState.amount}
                        onChange={handleInputChange}
                        placeholder="Amount"
                    />
                    <label>Transaction Type</label>
                    <select
                        name="transactionType"
                        value={formState.transactionType}
                        onChange={handleInputChange}
                    >
                        <option value="EXPENSE">Expense</option>
                        <option value="INCOME">Income</option>
                    </select>
                    <label>Category</label>
                    <select
                        name="category"
                        value={formState.category}
                        onChange={handleInputChange}
                    >
                        {formState.transactionType === 'EXPENSE' ? (
                            <>
                                <option value="">Select a category</option>
                                <option value="FOOD">Food</option>
                                <option value="BILLS">Bills</option>
                                <option value="CAR">Car</option>
                                <option value="OTHER">Other</option>
                            </>
                        ) : (
                            <>
                                <option value="">Select a category</option>
                                <option value="SALARY">Salary</option>
                                <option value="FREELANCE">Freelance</option>
                                <option value="BONUS">Bonus</option>
                                <option value="OTHER">Other</option>
                            </>
                        )}
                    </select>
                    <label>Recurrence Frequency</label>
                    <select
                        name="recurrenceFrequency"
                        value={formState.recurrenceFrequency}
                        onChange={handleInputChange}
                    >
                        <option value="SINGLE">Single</option>
                        <option value="DAILY">Daily</option>
                        <option value="WEEKLY">Weekly</option>
                        <option value="MONTHLY">Monthly</option>
                    </select>
                    <label>End Date</label>
                    <input
                        type="date"
                        name="endDate"
                        value={formState.endDate}
                        onChange={handleInputChange}
                    />
                </div>
                <div style={buttonGroupStyle}>
                    <button style={modalButtonStyle} onClick={handleSave}>
                        {editingTransaction ? 'Update' : 'Save'}
                    </button>
                    <button style={modalButtonStyle} onClick={onClose}>
                        Close
                    </button>
                </div>
                <h4>Transactions for {selectedDay?.date?.toLocaleDateString()}</h4>
                <div style={scrollableTransactionList}>
                    {selectedDay?.transactions?.length > 0 ? (
                        selectedDay.transactions.map((transaction, index) => (
                            <div key={index} style={transactionCard}>
                                <div>
                                    <strong>{transaction.name}</strong>
                                </div>
                                <div>
                                    Amount: <span style={amountStyle}>£{transaction.amount}</span>
                                </div>
                                <div>Type: {transaction.transactionType}</div>
                                <div>Category: {transaction.category}</div>
                                <div style={buttonGroup}>
                                    <button
                                        style={editButtonStyle}
                                        onClick={() => onEditTransaction(transaction)}
                                    >
                                        Edit
                                    </button>
                                    <button
                                        style={deleteButtonStyle}
                                        onClick={() => onDeleteTransaction(transaction.id)}
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        ))
                    ) : (
                        <p>No transactions for this day.</p>
                    )}
                </div>
            </div>
        </div>
    );
}    

const scrollableTransactionList = {
    maxHeight: '300px', // Limits the height of the list
    overflowY: 'auto', // Adds vertical scroll when content overflows
    marginTop: '10px',
    padding: '10px',
    border: '1px solid #ddd',
    borderRadius: '8px',
    backgroundColor: '#f9f9f9',
};


const modalStyle = {
    backgroundColor: '#fff',
    padding: '30px',
    borderRadius: '10px',
    width: '600px',
    maxWidth: '90%',
    textAlign: 'left',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.2)',
    animation: 'fadeIn 0.3s ease-in-out',
};

const transactionListContainer = {
    marginTop: '20px',
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
};

const transactionCard = {
    padding: '15px',
    border: '1px solid #ddd',
    borderRadius: '8px',
    backgroundColor: '#f9f9f9',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
};

const amountStyle = {
    color: '#28a745',
    fontWeight: 'bold',
};

const buttonGroup = {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '10px',
};

const editButtonStyle = {
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    padding: '8px 12px',
    borderRadius: '5px',
    cursor: 'pointer',
};

const deleteButtonStyle = {
    backgroundColor: '#dc3545',
    color: 'white',
    border: 'none',
    padding: '8px 12px',
    borderRadius: '5px',
    cursor: 'pointer',
};


const buttonGroupStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    marginTop: '20px',
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
    fontSize: '1.4rem',
    zIndex: '1001',
};

const formStyle = {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
    padding: '20px',
    backgroundColor: '#f9f9f9',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
};


const modalButtonStyle = {
    padding: '10px 20px',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '5px',
    fontSize: '1rem',
    cursor: 'pointer',
    transition: 'background 0.3s',
};

const transactionStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '10px',
    border: '1px solid #ddd',
    borderRadius: '5px',
    margin: '10px 0',
    backgroundColor: '#f9f9f9',
};

const buttonStyle = {
    padding: '5px 10px',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
    marginLeft: '10px',
};



export default TransactionModal;
