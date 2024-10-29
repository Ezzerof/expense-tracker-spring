import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import TransactionModal from './TransactionModal';
import SavingsModal from './SavingsModal';
import { removeAuthToken, getAuthToken } from '../utils/storage';

const Calendar = () => {
    const [selectedMonth, setSelectedMonth] = useState('');
    const [calendarDays, setCalendarDays] = useState([]);
    const [selectedDay, setSelectedDay] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [hoverIndex, setHoverIndex] = useState(null);
    const [menuOpen, setMenuOpen] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isSavingsModalOpen, setIsSavingsModalOpen] = useState(false);
    const [savings, setSavings] = useState(0);
    const navigate = useNavigate();
    const menuRef = useRef(null);

    useEffect(() => {
        const fetchSavings = async () => {
            try {
                const response = await fetch('http://localhost:8080/api/v1/transaction/savings', {
                    headers: {
                        'Authorization': `Basic ${getAuthToken()}`,
                        'Content-Type': 'application/json',
                    },
                });

                if (response.ok) {
                    const data = await response.json();
                    setSavings(data.savings);
                } else {
                    console.error(`Failed to fetch savings: ${response.status} - ${response.statusText}`);
                }
            } catch (error) {
                console.error('Error fetching savings:', error);
            }
        };

        const today = new Date();
        const defaultMonth = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`;
        setSelectedMonth(defaultMonth);
        fetchSavings();
        fetchTransactionsForMonth(defaultMonth);
    }, []);

    const fetchTransactionsForDate = async (date) => {
        const formattedDate = `${String(date.getDate()).padStart(2, '0')}-${String(date.getMonth() + 1).padStart(2, '0')}-${date.getFullYear()}`;
        try {
            const response = await fetch(`http://localhost:8080/api/v1/transaction/day/${formattedDate}`, {
                headers: {
                    'Authorization': `Basic ${getAuthToken()}`,
                    'Content-Type': 'application/json',
                },
            });
    
            if (response.ok) {
                const data = await response.json();
                setTransactions(data);
                generateCalendar(date.getFullYear(), date.getMonth(), data);
            } else {
                console.error(`Failed to fetch transactions: ${response.status} - ${response.statusText}`);
            }
        } catch (error) {
            console.error('Error fetching transactions:', error);
        }
    };
    

    const fetchTransactionsForMonth = async (yearMonth) => {
        try {
            const response = await fetch(`http://localhost:8080/api/v1/transaction/month/${yearMonth}`, {
                headers: {
                    'Authorization': `Basic ${getAuthToken()}`,
                    'Content-Type': 'application/json',
                },
            });
    
            if (response.ok) {
                const data = await response.json();
                setTransactions(data);
                const [year, month] = yearMonth.split('-');
                generateCalendar(parseInt(year), parseInt(month) - 1, data);
            } else {
                console.error(`Failed to fetch transactions: ${response.status} - ${response.statusText}`);
            }
        } catch (error) {
            console.error('Error fetching transactions:', error);
        }
    };
    

    const handleSaveSavings = async (newSavingsValue) => {
        try {
            const response = await fetch('http://localhost:8080/api/v1/transaction/savings', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Basic ${getAuthToken()}`,
                },
                body: JSON.stringify({ savings: newSavingsValue }),
            });

            if (response.ok) {
                setSavings(newSavingsValue);
                fetchTransactionsForMonth(selectedMonth);
                setIsSavingsModalOpen(false);
            } else {
                console.error('Failed to save savings:', response.statusText);
            }
        } catch (error) {
            console.error('Error saving savings:', error);
        }
    };

    const generateCalendar = (year, month, transactionsForMonth = []) => {
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const daysArray = [];
        let accumulatedSavings = savings;
    
        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(year, month, day);
            const dayName = date.toLocaleDateString('en-US', { weekday: 'short' });
    
            // Filter transactions for the current day
            const transactionsForDay = transactionsForMonth.filter(
                (transaction) => {
                    const transactionDate = new Date(transaction.startDate);
                    return (
                        transactionDate.getFullYear() === date.getFullYear() &&
                        transactionDate.getMonth() === date.getMonth() &&
                        transactionDate.getDate() === date.getDate()
                    );
                }
            );
    
            // Calculate total income and expenses for the current day
            const totalExpenses = transactionsForDay
                .filter((transaction) => transaction.transactionType === 'EXPENSE')
                .reduce((sum, transaction) => sum + transaction.amount, 0);
    
            const totalIncome = transactionsForDay
                .filter((transaction) => transaction.transactionType === 'INCOME')
                .reduce((sum, transaction) => sum + transaction.amount, 0);
    
            // Update accumulated savings for each day
            accumulatedSavings = accumulatedSavings - totalExpenses + totalIncome;
    
            daysArray.push({
                day,
                dayName,
                totalExpenses,
                totalIncome,
                remainingSavings: accumulatedSavings,
            });
        }
    
        setCalendarDays(daysArray);
    };
    
    

    const handleDayClick = (date) => {
        setSelectedDay(date);
        const transactionsForDay = transactions.filter(
            (t) => new Date(t.startDate).toDateString() === date.toDateString()
        );
        setSelectedDay({ date, transactionsForDay });
        setIsModalOpen(true);
    };

    const handleMonthChange = (e) => {
        const selectedDate = new Date(e.target.value);
        setSelectedMonth(e.target.value);
        generateCalendarForEntireMonth(selectedDate.getFullYear(), selectedDate.getMonth());
    };
    
    const generateCalendarForEntireMonth = (year, month) => {
        const daysInMonth = new Date(year, month + 1, 0).getDate();
    
        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(year, month, day);
            fetchTransactionsForDate(date);
        }
    };
    

    const handleLogout = () => {
        removeAuthToken();
        navigate('/');
    };

    const toggleMenu = () => {
        setMenuOpen(!menuOpen);
    };

    const handleChangeSavings = () => {
        setIsSavingsModalOpen(true);
    };

    const closeSavingsModal = () => {
        setIsSavingsModalOpen(false);
    };

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setMenuOpen(false);
            }
        };

        if (menuOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        } else {
            document.removeEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [menuOpen]);

    const handleSaveTransaction = async (transaction) => {
        try {
            const response = await fetch('http://localhost:8080/api/v1/transaction', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Basic ${getAuthToken()}`,
                },
                body: JSON.stringify(transaction),
            });

            if (response.ok) {
                fetchTransactionsForMonth(selectedMonth);
                setIsModalOpen(false);
            } else {
                console.error('Failed to save transaction:', response.statusText);
            }
        } catch (error) {
            console.error('Error saving transaction:', error);
        }
    };

    const handleEditTransaction = async (index, updatedTransaction) => {
        try {
            const response = await fetch(`http://localhost:8080/api/v1/transaction/${updatedTransaction.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Basic ${getAuthToken()}`,
                },
                body: JSON.stringify(updatedTransaction),
            });

            if (response.ok) {
                fetchTransactionsForMonth(selectedMonth);
                setIsModalOpen(false);
            } else {
                console.error('Failed to update transaction:', response.statusText);
            }
        } catch (error) {
            console.error('Error updating transaction:', error);
        }
    };

    const handleDeleteTransaction = async (transactionId) => {
        try {
            const response = await fetch(`http://localhost:8080/api/v1/transaction/${transactionId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Basic ${getAuthToken()}`,
                },
            });

            if (response.ok) {
                fetchTransactionsForMonth(selectedMonth);
                setIsModalOpen(false);
            } else {
                console.error('Failed to delete transaction:', response.statusText);
            }
        } catch (error) {
            console.error('Error deleting transaction:', error);
        }
    };

    return (
        <div className="container mt-5">
            <h3 className="text-center">Expense Tracker - Calendar</h3>

            <div className="d-flex justify-content-start mb-4">
                <button className="menu-btn" onClick={toggleMenu} style={menuBtnStyle}>
                    ☰
                </button>
            </div>

            {menuOpen && (
                <div className="menu" style={menuStyle} onClick={(e) => e.stopPropagation()} ref={menuRef}>
                    <p style={menuHeadingStyle}>Current Savings: £{savings}</p>
                    <ul style={menuListStyle}>
                        <li onClick={handleChangeSavings} style={menuItemStyle}>Change Current Savings</li>
                        <li onClick={handleLogout} style={menuItemStyle}>Logout</li>
                    </ul>
                </div>
            )}

            {isSavingsModalOpen && (
                <SavingsModal
                    onClose={closeSavingsModal}
                    onSaveSavings={handleSaveSavings}
                    currentSavings={savings}
                />
            )}

            {isModalOpen && (
                <TransactionModal
                    transactions={selectedDay?.transactionsForDay || []}
                    selectedDay={selectedDay?.date}
                    onClose={() => setIsModalOpen(false)}
                    onAddTransaction={handleSaveTransaction}
                    onEditTransaction={handleEditTransaction}
                    onDeleteTransaction={handleDeleteTransaction}
                />
            )}

            <div className="row mb-4">
                <div className="col-md-6">
                    <label htmlFor="monthSelect">Select Month</label>
                    <input
                        type="month"
                        id="monthSelect"
                        className="form-control"
                        value={selectedMonth}
                        onChange={handleMonthChange}
                    />
                </div>
            </div>

            <div className="calendar-container" style={calendarContainerStyle}>
                {calendarDays.map((day, index) => (
                    <div
                        key={day.day}
                        className="calendar-day"
                        style={index === hoverIndex ? { ...calendarDayStyle, ...calendarDayHoverStyle } : calendarDayStyle}
                        onMouseEnter={() => setHoverIndex(index)}
                        onMouseLeave={() => setHoverIndex(null)}
                        onClick={() => handleDayClick(new Date(selectedMonth.split('-')[0], selectedMonth.split('-')[1] - 1, day.day))}
                    >
                        <div className="date-header" style={dateHeaderStyle}>
                            <span style={index === hoverIndex ? dayNameHoverStyle : dayNameStyle}>{day.dayName}</span>
                            <span style={dateStyle}>{day.day}</span>
                        </div>
                        <div style={infoContainerStyle}>
                            <div style={expenseStyle}>- £{day.totalExpenses}</div>
                            <div style={incomeStyle}>+ £{day.totalIncome}</div>
                            <div style={savingsStyle}>Savings Left: £{day.remainingSavings}</div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

const menuBtnStyle = {
    fontSize: '24px',
    cursor: 'pointer',
    border: 'none',
    backgroundColor: '#63ADF2',
    color: '#fff',
    padding: '10px',
    borderRadius: '5px',
    transition: 'all 0.3s',
};

const menuStyle = {
    position: 'absolute',
    top: '60px',
    left: '10px',
    width: '250px',
    backgroundColor: '#f8f9fa',
    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
    borderRadius: '8px',
    padding: '15px',
};

const menuHeadingStyle = {
    fontSize: '18px',
    fontWeight: 'bold',
    marginBottom: '15px',
    textAlign: 'center',
};

const menuListStyle = {
    listStyleType: 'none',
    padding: 0,
};

const menuItemStyle = {
    padding: '10px',
    cursor: 'pointer',
    textAlign: 'center',
    transition: 'background 0.3s',
    backgroundColor: '#63ADF2',
    color: '#fff',
    marginBottom: '10px',
    borderRadius: '5px',
};

const calendarContainerStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))',
    gridGap: '15px',
    marginBottom: '20px',
};

const calendarDayStyle = {
    border: '1px solid #ddd',
    padding: '15px',
    borderRadius: '15px',
    height: '180px',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    textAlign: 'center',
    backgroundColor: '#fff',
    transition: 'all 0.3s ease-in-out',
    cursor: 'pointer',
    boxShadow: '0 2px 5px rgba(0, 0, 0, 0.1)',
};

const calendarDayHoverStyle = {
    backgroundColor: '#63ADF2',
    color: 'black',
    transform: 'scale(1.05)',
    boxShadow: '0 4px 10px rgba(0, 0, 0, 0.2)',
};

const dateHeaderStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    fontWeight: 'bold',
    fontSize: '18px',
    marginBottom: '10px',
};

const dayNameStyle = {
    fontSize: '14px',
    color: '#005b96',
};

const dayNameHoverStyle = {
    ...dayNameStyle,
    color: '#fff',
};

const dateStyle = {
    fontSize: '22px',
};

const infoContainerStyle = {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    fontSize: '16px',
    fontWeight: 'bold',
    marginTop: 'auto',
};

const expenseStyle = {
    color: '#BC4749',
};

const incomeStyle = {
    color: '#386641',
};

const savingsStyle = {
    color: '#005b96',
};

export default Calendar;
