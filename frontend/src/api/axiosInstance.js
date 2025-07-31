import axios from 'axios';
import router from '@/router';
import {userApi} from '@/api/UserApi.js'
import {useUserStore} from "@/stores/useUserStore.js";

let userStore; // 아직 주입되지 않음

export function setUserStore(store) {
    userStore = store;
}

const instance = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});
const loginInstance = axios.create({
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
        const userStore = useUserStore();

        if (status === 403) {
            if (!userStore.isFetched) {
                router.push({name: 'Login'});
                return;
            } else {
                if (confirm("세션이 만료되었습니다.\n 연장하시겠습니까?")) {
                    const res = await userApi.reissueToken();
                    if (res) {
                        await userStore.fetchUserInfo();
                        router.push({name: 'Main'});
                    } else {
                        alert("인증정보가 만료되었습니다.\n 다시 로그인해주세요!");
                        userStore.reset();
                        localStorage.removeItem('chatHistory');
                        sessionStorage.clear();
                        router.push({name: 'Login'});
                    }
                } else {
                    userStore.reset();
                    localStorage.removeItem('chatHistory');
                    sessionStorage.clear();
                    router.push({name: 'Main'});
                    return Promise.reject(error);
                }
            }
        }

        return Promise.reject(error);
    }
);

export {instance, loginInstance};