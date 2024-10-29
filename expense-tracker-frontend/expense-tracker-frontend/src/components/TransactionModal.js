import React, { useState, useEffect } from 'react';

const TransactionModal = ({ onClose, onAddTransaction, selectedDay, onEditTransaction, onDeleteTransaction, transactions }) => {
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [amount, setAmount] = useState('');
    const [category, setCategory] = useState('');
    const [endDate, setEndDate] = useState('');
    const [recurrenceFrequency, setRecurrenceFrequency] = useState('NONE');
    const [transactionType, setTransactionType] = useState('EXPENSE');
    const [showTransactions, setShowTransactions] = useState(false);

    useEffect(() => {
        if (selectedDay) {
            setEndDate(selectedDay.toISOString().split('T')[0]);
        }
    }, [selectedDay]);

    const handleSave = () => {
        if (!name || !amount || !category) {
            alert('Please fill in all the required fields.');
            return;
        }

        const transaction = {
            id: null,
            name,
            description,
            amount: parseFloat(amount),
            category,
            startDate: selectedDay.toISOString().split('T')[0],
            endDate,
            recurrenceFrequency,
            transactionType,
        };

        onAddTransaction(transaction);
    };

    return (
        <div style={modalOverlayStyle}>
            <div style={modalStyle}>
                <h3>Add Transaction</h3>
                <div style={formContainerStyle}>
                    <label style={labelStyle}>Name</label>
                    <input
                        type="text"
                        placeholder="Enter transaction name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        style={inputStyle}
                    />

                    <label style={labelStyle}>Description</label>
                    <input
                        type="text"
                        placeholder="Enter description"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        style={inputStyle}
                    />

                    <label style={labelStyle}>Amount</label>
                    <input
                        type="number"
                        placeholder="Enter amount"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                        style={inputStyle}
                    />

                    <label style={labelStyle}>Transaction Type</label>
                    <select
                        value={transactionType}
                        onChange={(e) => setTransactionType(e.target.value)}
                        style={inputStyle}
                    >
                        <option value="EXPENSE">Expense</option>
                        <option value="INCOME">Income</option>
                    </select>

                    <label style={labelStyle}>Category</label>
                    <select
                        value={category}
                        onChange={(e) => setCategory(e.target.value)}
                        style={inputStyle}
                    >
                        {transactionType === 'EXPENSE' ? (
                            <>
                                <option value="">Select a category</option>
                                <option value="HOME">Home</option>
                                <option value="BILLS">Bills</option>
                                <option value="ENTERTAINMENT">Entertainment</option>
                                <option value="FOOD">Food</option>
                                <option value="CAR">Car</option>
                                <option value="DEBT">Debt</option>
                                <option value="OTHER">Other</option>
                            </>
                        ) : (
                            <>
                                <option value="">Select a category</option>
                                <option value="WAGES">Wages</option>
                                <option value="BONUSES">Bonuses</option>
                                <option value="FREELANCE">Freelance</option>
                                <option value="SELLINGS">Sellings</option>
                            </>
                        )}
                    </select>

                    <label style={labelStyle}>End Date</label>
                    <input
                        type="date"
                        value={endDate}
                        onChange={(e) => setEndDate(e.target.value)}
                        style={inputStyle}
                    />

                    <label style={labelStyle}>Recurrence Frequency</label>
                    <select
                        value={recurrenceFrequency}
                        onChange={(e) => setRecurrenceFrequency(e.target.value)}
                        style={inputStyle}
                    >
                        <option value="NONE">None</option>
                        <option value="SINGLE">Single</option>
                        <option value="DAILY">Daily</option>
                        <option value="WEEKLY">Weekly</option>
                        <option value="MONTHLY">Monthly</option>
                    </select>
                </div>
                <div style={buttonContainerStyle}>
                    <button onClick={handleSave} style={buttonStyle}>Save</button>
                    <button onClick={onClose} style={buttonStyle}>Close</button>
                </div>
                <h4>Existing Transactions</h4>
                <button onClick={() => setShowTransactions(!showTransactions)} style={buttonStyle}>
                    {showTransactions ? 'Hide Transactions' : 'Show Transactions'}
                </button>
                {showTransactions && (
                    transactions.length > 0 ? (
                        transactions.map((transaction, index) => (
                            <div key={index} style={{ marginBottom: '10px' }}>
                                <div><strong>{transaction.name}</strong></div>
                                <div>Amount: £{transaction.amount}</div>
                                <div>Category: {transaction.category}</div>
                                <div>Description: {transaction.description}</div>
                                <div>Type: {transaction.transactionType}</div>
                                <div>Start Date: {transaction.startDate}</div>
                                <div>End Date: {transaction.endDate}</div>
                                <div>Recurrence: {transaction.recurrenceFrequency}</div>
                                <button onClick={() => onEditTransaction(index, transaction)} style={editButtonStyle}>Edit</button>
                                <button onClick={() => onDeleteTransaction(transaction.id)} style={deleteButtonStyle}>Delete</button>
                            </div>
                        ))
                    ) : (
                        <p>No transactions for this day.</p>
                    )
                )}
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
    width: '400px',
    textAlign: 'left',
    boxShadow: '0 2px 10px rgba(0, 0, 0, 0.2)',
};

const formContainerStyle = {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
    marginBottom: '20px',
};

const labelStyle = {
    fontWeight: 'bold',
};

const inputStyle = {
    padding: '8px',
    borderRadius: '4px',
    border: '1px solid #ddd',
};

const buttonContainerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    marginTop: '20px',
};

const buttonStyle = {
    padding: '10px 20px',
    borderRadius: '5px',
    border: 'none',
    backgroundColor: '#63ADF2',
    color: '#fff',
    cursor: 'pointer',
};

const editButtonStyle = {
    padding: '5px 10px',
    borderRadius: '3px',
    border: 'none',
    backgroundColor: '#FFA500',
    color: '#fff',
    cursor: 'pointer',
    marginRight: '5px',
};

const deleteButtonStyle = {
    padding: '5px 10px',
    borderRadius: '3px',
    border: 'none',
    backgroundColor: '#FF6347',
    color: '#fff',
    cursor: 'pointer',
};

export default TransactionModal;
