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
        return error.response.data.error;
      });
  },
};
