import instance from '@/api/axiosInstance.js'

export const jenkinsInfoApi = {
    // Jenkins Info 상세 조회
    getDetail(infoId) {
        return instance.post("/jenkins/info", {infoId})

            .then((res) => res)
            .catch((error) => {
                throw error.response.data.error;
            });
    },

    // Jenkins Info 삭제
    delete(infoId) {
        return instance.delete(`/jenkins/info/${infoId}`)
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
            });
    },

    // Jenkins Info 수정
    update(payload) {
        return instance.put("/jenkins/info", payload)
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
            });
    },

    // Jenkins 연결 확인
    verify(infoId) {
        return instance.post("/jenkins/info/verification", infoId)
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
            });
    },
};
