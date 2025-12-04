<template>
  <div class="dictation-page">
    <div class="toolbar">
      <button class="btn" @click="goBack">返回单词列表</button>
      <button class="btn" :disabled="words.length === 0 || dictationStarted" @click="startDictation">
        开始听写
      </button>
      <button
        class="btn"
        :disabled="!dictationStarted || dictationFinished"
        @click="finishDictation"
      >
        听写完成
      </button>
      <button
        class="btn"
        v-if="dictationFinished && wrongIds.length > 0"
        @click="addToWrongBook"
      >
        加入错词本
      </button>
      <button class="btn link" @click="goWrongBook">错词本</button>
    </div>

    <div v-if="msg" class="msg">{{ msg }}</div>

    <div v-if="loading" class="msg">加载中...</div>
    <div v-else>
      <table class="dictation-table">
        <thead>
          <tr>
            <th>原词</th>
            <th>拼写</th>
            <th>中文</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="w in words" :key="w.id">
            <td>
              <span v-if="!dictationStarted || dictationFinished">{{ w.word }}</span>
              <span v-else class="hidden-text">••••••</span>
            </td>
            <td>
              <input
                class="spell-input"
                v-model="inputs[w.id]"
                :disabled="!dictationStarted || dictationFinished"
                :class="{ 'is-wrong': dictationFinished && wrongIds.includes(w.id) }"
                placeholder="请输入拼写"
              />
            </td>
            <td>
              <span v-if="!dictationStarted || dictationFinished" v-html="formatMeaning(w.meaning)"></span>
              <span v-else class="hidden-text">••••••</span>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="dictationFinished" class="stats">
        正确 {{ stats.correct }} / {{ stats.total }} ，正确率 {{ accuracy }}%
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

const words = ref([])
const inputs = ref({})
const loading = ref(false)
const msg = ref('')
const dictationStarted = ref(false)
const dictationFinished = ref(false)
const wrongIds = ref([])

const day = ref(Number(route.query.day) || Number(localStorage.getItem('currentDay')) || 1)
const lang = ref(route.query.lang || localStorage.getItem('wordLang') || 'EN')
const bookId = ref(route.query.bookId || localStorage.getItem('wordBookId') || '')

const stats = computed(() => {
  const total = words.value.length
  const correct = total - wrongIds.value.length
  return { total, correct }
})

const accuracy = computed(() => {
  if (stats.value.total === 0) return 0
  return Math.round((stats.value.correct / stats.value.total) * 100)
})

const formatMeaning = (meaning) => {
  return meaning || ''
}

const normalizeWords = (data) => {
  return data.map((w) => {
    let displayWord = w.word
    if (lang.value === 'EN' && typeof w.word === 'string') {
      const idx = w.word.indexOf('[')
      if (idx > -1) {
        displayWord = w.word.slice(0, idx).trim()
      }
    }
    return { ...w, word: displayWord }
  })
}

const fetchWords = async () => {
  loading.value = true
  msg.value = ''
  try {
    if (!bookId.value) {
      router.push('/book-select')
      loading.value = false
      return
    }
    const resp = await fetch(`/api/words/day/${day.value}?lang=${lang.value}&bookId=${bookId.value}`)
    const data = await resp.json()
    words.value = Array.isArray(data) ? normalizeWords(data) : []
    resetInputs()
  } catch (e) {
    msg.value = '获取单词失败'
    words.value = []
  }
  loading.value = false
}

const resetInputs = () => {
  inputs.value = {}
  words.value.forEach((w) => {
    inputs.value[w.id] = ''
  })
  dictationStarted.value = false
  dictationFinished.value = false
  wrongIds.value = []
}

const startDictation = () => {
  wrongIds.value = []
  dictationStarted.value = true
  dictationFinished.value = false
  msg.value = ''
}

const finishDictation = () => {
  dictationFinished.value = true
  wrongIds.value = words.value
    .filter((w) => (inputs.value[w.id] || '').trim() !== (w.word || '').trim())
    .map((w) => w.id)
}

const addToWrongBook = async () => {
  const username = localStorage.getItem('username') || ''
  if (wrongIds.value.length === 0) {
    return
  }
  await fetch(`/api/wrongWords/add?username=${encodeURIComponent(username)}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(wrongIds.value)
  })
  msg.value = '已加入错词本'
}

const goBack = () => {
  router.push(`/words/${day.value}`)
}

const goWrongBook = () => {
  router.push({ path: '/wrongbook', query: { lang: lang.value } })
}

onMounted(() => {
  fetchWords()
})
</script>

<style scoped>
.dictation-page {
  padding: 24px;
  color: #f5f7fa;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.btn {
  padding: 8px 14px;
  border: 1px solid #1e90ff;
  background: transparent;
  color: #1e90ff;
  border-radius: 6px;
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.link {
  margin-left: auto;
}

.dictation-table {
  width: 100%;
  border-collapse: collapse;
  background: #1a222d;
  border: 1px solid #2f3b4b;
}

.dictation-table th,
.dictation-table td {
  border: 1px solid #2f3b4b;
  padding: 10px;
  text-align: left;
}

.spell-input {
  width: 100%;
  padding: 6px 8px;
  border-radius: 4px;
  border: 1px solid #3c4b60;
  background: #0f1620;
  color: #f5f7fa;
}

.is-wrong {
  color: #ff6b6b;
  border-color: #ff6b6b;
}

.hidden-text {
  color: #425168;
}

.stats {
  margin-top: 12px;
  font-weight: bold;
}

.msg {
  margin-bottom: 12px;
  color: #ffae00;
}
</style>
