import dotenv from "dotenv";
import dotenvExpand from "dotenv-expand";
import { Singleton } from "tstl";
import typia from "typia";

export class SGlobal {
  public static readonly env = typia.assert<{
    PORT: `${number}`;
    OPENAI_API_KEY: string;
  }>({
    PORT: process.env.PORT ?? "3001",
    OPENAI_API_KEY: process.env.OPENAI_API_KEY ?? "",
  });
}

interface IEnvironments {
  OPENAI_API_KEY?: string;
  PORT: `${number}`;
}

const environments = new Singleton(() => {
  const env = dotenv.config();
  dotenvExpand.expand(env);
  return typia.assert<IEnvironments>(process.env);
});
