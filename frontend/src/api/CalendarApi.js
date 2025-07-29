import axios from "axios"

const instance = axios.create({
    baseURL: "/api",
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials: true,
})

export const calendarApi = {
    // 특정 날짜의 이벤트 조회
    getEventsByDate(infoId, date) {
        return instance
            .get("/calendar/events/by-date", {
                params: {
                    infoId: infoId,
                    date: date,
                },
            })
            .then((res) => {
                return res
            })
            .catch((error) => {
                return error.response.data.error
            })
    },

    // 특정 날짜의 빌드 요약 조회
    getSummaryByDate(infoId, date) {
        return instance
            .get("/calendar/summary/by-date", {
                params: {
                    infoId: infoId,
                    date: date,
                },
            })
            .then((res) => {
                return res
            })
            .catch((error) => {
                return error.response.data.error
            })
    },

    // 월별 이벤트 조회
    getEventsByMonth(infoId, year, month) {
        return instance
            .get("/calendar/events/by-month", {
                params: {
                    infoId: infoId,
                    year: year,
                    month: month + 1,
                },
            })
            .then((res) => {
                return res
            })
            .catch((error) => {
                return error.response.data.error
            })
    },
}
