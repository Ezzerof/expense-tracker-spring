import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import TransactionModal from './TransactionModal';
import SavingsModal from './SavingsModal';
import { removeAuthToken } from '../utils/storage';
import fetchAPI from '../utils/apiClient';

const Calendar = () => {
    const [selectedMonth, setSelectedMonth] = useState('');
    const [calendarDays, setCalendarDays] = useState([]);
    const [selectedDay, setSelectedDay] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [editingTransaction, setEditingTransaction] = useState(null); 
    const [hoverIndex, setHoverIndex] = useState(null);
    const [menuOpen, setMenuOpen] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isSavingsModalOpen, setIsSavingsModalOpen] = useState(false);
    const [savings, setSavings] = useState(0);
    const navigate = useNavigate();
    const menuRef = useRef(null);
    const [loading, setLoading] = useState(false);
    const [fetchCompleted, setFetchCompleted] = useState(false);

    useEffect(() => {
        const fetchSavings = async () => {
            try {
                const data = await fetchAPI('http://localhost:8080/api/v1/transaction/savings');
                setSavings(data.savings);
            } catch (error) {
                console.error('Error fetching savings:', error);
            }
        };
        fetchSavings();
    }, []);
    
    useEffect(() => {
        if (savings !== null) { 
            const today = new Date();
            const defaultMonth = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`;
            setSelectedMonth(defaultMonth);
            fetchTransactionsForMonth(defaultMonth);
        }
    }, [savings]);

    const fetchTransactionsForMonth = async (yearMonth) => {
        setLoading(true);
        try {
            const data = await fetchAPI(`http://localhost:8080/api/v1/transaction/month/${yearMonth}`);
            setTransactions(data);
            const [year, month] = yearMonth.split('-');
            generateCalendar(parseInt(year), parseInt(month) - 1, data);
            setFetchCompleted(true);
        } catch (error) {
            console.error('Error fetching transactions:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSaveSavings = async (newSavingsValue) => {
        try {
            await fetchAPI('http://localhost:8080/api/v1/transaction/savings', 'POST', { savings: newSavingsValue });
            setSavings(newSavingsValue);
            fetchTransactionsForMonth(selectedMonth);
            setIsSavingsModalOpen(false);
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
    
            const transactionsForDay = transactionsForMonth.filter((transaction) => {
                const transactionDate = new Date(transaction.startDate);
                return (
                    transactionDate.getFullYear() === date.getFullYear() &&
                    transactionDate.getMonth() === date.getMonth() &&
                    transactionDate.getDate() === date.getDate()
                );
            });
    
            const totalExpenses = transactionsForDay
                .filter((transaction) => transaction.transactionType === 'EXPENSE')
                .reduce((sum, transaction) => sum + transaction.amount, 0);
    
            const totalIncome = transactionsForDay
                .filter((transaction) => transaction.transactionType === 'INCOME')
                .reduce((sum, transaction) => sum + transaction.amount, 0);
    
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
        const newMonth = e.target.value; 
        setSelectedMonth(newMonth);
        fetchTransactionsForMonth(newMonth);
    };

    const handleLogout = () => {
        removeAuthToken();
        navigate('/');
    };

    const toggleMenu = (event) => {
        event.stopPropagation();
        setMenuOpen((prev) => !prev);
    };
    
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuOpen && menuRef.current && !menuRef.current.contains(event.target)) {
                setMenuOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [menuOpen]);

    const handleChangeSavings = () => {
        setIsSavingsModalOpen(true);
    };

    const closeSavingsModal = () => {
        setIsSavingsModalOpen(false);
    };

    const handleSaveTransaction = async (transaction) => {
        try {
            const response = await fetchAPI('http://localhost:8080/api/v1/transaction', 'POST', transaction);
    
            if (response.ok) { 
                await fetchTransactionsForMonth(selectedMonth);
                window.location.reload()
                setIsModalOpen(false);
            } else {
                console.error('Failed to save transaction:', response.statusText);
            }
        } catch (error) {
            console.error('Error saving transaction:', error);
        }
    };

    const handleEditClick = (transaction) => {
        setEditingTransaction(transaction);
        setIsModalOpen(true); 
    };

    const handleEditTransaction = async (updatedTransaction) => {
        try {
            await fetchAPI(`http://localhost:8080/api/v1/transaction/${updatedTransaction.id}`, 'PUT', updatedTransaction);
            fetchTransactionsForMonth(selectedMonth);
            setEditingTransaction(null);
            setIsModalOpen(false);
        } catch (error) {
            console.error('Error updating transaction:', error);
        }
    };

    const handleDeleteTransaction = async (transactionId) => {
        const deleteAll = window.confirm("Delete all occurrences of this recurring transaction?");
        try {
            await fetchAPI(`http://localhost:8080/api/v1/transaction/${transactionId}?deleteAll=${deleteAll}`, 'DELETE');
            fetchTransactionsForMonth(selectedMonth);
            window.location.reload() 
            setIsModalOpen(false);
        } catch (error) {
            console.error('Error deleting transaction:', error);
        }
    };
    
    

    return (
        <div className="container mt-5">
            <header className="d-flex justify-content-between align-items-center p-3 mb-4 border-bottom">
                <h2 className="text-primary">Expense Tracker</h2>
                <button
                    className="btn btn-primary"
                    onClick={toggleMenu}
                    style={menuBtnStyle}
                    ref={menuRef}
                >
                    ☰
                </button>
            </header>

            {menuOpen && (
                <div
                    className="menu"
                    ref={menuRef}
                    style={{
                        ...menuStyle,
                        left: `${menuRef.current?.offsetLeft}px`,
                    }}
                >
                    <p style={menuHeadingStyle}>Current Savings: £{savings}</p>
                    <ul style={menuListStyle}>
                        <li
                            onClick={handleChangeSavings}
                            style={menuItemStyle}
                        >
                            Change Current Savings
                        </li>
                        <li
                            onClick={handleLogout}
                            style={menuItemStyle}
                        >
                            Logout
                        </li>
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
                    editingTransaction={editingTransaction}
                    onClose={() => {
                        setIsModalOpen(false);
                        setEditingTransaction(null);
                    }}
                    onAddTransaction={handleSaveTransaction}
                    onEditTransaction={handleEditTransaction}
                    onDeleteTransaction={handleDeleteTransaction}
                    onEditClick={(transaction) => setEditingTransaction(transaction)}
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
                        <span style={dayNameStyle}>{day.dayName}</span>
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
    borderRadius: '50%',
    transition: 'all 0.3s',
};

const menuStyle = {
    position: 'absolute',
    top: '60px',
    width: '200px',
    backgroundColor: '#f8f9fa',
    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
    borderRadius: '8px',
    padding: '10px',
    zIndex: 10,
};

const menuHeadingStyle = {
    fontSize: '16px',
    fontWeight: 'bold',
    marginBottom: '10px',
    textAlign: 'center',
    color: '#333',
};

const menuListStyle = {
    listStyleType: 'none',
    padding: 0,
    margin: 0,
};

const menuItemStyle = {
    padding: '10px',
    cursor: 'pointer',
    textAlign: 'center',
    color: '#007bff',
    borderRadius: '5px',
    backgroundColor: '#e9ecef',
    marginBottom: '10px',
    transition: 'background 0.3s',
};

const calendarContainerStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
    gridGap: '15px',
    padding: '20px',
    backgroundColor: '#f8f9fa',
    borderRadius: '10px',
};

const calendarDayStyle = {
    border: '1px solid #ddd',
    padding: '20px',
    borderRadius: '10px',
    height: '180px',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    textAlign: 'center',
    backgroundColor: '#ffffff',
    transition: 'all 0.3s ease-in-out',
    cursor: 'pointer',
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
};

const calendarDayHoverStyle = {
    backgroundColor: '#d4e9ff', 
    color: '#000000',           
    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.2)',
    transform: 'scale(1.03)',
};

const dateHeaderStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    fontWeight: 'bold',
    fontSize: '16px',
    color: '#0056b3', 
};

const dayNameStyle = {
    fontSize: '16px',
    color: '#0056b3',
};

const dateStyle = {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#0056b3',
};

const infoContainerStyle = {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    fontSize: '16px',
    fontWeight: '500',
};

const expenseStyle = {
    color: '#d9534f', 
};

const incomeStyle = {
    color: '#5cb85c', 
};

const savingsStyle = {
    color: '#337ab7', 
    fontWeight: 'bold', 
    marginTop: '8px',
}

export default Calendar;
