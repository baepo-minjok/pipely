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
    },

    rollbackSnapshot(versionId) {
        return instance
            .post("/jenkins/job/version/rollback", null, {params: {versionId: versionId}})
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
            })
    },

    deleteSnapshot(versionId) {
        return instance
            .delete(`/jenkins/job/version`, {params: {versionId: versionId}})
            .then((res) => {
                return true;
            })
            .catch((error) => {
                return false;
            })
    },

    renameVersion(data) {
        return instance
            .post("/jenkins/job/version/rename", data)
            .then((res) => {
                return res;
            })
            .catch((error) => {
                throw error;
            })
    }
}

