import axios from "axios";

const instance = axios.create({
    baseURL: "/api",
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials: true,
});

export const emailApi = {
    verifyEmail(data) {
        return instance
            .get("/auth/email/verify-email", {
                params: {
                    token: data
                }
            })
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
            })
    }
}