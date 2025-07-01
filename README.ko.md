<p align="center">
  <img src="frontend/src/assets/images/logo.png" alt="Pipely 대표 이미지" />
</p>

![License](https://img.shields.io/badge/license-MIT-blue)  
![Service Status](https://img.shields.io/badge/status-online-brightgreen)

Looking for the Korean version? [Click here](./README.ko.md)

## 🔦 Overview

**Pipely** is a ChatOps platform that combines “Pipeline” and “Simply” — enabling effortless management of complex CI/CD
workflows with just a single line of natural language.

Simply access the web interface and type phrases like
“Redeploy the backend” or “Run tests on a new branch,”
and the **Agentica engine** instantly interprets your intent, calling automation tools such as Jenkins, GitHub Webhooks,
and
more. The results are displayed in a user-friendly interface.

With Pipely, developers no longer need to modify scripts or memorize CLI commands.
Instead, they can simply type their intent in natural language, and Pipely will automatically compose and execute the
deployment workflow, providing interactive logs, execution history, and success/failure summaries — enabling **faster
issue resolution**.
Non-developers can also use familiar expressions through Slack or messaging apps to control pipelines, allowing **the
entire team to participate** in the deployment process with ease.

By introducing Pipely, organizations can reduce time and resources spent on deployment and testing tasks — focusing more
on development and creating business value.

**Ultimately, Pipely is a next-generation CI/CD solution that dramatically boosts software delivery speed while ensuring
high quality and reliability**.

---

## 🚀 Key Features

- **Natural Language Command Execution & UI Integration**  
  Easily invoke and monitor CI/CD pipelines via natural language chat and intuitive UI.
- **Jenkins Job Management**  
  Create, edit, delete, recover jobs, configure templates and parameters.
- **Notification Integration**  
  Send build/deployment notifications via Slack, Discord, and Notion, view job history.
- **Error Analysis & Permission Control**  
  Summarize failure reasons, auto-retry failed jobs, and manage user/job-level permissions.
- **Real-Time Monitoring UI**  
  Track deployment status and history through web dashboard or chat interface, with rollback support.

---

## 🏗️ Project Deliverables

- [Project Proposal](https://docs.google.com/document/d/1k67gqPe3trgWKEwNCG3rgeZHXsozqcXqJuEBqYkPhXc/edit?tab=t.0)
- [Requirements Specification](https://docs.google.com/spreadsheets/d/1apwYQch5wEJAdfZa1ZlTZmPqGK5NtT89TuzFT1B8dys/edit?gid=0#gid=0)
- [UI Design (Figma)](https://www.figma.com/design/d22K0lpjOcG7vH8kbQ4DzE/Pipely?node-id=2-2&t=8GOfcsjIorznJyGb-1)
- [WBS](https://docs.google.com/spreadsheets/d/1iceBM2KVSLNEKkg-Gaetar4pg-WtLEhwqcESs2ilzfQ/edit?gid=0#gid=0)
- [ERD](https://www.erdcloud.com/d/TTcoWmJC4Q64MauFX)
- [System Architecture](https://github.com/baepo-minjok/pipely/wiki/System-Architecture)

---

## 📚 Documentation

- [Coding Convention](https://github.com/baepo-minjok/pipely/wiki#pipely-wiki)
- [Exception Handling Guide](https://github.com/baepo-minjok/pipely/wiki/%EC%98%88%EC%99%B8-%EC%B2%98%EB%A6%AC-%EA%B0%80%EC%9D%B4%EB%93%9C)

---

## 🧰 Technical stack

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

### Collaborative Tools

<p> 
  <img src="https://img.shields.io/badge/Notion-000000?logo=notion&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white&style=flat">&nbsp;
  <img src="https://img.shields.io/badge/Discord-5865F2?logo=discord&logoColor=white&style=flat">
</p>

---

## 🤝 Contribution Guide

1. Fork this repository.
2. Open an issue to discuss your changes.
2. Create a branch named feat/IssueNumber_YourFeature.
3. Make your changes and commit them.
4. Push to your fork and open a Pull Request.

---

## 📄 License

This project is licensed under the [MIT 라이선스](./LICENSE).
