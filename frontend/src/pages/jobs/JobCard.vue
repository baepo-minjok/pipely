<script setup>
import {computed, toRefs} from 'vue';
import {formatDateTime} from '@/utils/formatDateTime.js';

const props = defineProps({
  job: {
    type: Object,
    required: true
  },
  openDropdownJob: String
});

const {job, openDropdownJob} = toRefs(props);

// 현재 카드의 드롭다운이 열려있는지 확인
const isDropdownOpen = computed(() => openDropdownJob.value === job.value.name);

const emit = defineEmits(['action', 'delete', 'saveSnapshot', 'viewSnapshots', 'toggleDropdown']);

const handleDeleteJob = () => {
  emit('delete', job.value);
};

const handleSaveSnapshot = () => {
  emit('saveSnapshot', job.value);
};

const handleViewSnapshots = () => {
  emit('viewSnapshots', job.value);
};

const handleToggleDropdown = () => {
  emit('toggleDropdown', job.value);
};

const getStatusClass = (state) => {
  switch (state) {
    case 'BUILD_SUCCESS':
      return 'status-success';
    case 'BUILD_FAILURE':
      return 'status-failed';
    case 'BUILD_RUNNING':
      return 'status-running';
    default:
      return 'status-pending';
  }
};

const getStatusText = (state) => {
  switch (state) {
    case 'BUILD_SUCCESS':
      return '성공';
    case 'BUILD_FAILURE':
      return '실패';
    case 'BUILD_RUNNING':
      return '실행 중';
    default:
      return '대기';
  }
};

const getButtonClass = (state) => {
  switch (state) {
    case 'BUILD_SUCCESS':
      return 'btn-start';
    case 'BUILD_FAILURE':
      return 'btn-retry';
    case 'BUILD_RUNNING':
      return 'btn-stop';
    default:
      return 'btn-start';
  }
};

const getButtonText = (state) => {
  switch (state) {
    case 'BUILD_SUCCESS':
      return '실행';
    case 'BUILD_FAILURE':
      return '재시도';
    case 'BUILD_RUNNING':
      return '중지';
    default:
      return '실행';
  }
};

const handleActionClick = () => {
  emit('action', job.value);
};
</script>

<template>
  <div :class="{ 'dropdown-open': isDropdownOpen }" class="job-card" @click.stop>
    <!-- 카드 헤더 -->
    <div class="card-header">
      <div class="job-info">
        <h3 class="job-title">{{ job.name }}</h3>
        <div class="job-meta">
          <span class="meta-item">
            <svg class="meta-icon" fill="none" height="14" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                 width="14">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12,6 12,12 16,14"/>
            </svg>
            {{ formatDateTime(job.lastExe) }}
          </span>
        </div>
      </div>

      <div :class="getStatusClass(job.buildState)" class="status-badge">
        <svg v-if="job.buildState === 'BUILD_SUCCESS'" class="status-icon" fill="none" height="16" stroke="currentColor"
             stroke-width="2" viewBox="0 0 24 24" width="16">
          <polyline points="20,6 9,17 4,12"/>
        </svg>
        <svg v-else-if="job.buildState === 'BUILD_FAILURE'" class="status-icon" fill="none" height="16"
             stroke="currentColor"
             stroke-width="2" viewBox="0 0 24 24" width="16">
          <line x1="18" x2="6" y1="6" y2="18"/>
          <line x1="6" x2="18" y1="6" y2="18"/>
        </svg>
        <svg v-else-if="job.buildState === 'BUILD_RUNNING'" class="status-icon animate-spin" fill="none" height="16"
             stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
          <path d="M21 12a9 9 0 11-6.219-8.56"/>
        </svg>
        <svg v-else class="status-icon" fill="none" height="16" stroke="currentColor" stroke-width="2"
             viewBox="0 0 24 24"
             width="16">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12,6 12,12 16,14"/>
        </svg>
        <span class="status-text">{{ getStatusText(job.buildState) }}</span>
      </div>
    </div>

    <!-- 설명 -->
    <div class="card-content">
      <p class="job-description">
        <svg class="description-icon" fill="none" height="14" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
             width="14">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14,2 14,8 20,8"/>
          <line x1="16" x2="8" y1="13" y2="13"/>
          <line x1="16" x2="8" y1="17" y2="17"/>
          <polyline points="10,9 9,9 8,9"/>
        </svg>
        {{ job.description || '설명이 없습니다.' }}
      </p>
    </div>

    <!-- 스테이지 표시 (있는 경우) -->
    <div v-if="job.stages && job.stages.length > 0" class="stages-container">
      <div class="stages-header">
        <svg class="stages-icon" fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
             width="16">
          <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
        </svg>
        <span class="stages-title">스테이지</span>
      </div>
      <div class="stages-list">
        <div
            v-for="(stage, index) in job.stages"
            :key="index"
            :class="getStatusClass(stage.state)"
            class="stage-item"
        >
          <div class="stage-indicator">
            <svg v-if="stage.state === 'BUILD_SUCCESS'" class="stage-icon" fill="none" height="12" stroke="currentColor"
                 stroke-width="2" viewBox="0 0 24 24" width="12">
              <polyline points="20,6 9,17 4,12"/>
            </svg>
            <svg v-else-if="stage.state === 'BUILD_FAILURE'" class="stage-icon" fill="none" height="12"
                 stroke="currentColor"
                 stroke-width="2" viewBox="0 0 24 24" width="12">
              <line x1="18" x2="6" y1="6" y2="18"/>
              <line x1="6" x2="18" y1="6" y2="18"/>
            </svg>
            <svg v-else-if="stage.state === 'BUILD_RUNNING'" class="stage-icon animate-spin" fill="none" height="12"
                 stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="12">
              <path d="M21 12a9 9 0 11-6.219-8.56"/>
            </svg>
            <div v-else class="stage-dot"></div>
          </div>
          <span class="stage-name">{{ stage.type }}</span>
        </div>
      </div>
    </div>

    <!-- 액션 버튼 -->
    <div class="card-footer">
      <button
          :class="getButtonClass(job.buildState)"
          class="action-btn"
          @click.stop="handleActionClick"
      >
        <svg v-if="job.buildState === 'BUILD_SUCCESS'" class="btn-icon" fill="currentColor" height="16"
             stroke="currentColor"
             stroke-width="1" viewBox="0 0 24 24" width="16" xmlns="http://www.w3.org/2000/svg"><title>replay</title>
          <path
              d="M12,5V1L7,6L12,11V7A6,6 0 0,1 18,13A6,6 0 0,1 12,19A6,6 0 0,1 6,13H4A8,8 0 0,0 12,21A8,8 0 0,0 20,13A8,8 0 0,0 12,5Z"/>
        </svg>
        <svg v-else-if="job.buildState === 'BUILD_FAILURE'" class="btn-icon" fill="currentColor" height="16"
             stroke="currentColor"
             stroke-width="1" viewBox="0 0 24 24" width="16" xmlns="http://www.w3.org/2000/svg"><title>replay</title>
          <path
              d="M12,5V1L7,6L12,11V7A6,6 0 0,1 18,13A6,6 0 0,1 12,19A6,6 0 0,1 6,13H4A8,8 0 0,0 12,21A8,8 0 0,0 20,13A8,8 0 0,0 12,5Z"/>
        </svg>
        <svg v-else-if="job.buildState === 'BUILD_RUNNING'" class="btn-icon" fill="none" height="16"
             stroke="currentColor"
             stroke-width="2" viewBox="0 0 24 24" width="16">
          <rect height="10" rx="1" ry="1" width="4" x="6" y="7"/>
          <rect height="10" rx="1" ry="1" width="4" x="14" y="7"/>
        </svg>
        <svg v-else class="btn-icon" fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
             width="16">
          <polygon points="5,3 19,12 5,21"/>
        </svg>
        <span>{{ getButtonText(job.buildState) }}</span>
      </button>

      <div class="more-container">
        <button class="more-btn" @click.stop="handleToggleDropdown">
          <svg class="more-icon" fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
               width="16">
            <circle cx="12" cy="12" r="1"/>
            <circle cx="19" cy="12" r="1"/>
            <circle cx="5" cy="12" r="1"/>
          </svg>
        </button>

        <!-- 드롭다운 메뉴 -->
        <div v-if="isDropdownOpen" class="dropdown-menu" @click.stop>
          <button class="dropdown-item" @click="handleSaveSnapshot">
            <svg class="dropdown-icon" fill="none" height="16" stroke="currentColor" stroke-width="2"
                 viewBox="0 0 24 24" width="16">
              <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
              <polyline points="17,21 17,13 7,13 7,21"/>
              <polyline points="7,3 7,8 15,8"/>
            </svg>
            스냅샷 저장
          </button>

          <button class="dropdown-item" @click="handleViewSnapshots">
            <svg class="dropdown-icon" fill="none" height="16" stroke="currentColor" stroke-width="2"
                 viewBox="0 0 24 24" width="16">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14,2 14,8 20,8"/>
              <line x1="16" x2="8" y1="13" y2="13"/>
              <line x1="16" x2="8" y1="17" y2="17"/>
              <polyline points="10,9 9,9 8,9"/>
            </svg>
            스냅샷 목록
          </button>

          <div class="dropdown-divider"></div>

          <button class="dropdown-item danger" @click="handleDeleteJob">
            <svg class="dropdown-icon" fill="none" height="16" stroke="currentColor" stroke-width="2"
                 viewBox="0 0 24 24" width="16">
              <path d="M3 6h18l-1.5 14H4.5L3 6z"/>
              <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
            삭제
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.job-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.2s ease;
  cursor: pointer;
  position: relative;
  overflow: visible;
}

.job-card:hover,
.job-card.dropdown-open {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: #2563eb;
  z-index: 10;
}

/* 카드 헤더 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 16px;
}

.job-info {
  flex: 1;
  min-width: 0;
}

.job-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.job-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748b;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-icon {
  color: #9ca3af;
}

.meta-separator {
  color: #cbd5e1;
}

/* 상태 배지 */
.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.status-success {
  background: #f0fdf4;
  color: #16a34a;
}

.status-failed {
  background: #fef2f2;
  color: #dc2626;
}

.status-running {
  background: #fef3c7;
  color: #d97706;
}

.status-pending {
  background: #f1f5f9;
  color: #64748b;
}

.status-icon {
  width: 14px;
  height: 14px;
}

/* 카드 내용 */
.card-content {
  margin-bottom: 16px;
}

.job-description {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
  margin: 0;
}

.description-icon {
  color: #9ca3af;
  margin-top: 2px;
  flex-shrink: 0;
}

/* 스테이지 */
.stages-container {
  margin-bottom: 16px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #f1f5f9;
}

.stages-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.stages-icon {
  color: #64748b;
}

.stages-title {
  font-size: 12px;
  font-weight: 500;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stages-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.stage-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.stage-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
}

.stage-icon {
  width: 12px;
  height: 12px;
}

.stage-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #cbd5e1;
}

.stage-name {
  font-size: 11px;
}

/* 카드 푸터 */
.card-footer {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-start {
  background: var(--main-color, #2563eb);
  color: white;
}

.btn-start:hover {
  background: var(--main-color-hover, #1d4ed8);
  transform: translateY(-1px);
}

.btn-restart {
  background: #f1f5f9;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-restart:hover {
  background: #e2e8f0;
  color: #475569;
  transform: translateY(-1px);
}

.btn-retry {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.btn-retry:hover {
  background: #fecaca;
  transform: translateY(-1px);
}

.btn-stop {
  background: #fef3c7;
  color: #d97706;
  border: 1px solid #fed7aa;
}

.btn-stop:hover {
  background: #fed7aa;
  transform: translateY(-1px);
}

.btn-icon {
  width: 14px;
  height: 14px;
}

/* 더보기 컨테이너 */
.more-container {
  position: relative;
}

/* 드롭다운 메뉴 */
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  min-width: 160px;
  z-index: 1000;
  margin-top: 4px;
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 12px;
  border: none;
  background: none;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  transition: background-color 0.2s ease;
  text-align: left;
}

.dropdown-item:hover {
  background: #f9fafb;
}

.dropdown-item.danger {
  color: #dc2626;
}

.dropdown-item.danger:hover {
  background: #fef2f2;
}

.dropdown-icon {
  width: 16px;
  height: 16px;
  color: currentColor;
}

.dropdown-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 4px 0;
}

.more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
}

.more-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.more-icon {
  color: #64748b;
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
@media (max-width: 768px) {
  .job-card {
    padding: 16px;
  }

  .card-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .job-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .stages-list {
    flex-direction: column;
    gap: 6px;
  }

  .card-footer {
    flex-direction: column;
    gap: 8px;
  }

  .action-btn {
    width: 100%;
  }

  .more-btn {
    align-self: flex-end;
  }
}
</style>