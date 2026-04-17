export function fenToYuan(fen) {
  return `¥${(Number(fen || 0) / 100).toFixed(2)}`
}
