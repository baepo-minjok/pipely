import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/stores/useUserStore.js';
import { userApi } from '@/api/UserApi.js';
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
import VerifyEmail from '../pages/users/VerifyEmail.vue';

const routes = [
  { path: '/', component: Main, name: 'Main' },
  { path: '/user/login', component: Login, name: 'Login' },
  { path: '/user/oAuth', component: OAuth, name: 'OAuth' },
  { path: '/user/signup', component: Signup, name: 'Signup' },
  { path: '/user/email/verify', component: VerifyEmail },
  { path: '/user/find/password', component: FindPassword, meta: { requiresAuth: true } },
  { path: '/mypage', component: Mypage, name: 'Mypage', meta: { requiresAuth: true } },
  { path: '/mypage/cicd/create', component: CreateCicdInfo, meta: { requiresAuth: true } },
  { path: '/mypage/cicd/:id', component: CicdInfoDetail, meta: { requiresAuth: true } },
  { path: '/job', component: JobList, meta: { requiresAuth: true } },
  { path: '/job/create', component: CreateJob, name: 'CreateJob', meta: { requiresAuth: true } },
  { path: '/:catchAll(.*)', redirect: '/' },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();

  // 로그인된 사용자가 로그인/회원가입 페이지로 가면 메인으로
  if (['/user/login', '/user/signup', '/user/oAuth'].includes(to.path) && userStore.isFetched.value) {
    next('/');
    return;
  }

  // 인증 필요 페이지
  if (to.meta.requiresAuth && !userStore.isFetched) {
    try {
      const isLoggedIn = await userApi.isLoggedIn();
      console.log(isLoggedIn);
      if (isLoggedIn) {
        await userStore.fetchUserInfo();
        if (!userStore.isFetched) {
          next('/user/login');
        } else {
          next();
        }
      } else {
        userStore.reset();
        next('/user/login');
      }
    } catch (e) {
      userStore.reset();
      next('/user/login');
    }
  } else {
    next();
  }
});

export default router;
