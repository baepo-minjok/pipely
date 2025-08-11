"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const core_1 = require("@agentica/core");
const cookie = __importStar(require("cookie"));
const dotenv_1 = __importDefault(require("dotenv"));
const openai_1 = __importDefault(require("openai"));
const tgrid_1 = require("tgrid");
const SGlobal_1 = require("./SGlobal");
const persisted_rpc_service_1 = require("./services/persisted-rpc-service");
const spring_history_client_1 = require("./services/spring-history-client");
dotenv_1.default.config();
const getPromptHistories = (id) => __awaiter(void 0, void 0, void 0, function* () {
    // GET PROMPT HISTORIES FROM DATABASE
    id;
    return [];
});
const main = () => __awaiter(void 0, void 0, void 0, function* () {
    if (SGlobal_1.SGlobal.env.OPENAI_API_KEY === undefined)
        console.error("env.OPENAI_API_KEY is not defined.");
    const swaggerDoc = yield fetch("https://www.pipely.kro.kr/v3/api-docs").then((r) => r.json());
    const server = new tgrid_1.WebSocketServer();
    yield server.open(Number(SGlobal_1.SGlobal.env.PORT), (acceptor) => __awaiter(void 0, void 0, void 0, function* () {
        var _a;
        const headers = (_a = acceptor.request_) === null || _a === void 0 ? void 0 : _a.headers;
        let accessToken = "";
        if (headers) {
            const cookieHeader = headers.cookie || headers.Cookie;
            if (cookieHeader) {
                const cookies = cookie.parse(cookieHeader);
                accessToken = cookies.access || "";
            }
        }
        const agent = new core_1.Agentica({
            model: "chatgpt",
            vendor: {
                api: new openai_1.default({ apiKey: SGlobal_1.SGlobal.env.OPENAI_API_KEY }),
                model: "gpt-4o-mini",
            },
            controllers: [
                (0, core_1.assertHttpController)({
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
                    describe: () => [
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
                    common: () => [
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
            histories: yield (0, spring_history_client_1.fetchHistoriesFromSpring)(accessToken),
        });
        const service = new persisted_rpc_service_1.PersistedRpcService(accessToken, agent, {
            agent,
            listener: acceptor.getDriver(),
        });
        yield acceptor.accept(service);
    }));
});
main().catch(console.error);
//# sourceMappingURL=index.js.map