import instance from '@/api/axiosInstance.js'

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

    // 로그아웃
    logout() {
        return instance
            .post("/auth/user/logout")
            .then((res) => {
                return res;
            })
            .catch((error) => {
                return error.response.data.error;
            })
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

    // 회원 탈퇴
    withdraw() {
        return instance.delete("/auth/user/withdraw")
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
            })
    },

    // 탈퇴한 회원 재활성화
    reactivation(data) {
        return instance.post("/auth/user/reactivation", data)
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
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
    },

    isLoggedIn() {
        return instance
            .get("/auth/user/isLogged")
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
            })
    },

    // Jenkins info 등록
    createInfo(data) {
        return instance
            .post("/jenkins/info/create", data)
            .then((res) => {
                return res;
            })
            .catch((error) => {
                return error.response.data.error;
            })
    },

    getToken(data) {
        return instance
            .get("/auth/reset", {
                params: {email: data}
            })
            .then((res) => {
                return res;
            })
            .catch((error) => {
                return error.response.data.error;
            });
    },
};