<p align="center">
  <img src="frontend/src/assets/images/logo.png" alt="Pipely 대표 이미지" />
</p>

![License](https://img.shields.io/badge/license-MIT-blue)  
![Service Status](https://img.shields.io/badge/status-online-brightgreen)

## 🧰 사용 기술 스택

### Frontend

<p>
  <img src="https://img.shields.io/badge/Vue.js-35495E?logo=vue.js&logoColor=4FC08D&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/Vite-646CFF?logo=vite&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/Axios-5A29E4?logo=axios&logoColor=white&style=flat">
</p>

### Backend

<p>
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?logo=spring-boot&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/Java-007396?logo=java&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/JPA-Hibernate-59666C?logo=hibernate&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/MariaDB-003545?logo=mariadb&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/Swagger-85EA2D?logo=swagger&logoColor=black&style=flat">
</p>

### 인프라 및 DevOps

<p>
  <img src="https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/Kubernetes-326CE5?logo=kubernetes&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/Jenkins-D24939?logo=jenkins&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/GitHub_Webhook-181717?logo=github&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/Agentica-0A192F?style=flat&logo=chatbot&logoColor=white">&nbsp;
  <img src="https://img.shields.io/badge/Discord_Webhook-5865F2?logo=discord&logoColor=white&style=flat">
</p>

---

## 🔦 개요

Pipely는 "파이프라인(Pipeline)"과 "심플(Simple)"을 합친 이름처럼, 자연어 대화와 직관적인 UI를 통해 복잡한 CI/CD 과정을 간편하게 관리할 수 있는 Agentica 기반 ChatOps
플랫폼입니다.

- **대화형 CI/CD & UI 관리**: 웹 브라우저에서 자연어 명령(예: `백엔드 재배포해줘`, `새로운 브랜치로 테스트를 실행해줘`)을 입력하거나, 제공되는 UI 대시보드에서 클릭만으로 배포·테스트
  파이프라인을 제어할 수 있습니다.
- **Agentica 매핑 엔진**: 우리 팀이 배포한 백엔드 서버의 Swagger 스펙을 읽어, 사용자가 입력한 자연어와 서버 API를 자동으로 매핑하여 호출합니다.
- **실행 로그 및 피드백**: 성공/실패 로그와 상세 실행 이력을 대화형 인터페이스 및 UI에서 실시간 확인할 수 있습니다.

---

## 🚀 주요 기능

- **자연어 명령 처리 & UI 제어**  
  자연어 대화창과 직관적 UI를 통해 누구나 손쉽게 CI/CD 파이프라인을 호출·모니터링
- **Agentica Swagger 매핑**  
  배포된 백엔드 Swagger를 활용해 자연어 → API 호출 자동 변환
- **멀티 도구 연동**  
  Jenkins, GitHub Actions, Docker Registry, Slack 알림 등 다양한 자동화 도구
- **실시간 피드백**  
  대화형 인터페이스 및 대시보드에서 워크플로우 진행 상태 및 로그 확인
- **사용자 역할 관리**  
  개발자, 비개발자, 운영팀 등 역할별 권한 설정
- **확장성**  
  플러그인 형태로 새로운 CI/CD 도구 및 명령 추가 가능

---

## 🏗️ 프로젝트 산출물

- [프로젝트 기획서](https://docs.google.com/document/d/1k67gqPe3trgWKEwNCG3rgeZHXsozqcXqJuEBqYkPhXc/edit?tab=t.0)
- [요구사항 정의서](https://docs.google.com/document/d/1k67gqPe3trgWKEwNCG3rgeZHXsozqcXqJuEBqYkPhXc/edit?tab=t.0)
- [WBS](https://docs.google.com/spreadsheets/d/1iceBM2KVSLNEKkg-Gaetar4pg-WtLEhwqcESs2ilzfQ/edit?gid=0#gid=0)
- [ERD](https://www.erdcloud.com/d/TTcoWmJC4Q64MauFX)

---

## 📚 문서

- [코딩 컨벤션](https://github.com/baepo-minjok/pipely/wiki#pipely-wiki)
- [예외처리 가이드](https://github.com/baepo-minjok/pipely/wiki/%EC%98%88%EC%99%B8-%EC%B2%98%EB%A6%AC-%EA%B0%80%EC%9D%B4%EB%93%9C)

---

## 🤝 기여

1. 저장소를 Fork 합니다.
2. Issue를 발행합니다.
2. `feat/IssueNumber_YourFeature` 형태의 브랜치를 생성합니다.
3. 코드 변경 및 커밋합니다.
4. 원격에 푸시 후 Pull Request를 생성합니다.

---

## 📄 라이선스

본 프로젝트는 [MIT 라이선스](./LICENSE) 하에 배포됩니다.
