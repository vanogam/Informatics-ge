/** Renders an ISO timestamp as "YYYY/MM/DD HH:mm:ss" in the viewer's local timezone. */
export function formatDateTime(dateString) {
    if (!dateString) return ''
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return dateString

    const pad = (n) => String(n).padStart(2, '0')
    return `${date.getFullYear()}/${pad(date.getMonth() + 1)}/${pad(date.getDate())} `
        + `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}
