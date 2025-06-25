<script setup>


</script>

<template>
  <div class="container">
    <div class="left_wrapper">
      <h1>Sign In</h1>
      <div class="login_box">
        <button class="oauth_btn">
          <img src="/src/assets/images/google_logo.png" alt="google" />
          Google로 로그인
        </button>
        <button class="oauth_btn">
          <img src="/src/assets/images/github_logo.png" alt="github" />
          Github로 로그인
        </button>
        <p>또는</p>
        <input type="text" class="input_box" placeholder="이메일 주소를 입력해주세요." />
        <input type="password" class="input_box" placeholder="비밀번호를 입력해주세요." />
        <button class="btn login_btn">로그인</button>

        <div class="bottom_box">
          <router-link to='signup'>회원가입</router-link>
          <div class="col_line"></div>
          <router-link to="find/password">비밀번호 찾기</router-link>
        </div>
      </div>
    </div>
    <div class="right_wrapper">
      <img src="/src/assets/images/logo.png" alt="logo" />
      <p class="text">
        이제 복잡한 배포는 그만! <br />
        AI가 함께 하는 간편한 CI/CD를 경험해보세요.
      </p>

      <div class="chat_box">
        <div class="send_box">빌드 스크립트 작성해주세요.</div>
        <div class="rec_box">요청하신 빌드 스크립트입니다.</div>
        <div class="rec_box">
          <pre>
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/your-repo/project.git'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew build'
            }
        }
    }
    ...
}
    </pre>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  width: 100%;
  height: 100%;
}

/* 왼쪽 */
.left_wrapper {
  width: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 40px;
}

.left_wrapper>h1 {
  font-size: 35px;
}

.login_box {
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

.login_box>p {
  font-size: 14px;
  margin: 6px 0;
}

.oauth_btn {
  font-size: 14.5px;
  border-radius: 10px;
  border: 1px solid var(--gray300);
  padding: 10px;
  width: 100%;
  background-color: white;
  cursor: pointer;
  display: flex;
  justify-content: center;
  gap: 5px;
  align-items: center;
  transition: all 0.3s;
}

.oauth_btn:hover {
  background-color: var(--main-color-bg);
}

.oauth_btn>img {
  width: 16px;
  height: 16px;
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

.login_btn {
  width: 100%;
}

.bottom_box {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.col_line {
  width: 1px;
  height: 100%;
  background-color: var(--gray300);
}

.bottom_box>a {
  font-size: 12px;
  text-decoration: none;
  color: var(--gray600);
  white-space: nowrap;
}

/* 오른쪽  */
.right_wrapper {
  width: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: var(--main-color-bg);
}

.right_wrapper>img {
  width: 200px;
  margin: 20px 0;
}

.right_wrapper>.text {
  text-align: center;
  line-height: 160%;
  margin-bottom: 40px;
}

.chat_box div {
  border-radius: 10px;
  padding: 11px 16px;
  width: fit-content;
  font-size: 14px;
  margin-bottom: 15px;
  max-width: 400px;
}

.chat_box pre {
  font-family: Consolas, 'Courier New', monospace;
  font-size: 11px;
}

.send_box {
  background-color: var(--chat-send);
  justify-self: right;
}

.rec_box {
  background-color: white;
}
</style>
