<template>
  <div class="container">
    <!-- 스켈레톤 UI -->
    <div v-if="isLoading" class="skeleton-container">
      <div class="skeleton-header">
        <div class="skeleton-title"></div>
        <div class="skeleton-controls"></div>
      </div>
      <div class="skeleton-calendar">
        <div v-for="i in 42" :key="i" class="skeleton-day"></div>
      </div>
    </div>

    <!-- 실제 컨텐츠 -->
    <div v-else>
      <div class="header">
        <h1>빌드 캘린더</h1>
      </div>

      <!-- 캘린더 컨트롤 -->
      <div class="calendar-controls">
        <div class="month-navigation">
          <button class="nav-btn" @click="previousMonth">
            <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
              <polyline points="15,18 9,12 15,6"/>
            </svg>
          </button>
          <h2 class="current-month">{{ currentMonthYear }}</h2>
          <button class="nav-btn" @click="nextMonth">
            <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
              <polyline points="9,18 15,12 9,6"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 캘린더 -->
      <div class="calendar-section">
        <div class="calendar-grid">
          <!-- 요일 헤더 -->
          <div v-for="day in weekdays" :key="day" class="weekday-header">
            {{ day }}
          </div>

          <!-- 날짜 셀 -->
          <div
              v-for="date in calendarDates"
              :key="date.key"
              :class="['calendar-cell', {
              'other-month': !date.isCurrentMonth,
              'today': date.isToday,
              'has-events': date.events.length > 0
            }]"
          >
            <div class="date-number">{{ date.day }}</div>

            <!-- 배지 컨테이너 -->
            <div class="badges-container">
              <div
                  v-for="badge in getBadgesForDate(date.events)"
                  :key="badge.type"
                  :class="['event-badge-with-count', `badge-${badge.type}`]"
                  @click="openEventDropdown($event, badge.type, badge.events)"
                  :title="`${badge.type} ${badge.count}개`"
              >
                <span class="badge-text">{{ badge.type.toUpperCase() }} *{{ badge.count }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 드롭다운 메뉴 -->
    <div v-if="dropdownVisible" class="dropdown-overlay" @click="closeDropdown">
      <div
          class="dropdown-menu"
          @click.stop
          :style="{
          left: dropdownPosition.x + 'px',
          top: dropdownPosition.y + 'px'
        }"
      >
        <div class="dropdown-header">
          <h4>{{ dropdownType === 'build' ? 'Build 목록' : 'Error 목록' }}</h4>
        </div>
        <div class="dropdown-list">
          <div
              v-for="(event, index) in dropdownEvents"
              :key="event.id"
              class="dropdown-item"
              @click="selectEventFromDropdown(event)"
          >
            <div class="item-number">{{ index + 1 }}.</div>
            <div class="item-content">
              <div class="item-title">{{ event.project }} #{{ event.buildNumber }}</div>
              <div class="item-time">({{ event.time }})</div>
            </div>
            <div :class="['item-status', `status-${event.status}`]">
              {{ event.status === 'success' ? '성공' : '실패' }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 이벤트 상세 팝업 -->
    <div v-if="selectedEvent" class="modal-overlay" @click="closeEventDetail">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">
            <div :class="['title-badge', `badge-${selectedEvent.type}`]">
              <div class="badge-dot"></div>
            </div>
            {{ selectedEvent.type === 'build' ? 'Build 정보' : 'Error 정보' }}
          </h3>
          <button class="close-btn" @click="closeEventDetail">
            <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
              <line x1="18" x2="6" y1="6" y2="18"/>
              <line x1="6" x2="18" y1="6" y2="18"/>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="detail-section">
            <div class="detail-item">
              <span class="detail-label">제목</span>
              <span class="detail-value">{{ selectedEvent.title }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">프로젝트</span>
              <span class="detail-value">{{ selectedEvent.project }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">시간</span>
              <span class="detail-value">{{ selectedEvent.time }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">상태</span>
              <span :class="['status-badge', `status-${selectedEvent.status}`]">
                {{ selectedEvent.status === 'success' ? '성공' : selectedEvent.status === 'failed' ? '실패' : '진행중' }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">브랜치</span>
              <span class="detail-value">{{ selectedEvent.branch }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">빌드 번호</span>
              <span class="detail-value">#{{ selectedEvent.buildNumber }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">커밋</span>
              <span class="detail-value commit-hash">{{ selectedEvent.commit }}</span>
            </div>
            <div v-if="selectedEvent.type === 'build'" class="detail-item">
              <span class="detail-label">빌드 시간</span>
              <span class="detail-value">{{ selectedEvent.buildTime }}</span>
            </div>
            <div v-if="selectedEvent.type === 'error'" class="detail-item">
              <span class="detail-label">에러 타입</span>
              <span class="detail-value">{{ selectedEvent.errorType }}</span>
            </div>
          </div>

          <div v-if="selectedEvent.logs" class="logs-section">
            <h4 class="logs-title">
              {{ selectedEvent.type === 'build' ? '빌드 로그' : '에러 로그' }}
            </h4>
            <div class="logs-container">
              <pre class="logs-content">{{ selectedEvent.logs }}</pre>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeEventDetail">닫기</button>
          <button v-if="selectedEvent.type === 'build'" class="btn btn-primary" @click="rebuildProject">
            <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <path d="M21 12a9 9 0 11-6.219-8.56"/>
            </svg>
            다시 빌드
          </button>
          <button v-if="selectedEvent.type === 'error'" class="btn btn-primary" @click="viewErrorDetails">
            <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" x2="12" y1="8" y2="12"/>
              <line x1="12" x2="12.01" y1="16" y2="16"/>
            </svg>
            상세 분석
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

const isLoading = ref(true)
const selectedEvent = ref(null)
const dropdownVisible = ref(false)
const dropdownEvents = ref([])
const dropdownPosition = ref({ x: 0, y: 0 })
const dropdownType = ref('')

const currentDate = reactive({
  year: new Date().getFullYear(),
  month: new Date().getMonth()
})

const weekdays = ['일', '월', '화', '수', '목', '금', '토']

// 현재 날짜 기준으로 샘플 이벤트 데이터 생성
const events = ref([
  {
    id: 1,
    type: 'build',
    title: 'Frontend Build',
    project: 'MyApp Frontend',
    date: new Date(2025, 0, 26),
    time: '14:30',
    status: 'success',
    branch: 'main',
    commit: 'a1b2c3d',
    buildNumber: 1,
    buildTime: '2분 30초',
    logs: 'Build started at 14:30:00\n✓ Installing dependencies...\n✓ Running tests... (45 tests passed)\n✓ Building for production...\n✓ Build completed successfully!\n\nBuild time: 2m 30s\nBundle size: 1.2MB'
  },
  {
    id: 2,
    type: 'error',
    title: 'API Connection Error',
    project: 'MyApp Backend',
    date: new Date(2025, 0, 26),
    time: '15:45',
    status: 'failed',
    branch: 'develop',
    commit: 'e4f5g6h',
    buildNumber: 2,
    errorType: 'Connection Timeout',
    logs: 'Error occurred at 15:45:23\n\n✗ Connection timeout to database\n✗ Failed to establish connection after 30s\n\nStack trace:\n  at Connection.connect() line 45\n  at Database.init() line 12\n  at Server.start() line 8\n\nSuggestion: Check database server status'
  },
  {
    id: 3,
    type: 'build',
    title: 'Backend Build',
    project: 'MyApp Backend',
    date: new Date(2025, 0, 28),
    time: '09:15',
    status: 'success',
    branch: 'main',
    commit: 'i7j8k9l',
    buildNumber: 3,
    buildTime: '1분 45초',
    logs: 'Build started at 09:15:00\n✓ Compiling TypeScript sources...\n✓ Running unit tests... (128 tests passed)\n✓ Creating Docker image...\n✓ Deployment successful!\n\nBuild time: 1m 45s'
  },
  {
    id: 4,
    type: 'error',
    title: 'Deploy Error',
    project: 'MyApp Frontend',
    date: new Date(2025, 0, 30),
    time: '16:20',
    status: 'failed',
    branch: 'feature/new-ui',
    commit: 'm1n2o3p',
    buildNumber: 4,
    errorType: 'Configuration Error',
    logs: 'Deployment failed at 16:20:15\n\n✗ Invalid configuration in deploy.yml\n✗ Missing required environment variable: API_URL\n✗ Port 3000 already in use\n\nPlease check your deployment configuration'
  },
  {
    id: 5,
    type: 'build',
    title: 'Mobile App Build',
    project: 'MyApp Mobile',
    date: new Date(2025, 1, 1),
    time: '11:00',
    status: 'success',
    branch: 'main',
    commit: 'q4r5s6t',
    buildNumber: 5,
    buildTime: '5분 12초',
    logs: 'Build started at 11:00:00\n✓ Installing React Native dependencies...\n✓ Running Metro bundler...\n✓ Building Android APK...\n✓ Building iOS IPA...\n✓ Build completed successfully!\n\nBuild time: 5m 12s\nAPK size: 25.4MB\nIPA size: 28.1MB'
  }
])

const currentMonthYear = computed(() => {
  const date = new Date(currentDate.year, currentDate.month)
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long' })
})

const calendarDates = computed(() => {
  const firstDay = new Date(currentDate.year, currentDate.month, 1)
  const lastDay = new Date(currentDate.year, currentDate.month + 1, 0)
  const startDate = new Date(firstDay)
  startDate.setDate(startDate.getDate() - firstDay.getDay())

  const dates = []
  const today = new Date()

  for (let i = 0; i < 42; i++) {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + i)

    const dayEvents = events.value.filter(event =>
        event.date.toDateString() === date.toDateString()
    )

    dates.push({
      key: `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`,
      day: date.getDate(),
      date: new Date(date),
      isCurrentMonth: date.getMonth() === currentDate.month,
      isToday: date.toDateString() === today.toDateString(),
      events: dayEvents
    })
  }

  return dates
})

const previousMonth = () => {
  if (currentDate.month === 0) {
    currentDate.month = 11
    currentDate.year--
  } else {
    currentDate.month--
  }
}

const nextMonth = () => {
  if (currentDate.month === 11) {
    currentDate.month = 0
    currentDate.year++
  } else {
    currentDate.month++
  }
}

const getBadgesForDate = (dayEvents) => {
  const badges = []

  const buildEvents = dayEvents.filter(e => e.type === 'build')
  const errorEvents = dayEvents.filter(e => e.type === 'error')

  if (buildEvents.length > 0) {
    badges.push({
      type: 'build',
      count: buildEvents.length,
      events: buildEvents.sort((a, b) => a.time.localeCompare(b.time))
    })
  }

  if (errorEvents.length > 0) {
    badges.push({
      type: 'error',
      count: errorEvents.length,
      events: errorEvents.sort((a, b) => a.time.localeCompare(b.time))
    })
  }

  return badges
}

const openEventDropdown = (event, type, events) => {
  event.stopPropagation()

  // 드롭다운 위치 계산
  const rect = event.target.getBoundingClientRect()
  dropdownPosition.value = {
    x: rect.left,
    y: rect.bottom + 8
  }

  dropdownEvents.value = events
  dropdownType.value = type
  dropdownVisible.value = true
}

const selectEventFromDropdown = (event) => {
  dropdownVisible.value = false
  selectedEvent.value = event
}

const closeDropdown = () => {
  dropdownVisible.value = false
}

const closeEventDetail = () => {
  selectedEvent.value = null
}

const rebuildProject = () => {
  alert(`${selectedEvent.value.project} 프로젝트를 다시 빌드합니다.`)
  closeEventDetail()
}

const viewErrorDetails = () => {
  alert(`${selectedEvent.value.project} 에러를 상세 분석합니다.`)
  closeEventDetail()
}

onMounted(() => {
  // 로딩 시뮬레이션
  setTimeout(() => {
    isLoading.value = false
  }, 1000)
})
</script>

<style scoped>
.container {
  width: 100%;
  margin: 20px auto 0;
  background: #f8fafc;
}

/* 스켈레톤 UI */
.skeleton-container {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.skeleton-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.skeleton-title {
  height: 36px;
  width: 200px;
  background: #e2e8f0;
  border-radius: 8px;
}

.skeleton-controls {
  height: 40px;
  width: 300px;
  background: #e2e8f0;
  border-radius: 8px;
}

.skeleton-calendar {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
}

.skeleton-day {
  height: 120px;
  background: white;
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
/* 캘린더 컨트롤 */
.calendar-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.month-navigation {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
}

.nav-btn:hover {
  background: #e2e8f0;
  color: #1e293b;
}

.current-month {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  min-width: 200px;
  text-align: center;
}

/* 캘린더 */
.calendar-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  width: 100%;
  background: #e2e8f0;
}

.weekday-header {
  background: #f8fafc;
  padding: 16px 8px;
  text-align: center;
  font-weight: 600;
  color: #64748b;
  font-size: 14px;
}

.calendar-cell {
  background: white;
  min-height: 120px;
  padding: 12px 8px 8px 8px;
  position: relative;
  transition: background-color 0.2s ease;
  display: flex;
  flex-direction: column;
}

.calendar-cell:hover {
  background: #f8fafc;
}

.calendar-cell.other-month {
  background: #f8fafc;
  color: #cbd5e1;
}

.calendar-cell.today {
  background: #eff6ff;
}

.calendar-cell.today .date-number {
  background: #2563eb;
  color: white;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.date-number {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
  align-self: flex-start;
}

.badges-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: auto;
  align-self: flex-end;
}

.event-badge-with-count {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 4px 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 10px;
  font-weight: 600;
  margin-bottom: 2px;
}

.event-badge-with-count:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.badge-build {
  background: #dcfce7;
  color: #166534;
  border-color: #bbf7d0;
}

.badge-error {
  background: #fef2f2;
  color: #dc2626;
  border-color: #fecaca;
}

.badge-text {
  white-space: nowrap;
}

/* 모달 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.title-badge {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-badge .badge-dot {
  width: 10px;
  height: 10px;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: none;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-weight: 500;
  color: #64748b;
  font-size: 14px;
}

.detail-value {
  color: #1e293b;
  font-weight: 500;
}

.commit-hash {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-success {
  background: #dcfce7;
  color: #166534;
}

.status-failed {
  background: #fef2f2;
  color: #dc2626;
}

.status-running {
  background: #fef3c7;
  color: #d97706;
}

.logs-section {
  margin-top: 24px;
}

.logs-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px 0;
}

.logs-container {
  background: #1e293b;
  border-radius: 8px;
  padding: 16px;
  max-height: 200px;
  overflow-y: auto;
}

.logs-content {
  color: #e2e8f0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  white-space: pre-wrap;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 24px;
  border-top: 1px solid #e2e8f0;
}

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
}

.btn-secondary {
  background: #f1f5f9;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

.btn-primary {
  background: var(--main-color, #2563eb);
  color: white;
}

.btn-primary:hover {
  background: var(--main-color-hover, #1d4ed8);
}

/* 반응형 */
@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .calendar-controls {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .calendar-cell {
    min-height: 80px;
    padding: 8px 4px 4px 4px;
  }

  .date-number {
    font-size: 14px;
  }

  .event-time {
    font-size: 10px;
  }

  .event-badge-small {
    width: 16px;
    height: 16px;
  }

  .badge-dot {
    width: 6px;
    height: 6px;
  }

  .modal-content {
    margin: 20px;
    max-height: calc(100vh - 40px);
  }

  .modal-footer {
    flex-direction: column-reverse;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}

/* 드롭다운 */
.dropdown-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}

.dropdown-menu {
  position: fixed;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 1px solid #e2e8f0;
  min-width: 280px;
  max-width: 350px;
  max-height: 300px;
  overflow: hidden;
  z-index: 1000;
}

.dropdown-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.dropdown-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.dropdown-list {
  max-height: 300px;
  overflow-y: auto;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  border-bottom: 1px solid #f1f5f9;
}

.dropdown-item:hover {
  background: #f8fafc;
}

.dropdown-item:last-child {
  border-bottom: none;
}

.item-number {
  font-weight: 600;
  color: #64748b;
  font-size: 14px;
  min-width: 20px;
}

.item-content {
  flex: 1;
}

.item-title {
  font-size: 13px;
  font-weight: 500;
  color: #1e293b;
}

.item-time {
  font-size: 12px;
  color: #64748b;
  display: inline;
  margin-left: 4px;
}

.item-status {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 10px;
}

.item-status.status-success {
  background: #dcfce7;
  color: #166534;
}

.item-status.status-failed {
  background: #fef2f2;
  color: #dc2626;
}
</style>
