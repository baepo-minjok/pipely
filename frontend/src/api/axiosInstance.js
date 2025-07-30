import axios from 'axios';
import router from '@/router';

const instance = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});
instance.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        const status = error.response?.status;

        if (status === 403) {
            alert("로그인이 만료되었습니다\n 다시 로그인해주세요!");

            localStorage.removeItem('chatHistory');
            sessionStorage.clear();

            router.push({name: 'Login'});
        }

        return Promise.reject(error);
    }
);

export default instance;