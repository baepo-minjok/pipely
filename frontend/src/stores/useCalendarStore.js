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
            summaries: {},
            currentMonth: new Date().getMonth(),
            currentYear: new Date().getFullYear(),
            selectedInfoId: null,
        })

        // 월별 데이터 로드 (최적화)
        async function fetchMonthData(infoId, year, month) {
            try {
                isLoading.value = true
                calendarData.selectedInfoId = infoId
                calendarData.currentYear = year
                calendarData.currentMonth = month

                const response = await calendarApi.getEventsByMonth(infoId, year, month)

                if (response.status === 200) {
                    calendarData.events = response.data.data

                    // summaries 동기화
                    calendarData.summaries = Object.fromEntries(
                        calendarData.events.map(day => {
                            const buildCount = day.events.filter(e => e.type === 'BUILD').length
                            const errorCount = day.events.filter(e => e.type === 'ERROR').length
                            return [day.date, { date: day.date, buildCount, errorCount }]
                        })
                    )

                    isFetched.value = true
                } else {
                    console.error("Failed to fetch month events:", response)
                    calendarData.events = []
                    calendarData.summaries = {}
                    isFetched.value = false
                }
            } catch (error) {
                console.error("Error fetching month data:", error)
                calendarData.events = []
                calendarData.summaries = {}
                isFetched.value = false
            } finally {
                isLoading.value = false
            }
        }

        function getCalendarData() {
            return calendarData
        }

        function reset() {
            calendarData.events = []
            calendarData.summaries = {}
            calendarData.selectedInfoId = null
            isFetched.value = false
        }


        return {
            calendarData,
            isLoading,
            isFetched,
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
