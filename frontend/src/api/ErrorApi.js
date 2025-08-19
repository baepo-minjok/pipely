import { instance } from "@/api/axiosInstance.js"

export const errorApi = {
    // 특정 Job의 실패한 빌드 조회
    getFailedBuildsByJob(jobId) {
        return instance
            .post("/jenkins/error/failed/job", { jobId }) // JobDto 구조
            .then((res) => {
                const data = res.data?.data
                if (!data) {
                    throw new Error("서버 응답에 실패한 빌드 데이터가 없습니다.")
                }
                return Array.isArray(data) ? data : [data]
            })
            .catch((error) => {
                console.error("API Error(getFailedBuildsByJob):", error.response?.status, error.response?.data || error.message)
                throw error.response?.data?.error || error
            })
    },

    // 실패 빌드에 대한 요약 제공
    getBuildSummaryWithSolution(jobId, buildNumber) {
        return instance
            .post("/jenkins/error/failed/summary", {
                jobId,
                buildNumber,
            }) // JobSummaryDto 구조
            .then((res) => {
                const data = res.data?.data
                if (!data) {
                    throw new Error("서버 응답에 빌드 요약 데이터가 없습니다.")
                }
                return {
                    jobName: data.jobName,
                    buildNumber: data.buildNumber,
                    summary: data.naturalResponse ?? "",   // ← 서버 필드명: naturalResponse
                    solution: null,
                    recommendations: [],
                };
            })
            .catch((error) => {
                console.error(
                    "API Error(getBuildSummaryWithSolution):",
                    error.response?.status,
                    error.response?.data || error.message,
                )
                throw error.response?.data?.error || error
            })
    },
}
