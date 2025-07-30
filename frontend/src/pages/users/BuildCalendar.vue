<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useCalendarStore } from '@/stores/useCalendarStore.js'
import { useUserStore } from '@/stores/useUserStore.js'

// Stores
const calendarStore = useCalendarStore()
const userStore = useUserStore()

// Reactive data
const selectedInfoId = ref('')
const selectedEvent = ref(null)
const dropdownVisible = ref(false)
const dropdownEvents = ref([])
const dropdownPosition = ref({ x: 0, y: 0 })
const dropdownType = ref('')
const dropdownListRef = ref(null)
const displayedEventsCount = ref(4) // 초기 표시 개수를 4로 변경
const currentDate = reactive({
  year: new Date().getFullYear(),
  month: new Date().getMonth()
})
const weekdays = ['일', '월', '화', '수', '목', '금', '토']

// Computed
const isLoading = computed(() => calendarStore.isLoading)
const selectedServerInfo = computed(() => {
  if (!selectedInfoId.value) return null
  return userStore.userInfo.infoList.find(info => info.id === selectedInfoId.value)
})

const currentMonthYear = computed(() => {
  const date = new Date(currentDate.year, currentDate.month)
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long' })
})

const displayedDropdownEvents = computed(() => {
  return dropdownEvents.value.slice(0, displayedEventsCount.value)
})

const hasMoreEvents = computed(() => {
  return dropdownEvents.value.length > displayedEventsCount.value
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

    // 해당 날짜의 이벤트 찾기
    const dateStr = formatDateString(date)
    const dayEvents = getEventsForDate(dateStr)

    dates.push({
      key: `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`,
      day: date.getDate(),
      date: new Date(date),
      isCurrentMonth: date.getMonth() === currentDate.month,
      isToday: date.toDateString() === today.toDateString(),
      isSunday: date.getDay() === 0 && date.getMonth() === currentDate.month, // 일요일 체크
      events: dayEvents
    })
  }

  return dates
})

// Methods
const formatDateString = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getEventsForDate = (dateStr) => {
  const calendarData = calendarStore.getCalendarData()
  const dayData = calendarData.events.find(item => item.date === dateStr)
  return dayData ? dayData.events : []
}

const formatTime = (timeString) => {
  // "2025-07-24 13:20:00" -> "13:20"
  return timeString.split(' ')[1]?.substring(0, 5) || timeString
}

const formatDateTime = (timeString) => {
  const date = new Date(timeString)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const previousMonth = async () => {
  if (currentDate.month === 0) {
    currentDate.month = 11
    currentDate.year--
  } else {
    currentDate.month--
  }
  if (selectedInfoId.value) {
    await calendarStore.fetchSummaryByMonth(
        selectedInfoId.value,
        currentDate.year,
        currentDate.month
    )
  }
}

const nextMonth = async () => {
  if (currentDate.month === 11) {
    currentDate.month = 0
    currentDate.year++
  } else {
    currentDate.month++
  }
  if (selectedInfoId.value) {
    await calendarStore.fetchSummaryByMonth(
        selectedInfoId.value,
        currentDate.year,
        currentDate.month
    )
  }
}

// 오늘로 돌아가기 함수 추가
const goToToday = async () => {
  const today = new Date()
  currentDate.year = today.getFullYear()
  currentDate.month = today.getMonth()
  if (selectedInfoId.value) {
    await calendarStore.fetchSummaryByMonth(
        selectedInfoId.value,
        currentDate.year,
        currentDate.month
    )
  }
}

const getBadgesForDate = (dateStr) => {
  const summaries = calendarStore.calendarData.summaries || {}
  const daySummary = summaries[dateStr]
  const badges = []

  if (daySummary?.buildCount > 0) {
    badges.push({ type: 'BUILD', count: daySummary.buildCount })
  }
  if (daySummary?.errorCount > 0) {
    badges.push({ type: 'ERROR', count: daySummary.errorCount })
  }

  return badges
}

const openEventDropdown = (event, type, events, dateStr) => {
  event.stopPropagation()
  const rect = event.target.getBoundingClientRect()
  dropdownPosition.value = { x: rect.left, y: rect.bottom + 8 }

  const cachedDay = calendarStore.calendarData.events.find(d => d.date === dateStr)
  const allEvents = cachedDay ? cachedDay.events : []

  // 선택된 타입에 맞게 필터링
  dropdownEvents.value = allEvents.filter(e => e.type === type)
  dropdownType.value = type
  displayedEventsCount.value = 4
  dropdownVisible.value = true

  nextTick(() => {
    if (dropdownListRef.value) {
      dropdownListRef.value.scrollTop = 0
    }
  })
}

const handleDropdownScroll = () => {
  if (!dropdownListRef.value) return

  const { scrollTop, scrollHeight, clientHeight } = dropdownListRef.value

  // 스크롤이 하단 근처에 도달했을 때
  if (scrollTop + clientHeight >= scrollHeight - 10 && hasMoreEvents.value) {
    displayedEventsCount.value += 4 // 4개씩 증가
  }
}

const selectEventFromDropdown = (event) => {
  dropdownVisible.value = false
  selectedEvent.value = event
}

const closeDropdown = () => {
  dropdownVisible.value = false
  displayedEventsCount.value = 4
  dropdownEvents.value = []
}

const closeEventDetail = () => {
  selectedEvent.value = null
}

const onInfoChange = async () => {
  if (selectedInfoId.value) {
    await loadMonthData()
  }
}

const loadMonthData = async () => {
  if (!selectedInfoId.value) return

  await calendarStore.fetchMonthData(
      selectedInfoId.value,
      currentDate.year,
      currentDate.month
  )
}

// Lifecycle
onMounted(async () => {
  if (!userStore.isFetched) {
    await userStore.fetchUserInfo()
  }

  if (userStore.userInfo.infoList.length > 0) {
    selectedInfoId.value = userStore.userInfo.infoList[0].id
    // summary 대신 monthData 한 번만 호출
    await calendarStore.fetchMonthData(
        selectedInfoId.value,
        currentDate.year,
        currentDate.month
    )
  }
})
</script>

<template>
  <div class="calendar-container">
    <!-- 스켈레톤 UI -->
    <div v-if="isLoading" class="skeleton-container">
      <div class="skeleton-section">
        <div class="skeleton-section-title"></div>
        <div class="skeleton-selector"></div>
        <div class="skeleton-controls"></div>
        <div class="skeleton-calendar">
          <div v-for="i in 42" :key="i" class="skeleton-day"></div>
        </div>
      </div>
    </div>

    <!-- 실제 컨텐츠 -->
    <div v-else class="section">
      <h3 class="section-title">
        <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
          <rect height="18" rx="2" ry="2" width="18" x="3" y="4"/>
          <path d="M16 2v4"/>
          <path d="M8 2v4"/>
          <path d="M3 10h18"/>
        </svg>
        빌드 캘린더
      </h3>

      <!-- Jenkins Info 선택 -->
      <div class="info-selector" v-if="userStore.userInfo.infoList.length > 0">
        <div class="selector-card">
          <div class="selector-header">
            <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <rect height="14" rx="2" ry="2" width="20" x="2" y="3"/>
              <line x1="8" x2="16" y1="21" y2="21"/>
              <line x1="12" x2="12" y1="17" y2="21"/>
            </svg>
            <span class="selector-title">Jenkins 서버</span>
          </div>
          <div class="selector-content">
            <select v-model="selectedInfoId" @change="onInfoChange" class="info-select">
              <option value="">서버를 선택해주세요</option>
              <option
                  v-for="info in userStore.userInfo.infoList"
                  :key="info.id"
                  :value="info.id"
              >
                {{ info.name || info.uri }}
              </option>
            </select>
            <div class="selector-status" v-if="selectedServerInfo">
              <span class="server-name"></span>
              <div class="status-indicator">
                <div class="status-dot connected"></div>
                <span class="status-text">연결됨</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 빈 상태 (Jenkins 서버가 없을 때) -->
      <div v-if="userStore.userInfo.infoList.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg fill="none" height="64" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24" width="64">
            <rect height="18" rx="2" ry="2" width="18" x="3" y="4"/>
            <path d="M16 2v4"/>
            <path d="M8 2v4"/>
            <path d="M3 10h18"/>
          </svg>
        </div>
        <h4 class="empty-title">등록된 Jenkins 서버가 없습니다</h4>
        <p class="empty-description">Jenkins 서버를 추가하여 빌드 일정을 확인해보세요.</p>
      </div>

      <!-- 캘린더 컨텐츠 -->
      <div v-else-if="selectedInfoId" class="calendar-content">
        <!-- 캘린더 컨트롤 -->
        <div class="calendar-controls">
          <div class="month-navigation">
            <button class="nav-btn" @click="previousMonth">
              <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <polyline points="15,18 9,12 15,6"/>
              </svg>
            </button>
            <h4 class="current-month">{{ currentMonthYear }}</h4>
            <button class="nav-btn" @click="nextMonth">
              <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <polyline points="9,18 15,12 9,6"/>
              </svg>
            </button>
          </div>
          <!-- 오늘로 돌아가기 버튼 추가 -->
          <button
              class="btn btn-primary today-btn"
              @click="goToToday"
              title="오늘로 돌아가기"
          >
            <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12,6 12,12 16,14"/>
            </svg>
            <span class="today-btn-text">오늘</span>
          </button>
        </div>

        <!-- 캘린더 그리드 -->
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
              'has-events': date.events.length > 0,
              'sunday': date.isSunday // 일요일 클래스 추가
            }]"
          >
            <div class="date-number">{{ date.day }}</div>
            <!-- 배지 컨테이너 -->
            <div class="badges-container">
              <div
                  v-for="badge in getBadgesForDate(formatDateString(date.date))"
                  :key="badge.type"
                  :class="['event-badge-with-count', `badge-${badge.type.toLowerCase()}`]"
                  @click="openEventDropdown($event, badge.type, badge.events, formatDateString(date.date))"
                  :title="`${badge.type} ${badge.count}개`"
              >
                <span class="badge-text">{{ badge.type.toUpperCase() }} *{{ badge.count }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 서버 선택 안내 -->
      <div v-else class="select-server-state">
        <div class="select-icon">
          <svg fill="none" height="48" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24" width="48">
            <rect height="14" rx="2" ry="2" width="20" x="2" y="3"/>
            <line x1="8" x2="16" y1="21" y2="21"/>
            <line x1="12" x2="12" y1="17" y2="21"/>
          </svg>
        </div>
        <p class="select-description">Jenkins 서버를 선택하여 빌드 캘린더를 확인하세요.</p>
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
          <div class="dropdown-title">
            <div :class="['dropdown-icon', `icon-${dropdownType.toLowerCase()}`]">
              <svg v-if="dropdownType === 'BUILD'" fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <polyline points="20,6 9,17 4,12"/>
              </svg>
              <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" x2="9" y1="9" y2="15"/>
                <line x1="9" x2="15" y1="9" y2="15"/>
              </svg>
            </div>
            <h4>{{ dropdownType === 'BUILD' ? 'Build 목록' : 'Error 목록' }}</h4>
          </div>
          <div class="dropdown-count">
            총 {{ dropdownEvents.length }}개
          </div>
        </div>
        <div
            ref="dropdownListRef"
            class="dropdown-list"
            @scroll="handleDropdownScroll"
        >
          <div
              v-for="(event, index) in displayedDropdownEvents"
              :key="event.id || index"
              class="dropdown-item"
              @click="selectEventFromDropdown(event)"
          >
            <div class="item-index">{{ index + 1 }}</div>
            <div class="item-main">
              <div class="item-header">
                <div class="item-title">{{ event.jobName }}</div>
                <div class="item-build-number">#{{ event.buildNumber }}</div>
              </div>
              <div class="item-time">
                <svg fill="none" height="12" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="12">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12,6 12,12 16,14"/>
                </svg>
                {{ formatTime(event.start) }}
              </div>
            </div>
            <div :class="['item-status', `status-${event.type === 'ERROR' ? 'failed' : 'success'}`]">
              <div class="status-dot"></div>
              <span>{{ event.type === 'ERROR' ? '실패' : '성공' }}</span>
            </div>
          </div>
          <div v-if="hasMoreEvents" class="dropdown-loading">
            <div class="loading-spinner"></div>
            <span>더 많은 항목 로딩 중...</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 이벤트 상세 팝업 -->
    <div v-if="selectedEvent" class="modal-overlay" @click="closeEventDetail">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <div class="modal-title-section">
            <div :class="['modal-icon', `icon-${selectedEvent.type.toLowerCase()}`]">
              <svg v-if="selectedEvent.type === 'BUILD'" fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <polyline points="20,6 9,17 4,12"/>
              </svg>
              <svg v-else fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" x2="9" y1="9" y2="15"/>
                <line x1="9" x2="15" y1="9" y2="15"/>
              </svg>
            </div>
            <div class="modal-title-text">
              <h3 class="modal-title">{{ selectedEvent.type === 'BUILD' ? 'Build 정보' : 'Error 정보' }}</h3>
              <div :class="['modal-status', `status-${selectedEvent.type === 'ERROR' ? 'failed' : 'success'}`]">
                <div class="status-dot"></div>
                <span>{{ selectedEvent.type === 'ERROR' ? '빌드 실패' : '빌드 성공' }}</span>
              </div>
            </div>
          </div>
          <button class="close-btn" @click="closeEventDetail">
            <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
              <line x1="18" x2="6" y1="6" y2="18"/>
              <line x1="6" x2="18" y1="6" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="detail-cards">
            <div class="detail-card">
              <div class="detail-card-header">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <rect height="14" rx="2" ry="2" width="20" x="2" y="3"/>
                  <line x1="8" x2="16" y1="21" y2="21"/>
                  <line x1="12" x2="12" y1="17" y2="21"/>
                </svg>
                <span>Job 정보</span>
              </div>
              <div class="detail-card-content">
                <div class="detail-item">
                  <span class="detail-label">Job 이름</span>
                  <span class="detail-value">{{ selectedEvent.jobName }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">빌드 번호</span>
                  <span class="detail-value build-number">#{{ selectedEvent.buildNumber }}</span>
                </div>
              </div>
            </div>
            <div class="detail-card">
              <div class="detail-card-header">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12,6 12,12 16,14"/>
                </svg>
                <span>실행 정보</span>
              </div>
              <div class="detail-card-content">
                <div class="detail-item">
                  <span class="detail-label">시작 시간</span>
                  <span class="detail-value">{{ formatDateTime(selectedEvent.start) }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">타입</span>
                  <span :class="['detail-value', 'type-badge', `type-${selectedEvent.type.toLowerCase()}`]">
                    {{ selectedEvent.type }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeEventDetail">
            <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <line x1="18" x2="6" y1="6" y2="18"/>
              <line x1="6" x2="18" y1="6" y2="18"/>
            </svg>
            닫기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 기존 스타일 유지 */
.calendar-container {
  width: 100%;
}

.section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
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

/* 스켈레톤 UI */
.skeleton-container {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.skeleton-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.skeleton-section-title {
  height: 24px;
  width: 150px;
  background: #e2e8f0;
  border-radius: 6px;
  margin-bottom: 20px;
}

.skeleton-selector {
  height: 40px;
  width: 300px;
  background: #f1f5f9;
  border-radius: 8px;
  margin-bottom: 20px;
}

.skeleton-controls {
  height: 40px;
  width: 200px;
  background: #f1f5f9;
  border-radius: 8px;
  margin: 0 auto 20px;
}

.skeleton-calendar {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.skeleton-day {
  height: 100px;
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

/* Jenkins 서버 선택 */
.info-selector {
  margin-bottom: 24px;
}

.selector-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.2s ease;
}

.selector-card:hover {
  border-color: #2563eb;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.1);
}

.selector-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.selector-header svg {
  color: #2563eb;
}

.selector-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.selector-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-select {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  font-size: 14px;
  outline: none;
  transition: all 0.2s ease;
}

.info-select:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.selector-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.server-name {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
}

.status-text {
  font-size: 12px;
  color: #059669;
  font-weight: 500;
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

/* 서버 선택 안내 */
.select-server-state {
  text-align: center;
  padding: 40px 20px;
}

.select-icon {
  margin-bottom: 16px;
  color: #cbd5e1;
}

.select-description {
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

/* 캘린더 컨텐츠 */
.calendar-content {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

/* 캘린더 컨트롤 */
.calendar-controls {
  display: flex;
  justify-content: center; /* 년월을 중앙에 배치 */
  align-items: center;
  padding: 20px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  position: relative; /* today-btn의 absolute 포지셔닝을 위한 기준 */
}

.month-navigation {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-month {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  min-width: 200px; /* 년월 텍스트가 중앙에 오도록 최소 너비 유지 */
  text-align: center;
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

.btn-primary:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
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

.today-btn {
  position: absolute;
  right: 20px;
}

.today-btn-text {
  white-space: nowrap;
}

/* 캘린더 그리드 */
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: white;
}

.weekday-header {
  background: #f8fafc;
  padding: 12px 8px;
  text-align: center;
  font-weight: 600;
  color: #64748b;
  font-size: 14px;
  border-bottom: 1px solid #e2e8f0;
}

.calendar-cell {
  background: white;
  min-height: 120px;
  padding: 12px 8px 8px 8px;
  position: relative;
  transition: background-color 0.2s ease;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #f1f5f9;
  border-bottom: 1px solid #f1f5f9;
  box-sizing: border-box;
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

.calendar-cell.sunday .date-number {
  color: #dc2626;
  font-weight: 700;
}

.calendar-cell.today.sunday .date-number {
  background: #2563eb;
  color: white;
}

.date-number {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
  align-self: flex-start;
}

.badges-container {
  position: absolute;
  bottom: 4px;
  right: 4px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: flex-end;
}

.event-badge-with-count {
  min-width: 60px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  padding: 0 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 9px;
  font-weight: 600;
  white-space: nowrap;
  border: 1px solid;
}

.event-badge-with-count:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.badge-build {
  background: #f0f9ff;
  color: #16a34a;
  border-color: #bbf7d0;
}

.badge-error {
  background: #fff2f0;
  color: #ff4d4f;
  border-color: #ffccc7;
}

.badge-text {
  white-space: nowrap;
}

/* 드롭다운 개선 */
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
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid #e2e8f0;
  width: 320px;
  max-height: 300px;
  overflow: hidden;
  z-index: 1000;
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.dropdown-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dropdown-icon {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dropdown-icon.icon-build {
  background: #f0f9ff;
  color: #16a34a;
}

.dropdown-icon.icon-error {
  background: #fff2f0;
  color: #ff4d4f;
}

.dropdown-title h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.dropdown-count {
  font-size: 12px;
  color: #64748b;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.dropdown-list {
  max-height: 220px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid #f8fafc;
  min-width: 0;
}

.dropdown-item:hover {
  background: #f8fafc;
  transform: translateX(2px);
}

.dropdown-item:last-child {
  border-bottom: none;
}

.item-index {
  width: 24px;
  height: 24px;
  background: #f1f5f9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  flex-shrink: 0;
}

.item-main {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-build-number {
  font-size: 12px;
  color: #64748b;
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.item-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
}

.item-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 12px;
  flex-shrink: 0;
}

.item-status .status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.item-status.status-success {
  background: #f0f9ff;
  color: #16a34a;
}

.item-status.status-success .status-dot {
  background: #16a34a;
}

.item-status.status-failed {
  background: #fff2f0;
  color: #ff4d4f;
}

.item-status.status-failed .status-dot {
  background: #ff4d4f;
}

.dropdown-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  color: #64748b;
  font-size: 12px;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #f1f5f9;
  border-top: 2px solid #2563eb;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 모달 개선 */
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
  backdrop-filter: blur(4px);
}

.modal-content {
  background: white;
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
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
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.modal-title-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.modal-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-icon.icon-build {
  background: #f0f9ff;
  color: #16a34a;
}

.modal-icon.icon-error {
  background: #fff2f0;
  color: #ff4d4f;
}

.modal-title-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.modal-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
}

.modal-status .status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.modal-status.status-success {
  color: #16a34a;
}

.modal-status.status-success .status-dot {
  background: #16a34a;
}

.modal-status.status-failed {
  color: #ff4d4f;
}

.modal-status.status-failed .status-dot {
  background: #ff4d4f;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: none;
  border: none;
  border-radius: 8px;
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

.detail-cards {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
}

.detail-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.detail-card-header svg {
  color: #2563eb;
}

.detail-card-content {
  padding: 20px;
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
  font-size: 14px;
}

.build-number {
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 6px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.type-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.type-badge.type-build {
  background: #f0f9ff;
  color: #16a34a;
}

.type-badge.type-error {
  background: #fff2f0;
  color: #ff4d4f;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 24px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}

.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary {
  background: white;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

/* 반응형 */
@media (max-width: 768px) {
  .section {
    padding: 20px 16px;
  }

  .calendar-controls {
    padding: 16px;
    flex-direction: column; /* 모바일에서는 세로로 정렬 */
    gap: 16px;
    position: static; /* 모바일에서는 absolute 해제 */
  }

  .month-navigation {
    order: 1;
  }

  .today-btn {
    order: 2;
    position: static; /* 모바일에서는 absolute 해제 */
    right: auto; /* right 속성 초기화 */
    align-self: center; /* 중앙 정렬 */
  }

  .current-month {
    font-size: 20px;
    min-width: auto;
  }

  .calendar-cell {
    min-height: 80px;
    padding: 6px;
  }

  /* 배지 반응형 개선 */
  .badges-container {
    bottom: 2px;
    right: 2px;
    gap: 1px;
  }

  .event-badge-with-count {
    min-width: 45px;
    height: 14px;
    font-size: 8px;
    padding: 0 4px;
    border-radius: 7px;
  }

  .badge-text {
    font-size: 7px;
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

  .info-select {
    width: 100%;
  }

  .selector-status {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .dropdown-menu {
    width: calc(100vw - 40px);
    max-width: 320px;
  }

  .detail-cards {
    gap: 16px;
  }

  .modal-title-section {
    gap: 12px;
  }

  .modal-icon {
    width: 40px;
    height: 40px;
  }

  /* 오늘 버튼 모바일 최적화 - 텍스트 항상 보이게 */
  .today-btn-text {
    display: inline; /* 모바일에서도 텍스트 보이게 */
  }

  .today-btn {
    padding: 8px 12px;
    min-width: auto; /* 너비 자동 조절 */
  }
}

/* 더 작은 화면 (480px 이하) */
@media (max-width: 480px) {
  .calendar-cell {
    min-height: 60px;
    padding: 4px;
  }

  .date-number {
    font-size: 11px;
  }

  /* 배지 더 작게 */
  .event-badge-with-count {
    min-width: 35px;
    height: 12px;
    font-size: 7px;
    padding: 0 3px;
    border-radius: 6px;
  }

  .badge-text {
    font-size: 6px;
  }

  .badges-container {
    bottom: 1px;
    right: 1px;
  }

  .current-month {
    font-size: 18px;
  }

  .nav-btn {
    width: 36px;
    height: 36px;
  }

  .today-btn {
    padding: 6px 10px;
    min-width: auto;
  }
}

/* 태블릿 세로 모드 (768px ~ 1024px) */
@media (min-width: 769px) and (max-width: 1024px) {
  .calendar-cell {
    min-height: 100px;
  }

  .event-badge-with-count {
    min-width: 55px;
    height: 16px;
    font-size: 8px;
  }

  .badge-text {
    font-size: 8px;
  }
}
</style>
