<script setup>
import { ref } from 'vue';
import Header from '../components/common/Header.vue';

const sendText = ref('');

</script>

<template>
  <div class="container">
    <Header></Header>  
    <div class="chat_box">
      <p>무엇을 도와드릴까요?</p>

      <div class="input_box">
        <input type="text" placeholder="오늘 어떤 도움을 드릴까요?" v-model="sendText">
        <button class="send_btn">
          <img src="/src/assets/icons/up_arrow.svg" alt="send">
        </button>
      </div>
    </div>
  </div>

</template>

<style scoped>
.container {
  width: 100%;
  height: 100%;
}

.chat_box {
  height: 90%;
  margin: 0 20%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.chat_box > p {
  font-size: 28px;
  margin-bottom: 47px;
}

.input_box {
  width: 100%;
  border-radius: 16px;
  border: 1px solid var(--gray200);
  box-shadow: 2px 2px 4px 0px var(--gray300);
  padding: 11px 15px 11px 22px;
  background-color: white;
  display: flex;
}

.input_box > input {
  font-size: 16px;
  width: 100%;
  border: none;
  outline: none;

  &::placeholder {
    color: var(--gray500);
  }
}

.send_btn {
  border-radius: 99px;
  background-color: var(--main-color-bg);
  width: 37px;
  height: 37px;
  display: grid;
  place-content: center;
  border: none;
  padding: 11px;
  cursor: pointer;
  transition: all .3s;

  &:hover {
    background-color: var(--chat-send);
  }

}

.send_btn > img {
  width: 16px;
  height: 16px;
}

</style>