import axios from "axios"

const instance = axios.create({
    baseURL: "/api",
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials: true,
})

export const calendarApi = {
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
