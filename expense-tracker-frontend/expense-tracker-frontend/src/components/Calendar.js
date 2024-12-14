import React, { useState, useEffect } from 'react';
import TransactionModal from './TransactionModal';
import fetchAPI from '../utils/apiClient';

const Calendar = () => {
    const [selectedMonth, setSelectedMonth] = useState(new Date());
    const [calendarDays, setCalendarDays] = useState([]);
    const [editingTransaction, setEditingTransaction] = useState(null);
    const [selectedDay, setSelectedDay] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [hoverIndex, setHoverIndex] = useState(null);
    const [transactions, setTransactions] = useState([]);

    // Fetch summary for the selected month
    const fetchMonthlySummary = async () => {
        const yearMonth = `${selectedMonth.getFullYear()}-${String(selectedMonth.getMonth() + 1).padStart(2, '0')}`;
        try {
            setLoading(true);
            const data = await fetchAPI(`http://localhost:8080/api/v1/summary/month/${yearMonth}`);
            console.log('Fetched summary:', data); // Debugging
            generateCalendar(data);
        } catch (err) {
            console.error('Error fetching monthly summary:', err);
            setError('Failed to fetch monthly summary');
        } finally {
            setLoading(false);
        }
    };

    // Generate calendar days
    const generateCalendar = (summary) => {
        const daysInMonth = new Date(selectedMonth.getFullYear(), selectedMonth.getMonth() + 1, 0).getDate();
        const daysArray = [];

        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(selectedMonth.getFullYear(), selectedMonth.getMonth(), day);
            const daySummary = summary.find(
                (s) => new Date(s.date).toDateString() === date.toDateString()
            ) || { income: 0, expenses: 0, savings: 0 };

            daysArray.push({
                day,
                dayName: date.toLocaleDateString('en-US', { weekday: 'short' }),
                income: daySummary.income,
                expenses: daySummary.expenses,
                savings: daySummary.savings,
            });
        }

        setCalendarDays(daysArray);
    };

    // Handle adding a transaction
    const handleAddTransaction = async (transaction) => {
        try {
            const token = localStorage.getItem('authToken');
            if (!token) {
                console.error('No auth token found.');
                return;
            }

            const response = await fetch('http://localhost:8080/api/v1/transaction', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Basic ${token}`, // Use Basic Auth
                },
                body: JSON.stringify(transaction),
            });

            if (response.ok) {
                console.log('Transaction saved successfully!');
                await fetchMonthlySummary(); // Refresh calendar
                setIsModalOpen(false); // Close the modal
            } else {
                const errorData = await response.json();
                console.error('Failed to save transaction:', errorData);
            }
        } catch (error) {
            console.error('Error saving transaction:', error);
        }
    };

    const handleEditTransaction = async (transaction) => {
        try {
            const token = localStorage.getItem('authToken');
            if (!token) {
                console.error('No auth token found.');
                return;
            }
    
            const response = await fetch(`http://localhost:8080/api/v1/transaction/${transaction.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Basic ${token}`,
                },
                body: JSON.stringify(transaction),
            });
    
            if (response.ok) {
                console.log('Transaction updated successfully!');
                // Refresh transactions for the selected day
                const updatedTransactions = await fetchAPI(
                    `http://localhost:8080/api/v1/transaction/day/${selectedDay.date.toISOString().split('T')[0]}`
                );
                setSelectedDay((prevState) => ({
                    ...prevState,
                    transactions: updatedTransactions,
                }));
                await fetchMonthlySummary(); // Refresh calendar
                setIsModalOpen(false); // Close the modal
            } else {
                console.error('Failed to update transaction:', response.statusText);
            }
        } catch (error) {
            console.error('Error updating transaction:', error);
        }
    };
    
    
    

    const handleDeleteTransaction = async (transactionId, deleteType) => {
        try {
            const token = localStorage.getItem('authToken');
            if (!token) {
                console.error('No auth token found.');
                
                
                return;
            }
    
            const requestUrl = `http://localhost:8080/api/v1/transaction/${transactionId}?deleteType=${deleteType}`;
            const response = await fetch(requestUrl, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Basic ${token}`, // Use Basic Auth
                },
            });
    
            if (response.ok) {
                console.log('Transaction deleted successfully!');
                const dayTransactions = await fetchAPI(
                    `http://localhost:8080/api/v1/transaction/day/${selectedDay.date.toISOString().split('T')[0]}`
                );
                setSelectedDay((prevState) => ({ ...prevState, transactions: dayTransactions }));
                await fetchMonthlySummary(); // Refresh calendar
                setIsModalOpen(false); // Close the modal
            } else {
                console.error('Failed to delete transaction:', response.statusText);
            }
        } catch (error) {
            console.error('Error deleting transaction:', error);
        }
    };
    


    

    const handleDayClick = async (day) => {
        const selectedDate = new Date(selectedMonth.getFullYear(), selectedMonth.getMonth(), day.day);
        const formattedDate = selectedDate.toISOString().split('T')[0]; // Format date for API

        try {
            const dayTransactions = await fetchAPI(`http://localhost:8080/api/v1/transaction/day/${formattedDate}`);
            setSelectedDay({
                date: selectedDate,
                transactions: dayTransactions,
            });
            setIsModalOpen(true); // Open the modal
        } catch (error) {
            console.error("Error fetching transactions for the day:", error);
        }
    };

    

    const handleMonthChange = (year, month) => {
        const newDate = new Date(year, month, 1);
        setSelectedMonth(newDate);
    };

    const isCurrentDay = (day, month, year) => {
        const today = new Date();
        return (
            today.getDate() === day &&
            today.getMonth() === month &&
            today.getFullYear() === year
        );
    };

    useEffect(() => {
        fetchMonthlySummary();
    }, [selectedMonth]);

    return (
        <div className="calendar-container" style={calendarContainerStyle}>
            <header style={headerStyle}>
                <select
                    style={{ ...dropdownStyle, ...buttonStyle }}
                    value={selectedMonth.getFullYear()}
                    onChange={(e) => handleMonthChange(parseInt(e.target.value), selectedMonth.getMonth())}
                >
                    {Array.from({ length: 10 }, (_, i) => {
                        const year = new Date().getFullYear() - 5 + i;
                        return (
                            <option key={year} value={year}>
                                {year}
                            </option>
                        );
                    })}
                </select>

                <select
                    style={{ ...dropdownStyle, ...buttonStyle }}
                    value={selectedMonth.getMonth()}
                    onChange={(e) => handleMonthChange(selectedMonth.getFullYear(), parseInt(e.target.value))}
                >
                    {Array.from({ length: 12 }, (_, i) => (
                        <option key={i} value={i}>
                            {new Date(0, i).toLocaleString('default', { month: 'long' })}
                        </option>
                    ))}
                </select>
            </header>

            <div className="calendar-grid">
                {calendarDays.map((day, index) => (
                    <div
                        key={index}
                        className="calendar-day"
                        style={{
                            ...calendarDayStyle,
                            ...(isCurrentDay(day.day, selectedMonth.getMonth(), selectedMonth.getFullYear()) && currentDayStyle),
                            ...(hoverIndex === index && calendarDayHoverStyle),
                        }}
                        onMouseEnter={() => setHoverIndex(index)}
                        onMouseLeave={() => setHoverIndex(null)}
                        onClick={() => handleDayClick(day)}
                    >
                        <div>{day.dayName} {day.day}</div>
                        <div style={incomeStyle}>Income: £{day.income}</div>
                        <div style={expenseStyle}>Expenses: £{day.expenses}</div>
                        <div style={savingsStyle}>Savings: £{day.savings}</div>
                    </div>
                ))}
            </div>

            {isModalOpen && selectedDay && (
                <TransactionModal
                    selectedDay={selectedDay}
                    setSelectedDay={setSelectedDay}
                    onClose={() => {
                        setIsModalOpen(false);
                        setEditingTransaction(null);
                    }}
                    onAddTransaction={handleAddTransaction}
                    onEditTransaction={handleEditTransaction}
                    onDeleteTransaction={handleDeleteTransaction}
                    editingTransaction={editingTransaction}
                    setEditingTransaction={setEditingTransaction} // Pass setter function
                />
            
            )}
        </div>
    );
};

// Styles
const calendarContainerStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '15px',
    padding: '40px',
    margin: '0 auto',
    backgroundColor: '#f0f4f8',
    borderRadius: '15px',
    boxShadow: '0 10px 30px rgba(0, 0, 0, 0.1)',
    maxWidth: '95vw',
};

const calendarDayStyle = {
    border: '2px solid #e0e0e0',
    padding: '20px',
    borderRadius: '10px',
    height: '180px',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    textAlign: 'center',
    backgroundColor: '#ffffff',
    cursor: 'pointer',
    transition: 'all 0.3s ease-in-out',
};

const calendarDayHoverStyle = {
    backgroundColor: '#cfe2f3',
    transform: 'scale(1.05)',
    boxShadow: '0 4px 10px rgba(0, 0, 0, 0.2)',
};

const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '20px',
    fontSize: '1.5rem',
    fontWeight: 'bold',
};

const buttonStyle = {
    padding: '10px 15px',
    backgroundColor: '#4CAF50', // Green color
    color: 'white',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
    fontSize: '1rem',
    transition: 'background 0.3s',
};

const dropdownStyle = {
    padding: '10px',
    fontSize: '1rem',
    borderRadius: '5px',
    border: '1px solid #ddd',
};


const currentDayStyle = {
    backgroundColor: '#ffefc3',
    border: '2px solid #ffc107',
};

const incomeStyle = {
    color: '#28a745',
    fontWeight: 'bold',
};

const expenseStyle = {
    color: '#dc3545',
    fontWeight: 'bold',
};

const savingsStyle = {
    color: '#007bff',
    fontWeight: 'bold',
};

export default Calendar;
