import {defineStore} from 'pinia'
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
        async updateJenkinsInfoDetail(form) {
            try {


                const payload = {
                    infoId: form.id,
                    name: form.name,
                    description: form.description,
                    jenkinsId: form.jenkinsId,
                    uri: form.uri,
                    apiToken: form.apiToken
                }


                const response = await axios.put('/api/jenkins/info', payload);
                console.log(response.data.data)


                this.jenkinsInfoDetail = response.data.data || response.data
            } catch (error) {
                console.error('Error fetching job list:', error);
                this.jenkinsInfoDetail = [];
            }
        },
        async jenkinsURITest(infoId) {
            try {

                const asd = {
                    infoId: infoId

                }



                const response = await axios.post('/api/jenkins/info/verification', asd);


                return response.data // 또는 return response.data.data
            } catch (error) {
                console.error('Error fetching job list:', error);
                this.jenkinsInfoDetail = [];
            }
        }


    }
})