// build/env.ts
import dotenv from "dotenv";
import path from "path";

// .env.local 불러오기
dotenv.config({ path: path.resolve(process.cwd(), ".env.local") });

console.log("✅ .env.local 환경변수 로드 완료");
