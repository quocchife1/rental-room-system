export const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
export const phoneRegex = /^(0|\+84)(3[2-9]|5[25689]|7[0|6-9]|8[1-9]|9[0-9])\d{7}$/
export const dateRegex = /^\d{4}-\d{2}-\d{2}$/

export function isRequired(val){ return val !== null && val !== undefined && String(val).trim().length > 0 }
export function isEmail(val){ return emailRegex.test(val || '') }
export function isPhone(val){ return phoneRegex.test(val || '') }
export function isDate(val){ return dateRegex.test(val || '') }
export function isPositiveNumber(val){ const n = Number(val); return !Number.isNaN(n) && n > 0 }
export function maxLength(val, max){ return String(val || '').length <= max }
