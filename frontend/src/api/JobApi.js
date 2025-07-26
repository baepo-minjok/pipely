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
    deletedJobs(jobId ) {
        return instance
            .delete (`/jenkins/job`, { params: { jobId  } })
            .then((res) => res)
            .catch((error) => {
                console.error('API Error:', error.response?.status, error.response?.data);
                return error.response.data.error;
            });
    },
    fetchJobList(jenkinsInfoId) {
        const store = useJobStore();
        return instance
            .get('/jenkins/job', { params: { jenkinsInfoId } })
            .then((res) => {
                store.jobList = res.data.data;
                return res;
            })
            .catch((error) => {
                console.error('API Error(getJobList):', error.response?.status, error.response?.data);
                throw error;
            });
    },

    getJenkinsInfo() {
        const store = useJobStore();
        return instance
            .get('/jenkins/info')
            .then((res) => {
                store.jenkinsInfo = res.data.data;
                return res;
            })
            .catch((error) => {
                console.error('API Error(getJenkinsInfo):', error.response?.status, error.response?.data);
                throw error;
            });
    },

};
