import {createApp} from 'vue';
import './reset.css';
import './style.css';
import router from './router';
import App from './App.vue';
import {createPinia} from 'pinia'
import {setUserStore} from '@/api/axiosInstance.js';
import {useUserStore} from "@/stores/useUserStore.js";
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'


const app = createApp(App);
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(router);
app.use(pinia);
setUserStore(useUserStore());
app.mount('#app');
