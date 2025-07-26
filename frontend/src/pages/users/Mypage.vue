<script setup>
import {useRouter} from 'vue-router';
import {onMounted, ref} from 'vue';
import {useUserStore} from "@/stores/useUserStore.js"
import CheckPasswordModal from "@/pages/users/CheckPasswordModal.vue";

const userStore = useUserStore();
const router = useRouter();
const email = ref("");
const name = ref("");
const isVerified = ref(true);
const modalTarget = ref("reset");
const isLoading = ref(false);
const infoList = ref([]);
const isPasswordModalOpen = ref(false);

const fetchUser = () => {
  const userInfo = userStore.getUserInfo();
  email.value = userInfo.email;
  name.value = userInfo.name;
  isVerified.value = userInfo.isVerified;
  infoList.value = userInfo.infoList;
}

const withdraw = () => {
  const isOk = confirm(
      "정말로 서비스를 탈퇴하시겠습니까?\n\n" +
      "탈퇴 시 계정 및 모든 데이터가 삭제되며,\n" +
      "10일 이내에는 다시 로그인하면 복구가 가능합니다."
  );
  if (!isOk) {
    return;
  }
  modalTarget.value = "withdraw";
  isPasswordModalOpen.value = true;
}

const sendVerificationEmail = async () => {
  try {
    // API 호출 로직
    alert("인증 메일이 발송되었습니다.");
  } catch (error) {
    alert("메일 발송에 실패했습니다. 다시 시도해주세요.");
  }
}

const goToChangePassword = () => {
  modalTarget.value = "reset";
  isPasswordModalOpen.value = true;
};

const closePasswordModal = () => {
  isPasswordModalOpen.value = false;
};

// 로딩될때 유저 정보 세팅
onMounted(async () => {
  isLoading.value = true;
  try {
    if (!userStore.isFetched.value) {
      fetchUser();
    } else {
      await userStore.fetchUserInfo();
      fetchUser();
    }
  } catch (error) {
    console.error("사용자 정보를 불러오는데 실패했습니다:", error);
  } finally {
    isLoading.value = false;
  }
});
</script>

<template>
  <div class="container">
    <!-- 스켈레톤 UI -->
    <div v-if="isLoading" class="skeleton-container">
      <!-- 헤더 스켈레톤 -->
      <div class="skeleton-header">
        <div class="skeleton-title"></div>
        <div class="skeleton-breadcrumb"></div>
      </div>

      <!-- 기본 정보 스켈레톤 -->
      <div class="skeleton-section">
        <div class="skeleton-section-title"></div>
        <div class="skeleton-info-box">
          <div v-for="i in 3" :key="i" class="skeleton-info-item">
            <div class="skeleton-label"></div>
            <div class="skeleton-value"></div>
            <div class="skeleton-button"></div>
          </div>
        </div>
      </div>

      <!-- CI/CD 정보 스켈레톤 -->
      <div class="skeleton-section">
        <div class="skeleton-section-title"></div>
        <div class="skeleton-cicd-header">
          <div class="skeleton-dropdown"></div>
          <div class="skeleton-add-button"></div>
        </div>
        <div class="skeleton-card-grid">
          <div v-for="i in 4" :key="i" class="skeleton-card">
            <div class="skeleton-card-title"></div>
            <div class="skeleton-card-description"></div>
            <div class="skeleton-card-url"></div>
            <div class="skeleton-card-status"></div>
          </div>
        </div>
      </div>

      <!-- 탈퇴 버튼 스켈레톤 -->
      <div class="skeleton-withdraw">
        <div class="skeleton-withdraw-button"></div>
      </div>
    </div>

    <!-- 실제 컨텐츠 -->
    <div v-else>
      <!-- 헤더 -->
      <div class="header">
        <h1>마이페이지</h1>
        <div class="breadcrumb">
          <span class="breadcrumb-item">홈</span>
          <span class="breadcrumb-separator">></span>
          <span class="breadcrumb-item current">마이페이지</span>
        </div>
      </div>

      <!-- 기본 정보 섹션 -->
      <div class="section">
        <h3 class="section-title">
          <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
          기본 정보
        </h3>

        <div class="info-card">
          <!-- 이메일 -->
          <div class="info-item">
            <div class="info-label">
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                <polyline points="22,6 12,13 2,6"/>
              </svg>
              이메일
            </div>
            <div class="info-content">
              <div class="email-display">
                <span class="email-text">{{ email }}</span>
                <div :class="{ verified: isVerified }" class="verification-badge">
                  <svg v-if="isVerified" fill="none" height="16" stroke="currentColor" stroke-width="2"
                       viewBox="0 0 24 24"
                       width="16">
                    <polyline points="20,6 9,17 4,12"/>
                  </svg>
                  <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                       width="16">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="15" x2="9" y1="9" y2="15"/>
                    <line x1="9" x2="15" y1="9" y2="15"/>
                  </svg>
                  <span>{{ isVerified ? '인증됨' : '미인증' }}</span>
                </div>
              </div>
            </div>
            <div class="info-action">
              <button v-if="!isVerified" class="btn btn-outline" @click="sendVerificationEmail">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                  <polyline points="22,6 12,13 2,6"/>
                </svg>
                인증 메일 발송
              </button>
            </div>
          </div>

          <!-- 이름 -->
          <div class="info-item">
            <div class="info-label">
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              이름
            </div>
            <div class="info-content">
              <span class="info-text">{{ name }}</span>
            </div>
            <div class="info-action"></div>
          </div>

          <!-- 비밀번호 -->
          <div class="info-item">
            <div class="info-label">
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <rect height="11" rx="2" ry="2" width="18" x="3" y="11"/>
                <circle cx="12" cy="16" r="1"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              비밀번호
            </div>
            <div class="info-content">
              <span class="password-display">••••••••••••</span>
            </div>
            <div class="info-action">
              <button class="btn btn-outline" @click="goToChangePassword">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="m18.5 2.5 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
                비밀번호 변경
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- CI/CD 정보 섹션 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">
            <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
              <rect height="14" rx="2" ry="2" width="20" x="2" y="3"/>
              <line x1="8" x2="16" y1="21" y2="21"/>
              <line x1="12" x2="12" y1="17" y2="21"/>
            </svg>
            CI/CD 정보
          </h3>
          <button class="btn btn-primary" @click="router.push('/mypage/cicd/create')">
            <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <line x1="12" x2="12" y1="5" y2="19"/>
              <line x1="5" x2="19" y1="12" y2="12"/>
            </svg>
            새 서버 추가
          </button>
        </div>

        <div v-if="infoList.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg fill="none" height="64" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24" width="64">
              <rect height="14" rx="2" ry="2" width="20" x="2" y="3"/>
              <line x1="8" x2="16" y1="21" y2="21"/>
              <line x1="12" x2="12" y1="17" y2="21"/>
            </svg>
          </div>
          <h4 class="empty-title">등록된 CI/CD 서버가 없습니다</h4>
          <p class="empty-description">새로운 Jenkins 서버를 추가하여 CI/CD 파이프라인을 시작해보세요.</p>
          <button class="btn btn-primary" @click="router.push('/mypage/cicd/create')">
            <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <line x1="12" x2="12" y1="5" y2="19"/>
              <line x1="5" x2="19" y1="12" y2="12"/>
            </svg>
            첫 번째 서버 추가하기
          </button>
        </div>

        <div v-else class="cicd-grid">
          <div
              v-for="info in infoList"
              :key="info.id"
              class="cicd-card"
              @click="router.push(`/mypage/cicd/${info.id}`)"
          >
            <div class="card-header">
              <h4 class="card-title">{{ info.name }}</h4>
              <div :class="{ connected: info.connected }" class="connection-status">
                <div class="status-dot"></div>
                <span class="status-text">{{ info.connected ? '연결됨' : '연결 실패' }}</span>
              </div>
            </div>

            <p class="card-description">{{ info.description || '설명이 없습니다.' }}</p>

            <div class="card-footer">
              <div class="card-url">
                <svg fill="none" height="14" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="14">
                  <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                  <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                </svg>
                <span>{{ info.uri }}</span>
              </div>

              <button class="card-action-btn" @click.stop="router.push(`/mypage/cicd/${info.id}`)">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M9 18l6-6-6-6"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 계정 관리 섹션 -->
      <div class="section danger-section">
        <h3 class="section-title">
          <img alt="warn" src="@/assets/icons/warn.svg" width="20"/>
          계정 관리
        </h3>

        <div class="danger-content">
          <div class="danger-info">
            <h4>회원 탈퇴</h4>
            <p>계정을 삭제하면 모든 데이터가 영구적으로 삭제됩니다. 이 작업은 되돌릴 수 없습니다.</p>
          </div>
          <button class="btn btn-danger" @click="withdraw">
            <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <path d="M3 6h18"/>
              <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/>
              <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>
            </svg>
            회원 탈퇴
          </button>
        </div>
      </div>
    </div>

    <!-- 모달 -->
    <CheckPasswordModal
        v-if="isPasswordModalOpen"
        :destination="modalTarget"
        @close="closePasswordModal"
    />
  </div>
</template>

<style scoped>
.container {
  max-width: 1000px;
  margin: 20px auto 0;
  padding: 24px;
  min-height: 100vh;
  background: #f8fafc;
}

/* 스켈레톤 UI */
.skeleton-container {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.skeleton-header {
  margin-bottom: 32px;
}

.skeleton-title {
  height: 36px;
  width: 200px;
  background: #e2e8f0;
  border-radius: 8px;
  margin-bottom: 8px;
}

.skeleton-breadcrumb {
  height: 20px;
  width: 150px;
  background: #f1f5f9;
  border-radius: 4px;
}

.skeleton-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.skeleton-section-title {
  height: 24px;
  width: 150px;
  background: #e2e8f0;
  border-radius: 6px;
  margin-bottom: 20px;
}

.skeleton-info-box {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.skeleton-info-item {
  display: grid;
  grid-template-columns: 1fr 3fr 1fr;
  gap: 20px;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f1f5f9;
}

.skeleton-label {
  height: 20px;
  width: 80px;
  background: #f1f5f9;
  border-radius: 4px;
}

.skeleton-value {
  height: 20px;
  width: 200px;
  background: #f1f5f9;
  border-radius: 4px;
}

.skeleton-button {
  height: 36px;
  width: 120px;
  background: #f1f5f9;
  border-radius: 6px;
}

.skeleton-cicd-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.skeleton-dropdown {
  height: 40px;
  width: 200px;
  background: #f1f5f9;
  border-radius: 8px;
}

.skeleton-add-button {
  height: 40px;
  width: 140px;
  background: #f1f5f9;
  border-radius: 8px;
}

.skeleton-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.skeleton-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e2e8f0;
}

.skeleton-card-title {
  height: 24px;
  width: 150px;
  background: #e2e8f0;
  border-radius: 6px;
  margin-bottom: 12px;
}

.skeleton-card-description {
  height: 16px;
  width: 100%;
  background: #f1f5f9;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-card-url {
  height: 16px;
  width: 80%;
  background: #f1f5f9;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-card-status {
  height: 20px;
  width: 100px;
  background: #f1f5f9;
  border-radius: 4px;
}

.skeleton-withdraw {
  display: flex;
  justify-content: flex-end;
  margin-top: 32px;
}

.skeleton-withdraw-button {
  height: 40px;
  width: 120px;
  background: #fee2e2;
  border-radius: 8px;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 헤더 */
.header {
  margin-bottom: 32px;
}

.header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #64748b;
}

.breadcrumb-item {
  color: #64748b;
}

.breadcrumb-item.current {
  color: #2563eb;
  font-weight: 500;
}

.breadcrumb-separator {
  color: #cbd5e1;
}

/* 섹션 */
.section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 20px 0;
}

.section-title svg {
  color: #2563eb;
}

/* 기본 정보 카드 */
.info-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.info-item {
  display: grid;
  grid-template-columns: 200px 1fr auto;
  gap: 20px;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  background: white;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
}

.info-label svg {
  color: #94a3b8;
}

.info-content {
  display: flex;
  align-items: center;
}

.info-text {
  color: #1e293b;
  font-weight: 500;
}

.email-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.email-text {
  color: #1e293b;
  font-weight: 500;
}

.verification-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: #fee2e2;
  color: #dc2626;
}

.verification-badge.verified {
  background: #dcfce7;
  color: #16a34a;
}

.verification-badge svg {
  width: 12px;
  height: 12px;
}

.password-display {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  color: #64748b;
  font-size: 14px;
}

.info-action {
  display: flex;
  justify-content: flex-end;
}

/* 버튼 */
.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
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
}

.btn-outline {
  background: transparent;
  color: var(--main-color);
  border: 1px solid var(--main-color);
}

.btn-outline:hover {
  background: var(--main-color-hover);
  color: white;
}

.btn-danger {
  background: #dc2626;
  color: white;
}

.btn-danger:hover {
  background: #b91c1c;
}

/* CI/CD 그리드 */
.cicd-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}

.cicd-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.cicd-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #2563eb;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;
}

.connection-status .status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
}

.connection-status.connected .status-dot {
  background: #10b981;
}

.connection-status .status-text {
  color: #dc2626;
}

.connection-status.connected .status-text {
  color: #059669;
}

.card-description {
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-url {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
  overflow: hidden;
}

.card-url span {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: #f1f5f9;
  border: none;
  border-radius: 6px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.card-action-btn:hover {
  background: #2563eb;
  color: white;
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  margin-bottom: 20px;
  color: #cbd5e1;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.empty-description {
  color: #64748b;
  margin: 0 0 24px 0;
  line-height: 1.5;
}

/* 위험 섹션 */
.danger-section {
  border-color: #fecaca;
  background: #fef2f2;
}

.danger-section .section-title {
  color: #dc2626;
}

.danger-section .section-title svg {
  color: #dc2626;
}

.danger-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #fecaca;
}

.danger-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.danger-info p {
  color: #64748b;
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

/* 반응형 */
@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .header h1 {
    font-size: 24px;
  }

  .section {
    padding: 20px 16px;
  }

  .info-item {
    grid-template-columns: 1fr;
    gap: 12px;
    text-align: left;
  }

  .info-action {
    justify-content: flex-start;
  }

  .section-header {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .cicd-grid {
    grid-template-columns: 1fr;
  }

  .danger-content {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .danger-info {
    text-align: center;
  }
}
</style>