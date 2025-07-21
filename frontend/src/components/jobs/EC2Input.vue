<script setup>
const props = defineProps({
  form: Object,
});
</script>

<template>
  <div class="input_group">
    <label>EC2 JAR 경로</label>
    <input v-model="form.ec2DeployPath" placeholder="예: /home/ec2-user/app" />
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
</style>
