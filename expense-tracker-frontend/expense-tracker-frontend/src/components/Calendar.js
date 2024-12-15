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

            <div className="calendar-grid" style={calendarGridStyle}>
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
                        <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                            <span style={dayNumberStyle}>{day.day}</span>
                            <span style={dayNameStyle}>{day.dayName}</span>
                        </div>
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
                    setEditingTransaction={setEditingTransaction} 
                />
            
            )}
        </div>
    );
    
};

// Styles
const calendarContainerStyle = {
    display: 'grid',
    gridTemplateRows: 'auto 1fr', 
    gap: '20px',
    padding: '0', 
    margin: '0',
    backgroundColor: '#f0f4f8',
    width: '100vw', 
    height: '100vh', 
    boxSizing: 'border-box', 
};

const calendarGridStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(7, 1fr)', 
    gap: '10px',
    width: '100%', 
    height: '100%', 
    boxSizing: 'border-box', 
    padding: '10px', 
};

const calendarDayStyle = {
    border: '2px solid #ccc', 
    borderRadius: '15px', 
    padding: '50px', 
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    alignItems: 'center',
    textAlign: 'center',
    backgroundColor: '#f9f9f9', 
    fontSize: '1rem',
    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
    transition: 'transform 0.2s ease, background-color 0.3s ease', 
    boxSizing: 'border-box',
    cursor: 'pointer',
};

const calendarDayHoverStyle = {
    backgroundColor: '#e3f2fd', 
    transform: 'scale(1.05)', 
    boxShadow: '0 6px 12px rgba(0, 0, 0, 0.2)', 
};

const headerStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    gap: '10px',
    padding: '10px',
};

const buttonStyle = {
    padding: '15px 20px',
    backgroundColor: '#4CAF50', 
    color: 'white',
    border: 'none',
    borderRadius: '10px',
    cursor: 'pointer',
    fontSize: '1.2rem',
    transition: 'background 0.3s',
    width: 'auto',
};

const dropdownStyle = {
    padding: '15px',
    fontSize: '1.2rem',
    borderRadius: '10px',
    border: '2px solid #ddd',
    width: '220px',
    cursor: 'pointer',
};

const currentDayStyle = {
    backgroundColor: '#fff3cd', 
    border: '2px solid #ffc107', 
    fontWeight: 'bold',
    color: '#856404', 
};

const dayNumberStyle = {
    fontSize: '3rem',
    fontWeight: 'bold',
    color: '#4CAF50',
    alignSelf: 'flex-start',
};

const dayNameStyle = {
    fontSize: '2rem',
    fontWeight: 'bold',
    color: '#1976d2',
    alignSelf: 'flex-end',
};

const incomeStyle = {
    fontSize: '1.5rem',
    color: '#28a745', 
    fontWeight: 'bold',
};

const expenseStyle = {
    fontSize: '1.5rem',
    color: '#dc3545', 
    fontWeight: 'bold',
};

const savingsStyle = {
    fontSize: '1.5rem',
    color: '#007bff',
    fontWeight: 'bold',
};

export default Calendar;
