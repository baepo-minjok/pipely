import { defineStore } from 'pinia';

export const useJobStore = defineStore('jobstore', {
    state: () => ({
        jobList: [],
        jenkinsInfo: [],
    })
});