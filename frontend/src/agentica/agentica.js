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
