<script setup>
import {useRouter} from 'vue-router'
import {userApi} from "@/api/UserApi.js";
import {ref} from "vue";
import {useUserStore} from "@/stores/useUserStore.js";

const userStore = useUserStore();

const router = useRouter();

const email = ref("");
const password = ref("");
const passwordError = ref("");

const check = async () => {
  passwordError.value = "";

  const userInfo = userStore.getUserInfo();

  email.value = userInfo.email;

  const data = {
    email: email.value,
    password: password.value,
  }
  try {
    const response = await userApi.login(data);

    if (response.status === 200) {

      const tokenResponse = await userApi.getToken(userInfo.email);
      const data = tokenResponse.data.data;
      if (tokenResponse.status === 200) {
        userStore.reset();
        await userApi.logout();
        router.push({name: 'ResetPassword', query: {token: data}});
      } else {
        alert("오류가 발생했습니다\n 다시 시도해주세요");
        router.push("/");
      }
    } else {
      passwordError.value = "비밀번호가 틀렸습니다.";
    }
  } catch (err) {
    alert("오류가 발생했습니다\n 다시 시도해주세요");
    router.push("/");
  }
}
</script>


<template>
  <div class="container">
    <img alt="logo" src="/src/assets/images/logo.png"/>
    <h1>비밀번호 확인</h1>
    <form action="" class="form_box">
      <input
          id="password"
          v-model="password"
          :class="['input_box', passwordError ? 'input_box--error' : '']"
          name="password"
          placeholder="기존 비밀번호를 입력해주세요."
          type="password"
      />
      <p v-if="passwordError" class="input-error">{{ passwordError }}</p>
      <button class="btn find_btn" type="button" @click="check">확인</button>
    </form>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  gap: 30px;
}

.container > img {
  width: 150px;
}

.container > h1 {
  font-size: 25px;
}

.form_box {
  width: 32%;
  min-width: 380px;
  display: flex;
  flex-direction: column;
  border-radius: 10px;
  border: 1px solid var(--gray300);
  padding: 40px 35px;
  gap: 10px;
}

.form_box > p {
  font-size: 16px;
  align-self: center;
  line-height: 140%;
  margin-bottom: 20px;
}

.input_box {
  border-radius: 10px;
  border: 1px solid var(--gray300);
  padding: 12px;
  width: 100%;
  outline: none;
  box-sizing: border-box;
}

.send_email_box {
  width: 32%;
  min-width: 380px;
  border-radius: 10px;
  border: 1px solid var(--gray300);
  padding: 40px 35px;
  text-align: center;
  line-height: 150%;
}

.send_email_box > img {
  width: 60px;
  margin-bottom: 10px;
}

.login_btn {
  display: block;
  width: 100%;
  margin-top: 40px;
  text-decoration: none;
  box-sizing: border-box;
}


.input-error {
  color: #ff5555;
  font-size: 10px !important;
  margin-top: 2px;
  width: 100%;
  text-align: left;
}

.input_box--error {
  border: 1.5px solid #ff7b7b !important;
  background-color: #fff5f5;
  transition: border-color 0.2s;
}

.input-success {
  color: #0f8713;
  font-size: 10px !important;
  margin-top: 2px;
  margin-bottom: 2px;
  width: 100%;
  text-align: left;
}

.input_box--success {
  border: 1.5px solid #0f8713 !important;
  background-color: #e7ffe5;
  transition: border-color 0.2s;
}
</style>
