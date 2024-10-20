
export const saveAuthToken = (username, password) => {
    const authToken = btoa(`${username}:${password}`);
    localStorage.setItem('authToken', authToken);
    console.log('Saved token: ', localStorage.getItem('authToken'));
};


export const getAuthToken = () => {
    return localStorage.getItem('authToken');
};


export const removeAuthToken = () => {
    localStorage.removeItem('authToken');
    console.log('Auth token removed from localStorage');
};
