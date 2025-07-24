<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useJenkinsStore } from "@/stores/useJenkinsStore.js";
import { useRoute } from "vue-router";

const route = useRoute();
const jenkinsStore = useJenkinsStore();

const isEdit = ref(false);
const jenkinsInfoId = route.params.id;

const data = reactive({
  apiToken: '',
  description: '',
  id: '',
  jenkinsId: '',
  name: '',
  uri: ''
});

const originalData = reactive({});

onMounted(async () => {
  await jenkinsStore.getJenkinInfoDetail(jenkinsInfoId);
  Object.assign(data, jenkinsStore.jenkinsInfoDetail);
  Object.assign(originalData, jenkinsStore.jenkinsInfoDetail);
});

const enabledEdit = () => {
  isEdit.value = true;
};

const disableEdit = () => {
  Object.assign(data, originalData);
  isEdit.value = false;
};

async function saveEdit() {
  await jenkinsStore.updateJenkinsInfoDetail(data);
  Object.assign(originalData, data);
  isEdit.value = false;
}

async function deleteInfo() {
  await jenkinsStore.deleteJenkinsInfoDetail(jenkinsInfoId);
}

async function jenkinsURITest() {
  await jenkinsStore.jenkinsURITest(data.id);
}

</script>
<template>
  <div class="container">
    <div class="header">
      <input type="text" v-model="data.name" :readonly="!isEdit" class="name" :class="{ editing: isEdit }" />
      <div class="btn_box">
        <img src="/src/assets/icons/edit.svg" alt="edit" v-if="!isEdit" class="edit_btn" @click="enabledEdit" />

        <img src="/src/assets/icons/delete.svg" alt="delete" v-if="!isEdit" class="delete_btn" @click="deleteInfo" />

        <button v-if="isEdit" @click="disableEdit" class="cancel_btn">취소</button>
        <button v-if="isEdit" @click="saveEdit" class="save_btn">저장</button>
      </div>
    </div>
    <div class="info_box">
      <div class="row">
        <div class="label">설명</div>
        <div class="value">
          <textarea v-if="isEdit" v-model="data.description" class="input_text description editing" />
          <div v-else class="description readonly">
            {{ data.description }}
          </div>
        </div>
      </div>
      <div class="row">
        <div class="label">URI</div>
        <div class="value with_actions">
          <input type="text" v-model="data.uri" :readonly="!isEdit" class="input_text" :class="{ editing: isEdit }" />
          <div class="actions">
            <button class="test_btn" @click="jenkinsURITest" >테스트 요청 보내기</button>
            <img src="/src/assets/icons/check.svg" alt="icon" class="action_icon"  />
          </div>
        </div>
      </div>
      <div class="row">
        <div class="label">Secret Token</div>
        <div class="value">
          <div v-if="isEdit">••••••••••••••</div>
          <div v-else class="token">
            <img src="/src/assets/icons/check.svg" alt="icon" class="action_icon success" />
            등록 완료
          </div>
        </div>
      </div>
      <div class="row">
        <div class="label">ID</div>
        <input type="text" v-model="data.jenkinsId" :readonly="!isEdit" class="input_text" :class="{ editing: isEdit }" />
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

  &.editing {
    border-bottom: 1px solid var(--gray700);
    margin-right: 30px;
    padding-bottom: 6px;
  }
}

.btn_box {
  display: flex;
  gap: 6px;

  & > img {
    width: 21px;
    height: 21px;
    cursor: pointer;
  }

  & > img:hover {
    scale: 1.1;
  }

  & > button {
    background: none;
    border: none;
    cursor: pointer;
    white-space: nowrap;
    padding: 0;
  }
}

.cancel_btn {
  color: var(--gray500);
}

.save_btn {
  color: var(--main-color);
}

.info_box {
  width: 100%;
  background-color: white;
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

.token {
  display: flex;
  align-items: center;
  gap: 8px;
}

.success {
  color: green;
}

.input_text {
  background: none;
  border: none;
  outline: none;
  font-size: 16px;
  color: var(--gray900);

  &.editing {
    padding: 10px 17px;
    background-color: white;
    border: 1px solid var(--gray300);
    border-radius: 6px;
  }
}

.description {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 16px;
  color: var(--gray900);
  line-height: 140%;

  &.editing {
    width: 100%;
    min-height: 80px;
    resize: none;
    outline: none;
    padding: 10px 17px;
    background-color: white;
    background-color: white;
    border: 1px solid var(--gray300);
    border-radius: 6px;
  }

  &.readonly {
    padding: 10px 0;
  }
}

.with_actions {
  justify-content: space-between;
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action_icon {
  width: 20px;
  height: 20px;
}

.test_btn {
  background: none;
  border: none;
  border-radius: 4px;
  color: var(--main-color);
  cursor: pointer;
  white-space: nowrap;

  &:hover {
    font-weight: 700;
  }
}
</style>
