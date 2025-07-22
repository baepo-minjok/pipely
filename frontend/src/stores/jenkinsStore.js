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
                console.log(response.data.data)


                this.jenkinsInfoDetail = response.data.data || response.data
            } catch (error) {
                console.error('Error fetching job list:', error);
                this.jenkinsInfoDetail = [];
            }
        },
        async deleteJenkinsInfoDetail(jenkinsInfoId) {
            try {
                const response = await axios.delete('/api/jenkins/info', {
                    infoId: jenkinsInfoId
                });
                console.log(response.data.data)


                this.jenkinsInfoDetail = response.data.data || response.data
            } catch (error) {
                console.error('Error fetching job list:', error);
                this.jenkinsInfoDetail = [];
            }
        },
        async updateJenkinsInfoDetail(jenkinsInfoId) {
            try {
                const response = await axios.put ('/api/jenkins/info', {
                    infoId: jenkinsInfoId
                });
                console.log(response.data.data)


                this.jenkinsInfoDetail = response.data.data || response.data
            } catch (error) {
                console.error('Error fetching job list:', error);
                this.jenkinsInfoDetail = [];
            }
        }


    }
})