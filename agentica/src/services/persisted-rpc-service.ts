import type {
  Agentica,
  AgenticaHistory,
  AgenticaUserMessageContent,
} from "@agentica/core";
import { AgenticaRpcService } from "@agentica/rpc";

import { savePromptsToSpring } from "./spring-history-client";

export class PersistedRpcService<
  M extends "chatgpt",
> extends AgenticaRpcService<M> {
  constructor(
    private readonly accessToken: string | undefined,
    private readonly agentRef: Agentica<M>,
    args: { agent: Agentica<M>; listener: any },
  ) {
    super(args);
  }

  override async conversate(
    content: string | AgenticaUserMessageContent | AgenticaUserMessageContent[],
  ): Promise<void> {
    const before = (this.agentRef.getHistories() as AgenticaHistory<M>[])
      .length;

    await super.conversate(content);

    const all = (this.agentRef.getHistories() as AgenticaHistory<M>[]) ?? [];
    const newOnes = all.slice(before);

    if (newOnes.length) {
      const jsons = newOnes.map((p) => p.toJSON());
      console.log("[persisted-rpc] saving new prompts", jsons);
      await savePromptsToSpring(jsons, this.accessToken);
    }
  }
}
