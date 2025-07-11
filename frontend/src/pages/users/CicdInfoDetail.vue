<script setup></script>

<template>
  <div class="container">
    <div class="header">
      <input type="text" value="젠킨스 01" class="name" />
      <div class="btn_box">
        <img src="/src/assets/icons/edit.svg" alt="edit" class="edit_btn" />
        <img src="/src/assets/icons/delete.svg" alt="delete" class="delete_btn" />
      </div>
    </div>
    <div class="info_box">
      <div class="row url-row">
        <div class="label">URL</div>
        <div class="value with-actions">
          <span class="url-text">https://jenkins.io</span>
          <div class="actions">
            <button class="test-button">테스트 요청 보내기</button>
            <img src="/src/assets/icons/check.svg" alt="icon" class="action-icon" />
          </div>
        </div>
      </div>
      <div class="row">
        <div class="label">Secret Token</div>
        <div class="value success">
          <img src="/src/assets/icons/check.svg" alt="icon" class="action-icon" />
          등록 완료
        </div>
      </div>
      <div class="row">
        <div class="label">ID</div>
        <div class="value">test01</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 60%;
  margin: 100px auto;
}

.header {
  display: flex;
}

.name {
  width: 100%;
  background: none;
  border: none;
  font-size: 22px;
  font-weight: 700;
  outline: none;
}

.btn_box {
  display: flex;
  gap: 6px;

  & > img {
    width: 21px;
    height: 21px;
  }
}

.info_box {
  width: 100%;
  box-shadow: 0px 4px 4px 0px var(--gray300);
  display: grid;
  grid-template-rows: auto auto auto;
  gap: 35px;
  padding: 16px;
  border: 1px solid var(--gray200);
  border-radius: 8px;
  box-sizing: border-box;
  margin-top: 19px;
  padding: 25px;
}

.row {
  display: grid;
  grid-template-columns: 100px 1fr;
  align-items: center;
  gap: 80px;
}

.label {
  color: var(--gray500);
}

.value {
  color: var(--gray900);
  display: flex;
  align-items: center;
  gap: 8px;
}

.value.success {
  color: green;
}

.with-actions {
  justify-content: space-between;
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-icon {
  width: 20px;
  height: 20px;
}

.test-button {
  background: none;
  border: none;
  border-radius: 4px;
  color: var(--main-color);
  cursor: pointer;
}
</style>
