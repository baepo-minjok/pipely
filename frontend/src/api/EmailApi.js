import instance from '@/api/axiosInstance.js'

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
    },


    // 비밀번호 재설정 이메일 요청
    sendResetEmail(data) {
        return instance
            .post("/auth/reset/password-reset/request", {
                email: data,
            })
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
            })
    },

    // 비밀번호 재설정 요청
    resetPassword(data) {
        return instance
            .post("/auth/reset/password-reset", data)
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
            })
    },

    // 휴면 유저 재활성화 이메일 요청
    sendDormantEmail(data) {
        return instance
            .get("/auth/reactive", {
                params: {
                    email: data,
                }
            })
            .then((res) => {
                return true;
            })
            .catch((error) => {
                    return false;
                }
            )
    },

    // 휴면 유저 재활성화 요청
    reactiveDormantUser(data) {
        return instance
            .post("/auth/reactive", data)
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
            })
    }
}