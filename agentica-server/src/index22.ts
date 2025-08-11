// .env.local 환경변수 로드
import dotenv from "dotenv";
import path from "path";
dotenv.config({ path: path.resolve(process.cwd(), ".env.local") });

// Agentica와 관련 타입 불러오기
import {
    Agentica,
    IAgenticaHistoryJson,
    IAgenticaEventJson,
    assertHttpController,
} from "@agentica/core";
import {
    AgenticaRpcService,
    IAgenticaRpcListener,
    IAgenticaRpcService,
} from "@agentica/rpc";

// OpenAI API 사용
import OpenAI from "openai";
import type { ChatCompletionMessageParam } from "openai/resources/chat/completions";

// WebSocketServer (TGrid)
import { WebSocketServer } from "tgrid";

// 프로젝트 공용 설정
import { SGlobal } from "./SGlobal";
import { randomUUID } from "node:crypto";

/* =========================================================
   1. 기본 프롬프트 정의 (한국어 전용, CI/CD 도메인 맞춤형)
========================================================= */
const getPromptHistories = async (id: string): Promise<IAgenticaHistoryJson[]> => {
    return [
        {
            id: randomUUID(),
            type: "systemMessage",
            created_at: new Date().toISOString(),
            text: `
너는 CI/CD 자동배포 시스템의 **한국어 전용 어시스턴트**야.

⚠️ 반드시 지켜야 할 규칙:
1. 응답은 무조건 한국어 존댓말로 작성하세요.
2. 영어 문장 전체를 작성하는 것은 ❌ 금지입니다.
3. 영어 기술 용어는 꼭 필요할 때만 한국어 설명 뒤 괄호 안에 짧게 병기하세요.
4. 영어 제목/헤더(Response Overview, Summary 등) 금지
5. 진행 상태와 결과는 이모지로 표현
6. 다음 액션 제안 포함
7. Swagger/HTTP 도구 원문(영문) 응답은 번역·요약하여 제공
            `,
        },
    ];
};

/* =========================================================
   2. OpenAI 클라이언트 & 보조 함수
========================================================= */
const openai = new OpenAI({ apiKey: SGlobal.env.OPENAI_API_KEY });

// 긴 원문 응답 잘라내기
function sanitizeSource(src: string, max = 6000) {
    if (!src) return "";
    return src.length > max ? src.slice(0, max) + "\n...(truncated)" : src;
}

/* =========================================================
   3. 모든 API 응답을 한국어로 번역/요약하는 함수
========================================================= */
async function finalizeKorean(textOrJson: string): Promise<string> {
    const source = sanitizeSource(textOrJson ?? "");

    const chatMsgs: ChatCompletionMessageParam[] = [
        {
            role: "system",
            content: `
너는 CI/CD 자동배포 시스템의 **한국어 전용 어시스턴트**야.
- 원문 API 응답(영문) → 사용자가 보기 좋게 한국어로 번역·요약
- 표는 필수 아님, 필요시 굵은 글씨/목록
- 이모지로 상태 표시
- 핵심 정보만 남기기
`.trim(),
        },
        {
            role: "user",
            content: `
다음 API 응답을 한국어로 간결하게 설명해 주세요.
[API 응답]
"""${source}"""
`.trim(),
        },
    ];

    try {
        const resp = await openai.chat.completions.create({
            model: "gpt-4o-mini",
            temperature: 0.2,
            messages: chatMsgs,
        });
        return resp.choices[0]?.message?.content ?? "요청이 정상 처리되었습니다.";
    } catch (e) {
        console.error("[finalizeKorean] error:", e);
        return "요청이 정상 처리되었습니다.";
    }
}

/* =========================================================
   4. 메인 서버 실행
========================================================= */
const main = async (): Promise<void> => {
    if (!SGlobal.env.OPENAI_API_KEY) {
        console.error("❌ OPENAI_API_KEY is not defined in .env.local");
        return;
    }

    // Swagger 문서 로드
    const swaggerDoc = await fetch("https://www.pipely.kro.kr/v3/api-docs").then((r) =>
        r.json(),
    );

    const port = Number(SGlobal.env.PORT) || 3001;
    const server: WebSocketServer<
        null,
        IAgenticaRpcService<"chatgpt">,
        IAgenticaRpcListener
    > = new WebSocketServer();

    await server.open(port, async (acceptor) => {
        // HTTP 요청 헤더/쿠키 추출
        const headers = (acceptor as any).request_?.headers;
        const cookieHeader = headers?.cookie || headers?.Cookie || "";

        // URL 경로에서 프롬프트 히스토리 구분용 ID 추출
        const url: URL = new URL(`http://localhost${acceptor.path}`);
        const pathName = url.pathname.slice(1) || "default";
        const histories = await getPromptHistories(pathName);

        /* -----------------------------
           Agentica 인스턴스 생성
        ----------------------------- */
        const agent: Agentica<"chatgpt"> = new Agentica({
            model: "chatgpt",
            vendor: {
                api: new OpenAI({ apiKey: SGlobal.env.OPENAI_API_KEY }),
                model: "gpt-4o-mini",
            },
            controllers: [
                assertHttpController({
                    name: "Auto CI/CD swagger",
                    document: swaggerDoc,
                    model: "chatgpt",
                    connection: {
                        host: "https://www.pipely.kro.kr",
                        headers: {
                            "Content-Type": "application/json",
                            ...(cookieHeader ? { Cookie: cookieHeader } : {}),
                        },
                    },
                }),
            ],
            histories, // 위에서 만든 한국어 전용 프롬프트 적용
        });

        /* -----------------------------
           describe → assistantMessage 변환 로직
           (첫 describe 응답만 오고 끝나던 문제를 방지)
        ----------------------------- */
        const raw = acceptor.getDriver() as unknown as IAgenticaRpcListener;

        let lastDescribe: IAgenticaEventJson.IDescribe | null = null;
        let promoteTimer: NodeJS.Timeout | null = null;
        const PROMOTE_DELAY_MS = 1000; // 1초 대기 후 변환

        const proxyListener: IAgenticaRpcListener = {
            // 사용자 메시지 그대로 전달
            userMessage: async (m) => {
                if (raw.userMessage) await raw.userMessage(m);
            },
            // 모델이 직접 보낸 응답
            assistantMessage: async (m) => {
                if (promoteTimer) {
                    clearTimeout(promoteTimer);
                    promoteTimer = null;
                }
                await raw.assistantMessage(m);
            },
            // describe 이벤트 (도구 실행 결과)
            describe: async (d) => {
                // describe 이벤트를 일정 시간 후 assistantMessage로 변환 예약
                lastDescribe = d;
                if (promoteTimer) clearTimeout(promoteTimer);
                promoteTimer = setTimeout(async () => {
                    const ld = lastDescribe;
                    lastDescribe = null;
                    promoteTimer = null;
                    if (!ld) return;

                    const source =
                        ld.text ?? JSON.stringify((ld as any).executes ?? {}, null, 2);
                    const text = await finalizeKorean(source);

                    await raw.assistantMessage({
                        id: ld.id || `tool_${Date.now()}`,
                        type: "assistantMessage",
                        created_at: (ld as any).created_at ?? new Date().toISOString(),
                        text,
                        done: true,
                    } as unknown as IAgenticaHistoryJson.IAssistantMessage);
                }, PROMOTE_DELAY_MS);

                // describe 원본 그대로 프론트로 전달 (UI에서 숨길 수도 있음)
                await raw.describe(d);

                // 만약 done:true면 바로 변환 (지연 없이)
                if (d.done) {
                    if (promoteTimer) {
                        clearTimeout(promoteTimer);
                        promoteTimer = null;
                    }
                    const source =
                        d.text ?? JSON.stringify((d as any).executes ?? {}, null, 2);
                    const text = await finalizeKorean(source);

                    await raw.assistantMessage({
                        id: d.id,
                        type: "assistantMessage",
                        created_at: (d as any).created_at ?? new Date().toISOString(),
                        text,
                        done: true,
                    } as unknown as IAgenticaHistoryJson.IAssistantMessage);
                }
            },
        };

        // RPC 서비스로 프록시 리스너 등록
        const service = new AgenticaRpcService<"chatgpt">({
            agent,
            listener: proxyListener,
        });

        await acceptor.accept(service);
        console.log("🤖 아젠티카 연결 성공!");
    });

    console.log(`🚀 Agentica 서버 실행됨 (포트: ${port})`);
};

// 서버 시작
main().catch((err) => {
    console.error("❌ 서버 실행 중 에러 발생:", err);
});
