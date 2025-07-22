<script setup>
import {onMounted, ref, watch} from 'vue';
import {useRouter} from "vue-router";
import {userApi} from "@/api/UserApi.js";

const router = useRouter();

// form 객체
const name = ref("");
const password = ref("");
const phoneValue = ref("");
const passwordCheck = ref("");

// 에러메시지
const nameError = ref("");
const passwordError = ref("");
const phoneError = ref("");
const passwordCheckError = ref("");

// 로그인 UI 관리
const isLoading = ref(false);

const handlePress = (e) => {
  let numbersOnly = e.target.value.replace(/\D/g, '');

  numbersOnly = numbersOnly.slice(0, 11);

  if (numbersOnly.length <= 3) {
    phoneValue.value = numbersOnly;
  } else if (numbersOnly.length <= 7) {
    phoneValue.value = numbersOnly.replace(/(\d{3})(\d+)/, '$1-$2');
  } else {
    phoneValue.value = numbersOnly.replace(/(\d{3})(\d{4})(\d+)/, '$1-$2-$3');
  }

  e.target.value = phoneValue.value;
};

watch(phoneValue, (newVal, _oldVal) => {
  if (newVal.length === 10) {
    phoneValue.value = newVal.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
  } else if (newVal.length === 11) {
    phoneValue.value = newVal.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
  }
});

function isValidPassword(pw) {
  // 10자 이상, 대문자 1개 이상, 특수문자 1개 이상
  return /^(?=.*[A-Z])(?=.*[!@#$%^&*()_\-+=\[\]{};':"\\|,.<>\/?]).{10,}$/.test(pw);
}

const signUp = async () => {
  // 에러 초기화
  nameError.value = "";
  passwordError.value = "";
  phoneError.value = "";
  passwordCheckError.value = "";

  let valid = true;

  // 이름
  if (!name.value.trim()) {
    nameError.value = "이름을 입력해주세요.";
    valid = false;
  }

  // 비밀번호
  if (!password.value) {
    passwordError.value = "비밀번호를 입력해주세요.";
    valid = false;
  } else if (!isValidPassword(password.value)) {
    passwordError.value = "비밀번호는 10자 이상, 대문자와 특수문자를 각각 1개 이상 포함해야 합니다.";
    valid = false;
  }

  // 비밀번호 확인
  if (!passwordCheck.value) {
    passwordCheckError.value = "비밀번호 확인을 입력해주세요.";
    valid = false;
  } else if (passwordCheck.value !== password.value) {
    passwordCheckError.value = "비밀번호가 일치하지 않습니다.";
    valid = false;
  }

  // 전화번호
  const onlyNumberPhone = phoneValue.value.replace(/\D/g, "");
  if (!onlyNumberPhone) {
    phoneError.value = "전화번호를 입력해주세요.";
    valid = false;
  } else if (onlyNumberPhone.length < 10 || onlyNumberPhone.length > 11) {
    phoneError.value = "전화번호를 정확히 입력해주세요.";
    valid = false;
  }

  if (!valid) return;

  isLoading.value = true;

  const signUpRequest = {
    name: name.value,
    password: password.value,
    phoneNumber: phoneValue.value,
  };

  const response = await userApi.oAuthSignup(signUpRequest);

  if (response.status === 200) {
    alert("회원가입 성공!\n로그인 해주세요!");
    if (window.opener && !window.opener.closed) {
      // 부모 창이 열려 있으면 로그인 페이지로 이동
      window.opener.location.href = "/user/login";
      window.close();
    } else {
      router.push({name: "Login"});
    }

  } else {
    alert("회원가입 실패!");
    name.value = "";
    phoneValue.value = "";
    password.value = "";
    passwordCheck.value = "";

    if (window.opener && !window.opener.closed) {
      window.close();
    } else {
      router.push({name: "Signup"});
    }
  }
};
onMounted(() => {
  const isOk = confirm("계정이 없습니다. 회원가입하시겠습니까?");
  if (!isOk) {
    window.close();
  }
  // isOk가 true면 이후 로직 진행
});
</script>

<template>
  <div class="container">

    <div class="right_wrapper">

      <img alt="logo" src="/src/assets/images/logo.png"/>
      <form class="signup_box" @submit.prevent="signUp">
        <input id="name" v-model="name"
               :class="['input_box', nameError ? 'input_box--error' : '']" name="name" placeholder="이름을 입력해주세요."/>
        <p v-if="nameError" class="input-error">{{ nameError }}</p>

        <input id="password" v-model="password"
               :class="['input_box', passwordError ? 'input_box--error' : '']" name="password"
               placeholder="비밀번호를 입력해주세요.(10자리 이상, 대문자 1개, 특수문자 1개 포함)"
               type="password"/>
        <p v-if="passwordError" class="input-error">{{ passwordError }}</p>

        <input
            id="password_check"
            v-model="passwordCheck"
            :class="['input_box', passwordCheckError ? 'input_box--error' : '']"
            name="password_check"
            placeholder="비밀번호를 다시 입력해주세요."
            type="password"
        />
        <p v-if="passwordCheckError" class="input-error">{{ passwordCheckError }}</p>

        <input
            id="phone"
            v-model="phoneValue"
            :class="['input_box', phoneError ? 'input_box--error' : '']"
            maxlength="13"
            name="phone"
            placeholder="010-1234-5678"
            type="tel"
            @input="handlePress"
        />
        <p v-if="phoneError" class="input-error">{{ phoneError }}</p>

        <button :disabled="isLoading" class="btn signup_btn" type="submit">
          {{ isLoading ? '회원가입중..' : '회원가입' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  width: 100%;
  height: 100%;
}

.input-error {
  color: #ff5555;
  font-size: 10px;
  margin-top: 2px;
  margin-bottom: 2px;
  width: 100%;
  text-align: left;
}

.input-success {
  color: #0f8713;
  font-size: 10px;
  margin-top: 2px;
  margin-bottom: 2px;
  width: 100%;
  text-align: left;
}

.btn[disabled] {
  color: #bbb;
  cursor: not-allowed;
}

.input_box--error {
  border: 1.5px solid #ff7b7b !important;
  background-color: #fff5f5;
  transition: border-color 0.2s;
}

.input_box--success {
  border: 1.5px solid #0f8713 !important;
  background-color: #e7ffe5;
  transition: border-color 0.2s;
}

.left_wrapper > img {
  width: 200px;
  margin: 20px 0;
}

.left_wrapper > h3 {
  font-weight: 700;
}

.left_wrapper > p {
  line-height: 140%;
  margin-top: 10px;
  text-align: center;
}

/* 오른쪽  */
.right_wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 30px;
}

.right_wrapper > h1 {
  font-size: 30px;
}

.signup_box {
  width: 45%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 35px;
  border-radius: 10px;
  border: 1px solid var(--gray300);
  gap: 10px;
}

.signup_box > p {
  font-size: 14px;
  margin: 6px 0;
}

.input_box {
  border-radius: 10px;
  border: 1px solid var(--gray300);
  padding: 12px;
  width: 100%;
  outline: none;
  box-sizing: border-box;
}

.input_box::placeholder {
  color: var(--gray400);
}

.signup_btn {
  width: 100%;
}
</style>
