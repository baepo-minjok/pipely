const WEEKDAY_LABELS = {
  mon: '월',
  tue: '화',
  wed: '수',
  thu: '목',
  fri: '금',
  sat: '토',
  sun: '일',
};

export function formatSchedule({ repeatType, selectedDays, ampm, hour, minute }) {
  if (!repeatType) return '';

  const repeatText = repeatType === 'daily' ? '매일' : '매주';
  const dayText = repeatType === 'weekly' ? selectedDays.map((d) => WEEKDAY_LABELS[d] || d).join(',') : '';

  const formattedHour = hour.toString().padStart(2, '0');
  const formattedMinute = minute.toString().padStart(2, '0');

  return `${repeatText}${dayText ? ' ' + dayText : ''} ${ampm} ${formattedHour}시 ${formattedMinute}분`;
}
