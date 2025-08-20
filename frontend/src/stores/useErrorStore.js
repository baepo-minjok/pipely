import { defineStore } from "pinia"
import { computed, ref } from "vue"
import { errorApi } from "@/api/ErrorApi"

export const useErrorStore = defineStore(
    "error",
    () => {
        // 1. 상태 정의
        const failedBuilds = ref({}) // jobId를 키로 하는 실패한 빌드 목록
        const buildSummaries = ref({}) // jobId를 키로 하는 빌드 요약 정보
        const loadingStates = ref({
            failedBuilds: false,
            buildSummary: false,
        })
        const errorMessages = ref({})

        // 2. Computed
        const getFailedBuildsByJobId = computed(() => {
            return (jobId) => failedBuilds.value[jobId] || []
        })

        const getBuildSummaryByJobId = computed(() => {
            return (jobId) => buildSummaries.value[jobId] || null
        })

        const isLoadingFailedBuilds = computed(() => loadingStates.value.failedBuilds)
        const isLoadingBuildSummary = computed(() => loadingStates.value.buildSummary)

        // 3. 함수 정의
        const resetErrorData = () => {
            failedBuilds.value = {}
            buildSummaries.value = {}
            errorMessages.value = {}
            loadingStates.value = {
                failedBuilds: false,
                buildSummary: false,
            }
        }

        // 특정 Job의 실패한 빌드 조회
        const fetchFailedBuilds = async (jobId) => {
            if (!jobId) return

            loadingStates.value.failedBuilds = true
            errorMessages.value[`failedBuilds_${jobId}`] = null

            try {
                const data = await errorApi.getFailedBuildsByJob(jobId)
                failedBuilds.value[jobId] = data
                return data
            } catch (error) {
                console.error("Failed to fetch failed builds:", error)
                errorMessages.value[`failedBuilds_${jobId}`] = error.message || "실패한 빌드 조회 중 오류가 발생했습니다."
                failedBuilds.value[jobId] = []
                throw error
            } finally {
                loadingStates.value.failedBuilds = false
            }
        }

        // 실패 빌드에 대한 요약 제공
        const fetchBuildSummary = async (jobId, buildNumber = null) => {
            if (!jobId) return

            loadingStates.value.buildSummary = true
            errorMessages.value[`buildSummary_${jobId}`] = null

            try {
                const data = await errorApi.getBuildSummaryWithSolution(jobId, buildNumber)
                buildSummaries.value[jobId] = data
                return data
            } catch (error) {
                console.error("Failed to fetch build summary:", error)
                errorMessages.value[`buildSummary_${jobId}`] = error.message || "빌드 요약 조회 중 오류가 발생했습니다."
                buildSummaries.value[jobId] = null
                throw error
            } finally {
                loadingStates.value.buildSummary = false
            }
        }

        // 특정 Job의 에러 데이터 초기화
        const clearJobErrorData = (jobId) => {
            if (failedBuilds.value[jobId]) {
                delete failedBuilds.value[jobId]
            }
            if (buildSummaries.value[jobId]) {
                delete buildSummaries.value[jobId]
            }
            if (errorMessages.value[`failedBuilds_${jobId}`]) {
                delete errorMessages.value[`failedBuilds_${jobId}`]
            }
            if (errorMessages.value[`buildSummary_${jobId}`]) {
                delete errorMessages.value[`buildSummary_${jobId}`]
            }
        }

        // 에러 메시지 가져오기
        const getErrorMessage = (type, jobId) => {
            return errorMessages.value[`${type}_${jobId}`] || null
        }

        // 실패한 빌드 통계 계산
        const getFailedBuildsStats = computed(() => {
            return (jobId) => {
                const builds = failedBuilds.value[jobId] || []
                if (builds.length === 0) return null

                const totalFailures = builds.length
                const recentFailures = builds.filter((build) => {
                    const buildDate = new Date(build.timestamp)
                    const weekAgo = new Date()
                    weekAgo.setDate(weekAgo.getDate() - 7)
                    return buildDate >= weekAgo
                }).length

                const errorTypes = builds.reduce((acc, build) => {
                    const errorType = build.result || "UNKNOWN"
                    acc[errorType] = (acc[errorType] || 0) + 1
                    return acc
                }, {})

                return {
                    totalFailures,
                    recentFailures,
                    errorTypes,
                    mostCommonError: Object.keys(errorTypes).reduce((a, b) => (errorTypes[a] > errorTypes[b] ? a : b), "UNKNOWN"),
                }
            }
        })

        return {
            // 상태
            failedBuilds,
            buildSummaries,
            loadingStates,
            errorMessages,

            // Computed
            getFailedBuildsByJobId,
            getBuildSummaryByJobId,
            isLoadingFailedBuilds,
            isLoadingBuildSummary,
            getFailedBuildsStats,

            // 함수
            fetchFailedBuilds,
            fetchBuildSummary,
            resetErrorData,
            clearJobErrorData,
            getErrorMessage,
        }
    },
    {
        persist: {
            enabled: true,
            strategies: [
                {
                    storage: sessionStorage,
                    paths: ["failedBuilds", "buildSummaries"],
                },
            ],
        },
    },
)
