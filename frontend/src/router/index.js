import {createRouter, createWebHistory} from 'vue-router';
import Login from '../pages/users/Login.vue';
import Signup from '../pages/users/Signup.vue';
import FindPassword from '../pages/users/FindPassword.vue';
import Main from '../pages/Main.vue';
import Mypage from '../pages/users/Mypage.vue';
import CreateCicdInfo from '../pages/users/CreateCicdInfo.vue';
import CicdInfoDetail from '../pages/users/CicdInfoDetail.vue';
import JobList from '../pages/jobs/JobList.vue';

const routes = [
    {path: '/', component: Main, name: 'Main'},
    {path: '/user/login', component: Login},
    {path: '/user/signup', component: Signup},
    {path: '/user/find/password', component: FindPassword},
    {path: '/mypage', component: Mypage},
    {path: '/mypage/cicd/create', component: CreateCicdInfo},
    {path: '/mypage/cicd/:id', component: CicdInfoDetail},
    {path: '/job', component: JobList},
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
