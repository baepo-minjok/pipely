import {Client} from "@stomp/stompjs";
import SockJS from "sockjs-client";

let stompClient = null;

export function connectWebSocket(onBuildMessage, onAlertMessage) {
  const wsUrl = "http://localhost:8080/ws-endpoint";
  stompClient = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    reconnectDelay: 5000,
    onConnect: () => {
      // 빌드 구독
      stompClient.subscribe("/user/queue/build", (msg) => {
        if (onBuildMessage) onBuildMessage(msg.body);
      });
      // 알림 구독
      stompClient.subscribe("/user/queue/alert", (msg) => {
        if (onAlertMessage) onAlertMessage(msg.body);
      });
    },
  });
  stompClient.activate();
}


export function disconnectWebSocket() {
  if (stompClient) stompClient.deactivate();
}
