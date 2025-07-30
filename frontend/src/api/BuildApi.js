import {instance} from '@/api/axiosInstance.js'

export const buildApi = {
    getJobBuildStreamLog(jobId) {
        return instance
            .get('/jenkins/build/streamlog', {
                params: {jobId},
            })
            .then((res) => res)
            .catch((error) => {
                console.error('❌ error', error.response?.status, error.response?.data);
                return error;
            });
    },

    getBuildHistoryAll(jobId) {
        return instance
            .get('/jenkins/build/history/all', {
                params: {jobId},
            })
            .then((res) => res)
            .catch((error) => {
                console.error('❌ error', error.response?.status, error.response?.data);
                return error;
            });
    },

    triggerBuildStages(data) {
        return instance
            .post('/jenkins/build/stage/trigger', data)
            .then((res) => res)
            .catch((error) => {
                console.error('❌ restart error', error.response?.status, error.response?.data);
                return error;
            });
    },
};
