<script setup>
import {ref} from "vue";
import {useRouter} from 'vue-router';
import {userApi} from "@/api/UserApi.js";

const router = useRouter();

// form
const name = ref("");
const description = ref("");
const jenkinsId = ref("");
const uri = ref("");
const apiToken = ref("");

const handleCancelClick = () => {
  router.back();
};

const createInfo = async () => {

  const data = {
    name: name.value,
    description: description.value,
    jenkinsId: jenkinsId.value,
    uri: uri.value,
    apiToken: apiToken.value,
  }

  const response = await userApi.createInfo(data);

  if (response.status === 200) {
    alert("Jenkins 정보가 등록되었습니다!");
  } else {
    alert("Jenkins 정보 등록 실패!\n다시 시도해주세요");
  }
  router.push({name: "Mypage"});
}
</script>

<template>
  <div class="container">
    <h1>CI/CD 정보 생성</h1>
    <div class="line"></div>
    <form @submit.prevent="createInfo">
      <div class="input_box">
        <label for="name">정보 닉네임</label>
        <input v-model="name" class="input" placeholder="정보 닉네임을 입력해주세요." type="text"/>
        <label for="description">정보 설명</label>
        <textarea id="description" v-model="description" name="description" placeholder="정보에 대한 설명을 입력해주세요."></textarea>
        <label for="url">URL</label>
        <input v-model="uri" class="input" placeholder="https://jenkins.io" type="text"/>
        <label for="secret_key">Secret Token</label>
        <input v-model="apiToken" class="input" placeholder="토큰 입력.." type="text"/>
        <label for="cicd_id">ID</label>
        <input v-model="jenkinsId" class="input" placeholder="ID를 입력해주세요." type="text"/>
      </div>
      <div class="btn_box">
        <button class="create_btn" type="submit">생성</button>
        <button class="cancel_btn" type="button" @click="handleCancelClick">취소</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.container {
  width: 60%;
  margin: 70px auto;
}

.container > h1 {
  font-size: 28px;
}

.line {
  width: 100%;
  height: 1px;
  background-color: var(--gray200);
  margin: 18px 0 30px;
}

form {
  display: flex;
  flex-direction: column;
  gap: 46px;
  margin: 35px 0 64px;
}

label {
  display: block;
  margin-bottom: 11px;
}

.input {
  width: 100%;
  border: 1px solid var(--gray200);
  padding: 14px 16px;
  font-size: 16px;
  margin-bottom: 46px;

  &::placeholder {
    color: var(--gray400);
  }
}

textarea {
  width: 100%;
  height: 100px;
  resize: none;
  background-color: white;
  border: 1px solid var(--gray200);
  margin-bottom: 46px;
  padding: 15px 17px;
  font-size: 16px;

  &::placeholder {
    color: var(--gray400);
  }
}
</style>
