import { getAuthToken } from './storage';

const fetchAPI = async (url, method = 'GET', body = null) => {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Basic ${getAuthToken()}`,
        },
    };
    if (body) options.body = JSON.stringify(body);

    const response = await fetch(url, options);
    if (!response.ok) throw new Error(`API Error: ${response.status}`);
    return response.json();
};

export default fetchAPI;
