const WEEKDAY_LABELS = {
    mon: '월',
    tue: '화',
    wed: '수',
    thu: '목',
    fri: '금',
    sat: '토',
    sun: '일'
};

export function formatSchedule({repeatType, selectedDays, time}) {
    if (!time) return '';

    // "HH:mm" → h, m 분리
    const [h, m] = time.split(':').map(Number);

    // 24시간 → 오전/오후 변환
    const ampm = h >= 12 ? '오후' : '오전';
    const convertedHour = h % 12 === 0 ? 12 : h % 12;

    const repeatText = repeatType === 'daily' ? '매일' : '매주';
    const dayText =
        repeatType === 'weekly'
            ? selectedDays.map((d) => WEEKDAY_LABELS[d] || d).join(',')
            : '';

    const formattedHour = String(convertedHour).padStart(2, '0');
    const formattedMinute = String(m).padStart(2, '0');

    return `${repeatText}${dayText ? ' ' + dayText : ''} ${ampm} ${formattedHour}시 ${formattedMinute}분`;
}
