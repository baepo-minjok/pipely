// src/websocket/index.js (혹은 src/websocket.js)

import {Client} from "@stomp/stompjs";
import SockJS from "sockjs-client";

let stompClient = null;

export function connectWebSocket(onMessage) {
  const wsUrl = "http://localhost:8080/ws-endpoint";
  stompClient = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe("/topic/messages", (msg) => {
        if (onMessage) onMessage(msg.body); // 콜백 실행
      });
      stompClient.subscribe("/user/queue/alert", (msg) => {
        if (onMessage) onMessage(msg.body);
      });
    },
  });
  stompClient.activate();
}

export function sendMessage(destination, body) {
  if (stompClient && stompClient.connected) {
    stompClient.publish({destination, body});
  }
}

export function disconnectWebSocket() {
  if (stompClient) stompClient.deactivate();
}
