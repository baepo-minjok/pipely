import { createWebHistory, createRouter } from 'vue-router';
import HelloWorld from '../components/HelloWorld.vue';
import Login from '../pages/users/Login.vue';
import Signup from '../pages/users/Signup.vue';
import FindPassword from '../pages/users/FindPassword.vue';

const routes = [
  { path: '/', component: HelloWorld },
  { path: '/login', component: Login },
  { path: '/signup', component: Signup },
  { path: '/find/password', component: FindPassword },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
