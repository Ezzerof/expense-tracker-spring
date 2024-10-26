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

    const fetchTransactions = async (year, month) => {
        try {
            const response = await fetch(`http://localhost:8080/api/v1/transaction/month/${year}-${String(month + 1).padStart(2, '0')}`, {
                headers: {
                    'Authorization': `Basic ${getAuthToken()}`,
                    'Content-Type': 'application/json',
                },
            });

            if (response.ok) {
                const data = await response.json();
                setTransactions(data);
                generateCalendar(year, month, data);  // Update calendar with fetched transactions
            } else {
                console.error(`Error fetching transactions: ${response.status} - ${response.statusText}`);
            }
        } catch (error) {
            console.error('Error fetching transactions:', error);
        }
    };

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
                fetchTransactions(new Date(selectedMonth).getFullYear(), new Date(selectedMonth).getMonth());
                setIsModalOpen(false);
            } else {
                console.error('Failed to save transaction:', response.statusText);
            }
        } catch (error) {
            console.error('Error saving transaction:', error);
        }
    };

    const generateCalendar = (year, month, transactionsForMonth = []) => {
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const daysArray = [];

        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(year, month, day);
            const dayName = date.toLocaleDateString('en-US', { weekday: 'short' });

            const transactionsForDay = transactionsForMonth.filter(
                (transaction) => new Date(transaction.startDate).getDate() === day
            );

            const totalExpenses = transactionsForDay
                .filter((transaction) => transaction.transactionType === 'EXPENSE')
                .reduce((sum, transaction) => sum + transaction.amount, 0);

            const totalIncome = transactionsForDay
                .filter((transaction) => transaction.transactionType === 'INCOME')
                .reduce((sum, transaction) => sum + transaction.amount, 0);

            const remainingSavings = savings + totalIncome - totalExpenses;

            daysArray.push({
                day,
                dayName,
                totalExpenses,
                totalIncome,
                remainingSavings,
            });
        }

        setCalendarDays(daysArray);
    };

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
        generateCalendar(today.getFullYear(), today.getMonth());

        fetchSavings();
        fetchTransactions(today.getFullYear(), today.getMonth());
    }, []);

    const handleDayClick = (date) => {
        setSelectedDay(date);
        const transactionsForDay = transactions.filter((t) => new Date(t.startDate).toDateString() === date.toDateString());
        setTransactions(transactionsForDay);
        setIsModalOpen(true);
    };

    const handleMonthChange = (e) => {
        const selectedDate = new Date(e.target.value);
        setSelectedMonth(e.target.value);
        fetchTransactions(selectedDate.getFullYear(), selectedDate.getMonth());
    };

    const handleLogout = () => {
        removeAuthToken();
        navigate('/');
    };

    return (
        <div className="container mt-5">
            <h3 className="text-center">Expense Tracker - Calendar</h3>

            <div className="d-flex justify-content-start mb-4">
                <button className="menu-btn" onClick={() => setMenuOpen(!menuOpen)} style={menuBtnStyle}>
                    ☰
                </button>
            </div>

            {menuOpen && (
                <div className="menu" style={menuStyle} ref={menuRef} onClick={(e) => e.stopPropagation()}>
                    <p style={menuHeadingStyle}>Current Savings: £{savings}</p>
                    <ul style={menuListStyle}>
                        <li onClick={() => setIsSavingsModalOpen(true)} style={menuItemStyle}>Change Current Savings</li>
                        <li onClick={handleLogout} style={menuItemStyle}>Logout</li>
                    </ul>
                </div>
            )}

            {isSavingsModalOpen && (
                <SavingsModal
                    onClose={() => setIsSavingsModalOpen(false)}
                    onSaveSavings={(newSavingsValue) => {
                        setSavings(newSavingsValue);
                        fetchTransactions(new Date(selectedMonth).getFullYear(), new Date(selectedMonth).getMonth());
                    }}
                    currentSavings={savings}
                />
            )}

            {isModalOpen && (
                <TransactionModal
                    transactions={transactions}
                    selectedDay={selectedDay}
                    onClose={() => setIsModalOpen(false)}
                    onAddTransaction={handleSaveTransaction}
                    onEditTransaction={(index, updatedTransaction) => handleSaveTransaction(updatedTransaction)}
                    onDeleteTransaction={(transactionId) => {
                        setTransactions((prevTransactions) =>
                            prevTransactions.filter((transaction) => transaction.id !== transactionId)
                        );
                    }}
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
