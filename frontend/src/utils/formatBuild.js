// 빌드 상태 텍스트 반환
export const getStatusText = (status) => {
  return status === 'SUCCESS' ? '성공' : status === 'FAILURE' ? '실패' : '대기';
};

// 빌드 상태에 따른 CSS 클래스 반환
export const getStatusClass = (status) => {
  return status === 'SUCCESS' ? 'success' : status === 'FAILURE' ? 'fail' : '';
};

// 트리거한 사용자 이름 포맷
export const getUser = (triggeredBy) => {
  return triggeredBy === 'unknown' ? '알 수 없음' : triggeredBy;
};

// 트리거 방식 포맷
export const getTriggerText = (triggeredBy) => {
  if (triggeredBy === 'unknown') return '알 수 없음';
  if (triggeredBy === 'timer') return '스케줄';
  return '수동';
};

// 날짜 포맷 (예: 2025-07-29 03:12:00 → 2025.07.29 03:12:00)
export const formatDate = (datetime) => {
  return datetime?.replace(/-/g, '.');
};

// 단계 이름 포맷 (ex: GIT_CLONE → Git Clone)
export const formatStageName = (stageName) => {
  const map = {
    GIT_CLONE: 'Git Clone',
    BUILD: 'Build',
    TEST: 'Test',
    DEPLOY: 'Deploy',
  };
  return map[stageName] || stageName;
};

// 단계별 상태 클래스 (ex: GIT_CLONE → 'success')
export const getStageStatusClass = (stageName) => {
  return (
    {
      GIT_CLONE: 'success',
      BUILD: 'success',
      DEPLOY: 'fail',
    }[stageName] || ''
  );
};
