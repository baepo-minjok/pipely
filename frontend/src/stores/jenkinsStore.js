import { defineStore } from 'pinia'
import axios from 'axios'




export const useJenkinsStore = defineStore('jenkinsStore', {
    // 상태
    state: () => ({
        jenkinsInfoDetail: {},
    }),


    actions: {
        async getJenkinInfoDetail(jenkinsInfoId) {
            try {
                const response = await axios.post('/api/jenkins/info', {
                    infoId: jenkinsInfoId
                });

                this.jenkinsInfoDetail = response.data.data || response.data
            } catch (error) {
                console.error('Error fetching job list:', error);
                this.jenkinsInfoDetail = [];
            }
        }

    }
})