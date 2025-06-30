<script setup>
import Header from '../../components/common/Header.vue';


</script>

<template>
  <div class="container">
    <Header></Header>
    <div class="wrapper">
      <div class="info_wrapper">
        <h3 class="title">기본 정보</h3>
        <div class="info_box">
          <div class="item_box">
            <p class="item_label">이메일</p>

            <div class="email_text">
              <p>test01@example.com</p>
              <img src="/src/assets/icons/check.svg" alt="check_icon">
            </div>
            <button class="info_btn">인증 메일 발송</button>

          </div>

          <div class="item_box">
            <p class="item_label">이름</p>
            <p>test01</p>
          </div> 
          <div class="item_box">
            <p class="item_label">비밀번호</p>
            <p>••••••••••••</p>
            <button class="info_btn">비밀번호 변경</button>
          </div> 
        </div>
      </div>
      <div class="cicd_box"></div>
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 100%;
  height: 100%;
}

.wrapper {
  width: 60%;
  margin: 70px auto;
}

.title {
  font-size: 22px;
  font-weight: 500;
  color: #4A5464;
  margin-bottom: 18px;
}

.info_box {
  background-color: white;
  border-radius: 12px;
  border: 1px solid var(--gray200);
  box-shadow: 2px 2px 4px 0px var(--gray400);
  padding: 25px 35px;
}

.item_box {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 4fr 1fr;
  grid-row-gap: 20px;
  align-items: center;
  padding: 0 10px;
  box-sizing: border-box;

}

.item_box:not(:last-of-type) {
  border-bottom: 1px solid var(--gray200);
  padding-bottom: 22px; /* 아래 여백 */
  margin-bottom: 22px;  /* 선과 다음 요소 사이 여백 */
}

.item_label {
  color: var(--gray500);
  white-space: nowrap;
}


.email_text {
  display: flex;
  align-items: center;
  gap: 5px;
}

.info_btn {
  background: none;
  color: var(--main-color);
  border: none;
  white-space: nowrap;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

</style>