export function formatDateTime(isoString) {
    if (isoString == null) {
        return '실행 이력 없음'
    }
    const date = new Date(isoString);

    const month = String(date.getMonth() + 1).padStart(2, '0'); // 0-indexed
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');

    return `${month}.${day} ${hour}:${minute}`;
}
