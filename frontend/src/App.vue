<script setup>
import { RouterView, useRoute } from 'vue-router';
import Header from './components/common/Header.vue';

const route = useRoute();
</script>

<template>
  <Header v-if="!route.path.startsWith('/user')" />
  <router-view></router-view>
</template>

<style scoped></style>
