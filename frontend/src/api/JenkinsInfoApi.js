import axios from "axios";

const instance = axios.create({
    baseURL: "/api",
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials: true,
});


export const jenkinsInfoApi = {
    // Jenkins Info 상세 조회
    getDetail(infoId) {
        return instance.post("/jenkins/info", { infoId })
            console.log("@@@@ : " + infoId)
            .then((res) => res.data)
            .catch((error) => {
                console.error("getDetail error:", error);
                throw error.response?.data?.error || error;
            });
    },

    // Jenkins Info 삭제
    delete(infoId) {
        return instance.delete("/jenkins/info", {
            data: { infoId },
        })
            .then((res) => res.data)
            .catch((error) => {
                console.error("delete error:", error);
                throw error.response?.data?.error || error;
            });
    },

    // Jenkins Info 수정
    update(payload) {



        return instance.put("/jenkins/info", payload)
            .then((res) => res.data)
            .catch((error) => {
                console.error("update error:", error);
                throw error.response?.data?.error || error;
            });
    },

    // Jenkins 연결 확인
    verify(infoId) {
        return instance.post("/jenkins/info/verification", { infoId })
            .then((res) => res.data)
            .catch((error) => {
                console.error("verify error:", error);
                throw error.response?.data?.error || error;
            });
    },
};
