<script setup>
import {onMounted} from 'vue';
import {useRoute, useRouter} from 'vue-router'
import {emailApi} from "@/api/EmailApi.js";

const route = useRoute();
const router = useRouter();

onMounted(async () => {

      const token = route.query.token;
      if (token) {
        try {
          const response = await emailApi.verifyEmail(token);
          if (response) {
            alert("이메일 인증 성공!\n 로그인해주세요");
          } else {
            alert("이메일 인증 실패!\n 다시 시도해주세요");
          }
        } catch (e) {
          alert("이메일 인증 실패!\n 다시 시도해주세요");
        }
      } else {
        alert("이메일 인증 실패!\n 다시 시도해주세요");
      }

      router.push({name: "Login"});
    }
)
</script>

<template>
</template>

<style scoped>
</style>
