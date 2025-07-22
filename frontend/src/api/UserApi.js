import axios from "axios";

const instance = axios.create({
    baseURL: "/api",
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials: true,
});

export const userApi = {
    // 로그인 api
    login(data) {
        return instance
            .post("/auth/user/login", data)
            .then((res) => {
                return res;
            })
            .catch((error) => {
                return error.response.data.error;
            });
    },

    // 회원가입 api
    signup(data) {
        return instance
            .post("/auth/user/signup", data)
            .then((res) => {
                return res;
            })
            .catch((error) => {
                return error.response.data.error;
            })
    },

    // oAuth 회원가입 api
    oAuthSignup(data) {
        return instance
            .post("/auth/user/oauth/signup", data)
            .then((res) => {
                return res;
            })
            .catch((error) => {
                return error.response.data.error;
            })
    },

    // 이메일 중복확인
    checkDuplicate(email) {
        return instance
            .get("/auth/user/duplicate", {
                params: {email: email}
            })
            .then((res) => {
                return res;
            })
            .catch((error) => {
                return error.response.data.error;
            })
    },

    // Mypage에서 유저 정보 불러오기
    getUserDetail() {
        return instance
            .get("/auth/user/detail")
            .then((res) => {
                return res;
            })
            .catch((error) => {
                return error.response.data.error;
            })
    }
};