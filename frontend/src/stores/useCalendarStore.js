import { defineStore } from "pinia"
import { reactive, ref } from "vue"
import { calendarApi } from "@/api/CalendarApi.js"

export const useCalendarStore = defineStore(
    "calendarStore",
    () => {
        const isFetched = ref(false)
        const isLoading = ref(false)

        const calendarData = reactive({
            events: [],
            currentMonth: new Date().getMonth(),
            currentYear: new Date().getFullYear(),
            selectedInfoId: null,
        })

        // 특정 날짜의 이벤트 조회
        async function fetchEventsByDate(infoId, date) {
            try {
                isLoading.value = true
                const response = await calendarApi.getEventsByDate(infoId, date)

                if (response.status === 200) {
                    return response.data.data // 백엔드 응답 구조에 맞게
                } else {
                    console.error("Failed to fetch events:", response)
                    return []
                }
            } catch (error) {
                console.error("Error fetching events:", error)
                return []
            } finally {
                isLoading.value = false
            }
        }

        // 특정 날짜의 요약 조회
        async function fetchSummaryByDate(infoId, date) {
            try {
                const response = await calendarApi.getSummaryByDate(infoId, date)

                if (response.status === 200) {
                    return response.data.data
                } else {
                    console.error("Failed to fetch summary:", response)
                    return { buildCount: 0, errorCount: 0 }
                }
            } catch (error) {
                console.error("Error fetching summary:", error)
                return { buildCount: 0, errorCount: 0 }
            }
        }

        // 월별 데이터 로드 (최적화)
        async function fetchMonthData(infoId, year, month) {
            try {
                isLoading.value = true
                calendarData.selectedInfoId = infoId
                calendarData.currentYear = year
                calendarData.currentMonth = month

                const response = await calendarApi.getEventsByMonth(infoId, year, month)

                if (response.status === 200) {
                    calendarData.events = response.data.data // [{date, events:[...]}, ...]
                    isFetched.value = true
                } else {
                    console.error("Failed to fetch month events:", response)
                    calendarData.events = []
                    isFetched.value = false
                }
            } catch (error) {
                console.error("Error fetching month data:", error)
                calendarData.events = []
                isFetched.value = false
            } finally {
                isLoading.value = false
            }
        }

        function reset() {
            calendarData.events = []
            calendarData.selectedInfoId = null
            isFetched.value = false
        }

        function getCalendarData() {
            return calendarData
        }

        return {
            calendarData,
            isLoading,
            isFetched,
            fetchEventsByDate,
            fetchSummaryByDate,
            fetchMonthData,
            getCalendarData,
            reset,
        }
    },
    {
        persist: {
            enabled: true,
            strategies: [
                {
                    storage: sessionStorage,
                    paths: ["calendarData"],
                },
            ],
        },
    },
)
