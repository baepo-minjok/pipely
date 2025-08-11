import type { IAgenticaHistoryJson } from "@agentica/core";

export async function fetchHistoriesFromSpring(
  accessToken?: string,
): Promise<IAgenticaHistoryJson[]> {
  try {
    const res = await fetch(
      "https://www.pipely.kro.kr/api/agentica/history/me",
      {
        headers: { Authorization: accessToken ? `Bearer ${accessToken}` : "" },
      },
    );
    if (!res.ok) return [];

    const dtos: any[] = await res.json();
    if (!Array.isArray(dtos)) return [];

    const hist: IAgenticaHistoryJson[] = dtos
      .map((d: any) => {
        const j = d?.json;
        if (!j) return null;
        return j;
      })
      .filter(Boolean);
    return hist;
  } catch (e) {
    return [];
  }
}

export async function savePromptsToSpring(
  jsons: IAgenticaHistoryJson[],
  accessToken?: string,
) {
  try {
    if (!Array.isArray(jsons) || jsons.length === 0) return;

    const body = jsons.map((j) => ({
      json: j,
    }));

    const res = await fetch(
      "https://www.pipely.kro.kr/api/agentica/history/me",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: accessToken ? `Bearer ${accessToken}` : "",
        },
        body: JSON.stringify(body),
      },
    );
    if (!res.ok)
      console.error("[hist] save failed:", res.status, res.statusText);
  } catch (e) {
    console.error("[hist] save error:", e);
  }
}
