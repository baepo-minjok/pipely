<script setup>
import { ref } from 'vue';


const sendEmail = ref(false);
const emailValue = ref('');

const handleNextClick = () => {
  console.log(emailValue.value);
  // [TODO] : 이메일 발송 인증 보내기 
  // [TODO] : 이메일 발송 완료 문구 띄우기
}

</script>

<template>
  <div class="container">
    <img src="/src/assets/images/logo.png" alt="logo">
    <h1>비밀번호 찾기</h1>
    <form action="" class="form_box">
      <p>기존에 가입하신 이메일을 입력하시면, <br />비밀번호 변경 메일을 발송해드립니다.</p>
      <input type="email" id="email" name="email" v-model="emailValue" class="input_box" placeholder="이메일 주소를 입력해주세요." />
      <button type="button" class="btn find_btn" @click="handleNextClick">다음</button>
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

.container>img {
  width: 150px;
}

.container>h1 {
  font-size: 25px;
}

.form_box {
  width: 25%;
  display: flex;
  flex-direction: column;
  border-radius: 10px;
  border: 1px solid var(--gray300);
  padding: 35px;
  gap: 10px;
}

.form_box>p {
  font-size: 14px;
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
</style>