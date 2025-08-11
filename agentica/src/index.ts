import {
  Agentica,
  AgenticaHistory,
  IAgenticaHistoryJson,
  assertHttpController,
} from "@agentica/core";
import {
  AgenticaRpcService,
  IAgenticaRpcListener,
  IAgenticaRpcService,
} from "@agentica/rpc";
import * as cookie from "cookie";
import dotenv from "dotenv";
import OpenAI from "openai";
import { WebSocketServer } from "tgrid";
import { Primitive } from "typia";

import { SGlobal } from "./SGlobal";
import { PersistedRpcService } from "./services/persisted-rpc-service";
import { fetchHistoriesFromSpring } from "./services/spring-history-client";

dotenv.config();

const main = async (): Promise<void> => {
  if (SGlobal.env.OPENAI_API_KEY === undefined)
    console.error("env.OPENAI_API_KEY is not defined.");

  const swaggerDoc = await fetch("https://www.pipely.kro.kr/v3/api-docs").then(
    (r) => r.json(),
  );

  const server: WebSocketServer<
    null,
    IAgenticaRpcService<"chatgpt">,
    IAgenticaRpcListener
  > = new WebSocketServer();

  await server.open(Number(SGlobal.env.PORT), async (acceptor) => {
    const headers = (acceptor as any).request_?.headers;
    let accessToken = "";
    if (headers) {
      const cookieHeader = headers.cookie || headers.Cookie;
      if (cookieHeader) {
        const cookies = cookie.parse(cookieHeader);
        accessToken = cookies.access || "";
      }
    }
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
              Authorization: accessToken ? `Bearer ${accessToken}` : "",
            },
          },
        }),
      ],
      config: {
        systemPrompt: {
          describe: () =>
            [
              "모든 응답은 한국어 마크다운으로 작성한다.",
              "출력은 데이터 내용과 상황에 따라 다음 규칙을 적용한다.",
              "",
              "## 1. 성공/실패 여부만 있는 경우",
              "- 응답에 '성공 여부', 'success', 'status' 등의 불리언 필드가 있고, 다른 주요 데이터가 없으면:",
              "  - true → `{행위명}에 성공했습니다!`",
              "  - false → `{행위명}에 실패했습니다.`",
              "- 행위명은 API나 컨텍스트에서 추론 (예: job 생성, 삭제, 수정, 빌드 등).",
              "- 메시지(message) 필드가 있으면 결과 뒤에 소괄호로 추가.",
              "",
              "## 2. 단일 객체 응답",
              "- 필드는 4~6개까지만 핵심을 선택.",
              "- 각 필드: `- 라벨: 값` 형식으로 나열.",
              "- 값이 null/빈 문자열이면 생략.",
              "- 날짜/시간은 YYYY-MM-DD HH:mm 형식으로 변환.",
              "",
              "## 3. 목록(배열) 응답",
              "- 항목 수와 제목을 헤더에 표시: `### {항목명} (총 N개)`",
              "- 표로 출력, 컬럼은 최대 4개 (핵심 필드만).",
              "- 값이 긴 경우 줄임표(...) 사용.",
              "",
              "## 4. 공통 규칙",
              "- 절대 장황한 설명, 사족, 요약문 금지.",
              "- 오직 결과 데이터만 명확하고 깔끔하게 표현.",
              "- 표/리스트는 마크다운 표준 문법 사용.",
              "- 불필요한 문장 부호, 감탄사, '추가로 궁금한 점...' 금지.",
            ].join("\n"),

          common: () =>
            [
              "너는 Pipely CI/CD 도우미다.",
              "톤은 간결하고 직관적이며, 존댓말을 사용한다.",
              "가능하면 API/함수를 활용하되, 결과 설명은 마크다운 규칙(describe)에 따른다.",
              "불필요한 서론/후기는 쓰지 않는다.",
            ].join("\n"),
        },

        locale: "ko-KR",
        timezone: "Asia/Seoul",
        retry: 3,
      },
      histories: await fetchHistoriesFromSpring(accessToken),
    });
    const service = new PersistedRpcService<"chatgpt">(accessToken, agent, {
      agent,
      listener: acceptor.getDriver(),
    });
    await acceptor.accept(service);
  });
};
main().catch(console.error);
