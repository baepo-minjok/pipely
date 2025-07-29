<script setup>
import {onMounted, ref} from 'vue'
import {jobApi} from '@/api/JobApi.js'

// Props & Emits
const props = defineProps(['infoId']);
const emit = defineEmits(['close', 'restored']);

// Reactive data
const loading = ref(true);
const deletedJobs = ref([]);
const restoring = ref(null);
const permanentDeleting = ref(null);

// Methods
const fetchDeletedJobs = async () => {
  try {
    loading.value = true;
    // API 호출 (실제 API에 맞게 수정 필요)
    const response = await jobApi.getDeletedJobList(props.infoId);
    deletedJobs.value = response || [];
  } catch (error) {
    console.error('삭제된 Job 목록 조회 실패:', error);
    deletedJobs.value = [];
  } finally {
    loading.value = false;
  }
}

const handleRestore = async (job) => {
  if (!confirm(`"${job.name}" Job을 복원하시겠습니까?`)) {
    return;
  }

  try {
    restoring.value = job.pipelineId;
    // API 호출 (실제 API에 맞게 수정 필요)
    await jobApi.restoreJob(job.pipelineId);
    // 목록에서 제거
    deletedJobs.value = deletedJobs.value.filter(j => j.pipelineId !== job.pipelineId);

    alert('Job이 성공적으로 복원되었습니다.');
    emit('restored', job);
  } catch (error) {
    alert('Job 복원에 실패했습니다.');
  } finally {
    restoring.value = null;
  }
}

const handlePermanentDelete = async (job) => {
  if (!confirm(`"${job.name}" Job을 영구적으로 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.`)) {
    return;
  }

  try {
    permanentDeleting.value = job.pipelineId;
    // API 호출 (실제 API에 맞게 수정 필요)
    await jobApi.hardDeleteJob(job.pipelineId);

    // 목록에서 제거
    deletedJobs.value = deletedJobs.value.filter(j => j.pipelineId !== job.pipelineId);

    alert('Job이 영구적으로 삭제되었습니다.')
  } catch (error) {
    console.error('Job 영구 삭제 실패:', error);
    alert('Job 영구 삭제에 실패했습니다.');
  } finally {
    permanentDeleting.value = null;
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', {
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// Lifecycle
onMounted(() => {
  fetchDeletedJobs();
})
</script>

<template>
  <div class="deleted-jobs-container">
    <!-- 헤더 -->
    <div class="modal-header">
      <div class="modal-title-section">
        <div class="modal-icon">
          <svg fill="none" height="24" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="24">
            <path d="M3 6h18"/>
            <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/>
            <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>
            <line x1="10" x2="10" y1="11" y2="17"/>
            <line x1="14" x2="14" y1="11" y2="17"/>
          </svg>
        </div>
        <h3 class="modal-title">삭제된 Job 목록</h3>
      </div>
      <button class="modal-close-btn" @click="$emit('close')">
        <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
          <line x1="18" x2="6" y1="6" y2="18"/>
          <line x1="6" x2="18" y1="6" y2="18"/>
        </svg>
      </button>
    </div>

    <!-- 컨텐츠 -->
    <div class="modal-content">
      <div v-if="loading" class="loading-state">
        <svg class="animate-spin loading-icon" fill="none" height="32" stroke="currentColor" stroke-width="2"
             viewBox="0 0 24 24" width="32">
          <path d="M21 12a9 9 0 11-6.219-8.56"/>
        </svg>
        <p>삭제된 Job 목록을 불러오는 중...</p>
      </div>

      <div v-else-if="deletedJobs.length === 0" class="empty-state">
        <svg class="empty-icon" fill="none" height="48" stroke="currentColor" stroke-width="1" viewBox="0 0 24 24"
             width="48">
          <path d="M3 6h18"/>
          <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/>
          <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>
        </svg>
        <h4 class="empty-title">삭제된 Job이 없습니다</h4>
        <p class="empty-description">현재 삭제된 Job이 없습니다.</p>
      </div>

      <div v-else class="deleted-jobs-list">
        <div class="list-header">
          <span class="job-count">총 {{ deletedJobs.length }}개의 삭제된 Job</span>
        </div>

        <div class="jobs-grid">
          <div
              v-for="job in deletedJobs"
              :key="job.id"
              class="deleted-job-card"
          >
            <div class="job-info">
              <div class="job-header">
                <h4 class="job-name">{{ job.name }}</h4>
              </div>

              <div class="job-details">
                <div class="detail-item">
                  <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                    <circle cx="12" cy="12" r="10"/>
                    <polyline points="12,6 12,12 16,14"/>
                  </svg>
                  <span>삭제일: {{ formatDate(job.deletedAt) }}</span>
                </div>

                <div v-if="job.description" class="detail-item">
                  <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14,2 14,8 20,8"/>
                  </svg>
                  <span>{{ job.description }}</span>
                </div>
              </div>
            </div>

            <div class="job-actions">
              <button
                  :disabled="restoring === job.pipelineId"
                  class="btn btn-secondary btn-sm"
                  @click="handleRestore(job)"
              >
                <svg v-if="restoring === job.id" class="animate-spin" fill="none" height="16" stroke="currentColor"
                     stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M21 12a9 9 0 11-6.219-8.56"/>
                </svg>
                <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                     width="16">
                  <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
                  <path d="M21 3v5h-5"/>
                  <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
                  <path d="M3 21v-5h5"/>
                </svg>
                {{ restoring === job.id ? '복원 중...' : '복원' }}
              </button>

              <button
                  :disabled="permanentDeleting === job.id"
                  class="btn btn-danger btn-sm"
                  @click="handlePermanentDelete(job)"
              >
                <svg v-if="permanentDeleting === job.id" class="animate-spin" fill="none" height="16"
                     stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M21 12a9 9 0 11-6.219-8.56"/>
                </svg>
                <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                     width="16">
                  <path d="M3 6h18"/>
                  <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/>
                  <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>
                </svg>
                {{ permanentDeleting === job.id ? '삭제 중...' : '영구 삭제' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.deleted-jobs-container {
  width: 100%;
  max-width: 800px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

/* 헤더 */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 0 24px;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 24px;
  padding-bottom: 20px;
}

.modal-title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.modal-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #fee2e2;
  border-radius: 10px;
  color: #dc2626;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.modal-close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: #f1f5f9;
  border: none;
  border-radius: 8px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.modal-close-btn:hover {
  background: #e2e8f0;
  color: #374151;
  transform: scale(1.05);
}

/* 컨텐츠 */
.modal-content {
  padding: 0 24px 24px;
  flex: 1;
  overflow-y: auto;
}

/* 로딩 상태 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-icon {
  color: #2563eb;
  margin-bottom: 16px;
}

.loading-state p {
  color: #6b7280;
  font-size: 16px;
  margin: 0;
}

/* 빈 상태 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  color: #9ca3af;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.empty-description {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}

/* 목록 */
.deleted-jobs-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.job-count {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.jobs-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Job 카드 */
.deleted-job-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.deleted-job-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.job-info {
  flex: 1;
}

.job-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.job-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.job-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
}

.job-status.deleted {
  background: #fee2e2;
  color: #dc2626;
}

.job-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #64748b;
}

.detail-item svg {
  color: #9ca3af;
  flex-shrink: 0;
}

/* 액션 버튼 */
.job-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
}

.btn-secondary {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #cbd5e1;
}

.btn-secondary:hover:not(:disabled) {
  background: #e2e8f0;
  border-color: #94a3b8;
}

.btn-danger {
  background: #dc2626;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #b91c1c;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.3);
}

/* 애니메이션 */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

/* 반응형 */
@media (max-width: 640px) {
  .deleted-job-card {
    flex-direction: column;
    gap: 16px;
  }

  .job-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .btn {
    flex: 1;
  }
}
</style>