import {createRouter, createWebHistory} from 'vue-router';
import {useUserStore} from "@/stores/useUserStore.js"
import Login from '../pages/users/Login.vue';
import Signup from '../pages/users/Signup.vue';
import FindPassword from '../pages/users/FindPassword.vue';
import Main from '../pages/Main.vue';
import Mypage from '../pages/users/Mypage.vue';
import CreateCicdInfo from '../pages/users/CreateCicdInfo.vue';
import CicdInfoDetail from '../pages/users/CicdInfoDetail.vue';
import JobList from '../pages/jobs/JobList.vue';
import CreateJob from '../pages/jobs/CreateJob.vue';
import OAuth from '../pages/users/OAuthSignup.vue';

const routes = [
    {path: '/', component: Main, name: 'Main', meta: {requiresAuth: true}},
    {path: '/user/login', component: Login, name: 'Login'},
    {path: '/user/oAuth', component: OAuth, name: 'OAuth'},
    {path: '/user/signup', component: Signup, name: 'Signup'},
    {path: '/user/find/password', component: FindPassword, meta: {requiresAuth: true}},
    {path: '/mypage', component: Mypage, name: 'Mypage', meta: {requiresAuth: true}},
    {path: '/mypage/cicd/create', component: CreateCicdInfo, meta: {requiresAuth: true}},
    {path: '/mypage/cicd/:id', component: CicdInfoDetail, meta: {requiresAuth: true}},
    {path: '/job', component: JobList, meta: {requiresAuth: true}},
    {path: '/job/create', component: CreateJob, meta: {requiresAuth: true}},
    {
        path: '/:catchAll(.*)',
        redirect: '/',
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to, from, next) => {

    const userStore = useUserStore();
    if (to.meta.requiresAuth && !userStore.isFetched) {
        next('/user/login');
    } else {
        next()
    }
});

export default router;
