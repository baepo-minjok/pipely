<p align="center">
  <img src="frontend/src/assets/images/logo.png" alt="Pipely 대표 이미지" />
</p>

![License](https://img.shields.io/badge/license-MIT-blue)  
![Service Status](https://img.shields.io/badge/status-online-brightgreen)

Want to read this in English? [Click here](./README.md)

## 🔦 개요

Pipely는 “Pipeline”과 “Simply”를 합친 이름으로, 복잡한 CI/CD 과정을 자연어 한 줄로 손쉽게 관리할 수 있는 ChatOps 플랫폼입니다.
웹 브라우저에 접속해 “백엔드 재배포해줘” 또는 “새로운 브랜치로 테스트를 실행해줘” 같은 일상적인 문장을 입력하면,
Agentica 엔진이 즉시 의도를 파악해 Jenkins, GitHub Webhook 등 다양한 자동화 도구를 호출하고, 실행 결과를 화면에 출력합니다.

Pipely를 도입하면 개발자는 더 이상 스크립트 파일을 일일이 수정하거나 CLI 명령어를 외울 필요가 없습니다.
그 대신 자연어 대화창에 간단히 명령만 입력하면, Pipely가 자동으로 배포 워크플로우를 구성·실행하고,
성공·실패 로그와 상세 실행 이력을 대화형 인터페이스로 제공해 **빠른 문제 해결**을 돕습니다. 비개발자는 업무 중에도 슬랙이나 메신저로 익숙한 표현을 그대로 사용해 파이프라인을 제어할 수 있어, 팀
전원이 **손쉽게 배포 과정에 참여**할 수 있습니다.
이 플랫폼을 통해 조직은 배포·테스트 업무에서 사용되는 리소스를 줄이고, 본연의 개발과 비즈니스 가치 창출에 집중할 수 있습니다.

**결과적으로 Pipely는 소프트웨어 딜리버리 속도를 획기적으로 높이는 동시에 품질과 안정성을 동시에 확보하도록 돕는 미래형 CI/CD 솔루션입니다.**

---

## 🚀 주요 기능

- **자연어 명령 처리 & UI 제어**  
  자연어 대화창과 직관적 UI를 통해 누구나 손쉽게 CI/CD 파이프라인을 호출·모니터링
- **Jenkins Job 관리**  
  생성·수정·삭제·복구, 템플릿 생성, 파라미터 설정
- **알림 통합**  
  Slack·Discord·Notion 알림, 이력 조회
- **오류 분석 & 권한 관리**  
  실패 원인 요약, 자동 재시도, 사용자·Job별 권한 제어
- **실시간 모니터링 UI**  
  웹 대시보드와 채팅 인터페이스에서 배포 현황·이력 확인 및 롤백

---

## 🏗️ 프로젝트 산출물

- [프로젝트 기획서](https://docs.google.com/document/d/1k67gqPe3trgWKEwNCG3rgeZHXsozqcXqJuEBqYkPhXc/edit?tab=t.0)
- [요구사항 정의서](https://docs.google.com/spreadsheets/d/1apwYQch5wEJAdfZa1ZlTZmPqGK5NtT89TuzFT1B8dys/edit?gid=0#gid=0)
- [화면설계서](https://www.figma.com/design/d22K0lpjOcG7vH8kbQ4DzE/Pipely?node-id=2-2&t=8GOfcsjIorznJyGb-1)
- [WBS](https://docs.google.com/spreadsheets/d/1iceBM2KVSLNEKkg-Gaetar4pg-WtLEhwqcESs2ilzfQ/edit?gid=0#gid=0)
- [ERD](https://www.erdcloud.com/d/TTcoWmJC4Q64MauFX)
- [System Architecture](https://github.com/baepo-minjok/pipely/wiki/System-Architecture)

---

## 📚 문서

- [코딩 컨벤션](https://github.com/baepo-minjok/pipely/wiki#pipely-wiki)
- [예외처리 가이드](https://github.com/baepo-minjok/pipely/wiki/%EC%98%88%EC%99%B8-%EC%B2%98%EB%A6%AC-%EA%B0%80%EC%9D%B4%EB%93%9C)

---

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
  <img src="https://img.shields.io/badge/Swagger-85EA2D?logo=swagger&logoColor=black&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/Spring_Security-6DB33F?logo=springsecurity&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/Mustache-FFC72C?logoColor=black&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/NestJS-E0234E?logo=nestjs&logoColor=white&style=flat"> 
</p>

### DevOps

<p> 
  <img src="https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white&style=flat">&nbsp; 
  <img src="https://img.shields.io/badge/Kubernetes-326CE5?logo=kubernetes&logoColor=white&style=flat">&nbsp; 
  <img src="https://img.shields.io/badge/Jenkins-D24939?logo=jenkins&logoColor=white&style=flat">&nbsp; 
  <img src="https://img.shields.io/badge/GitHub_Webhook-181717?logo=github&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/Helm-0F1689?logo=helm&logoColor=white&style=flat">&nbsp; 
  <img src="https://img.shields.io/badge/Agentica-0A192F?style=flat&logo=chatbot&logoColor=white">&nbsp; 
  <img src="https://img.shields.io/badge/Discord_Webhook-5865F2?logo=discord&logoColor=white&style=flat"> 
</p>

### 협업 툴

<p> 
  <img src="https://img.shields.io/badge/Notion-000000?logo=notion&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/Discord-5865F2?logo=discord&logoColor=white&style=flat">
</p>

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
