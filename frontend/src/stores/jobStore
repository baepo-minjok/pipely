import { defineStore } from 'pinia'
import axios from 'axios'


export const useJobStore = defineStore('jobstore', {
    // 상태
    state: () => ({
        jobList: [],
        jenkinsInfo: [


        ],
    }),


    actions: {
        async fetchJobList(jenkinsInfoId) {
            try {
                const response = await axios.get('/api/jenkins/job', {
                    params: { jenkinsInfoId }
                });

                this.jobList = response.data.data ? response.data.data : response.data;
            } catch (error) {
                console.error('Error fetching job list:', error);
                this.jobList = []; // 실패 시 목록 비움
            }
        }
        ,

        async getJenkinsInfo() {
            try {
                const response = await axios.get('/api/jenkins/info');
                this.jenkinsInfo = response.data.data;

            } catch (error) {
                console.error('Error fetching Jenkins info:', error);
            }
        }
    }
})