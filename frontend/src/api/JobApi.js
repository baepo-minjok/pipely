import axios from 'axios';

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

  getDetail(jobId) {
    return instance
      .get('/jenkins/job/detail', {
        params: { jobId: jobId },
      })
      .then((res) => {
        return res;
      })
      .catch((error) => {
        return error.response.data.error;
      });
  },
};
