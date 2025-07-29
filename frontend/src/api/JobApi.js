import axios from 'axios';
import {useJobStore} from "@/stores/useJobStore.js";

const instance = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

export const jobApi = {
    // Script 생성 및 반환
    createScript(data) {
        return instance
            .post('/jenkins/job/script/generate', data)
            .then((res) => {
                return res;
            })
            .catch((error) => {
                console.error('API Error:', error.response?.status, error.response?.data);
                return error.response.data.error;
            });
    },

    validateScript(data) {
        return instance
            .post('/jenkins/job/script/validate', data)
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
            });
    },

    createJob(data) {
        return instance
            .post('/jenkins/job/create', data)
            .then((res) => {
                return res;
            })
            .catch((error) => {
                console.error('API Error:', error.response?.status, error.response?.data);
                return error.response.data.error;
            });
    },
    deletedJobs(jobId) {
        return instance
            .delete(`/jenkins/job`, {params: {jobId: jobId}})
            .then((res) => res)
            .catch((error) => {
                console.error('API Error:', error.response?.status, error.response?.data);
                return error.response.data.error;
            });
    },

    fetchJobList(jenkinsInfoId) {
        const store = useJobStore();
        return instance
            .get('/jenkins/job', {params: {jenkinsInfoId: jenkinsInfoId}})
            .then((res) => {
                const data = res.data?.data;
                if (!data) {
                    throw new Error('서버 응답에 jobList 데이터가 없습니다.');
                }
                store.jobList = data;
                console.log(store.jobList);
            })
            .catch((error) => {
                console.error('API Error(getJobList):', error.response?.status, error.response?.data || error.message);
                throw (error.response?.data?.error || error);
            });

    },

    getJenkinsInfo() {
        const store = useJobStore();
        return instance
            .get('/jenkins/info')
            .then((res) => {
                const data = res.data?.data;
                if (!data) {
                    throw new Error('서버 응답에 젠킨스 정보가 없습니다.');
                }
                store.jenkinsInfo = data;
                return res;

            })
            .catch((error) => {
                console.error('API Error(getJenkinsInfo):', error.response?.status, error.response?.data || error.message);
                throw (error.response?.data?.error || error);

            });
    },

    getDeletedJobList(infoId) {
        return instance
            .get("/jenkins/job/deleted", {params: {jenkinsInfoId: infoId}})
            .then((res) => {
                return res.data?.data;
            })
            .catch((error) => {
                return error.response?.data.error;
            })
    },

    restoreJob(jobId) {
        return instance
            .get("/jenkins/job/restoration", {params: {jobId: jobId}})
            .then((res) => {
                return true;
            })
            .catch((error) => {
                throw error;
            })
    },

    hardDeleteJob(jobId) {
        return instance
            .delete("/jenkins/job/hard", {params: {jobId: jobId}})
            .then((res) => {
                return true;
            })
            .catch((error) => {
                throw error;
            })
    },

    buildJob(data) {
        return instance
            .post('/jenkins/build/stage/trigger', data)
            .then((res) => {
                return res;
            })
            .catch((error) => {
                throw error;
            })
    }


};
