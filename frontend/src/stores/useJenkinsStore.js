// src/stores/useJenkinsStore.js
import { defineStore } from 'pinia';
import { jenkinsInfoApi } from '@/api/JenkinsInfoApi';

export const useJenkinsStore = defineStore('jenkinsStore', {
    state: () => ({
        jenkinsInfoDetail: {},
    }),

    actions: {
        async getJenkinInfoDetail(jenkinsInfoId) {
            try {
                const response = await jenkinsInfoApi.getDetail(jenkinsInfoId);

                this.jenkinsInfoDetail = response.data.data;
            } catch (error) {
                this.jenkinsInfoDetail = [];
            }
        },

        async deleteJenkinsInfoDetail(jenkinsInfoId) {
            try {
                const data = await jenkinsInfoApi.delete(jenkinsInfoId);
                this.jenkinsInfoDetail = data;
            } catch (error) {
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


                const data = await jenkinsInfoApi.update(payload);
                this.jenkinsInfoDetail = data;
            } catch (error) {
                this.jenkinsInfoDetail = [];
            }
        },

        async jenkinsURITest(infoId) {
            try {
                return await jenkinsInfoApi.verify(infoId);
            } catch (error) {
                this.jenkinsInfoDetail = [];
                return null;
            }
        }
    }
});
