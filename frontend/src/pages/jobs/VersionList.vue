<script setup>
import {versionApi} from "@/api/VersionApi.js";
import {onMounted, onUnmounted, ref} from "vue";
import {formatDateTime} from "@/utils/formatDateTime.js";

const props = defineProps(['jobId']);

const emit = defineEmits([
  'create-snapshot',
  'select-snapshot',
  'onRollback',
  'duplicate-snapshot',
  'rename',
  'onDelete'
]);


const snapshotList = ref([]);
const isLoading = ref(true);
const activeMenu = ref(null);

onMounted(async () => {
  await fetchSnapshots();
});

const fetchSnapshots = async () => {
  isLoading.value = true;
  try {
    const response = await versionApi.getSnapshotList(props.jobId);
    console.log(response);
    snapshotList.value = response;
  } catch (error) {
    console.error('스냅샷 목록 조회 실패:', error);
    snapshotList.value = [];
  } finally {
    isLoading.value = false;
  }
};

const toggleMenu = (versionId) => {
  activeMenu.value = activeMenu.value === versionId ? null : versionId;
};

// 외부 클릭 시 메뉴 닫기
const handleClickOutside = () => {
  activeMenu.value = null;
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<template>
  <div class="snapshot-container">
    <!-- 헤더 -->
    <div class="header">
      <div class="title-section">
        <div class="title-content">
          <div class="title-icon">
            <svg fill="none" height="24" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="24">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="10,6 10,10 14,14"/>
            </svg>
          </div>
          <h2 class="title">스냅샷 목록</h2>
        </div>
        <div class="header-info">
          <span class="snapshot-count">{{ snapshotList.length }}개의 스냅샷</span>
        </div>
      </div>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-content">
        <div class="loading-spinner">
          <svg class="animate-spin" fill="none" height="32" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
               width="32">
            <path d="M21 12a9 9 0 11-6.219-8.56"/>
          </svg>
        </div>
        <p class="loading-text">스냅샷 목록을 불러오는 중...</p>
      </div>
    </div>

    <!-- 빈 상태 -->
    <div v-else-if="snapshotList.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg fill="none" height="64" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24" width="64">
          <circle cx="12" cy="12" r="10"/>
          <path d="M12 6v6l4 2"/>
        </svg>
      </div>
      <h3 class="empty-title">스냅샷이 없습니다</h3>
      <p class="empty-description">
        아직 저장된 스냅샷이 없습니다.<br>
        작업 중인 내용을 스냅샷으로 저장해보세요.
      </p>
      <button class="btn btn-primary empty-action" @click="$emit('create-snapshot')">
        <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
          <line x1="12" x2="12" y1="5" y2="19"/>
          <line x1="5" x2="19" y1="12" y2="12"/>
        </svg>
        첫 번째 스냅샷 만들기
      </button>
    </div>

    <!-- 스냅샷 목록 -->
    <div v-else class="snapshot-list">
      <div
          v-for="(snap, index) in snapshotList"
          :key="snap.versionId"
          :class="{ latest: index === 0 }"
          class="snapshot-card"
          @click="$emit('select-snapshot', snap)"
      >
        <div class="snapshot-header">
          <div class="snapshot-info">
            <div class="snapshot-name-section">
              <h4 class="snapshot-name">{{ snap.name }}</h4>
              <span v-if="index === 0" class="latest-badge">최신</span>
            </div>
          </div>
          <div class="snapshot-actions">
            <button
                :title="'이 스냅샷으로 복원'"
                class="action-btn restore-btn"
                @click.stop="$emit('onRollback', snap)"
            >
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <polyline points="1,4 1,10 7,10"/>
                <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/>
              </svg>
            </button>
            <button
                :title="'더 많은 옵션'"
                class="action-btn more-btn"
                @click.stop="toggleMenu(snap.versionId)"
            >
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <circle cx="12" cy="12" r="1"/>
                <circle cx="12" cy="5" r="1"/>
                <circle cx="12" cy="19" r="1"/>
              </svg>
            </button>

            <!-- 드롭다운 메뉴 -->
            <div v-if="activeMenu === snap.versionId" class="dropdown-menu" @click.stop>
              <button class="dropdown-item" @click="$emit('rename', snap)">
                <svg fill="none" height="14" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="14">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="m18.5 2.5 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
                이름 변경
              </button>
              <div class="dropdown-divider"></div>
              <button class="dropdown-item danger" @click="$emit('onDelete', snap)">
                <svg fill="none" height="14" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="14">
                  <polyline points="3,6 5,6 21,6"/>
                  <path d="m19,6v14a2,2 0 0,1 -2,2H7a2,2 0 0,1 -2,-2V6m3,0V4a2,2 0 0,1 2,-2h4a2,2 0 0,1 2,2v2"/>
                </svg>
                삭제
              </button>
            </div>
          </div>
        </div>
        <!-- 스냅샷 설명 (있는 경우) -->
        <div v-if="snap.description" class="snapshot-description">
          {{ snap.description }}ㄹㄷㄷㄹ
        </div>
        <div class="snapshot-stats">

          <div class="snapshot-meta">
            <div class="meta-item">
              <svg fill="none" height="14" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="14">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12,6 12,12 16,14"/>
              </svg>
              <span>{{ formatDateTime(snap.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.snapshot-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
}

.snapshot-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 60vh;
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 6px;
}

/* 헤더 */
.header {
  margin-bottom: 32px;
}

.title-section {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.title-content {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.title-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #dbeafe;
  border-radius: 10px;
  color: #2563eb;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.snapshot-count {
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
}

/* 로딩 상태 */
.loading-container {
  background: white;
  border-radius: 12px;
  padding: 60px 24px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-spinner {
  color: #2563eb;
}

.loading-text {
  color: #64748b;
  font-size: 16px;
  margin: 0;
}

/* 빈 상태 */
.empty-state {
  background: white;
  border-radius: 12px;
  padding: 60px 24px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.empty-icon {
  color: #94a3b8;
  margin-bottom: 24px;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px 0;
}

.empty-description {
  color: #64748b;
  font-size: 16px;
  line-height: 1.6;
  margin: 0 0 32px 0;
}

.empty-action {
  margin: 0 auto;
}

/* 스냅샷 목록 */
.snapshot-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.snapshot-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}


.snapshot-card.latest {
  border-color: #2563eb;
  box-shadow: 0 1px 3px rgba(37, 99, 235, 0.1);
}

.snapshot-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.snapshot-info {
  flex: 1;
}

.snapshot-name-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.snapshot-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.latest-badge {
  background: #2563eb;
  color: white;
  font-size: 12px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 12px;
}

.snapshot-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 14px;
}

.meta-item svg {
  color: #94a3b8;
}

/* 스냅샷 액션 */
.snapshot-actions {
  display: flex;
  gap: 8px;
  position: relative;
}

.action-btn {
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

.action-btn:hover {
  background: #e2e8f0;
  color: #374151;
  transform: scale(1.05);
}

.restore-btn:hover {
  background: #dbeafe;
  color: #2563eb;
}

/* 드롭다운 메뉴 */
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  z-index: 10;
  min-width: 160px;
  padding: 8px 0;
  margin-top: 4px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 16px;
  background: none;
  border: none;
  text-align: left;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.dropdown-item:hover {
  background: #f1f5f9;
}

.dropdown-item.danger {
  color: #dc2626;
}

.dropdown-item.danger:hover {
  background: #fee2e2;
}

.dropdown-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 8px 0;
}

/* 스냅샷 설명 */
.snapshot-description {
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 3px solid #e2e8f0;
}

/* 스냅샷 통계 */
.snapshot-stats {
  display: flex;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

/* 버튼 */
.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
}

.btn:hover {
  transform: translateY(-1px);
}

.btn-primary {
  background: var(--main-color);
  color: white;
}

.btn-primary:hover {
  background: var(--main-color-hover);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
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

/* 반응형 */
@media (max-width: 768px) {
  .snapshot-container {
    padding: 16px;
  }

  .title-section {
    padding: 20px 16px;
  }

  .title-content {
    margin-bottom: 12px;
  }

  .title {
    font-size: 20px;
  }

  .snapshot-card {
    padding: 20px 16px;
  }

  .snapshot-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .snapshot-actions {
    justify-content: flex-end;
  }

  .snapshot-stats {
    flex-wrap: wrap;
    gap: 16px;
  }

  .meta-item {
    font-size: 13px;
  }

  .dropdown-menu {
    right: auto;
    left: 0;
  }

}
</style>