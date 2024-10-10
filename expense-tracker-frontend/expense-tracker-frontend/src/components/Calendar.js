import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const Calendar = () => {
    const [selectedMonth, setSelectedMonth] = useState('');
    const [calendarDays, setCalendarDays] = useState([]);
    const [hoverIndex, setHoverIndex] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const today = new Date();
        const defaultMonth = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`;
        setSelectedMonth(defaultMonth);
        generateCalendar(today.getFullYear(), today.getMonth());
    }, []);

    const generateCalendar = (year, month) => {
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();

        const daysArray = [];
        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(year, month, day);
            const dayName = date.toLocaleDateString('en-US', { weekday: 'short' });

            const totalExpenses = Math.floor(Math.random() * 1000);
            const totalIncome = Math.floor(Math.random() * 1000);

            daysArray.push({
                day,
                dayName,
                totalExpenses,
                totalIncome
            });
        }

        setCalendarDays(daysArray);
    };

    const handleMonthChange = (e) => {
        const selectedDate = new Date(e.target.value);
        setSelectedMonth(e.target.value);
        generateCalendar(selectedDate.getFullYear(), selectedDate.getMonth());
    };

    const handleLogout = () => {
        localStorage.removeItem('authToken');
        navigate('/');
    };

    return (
        <div className="container mt-5">
            <h3 className="text-center">Expense Tracker - Calendar</h3>

            <div className="d-flex justify-content-end mb-4">
                <button className="btn btn-danger" onClick={handleLogout}>Logout</button>
            </div>

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
                        style={index === hoverIndex ? {...calendarDayStyle, ...calendarDayHoverStyle} : calendarDayStyle}
                        onMouseEnter={() => setHoverIndex(index)}
                        onMouseLeave={() => setHoverIndex(null)}
                    >
                        <div className="date-header" style={dateHeaderStyle}>
                            <span style={index === hoverIndex ? dayNameHoverStyle : dayNameStyle}>{day.dayName}</span>
                            <span style={dateStyle}>{day.day}</span>
                        </div>
                        <div style={infoContainerStyle}>
                            <div style={expenseStyle}>- £{day.totalExpenses}</div>
                            <div style={incomeStyle}>+ £{day.totalIncome}</div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

const calendarContainerStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(7, 1fr)', 
    gridGap: '15px',
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
    backgroundColor: '#ffff',
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
    color: '#ffff',
};

const dateStyle = {
    fontSize: '22px',
};

const infoContainerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
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

export default Calendar;
