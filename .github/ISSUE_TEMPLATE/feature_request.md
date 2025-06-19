---
name: "Feature Request"
about: "새로운 기능 제안 또는 개선 요청 시 사용합니다"
title: "[FEAT] 여기에 요약을 입력하세요"
labels: [enhancement]
assignees: []
---

**1. 기능 제안 배경 및 목표**
- 어떤 문제를 해결하고자 하는지, 또는 어떤 개선을 원하는지 설명해주세요.

**2. 현재 상황 (Current Behavior)**
- 현재 시스템에서는 어떻게 동작하는지 간략히 설명

**3. 제안하는 변경 사항 (Proposed Change)**
- 구체적으로 어떤 기능을 추가/변경할 것인지 서술

**4. 기대 효과 (Expected Benefit)**
- 사용자/시스템에 어떤 이점이 있는지 설명

**5. 구현 고려사항**
- Backend:
  - 새로운 엔티티/DTO/서비스/컨트롤러 설계
  - 데이터베이스 마이그레이션 필요 여부
  - JWT 인증/인가 연관 고려
- Frontend:
  - 컴포넌트 구조 설계 (파일명 PascalCase, 최상위 div 클래스명 `{feature}_container` 등)
  - API 통신 방식 (axios 설정 등)
  - 상태 관리 (Pinia/Vuex 등)
- 코드 컨벤션:
  - Java: builder 패턴 선호, DTO 변환 시 set 대신 builder
  - Vue.js: `<script> → <template> → <style>` 순서, 클래스명 snake_case, 변수/함수명 camelCase
  - Prettier 기본 스타일 적용 여부 확인
- 테스트:
  - 단위 테스트, 통합 테스트 계획
- 배포:
  - Docker/Kubernetes 설정 변경 필요 여부
  - CI/CD 파이프라인 영향

**6. 대안 및 고려사항 (Alternatives)**
- 다른 접근 방법이 있는 경우 기술

**7. 연관 이슈/참고 자료**
- 관련 이슈 링크
- 외부 문서, 스펙 문서

**8. 우선순위 및 일정**
- 우선순위(High/Medium/Low)
- 예상 작업 기간

**9. 담당자 지정 (Optional)**
- 구현 담당자 또는 리뷰 담당자

**Checklist**
- [ ] 제안 배경과 목표가 명확한가?
- [ ] 구현 고려사항을 충분히 검토했는가?
- [ ] 관련 기술 스펙/문서를 첨부했는가?
- [ ] 코드 컨벤션 체크리스트를 반영했는가?