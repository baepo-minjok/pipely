<script setup>
const props = defineProps({
  form: Object,
});
</script>
<template>
  <div class="input_group">
    <label>이미지 태그</label>
    <input v-model="form.tag" placeholder="예: latest, 1.0.0" />

    <label>SSH 키 경로</label>
    <input v-model="form.sshKeyPath" placeholder="예: ~/.ssh/id_rsa" />

    <label>SSH 포트 번호</label>
    <input v-model="form.sshPort" placeholder="기본값: 22" />

    <label>배포 대상 서버</label>
    <input v-model="form.deployTarget" placeholder="예: user@192.168.0.1" />

    <label>YAML 경로</label>
    <input v-model="form.k8sPath" placeholder="예: /home/user/app/deploy.yaml" />

    <label>Deployment 이름</label>
    <input v-model="form.deploymentName" placeholder="예: my-app-deployment" />

    <label>네임스페이스</label>
    <input v-model="form.namespace" placeholder="예: default" />

    <label>앱 이름 (Label)</label>
    <input v-model="form.appName" placeholder="예: my-app" />

    <label>컨테이너 이름</label>
    <input v-model="form.containerName" placeholder="예: app-container" />

    <label>이미지 저장소</label>
    <input v-model="form.imageRepo" placeholder="예: ghcr.io/org/project" />

    <div class="row_group">
      <div class="flex_field">
        <label>컨테이너 포트 번호</label>
        <input v-model="form.port" placeholder="예: 8080" />
      </div>

      <div class="flex_field">
        <label>복제 수 (replicas)</label>
        <input v-model="form.replicas" placeholder="예: 3" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.input_group {
  display: flex;
  flex-direction: column;
  gap: 11px;
}

label {
  color: var(--gray800);
}

label:not(:first-of-type) {
  margin-top: 16px;
}

input {
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

.row_group {
  display: flex;
  gap: 20px;
  margin-top: 12px;
}

.flex_field {
  flex: 1;
  display: flex;
  flex-direction: column;

  & > label {
    margin-bottom: 11px;
  }
}
</style>
