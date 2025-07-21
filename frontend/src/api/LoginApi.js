import axios from "axios";

const instance = axios.create({
    baseURL: "/api",
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials: true,
});

export const loginApi = {
    login(data) {
        return instance
            .post("/auth/user/login", data)
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false; // 실패 시 false 반환
            });
    },
};