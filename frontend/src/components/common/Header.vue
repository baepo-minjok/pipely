<script setup>
import {onBeforeUnmount, onMounted, ref} from 'vue';
import {useRouter} from 'vue-router';
import {userApi} from "@/api/UserApi.js";
import {useUserStore} from "@/stores/useUserStore.js"

const router = useRouter();
const showMenu = ref(false);
const profileWrapper = ref(null);
const userStore = useUserStore();

// 유저 정보 변수
const email = ref("");
const name = ref("");
const isLoggedIn = ref(false);

// 알림 관리 변수
const noti = ref(Number);

const fetchUser = () => {
  const userInfo = userStore.getUserInfo();
  email.value = userInfo.email;
  name.value = userInfo.name;
}

const goToHome = () => {
  router.push('/');
};

const handleMenuClick = () => {
  showMenu.value = !showMenu.value;
};

const handleLoginClick = () => {
  router.push({name: 'Login'});
}

const handleDropdownSelect = async (action) => {
  showMenu.value = false;

  switch (action) {
    case 'jobList' :
      router.push({name: 'JobList'})
      break;
    case 'mypage':
      router.push({name: 'Mypage'});
      break;
    case 'logout':
      if (confirm('정말로 로그아웃하시겠습니까?')) {
        userStore.reset();
        await userApi.logout();
        localStorage.removeItem('chatHistory');
        router.push({name: 'Login'});
      }
      break;
    default:
      console.log('Unknown action:', action);
  }
};

// 바깥 클릭 감지 함수
const handleClickOutside = (event) => {
  if (profileWrapper.value && !profileWrapper.value.contains(event.target)) {
    showMenu.value = false;
  }
};

// ESC 키로 드롭다운 닫기
const handleKeydown = (event) => {
  if (event.key === 'Escape' && showMenu.value) {
    showMenu.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
  document.addEventListener('keydown', handleKeydown);

  if (userStore.isFetched) {
    isLoggedIn.value = true;
    fetchUser();
  }
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside);
  document.removeEventListener('keydown', handleKeydown);
});
</script>

<template>
  <header class="header">
    <div class="header-container">
      <!-- 로고 섹션 -->
      <div class="logo-section" @click="goToHome">
        <div class="logo-wrapper">
          <img alt="Pipely" class="logo-img" src="/src/assets/images/logo.png"/>
        </div>
      </div>

      <!-- 우측 액션 영역 -->
      <div class="header-actions">
        <button class="action-btn notification-btn" title="알림">
          <svg height="20" viewBox="0 0 24 24" width="20" xmlns="http://www.w3.org/2000/svg"><title>bell-outline</title>
            <path
              d="M10 21H14C14 22.1 13.1 23 12 23S10 22.1 10 21M21 19V20H3V19L5 17V11C5 7.9 7 5.2 10 4.3V4C10 2.9 10.9 2 12 2S14 2.9 14 4V4.3C17 5.2 19 7.9 19 11V17L21 19M17 11C17 8.2 14.8 6 12 6S7 8.2 7 11V18H17V11Z"/>
          </svg>
          <span v-if="noti > 0" class="notification-badge">1</span>
        </button>

        <!-- 프로필 드롭다운 -->
        <div v-if="isLoggedIn" ref="profileWrapper" class="profile-wrapper">
          <button :class="{ active: showMenu }" class="profile-button" @click="handleMenuClick">
            <div class="profile-avatar">
              <img alt="프로필" class="profile-icon" src="/src/assets/icons/profile.svg"/>
            </div>
            <div class="profile-info">
              <span class="profile-name">{{ name }}</span>
            </div>
            <svg
              :class="{ rotated: showMenu }"
              class="dropdown-arrow"
              fill="none"
              height="16"
              stroke="currentColor"
              stroke-width="2"
              viewBox="0 0 24 24"
              width="16"
            >
              <polyline points="6,9 12,15 18,9"/>
            </svg>
          </button>

          <!-- 드롭다운 메뉴 -->
          <transition name="dropdown">
            <div v-if="showMenu" class="dropdown-menu">
              <div class="dropdown-header">
                <div class="user-avatar">
                  <img alt="프로필" src="/src/assets/icons/profile.svg"/>
                </div>
                <div class="user-info">
                  <div class="user-name">{{ name }}</div>
                  <div class="user-email">{{ email }}</div>
                </div>
              </div>

              <div class="dropdown-content">
                <button class="dropdown-item" @click="handleDropdownSelect('jobList')">
                  <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                    <path
                      d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z"/>
                  </svg>
                  Job 목록
                </button>
                <button class="dropdown-item" @click="handleDropdownSelect('mypage')">
                  <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                    <path
                      d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z"/>
                  </svg>
                  마이페이지
                </button>

                <div class="dropdown-divider"></div>

                <button v-if="isLoggedIn" class="dropdown-item danger" @click="handleDropdownSelect('logout')">
                  <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16,17 21,12 16,7"/>
                    <line x1="21" x2="9" y1="12" y2="12"/>
                  </svg>
                  로그아웃
                </button>
              </div>
            </div>
          </transition>
        </div>

        <div v-else class="profile-wrapper">
          <button class="profile-button not-login" @click="handleLoginClick">
            <div class="profile-avatar">
              <img alt="프로필" class="profile-icon" src="/src/assets/icons/profile.svg"/>
            </div>
            <div class="profile-info">
              <span class="profile-name">로그인</span>
            </div>
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 72px;
}

/* 로고 섹션 */
.logo-section {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.logo-section:hover {
  transform: scale(1.02);
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-img {
  height: 36px;
  width: auto;
  object-fit: contain;
}

/* 네비게이션 */
.navigation {
  flex: 1;
  display: flex;
  justify-content: left;
  padding-left: 30px;
}

.nav-links {
  display: flex;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: none;
  border: none;
  border-radius: 8px;
  color: #303030;
  font-size: 24px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-link:hover {
  color: #212020;
  transform: translateY(-1px);
}

.nav-link svg {
  color: currentColor;
}

/* 헤더 액션 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #f8fafc;
  border: none;
  border-radius: 10px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: #e2e8f0;
  color: #374151;
  transform: translateY(-1px);
}

.notification-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: #ef4444;
  color: white;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 프로필 드롭다운 */
.profile-wrapper {
  position: relative;
}

.profile-button {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 100px;
}

.profile-button.not-login {
  min-width: 50px;
}

.profile-button:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.profile-button.active {
  background: #f1f5f9;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.profile-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.profile-icon {
  width: 20px;
  height: 20px;
  color: #64748b;
}

.profile-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
}

.profile-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.2;
}

.profile-role {
  font-size: 12px;
  color: #64748b;
  line-height: 1.2;
}

.dropdown-arrow {
  color: #94a3b8;
  transition: transform 0.2s ease;
}

.dropdown-arrow.rotated {
  transform: rotate(180deg);
}

/* 드롭다운 메뉴 */
.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  min-width: 280px;
  z-index: 50;
  overflow: hidden;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.user-avatar img {
  width: 24px;
  height: 24px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 2px;
}

.user-email {
  font-size: 14px;
  color: #64748b;
}

.dropdown-content {
  padding: 8px 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 20px;
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

.dropdown-item svg {
  color: currentColor;
}

.dropdown-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 8px 0;
}

/* 드롭다운 애니메이션 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}

/* 반응형 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
    height: 64px;
  }

  .navigation {
    display: none;
  }

  .header-actions {
    gap: 8px;
  }

  .action-btn {
    width: 36px;
    height: 36px;
  }

  .profile-button {
    min-width: auto;
    padding: 6px 8px;
  }

  .profile-info {
    display: none;
  }

  .dropdown-menu {
    right: -8px;
    min-width: 240px;
  }
}

@media (max-width: 480px) {
  .header-container {
    padding: 0 12px;
  }

  .logo-img {
    height: 28px;
  }

  .dropdown-menu {
    right: -12px;
    left: 12px;
    min-width: auto;
  }
}
</style>