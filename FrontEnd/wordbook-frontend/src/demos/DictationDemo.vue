<template>
  <div class="dictation-demo">
    <h2>听写模式 Demo（单词：banana）</h2>

    <div>
      <label>请输入你听到的单词：</label>
      <input v-model="userInput" placeholder="例如：benana / beanana / bnana" />
    </div>

    <div style="margin-top: 10px;">
      <div>对照结果：</div>
      <div v-html="comparisonHtml" class="result"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const originalWord = 'banana'
const userInput = ref('')
const comparisonHtml = ref('')

// 防止 HTML 注入
function esc(ch) {
  if (!ch) return ''
  return ch.replace(/&/g, '&amp;')
           .replace(/</g, '&lt;')
           .replace(/>/g, '&gt;')
}

// 编辑距离 + 回溯得到 diff
function computeDiff(a, b) {
  const m = a.length
  const n = b.length
  const dp = Array.from({ length: m + 1 }, () => Array(n + 1).fill(0))
  const op = Array.from({ length: m + 1 }, () => Array(n + 1).fill(''))

  // 初始化
  for (let i = 1; i <= m; i++) op[i][0] = 'delete', dp[i][0] = i
  for (let j = 1; j <= n; j++) op[0][j] = 'insert', dp[0][j] = j

  // DP 填表
  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      const matchOrReplace = dp[i - 1][j - 1] + (a[i - 1] === b[j - 1] ? 0 : 1)
      const del = dp[i - 1][j] + 1
      const ins = dp[i][j - 1] + 1

      let best = matchOrReplace
      let bestOp = a[i - 1] === b[j - 1] ? 'match' : 'replace'

      if (del < best) best = del, bestOp = 'delete'
      if (ins < best) best = ins, bestOp = 'insert'

      dp[i][j] = best
      op[i][j] = bestOp
    }
  }

  // 回溯
  const res = []
  let i = m, j = n
  while (i > 0 || j > 0) {
    const cur = op[i][j]
    if (cur === 'match' || cur === 'replace') {
      res.push({ type: cur, a: a[i - 1], b: b[j - 1] })
      i--, j--
    } else if (cur === 'delete') {
      res.push({ type: 'delete', a: a[i - 1], b: '' })
      i--
    } else {
      res.push({ type: 'insert', a: '', b: b[j - 1] })
      j--
    }
  }
  return res.reverse()
}

// 渲染 HTML
function toHtml(diff) {
  let out = ''
  diff.forEach(d => {
    if (d.type === 'match') out += `<span>${esc(d.b)}</span>`
    else if (d.type === 'replace') out += `<span style="color:red">${esc(d.b)}</span>`
    else if (d.type === 'insert') out += `<span style="color:orange">${esc(d.b)}</span>`
    else if (d.type === 'delete') out += `<span style="color:orange">${esc(d.a)}</span>`
  })
  return out
}

watch(userInput, () => {
  const diff = computeDiff(originalWord, userInput.value)
  comparisonHtml.value = toHtml(diff)
}, { immediate: true })
</script>

<style scoped>
.result {
  font-size: 20px;
  font-family: Consolas, monospace;
  padding: 6px;
  background: #f5f5f5;
  min-height: 30px;
  border-radius: 4px;
}
</style>
