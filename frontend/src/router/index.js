import { createWebHistory, createRouter } from 'vue-router';
import Login from '../pages/users/Login.vue';
import Signup from '../pages/users/Signup.vue';
import FindPassword from '../pages/users/FindPassword.vue';
import Main from '../pages/Main.vue';
import Mypage from '../pages/users/Mypage.vue';

const routes = [
  { path: '/', component: Main },
  { path: '/login', component: Login },
  { path: '/signup', component: Signup },
  { path: '/find/password', component: FindPassword },
  { path: '/mypage', component: Mypage },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
