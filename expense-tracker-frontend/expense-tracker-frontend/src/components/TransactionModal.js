import React, { useState, useEffect } from 'react';
import fetchAPI from '../utils/apiClient';
import DeleteConfirmationModal from './DeleteConfirmationModal';
import { format } from 'date-fns';

const TransactionModal = ({
    onClose,
    onAddTransaction,
    selectedDay,
    setSelectedDay,
    onEditTransaction,
    onDeleteTransaction,
    editingTransaction = null,
    setEditingTransaction, 
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
    const [confirmationModalVisible, setConfirmationModalVisible] = useState(false);
    const [transactionToDelete, setTransactionToDelete] = useState(null);

    useEffect(() => {
        if (selectedDay && selectedDay instanceof Date) {
            const formattedDate = selectedDay.toISOString().split('T')[0];
            setFormState((prevState) => ({
                ...prevState,
                startDate: formattedDate,
                endDate: formattedDate,
            }));
        } else if (selectedDay?.day) {
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
        }
    }, [selectedDay, editingTransaction]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        console.log(`Input changed: ${name} = ${value}`);
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
    
        console.log('Attempting to save transaction with formState:', formState);
    
        // Set default startDate to the selected day if not provided
        const selectedDate = selectedDay?.date?.toISOString().split('T')[0];
        const finalStartDate = startDate?.trim() || selectedDate;
    
        // Ensure endDate is null for SINGLE recurrence
        const finalEndDate = recurrenceFrequency === 'SINGLE' ? null : endDate;
    
        // Validation for required fields
        if (
            !name?.trim() ||
            isNaN(parseFloat(amount)) ||
            parseFloat(amount) <= 0 ||
            !category?.trim() ||
            !finalStartDate?.trim() ||
            !recurrenceFrequency?.trim()
        ) {
            alert('Please fill in all the required fields with valid values.');
            return;
        }
    
        const transaction = {
            ...editingTransaction, // Retain existing transaction data when editing
            name,
            amount: parseFloat(amount),
            category,
            startDate: finalStartDate,
            endDate: finalEndDate ? format(new Date(finalEndDate), 'yyyy-MM-dd') : null,
            recurrenceFrequency,
            transactionType,
            description,
        };
    
        console.log('Transaction to save:', transaction);
    
        try {
            if (editingTransaction) {
                // Editing an existing transaction
                await onEditTransaction(transaction); // Call PUT endpoint
                console.log('Transaction updated successfully.');
            } else {
                // Adding a new transaction
                await onAddTransaction(transaction); // Call POST endpoint
                console.log('Transaction added successfully.');
            }
            onClose();
        } catch (error) {
            console.error('Error saving transaction:', error);
        }
    };
    

    const handleDeleteTransaction = async (deleteType) => {
        if (!transactionToDelete) return;

        console.log(`Attempting to delete transaction with ID: ${transactionToDelete.id} and deleteType: ${deleteType}`);
        console.log(`DeleteType received: ${deleteType}`);

        try {
            await onDeleteTransaction(transactionToDelete.id, deleteType);
            console.log('Transaction deleted successfully.');
            setConfirmationModalVisible(false);
            setTransactionToDelete(null);
            await refreshCalendar();
        } catch (error) {
            console.error('Error deleting transaction:', error);
        }
    };

    const refreshCalendar = async () => {
        if (selectedDay) {
            const formattedDate = selectedDay.date?.toISOString().split('T')[0];
            console.log(`Refreshing calendar for date: ${formattedDate}`);
            const updatedTransactions = await fetchAPI(
                `http://localhost:8080/api/v1/transaction/day/${formattedDate}`
            );
            setSelectedDay((prevState) => ({ ...prevState, transactions: updatedTransactions }));
        }
    };

    const confirmDeleteTransaction = (transaction) => {
        console.log('Preparing to delete transaction:', transaction);
        setTransactionToDelete(transaction);
        setConfirmationModalVisible(true);
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
                        onChange={(e) => {
                            const value = e.target.value;
                            setFormState((prevState) => ({
                                ...prevState,
                                recurrenceFrequency: value,
                                endDate: value === 'SINGLE' ? '' : prevState.endDate, // Clear endDate for SINGLE
                            }));
                        }}
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
                        disabled={formState.recurrenceFrequency === 'SINGLE'}
                        placeholder="End Date"
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
                                        onClick={() => {
                                            setEditingTransaction(transaction); // Set the transaction being edited
                                            setFormState({
                                                ...transaction,
                                                startDate: transaction.startDate,
                                                endDate: transaction.endDate || transaction.startDate,
                                                recurrenceFrequency: transaction.recurrenceFrequency || 'SINGLE',
                                            });
                                        }}
                                    >
                                        Edit
                                    </button>


                                    <button
                                        style={deleteButtonStyle}
                                        onClick={() => confirmDeleteTransaction(transaction)}
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
            {confirmationModalVisible && transactionToDelete && (
                <DeleteConfirmationModal
                    onClose={() => setConfirmationModalVisible(false)}
                    onConfirm={(deleteType) => handleDeleteTransaction(deleteType)}
                    transaction={transactionToDelete}
                />
            )}
        </div>
    );
};


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
