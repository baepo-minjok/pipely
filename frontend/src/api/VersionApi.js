import axios from 'axios';

const instance = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

export const versionApi = {

    createSnapshot(jobId, snapshotName) {

        return instance
            .post(`/jenkins/job/version/${jobId}/snapshot`, null, {params: {snapshotName: snapshotName}})
            .then((res) => {
                return true;
            })
            .catch((error) => {
                console.error('API Error(createSnapshot):', error.response?.status, error.response?.data);
                return error.response.data.error;
            });
    },

    getSnapshotList(jobId) {

        return instance
            .get("/jenkins/job/version", {
                params: {jobId: jobId}
            })
            .then((res) => {
                return res.data?.data;
            })
            .catch((error) => {
                return error.response?.data.error;
            })
    }
}

