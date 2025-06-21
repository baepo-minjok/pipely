import { createWebHistory, createRouter } from 'vue-router';
import HelloWorld from '../components/HelloWorld.vue';
import Login from '../pages/users/Login.vue';

const routes = [
  { path: '/', component: HelloWorld },
  { path: '/login', component: Login },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
