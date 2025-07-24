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
                this.jenkinsInfoDetail = response.data;
            } catch (error) {
                this.jenkinsInfoDetail = [];
            }
        },

        async deleteJenkinsInfoDetail(jenkinsInfoId) {
            try {
                const res = await jenkinsInfoApi.delete(jenkinsInfoId);

                return res.success === true;
            } catch (error) {
                console.error("삭제 실패:", error);
                return false;
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
                const response = await jenkinsInfoApi.verify(infoId);
                return response;
            } catch (error) {
                this.jenkinsInfoDetail = [];
                return null;
            }
        }
    }
});
