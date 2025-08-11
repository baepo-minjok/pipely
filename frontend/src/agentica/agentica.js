import {inject, provide, ref} from "vue";
import {WebSocketConnector} from "tgrid";

// Symbol로 유일 key 지정
const AgenticaRpcKey = Symbol("AgenticaRpc");

// Provider (setup에서 호출)
export function provideAgenticaRpc() {
    const messages = ref([]);
    const isError = ref(false);
    const driver = ref();
    const isConnected = ref(false);

     async function pushMessage(message) {
           console.debug("[WS 수신]", message?.type, message);

               // 1) Swagger 실행 로그(describe) 처리
                   if (message?.type === "describe") {
                 // 진행 중 로그는 숨김
                     if (!message?.done) return;

                     // 2)  최종 로그(done:true)는 assistantMessage로 변환해서 노출
                         //    (일부 구현에선 최종 결과가 describe.text에만 담겨 옵니다)
                             if (message?.text) {
                       messages.value.push({
                             id: message.id || `tool_${Date.now()}`,
                             type: "assistantMessage",
                            text: message.text,
                             created_at: message.created_at || new Date().toISOString(),
                           });
                     }
                 return;
               }

               // 일반 메시지는 그대로
                   messages.value.push(message);
         }

    async function tryConnect() {
        try {
            isError.value = false;
            const connector = new WebSocketConnector(
                null,
                {
                    assistantMessage: pushMessage,
                    describe: pushMessage,
                    userMessage: pushMessage,
                }
            );
            await connector.connect(import.meta.env.VITE_AGENTICA_WS_URL);
            driver.value = connector.getDriver();
            isConnected.value = true;
            return connector;
        } catch (e) {
            console.error(e);
            isError.value = true;
        }
    }

    async function conversate(message) {
        if (!driver.value) {
            console.error("Driver is not connected.");
            return;
        }
        try {
            await driver.value.conversate(message);
        } catch (e) {
            console.error(e);
            isError.value = true;
        }
    }

    // Provider로 등록
    provide(AgenticaRpcKey, {
        messages,
        conversate,
        isConnected,
        isError,
        tryConnect,
    });
}

// inject로 사용
export function useAgenticaRpc() {
    const context = inject(AgenticaRpcKey);
    if (!context) throw new Error("useAgenticaRpc must be used in provider");
    return context;
}
