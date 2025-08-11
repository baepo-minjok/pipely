"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.fetchHistoriesFromSpring = fetchHistoriesFromSpring;
exports.savePromptsToSpring = savePromptsToSpring;
// 히스토리 JSON 유효성(필수 최소 필드만 체크)
function isValidHistoryJson(j) {
    var _a, _b, _c;
    if (!j || typeof j !== "object")
        return false;
    const t = j.type;
    if (typeof t !== "string")
        return false;
    switch (t) {
        case "text":
            // text는 role/text 필수
            return ((j.role === "user" || j.role === "assistant") &&
                typeof j.text === "string");
        case "describe":
            // describe는 text는 문자열, executes는 배열(비어 있어도 OK)
            return typeof j.text === "string" && Array.isArray((_a = j.executes) !== null && _a !== void 0 ? _a : []);
        case "select":
            return Array.isArray((_b = j.selections) !== null && _b !== void 0 ? _b : []);
        case "cancel":
            return Array.isArray((_c = j.selections) !== null && _c !== void 0 ? _c : []);
        case "execute":
            // execute는 최소 필드들 존재
            return typeof j.id === "string" && typeof j.operation === "object";
        default:
            return false;
    }
}
/** 로드: 백엔드(me)에서 순수 IAgenticaHistoryJson[]만 뽑아서 정렬 */
function fetchHistoriesFromSpring(accessToken) {
    return __awaiter(this, void 0, void 0, function* () {
        try {
            const res = yield fetch("http://localhost:8080/api/agentica/history/me", {
                headers: { Authorization: accessToken ? `Bearer ${accessToken}` : "" },
            });
            if (!res.ok)
                return [];
            // Spring 응답 예: [{ turnIndex, type, json }, ...] 또는 [{ turnIndex, json }, ...]
            const dtos = yield res.json();
            if (!Array.isArray(dtos))
                return [];
            console.log(dtos);
            // 순수 IAgenticaHistoryJson로 변환 + 유효성 검사
            const hist = dtos
                .map((d) => {
                const j = d === null || d === void 0 ? void 0 : d.json;
                if (!j)
                    return null;
                return j;
            })
                .filter(Boolean);
            console.log(hist);
            return hist;
        }
        catch (e) {
            console.warn("[hist] load failed -> []", e);
            return [];
        }
    });
}
/** 저장: toJSON() 결과(= IAgenticaHistoryJson)를 그대로 전달 */
function savePromptsToSpring(jsons, accessToken) {
    return __awaiter(this, void 0, void 0, function* () {
        try {
            if (!Array.isArray(jsons) || jsons.length === 0)
                return;
            // 서버에서 turnIndex는 자동 부여하므로 여기선 type/json만
            const body = jsons.map((j) => ({
                type: j.type, // 참고용 (선택)
                json: j, // 핵심: IAgenticaHistoryJson 그대로
            }));
            const res = yield fetch("http://localhost:8080/api/agentica/history/me", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: accessToken ? `Bearer ${accessToken}` : "",
                },
                body: JSON.stringify(body),
            });
            if (!res.ok)
                console.error("[hist] save failed:", res.status, res.statusText);
        }
        catch (e) {
            console.error("[hist] save error:", e);
        }
    });
}
//# sourceMappingURL=spring-history-client.js.map