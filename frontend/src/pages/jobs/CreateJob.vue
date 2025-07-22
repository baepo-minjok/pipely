<script setup>
import { reactive, ref } from 'vue';
import KubernetesInput from '../../components/jobs/KubernetesInput.vue';
import EC2Input from '../../components/jobs/EC2Input.vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const jenkinsInfo = {
  name: route.query.jenkinsName,
  uri: route.query.jenkinsUri,
};

const isGithubChecked = ref(false);
const isWebhookChecked = ref(false);
const openDropdown = ref(false);

const linkData = reactive({
  githubUrl: '',
  webhookUrl: '',
});

const cicdItems = [
  {
    label: 'Kubernetes',
    value: 'k8s',
    image: '/src/assets/images/k8s.png',
  },
  {
    label: 'EC2',
    value: 'ec2',
    image: '/src/assets/images/ec2.png',
  },
];
const selectedItem = ref(cicdItems[0]);

const scriptData = reactive({
  scriptId: '',
  githubUrl: '',
  branch: 'main',
  isBuildSelected: false,
  isTestSelected: false,

  isK8sDeploy: true,
  isEc2Deploy: false,

  sshKeyPath: '',
  sshPort: '',
  deployTarget: '',
  tag: '',
  k8sPath: '',
  deploymentName: '',
  namespace: '',
  appName: '',
  containerName: '',
  imageRepo: '',
  port: '',
  replicas: '',

  ec2DeployPath: '',
});

const scriptText = ref('');

const toggleDropdown = () => {
  openDropdown.value = !openDropdown.value;
};

const selectItem = (item) => {
  selectedItem.value = item;
  openDropdown.value = false;

  scriptData.isK8sDeploy = item.value === 'k8s';
  scriptData.isEc2Deploy = item.value === 'ec2';
};

const handleCreateScriptClick = () => {
  // 스크립트 생성 버튼 클릭
};
</script>

<template>
  <div class="container">
    <h1>새 Job 생성</h1>
    <div class="body">
      <div class="jenkins_box">
        <h3 class="sub_title">Jenkins 인스턴스 정보</h3>
        <div>
          <p><span>이름 </span>{{ jenkinsInfo.name }}</p>
          <p><span>URI </span> {{ jenkinsInfo.uri }}</p>
        </div>
      </div>
      <div class="info_box">
        <h3 class="sub_title">Job 기본 정보</h3>
        <input type="text" id="name" class="input" placeholder="Job 이름을 입력해주세요." />
        <textarea
          name="description"
          id="description"
          class="textarea"
          placeholder="Job에 대한 설명을 입력해주세요."
        ></textarea>
      </div>

      <div class="link_box">
        <h3 class="sub_title">연동 설정</h3>
        <div class="checkbox">
          <input type="checkbox" v-model="isGithubChecked" name="github_check" id="github_check" />
          <span>Github 연동</span>
        </div>
        <div v-if="isGithubChecked" class="checkbox_input">
          <div class="col_line"></div>
          <input
            type="text"
            v-model="linkData.githubUrl"
            placeholder="Github 프로젝트 주소를 입력해주세요."
            class="input"
          />
        </div>
        <div class="checkbox">
          <input type="checkbox" v-model="isWebhookChecked" name="webhook_check" id="webhook_check" />
          <span>Webhook 연동</span>
        </div>
        <div v-if="isWebhookChecked" class="checkbox_input">
          <div class="col_line"></div>
          <input type="text" v-model="linkData.webhookUrl" placeholder="Webhook 링크를 입력해주세요." class="input" />
        </div>
      </div>
      <div class="script_box">
        <h3 class="sub_title">스크립트</h3>
        <div>
          <label for="github_url">Github 주소</label>
          <input
            type="text"
            v-model="scriptData.githubUrl"
            id="github_url"
            class="input"
            placeholder="Github 프로젝트 주소를 입력해주세요."
          />
        </div>
        <div>
          <label for="branch">Git Branch</label>
          <input type="text" v-model="scriptData.branch" id="branch" class="input" />
        </div>
        <div class="stage_box">
          <label for="stage">스테이지 선택</label>
          <div class="stage_group">
            <label
              ><input type="checkbox" v-model="scriptData.isBuildSelected" />
              Build
            </label>
            <label>
              <input type="checkbox" v-model="scriptData.isTestSelected" />
              Test
            </label>
            <label>
              <input type="checkbox" v-model="scriptData.isDeploySelected" />
              Deploy
            </label>
          </div>
          <div v-if="scriptData.isDeploySelected" class="deploy_section">
            <!-- ✅ 커스텀 드롭다운 -->
            <div class="dropdown_container">
              <button @click="toggleDropdown" class="dropdown">
                <div>
                  <img :src="selectedItem.image" alt="icon" class="dropdown_img" />
                  <span class="dropdown_label">{{ selectedItem.label }}</span>
                </div>
                <img src="/src/assets/icons/down_arrow.svg" alt="down_icon" class="dropdown_arrow" />
              </button>

              <div v-if="openDropdown" class="dropdown_menu">
                <ul>
                  <li v-for="(item, index) in cicdItems" :key="index" @click="selectItem(item)" class="dropdown_item">
                    <img :src="item.image" alt="icon" class="dropdown_img" />
                    <span>{{ item.label }}</span>
                  </li>
                </ul>
              </div>
            </div>

            <!-- ✅ 선택된 배포 유형에 따라 컴포넌트 표시 -->
            <KubernetesInput v-if="scriptData.isK8sDeploy" :form="scriptData" />
            <EC2Input v-if="scriptData.isEc2Deploy" :form="scriptData" />
          </div>
        </div>
        <button class="create_script_btn" @click="handleCreateScriptClick">스크립트 생성</button>
        <textarea name="script" id="script" v-model="scriptText" class="script"></textarea>
      </div>
    </div>
    <div class="btn_box">
      <button class="cancel_btn">취소</button>
      <button class="create_btn">생성</button>
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 60%;
  margin: 70px auto;
}

.container > h1 {
  font-size: 28px;
  margin-bottom: 6px;
}

.body > div {
  display: flex;
  flex-direction: column;
  padding: 34px 22px;
  border-bottom: 1px solid var(--gray200);

  &:last-child {
    border-bottom: none;
  }
}

.jenkins_box > div {
  display: flex;
  flex-direction: column;
  gap: 13px;
  border: 1px solid var(--gray200);
  border-radius: 8px;
  margin-top: 20px;
  padding: 24px 20px;
  background-color: white;

  & span {
    color: var(--gray500);
    font-size: 14px;
    margin-right: 16px;
  }
}

.info_box {
  gap: 14px;
}

.link_box {
  gap: 17px;
}

.script_box {
  gap: 27px;
}

.sub_title {
  font-size: 20px;
}

.input {
  width: 100%;
  background-color: white;
  border-radius: 8px;
  border: 1px solid var(--gray200);
  padding: 13px 16px;
  box-sizing: border-box;
  font-size: 16px;

  &::placeholder {
    color: var(--gray400);
  }
}

.textarea {
  height: 100px;
  resize: none;
  background-color: white;
  border: 1px solid var(--gray200);
  border-radius: 8px;
  padding: 15px 17px;
  font-size: 16px;

  &::placeholder {
    color: var(--gray400);
  }
}

input[type='checkbox'] {
  width: 17px;
  height: 17px;
  accent-color: var(--main-color);
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 5px;
}

.checkbox_input {
  display: flex;
  border-left: 1px solid var(--gray300);
  margin-left: 10px;
  padding-left: 15px;
}

label {
  display: block;
  margin-bottom: 10px;
  color: var(--gray700);
}

.deploy_section {
  border-left: 1px solid var(--gray300);
  margin-left: 10px;
  padding: 8px 0 8px 20px;
}

.stage_group > label {
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 배포 선택 드롭다운 */
.dropdown_container {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.dropdown {
  width: 180px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border: 1px solid var(--gray200);
  padding: 8px 12px;
  cursor: pointer;
  font-size: 16px;
}

.dropdown > div {
  display: flex;
  align-items: center;
}

.dropdown_img {
  width: 20px;
  height: 20px;
  margin-right: 6px;
}

.dropdown_label {
  white-space: nowrap;
}

.dropdown_arrow {
  width: 16px;
  height: 16px;
}

.dropdown_menu {
  position: absolute;
  top: 100%;
  left: 0;
  min-width: 100%;
  background: white;
  border: 1px solid var(--gray200);
  margin-top: 2px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  z-index: 10;
  box-sizing: border-box;
}

.dropdown_item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
}

.dropdown_item:hover {
  background: #f0f0f0;
}

/* 스크립트 생성 */
.create_script_btn {
  width: 126px;
  height: 32px;
  background-color: var(--main-color);
  border-radius: 99px;
  border: none;
  color: white;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background-color: var(--main-color-hover);
  }
}

.script {
  background-color: #f3f4f6;
  resize: none;
  height: 222px;
  border: none;
  outline: none;
  padding: 20px;
}
</style>
