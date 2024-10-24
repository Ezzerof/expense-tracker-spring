import React, { useState, useEffect } from 'react';
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
    const [isTransactionModalOpen, setIsTransactionModalOpen] = useState(false);
    const [isSavingsModalOpen, setIsSavingsModalOpen] = useState(false);
    const [newTransaction, setNewTransaction] = useState({ name: '', amount: '', type: 'EXPENSE' });
    const [savings, setSavings] = useState(0);
    const navigate = useNavigate();

    const generateCalendar = async (year, month) => {
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const daysArray = [];

        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(year, month, day);
            const dayName = date.toLocaleDateString('en-US', { weekday: 'short' });

            daysArray.push({
                day,
                dayName,
                totalExpenses: 0,
                totalIncome: 0,
                remainingSavings: savings,
            });
        }

        setCalendarDays(daysArray);
    };

    const fetchSavings = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/v1/transaction/savings', {
                headers: {
                    'Authorization': `Basic ${getAuthToken()}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                console.error(`Error fetching savings: ${response.status} - ${response.statusText}`);
                return;
            }

            const data = await response.json();

            if (data && data.savings !== undefined && data.savings !== null) {
                setSavings(data.savings);
            } else {
                console.log('Savings is null, setting it to 0');
                setSavings(0);
            }
        } catch (error) {
            console.error('Error fetching savings:', error);
        }
    };

    useEffect(() => {
        const today = new Date();
        const defaultMonth = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`;
        setSelectedMonth(defaultMonth);
        generateCalendar(today.getFullYear(), today.getMonth());

        fetchSavings();
    }, []);

    const handleAddTransaction = async () => {
        try {
            const response = await fetch('/api/transactions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Basic ${localStorage.getItem('authToken')}`,
                },
                body: JSON.stringify({
                    ...newTransaction,
                    date: selectedDay.toISOString().split('T')[0],
                }),
            });

            if (response.ok) {
                generateCalendar(new Date().getFullYear(), new Date().getMonth());
                closeTransactionModal();
            } else {
                console.error('Failed to save transaction');
            }
        } catch (error) {
            console.error('Error saving transaction:', error);
        }
    };

    const handleMonthChange = (e) => {
        const selectedDate = new Date(e.target.value);
        setSelectedMonth(e.target.value);
        generateCalendar(selectedDate.getFullYear(), selectedDate.getMonth());
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

    const closeTransactionModal = () => {
        setIsTransactionModalOpen(false);
    };

    const closeSavingsModal = () => {
        setIsSavingsModalOpen(false);
    };

    const handleDayClick = async (date) => {
        setSelectedDay(date);

        try {
            const response = await fetch(`/api/transactions/${date.toISOString().split('T')[0]}`, {
                headers: {
                    'Authorization': `Basic ${localStorage.getItem('authToken')}`,
                },
            });
            const data = await response.json();
            setTransactions(data);
            setIsTransactionModalOpen(true);
        } catch (error) {
            console.error(`Error fetching transactions for ${date}:`, error);
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
                setIsSavingsModalOpen(false);
                await fetchSavings();
            } else {
                console.error('Failed to save savings:', response.statusText);
            }
        } catch (error) {
            console.error('Error saving savings:', error);
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
                <div className="menu" style={menuStyle}>
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

            {isTransactionModalOpen && (
                <TransactionModal
                    transactions={transactions}
                    selectedDay={selectedDay}
                    onClose={closeTransactionModal}
                    onAddTransaction={handleAddTransaction}
                    setNewTransaction={setNewTransaction}
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
    textAlign: 'center',
    boxShadow: '0 2px 10px rgba(0, 0, 0, 0.2)',
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
