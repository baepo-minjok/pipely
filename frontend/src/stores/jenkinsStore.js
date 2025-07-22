import { defineStore } from 'pinia'
import axios from 'axios'




export const useJobStore = defineStore('jobstore', {
    // 상태
    state: () => ({
        jenkinsInfoDetail: [],
    }),


    actions: {
        async getJenkinInfoDetail(jenkinsInfoId) {
            try {
                const response = await axios.post('/api/jenkins/info', {
                    params: { jenkinsInfoId }
                });

                this.jobList = response.data.data ? response.data.data : response.data;
            } catch (error) {
                console.error('Error fetching job list:', error);
                this.jobList = []; // 실패 시 목록 비움
            }
        }

    }
})