<script setup>
import {useRouter} from 'vue-router'
import {userApi} from "@/api/UserApi.js";
import {ref} from "vue";
import {useUserStore} from "@/stores/useUserStore.js";

const userStore = useUserStore();

const emit = defineEmits(["close"]);

const props = defineProps({
  destination: {
    type: String,
    required: true,
  },
});

const router = useRouter();

const email = ref("");
const password = ref("");
const passwordError = ref("");

const handleCheck = async () => {
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
        if (props.destination === 'reset') {
          await userApi.logout();
          router.push({name: 'ResetPassword', query: {token: data}});
        } else if (props.destination === 'withdraw') {
          router.push({name: 'Withdraw'});
        } else {
          router.push({name: 'Main'});
        }
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
  <div class="modal-overlay">
    <div class="modal">
      <div class="close_button_container">
        <img alt="Close" class="close_button" src="../../assets/icons/close.svg" @click="$emit('close')"/>
      </div>
      <div class="container">
        <img alt="logo" src="/src/assets/images/logo.png"/>
        <h1>비밀번호 확인</h1>
        <form class="form_box" @submit.prevent="handleCheck">
          <input
            id="password"
            v-model="password"
            :class="['input_box', passwordError ? 'input_box--error' : '']"
            name="password"
            placeholder="기존 비밀번호를 입력해주세요."
            type="password"
          />
          <p v-if="passwordError" class="input-error">{{ passwordError }}</p>
          <button class="btn find_btn" type="button" @click="handleCheck">확인</button>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.close_button_container {
  align-items: center;
  justify-content: right;
  display: flex;
}

.close_button {
  top: 10px;
  right: 15px;
  background: none;
  border: none;
  font-size: 24px;
  font-weight: bold;
  cursor: pointer;
  color: #aaa;
  transition: color 0.2s ease;
}

.close-button:hover {
  color: #000;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 30px;
  border-radius: 10px;
  width: 400px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

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
  padding: 10px 10px;
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


.send_email_box > img {
  width: 60px;
  margin-bottom: 10px;
}

.input-error {
  color: #ff5555;
  font-size: 10px !important;
  margin-left: 10px;
  width: 100%;
  text-align: left;
}

.input_box--error {
  border: 1.5px solid #ff7b7b !important;
  background-color: #fff5f5;
  transition: border-color 0.2s;
}

</style>
