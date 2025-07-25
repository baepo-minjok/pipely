<script setup>
import {onMounted} from 'vue';
import {useRoute, useRouter} from 'vue-router'
import {emailApi} from "@/api/EmailApi.js";

const route = useRoute();
const router = useRouter();

onMounted(async () => {

      const token = {token: route.query.token};
      if (token) {
        try {
          const response = await emailApi.reactiveDormantUser(token);
          if (response) {
            alert("계정이 성공적으로 복구되었습니다!\n 로그인해주세요");
          } else {
            alert("계정이 성공적으로 복구되었습니다!\n 로그인해주세요");
          }
        } catch (e) {
          alert("오류 발생!\n 다시 시도해주세요");
        }
      } else {
        alert("오류 발생!\n 다시 시도해주세요");
      }

      router.push({name: "Login"});
    }
)
</script>

<template>
</template>

<style scoped>
</style>
