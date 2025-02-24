import { getAuthToken } from './storage';

const fetchAPI = async (url, method = 'GET', body) => {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Basic ${getAuthToken()}`,
        },
        body: body ? JSON.stringify(body) : undefined,
    };

    const response = await fetch(url, options);

    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }

    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        return await response.json();
    } else {
        return null;
    }
};


export default fetchAPI;
