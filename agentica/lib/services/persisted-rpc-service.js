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
exports.PersistedRpcService = void 0;
const rpc_1 = require("@agentica/rpc");
const spring_history_client_1 = require("./spring-history-client");
class PersistedRpcService extends rpc_1.AgenticaRpcService {
    constructor(accessToken, agentRef, args) {
        super(args);
        this.accessToken = accessToken;
        this.agentRef = agentRef;
    }
    conversate(content) {
        const _super = Object.create(null, {
            conversate: { get: () => super.conversate }
        });
        return __awaiter(this, void 0, void 0, function* () {
            var _a;
            // 1) 실행 전 히스토리 길이
            const before = this.agentRef.getHistories()
                .length;
            // 2) RPC 기본 처리(스트리밍/브로드캐스트) 실행 → 반환값 없음(void)
            yield _super.conversate.call(this, content);
            // 3) 실행 후 히스토리에서 "이번 턴에 추가된" 프롬프트만 추출
            const all = (_a = this.agentRef.getHistories()) !== null && _a !== void 0 ? _a : [];
            const newOnes = all.slice(before);
            // 4) 새 프롬프트만 저장
            if (newOnes.length) {
                const jsons = newOnes.map((p) => p.toJSON());
                console.log("[persisted-rpc] saving new prompts", jsons);
                yield (0, spring_history_client_1.savePromptsToSpring)(jsons, this.accessToken);
            }
        });
    }
}
exports.PersistedRpcService = PersistedRpcService;
//# sourceMappingURL=persisted-rpc-service.js.map