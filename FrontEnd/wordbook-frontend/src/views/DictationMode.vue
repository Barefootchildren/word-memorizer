<template>
  <div class="word-list-root dictation-mode">
    <!-- 顶部导航按钮 -->
    <div class="top-btns">
      <button class="nav-btn" @click="goHome">返回主页</button>
      <button class="nav-btn" @click="goDaySelect">返回天数选择</button>
      <button class="nav-btn" @click="goHardWords">顽固单词列表</button>
      <button class="nav-btn" @click="goTodayWords">当天单词</button>
      <button class="nav-btn" @click="goWrongBook">错词本</button>
    </div>

    <!-- 听写控制 + 正确率 -->
    <div class="dictation-controls">
      <button
        class="ctrl-btn"
        :disabled="words.length === 0 || dictationStarted"
        @click="startDictation"
      >
        开始听写
      </button>
      <button
        class="ctrl-btn"
        :disabled="!dictationStarted || dictationFinished"
        @click="finishDictation"
      >
        听写完成
      </button>
      <button
        class="ctrl-btn"
        v-if="dictationFinished && wrongIds.length > 0"
        @click="addToWrongBook"
      >
        加入错词本
      </button>

      <span v-if="dictationFinished" class="stats">
        正确 {{ stats.correct }} / {{ stats.total }} ，正确率 {{ accuracy }}%
      </span>
    </div>

    <!-- 标题 -->
    <h1>
      第{{ day }}天（{{ langLabel
      }}<template v-if="bookName"> / {{ bookName }}</template>）听写模式
    </h1>

    <div v-if="msg" class="msg">{{ msg }}</div>
    <div v-if="loading" class="msg">加载中...</div>

    <div v-else>
      <table>
        <thead>
          <tr>
            <th>序号</th>
            <th>原词</th>
            <th>拼写</th>
            <th>中文</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(w, i) in words" :key="w.id">
            <!-- 序号 -->
            <td>{{ i + 1 }}</td>

            <!-- 原词 -->
            <td>
              <template v-if="dictationStarted && !dictationFinished">
                <span class="hidden-text">••••••</span>
              </template>
              <template v-else>
                <!-- 英语 -->
                <template v-if="lang === 'EN'">
                  <div class="word-main">
                    <div class="word-row">
                      <span class="word-text" @click="speak(w.word)">
                        {{ w.word }}
                      </span>
                      <transition name="fadeout">
                        <span v-if="hardFade[i]" class="hard-check">✔</span>
                      </transition>
                      
                    </div>
                    <div
                      v-if="w.phonetic"
                      class="word-row word-phonetic-row"
                    >
                      <span class="word-phonetic">{{ w.phonetic }}</span>
                    </div>
                  </div>
                </template>

                <!-- 韩语 -->
                <template v-else>
                  <span class="word-text" @click="speak(w.word)">
                    {{ w.word }}
                  </span>
                  <transition name="fadeout">
                    <span v-if="hardFade[i]" class="hard-check">✔</span>
                  </transition>
                 
                </template>
              </template>
            </td>

            <!-- 拼写 -->
            <td>
              <input
                class="spell-input"
                v-model="inputs[w.id]"
                :disabled="!dictationStarted || dictationFinished"
                :class="{
                  'is-wrong':
                    dictationFinished && wrongIds.includes(w.id)
                }"
                placeholder="请输入拼写"
                autocomplete="off"
              />
            </td>

            <!-- 中文 -->
            <td>
              <template v-if="dictationStarted && !dictationFinished">
                <span class="hidden-text">••••••</span>
              </template>
              <template v-else>
                <span
                  class="chinese"
                  v-html="formatMeaning(w.meaning)"
                ></span>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 回到顶部 -->
    <button
      v-if="showBackToTop"
      class="back-top-btn"
      @click="scrollToTop"
      :style="{ left: backTopLeft + 'px' }"
      tabindex="-1"
    >
      回到顶部
    </button>
  </div>
</template>

<script setup>
import {
  computed,
  onMounted,
  onBeforeUnmount,
  ref,
  nextTick
} from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 路由状态
const day = ref(
  Number(route.query.day) ||
    Number(localStorage.getItem('currentDay')) ||
    1
)
const lang = ref(
  route.query.lang || localStorage.getItem('wordLang') || 'EN'
)
const bookId = ref(
  route.query.bookId || localStorage.getItem('wordBookId') || ''
)
const bookName = ref(localStorage.getItem('wordBookName') || '')
const langLabel = computed(() =>
  lang.value === 'KO' ? '韩语单词' : '英语单词'
)

// 数据 & 听写状态
const words = ref([])
const inputs = ref({})
const loading = ref(false)
const msg = ref('')
const dictationStarted = ref(false)
const dictationFinished = ref(false)
const wrongIds = ref([])

// 顽固单词
const hardList = ref([])
const hardFade = ref([])

// 回到顶部
const showBackToTop = ref(false)
const backTopLeft = ref(0)

// 统计
const stats = computed(() => {
  const total = words.value.length
  const correct = total - wrongIds.value.length
  return { total, correct }
})
const accuracy = computed(() => {
  if (stats.value.total === 0) return 0
  return Math.round((stats.value.correct / stats.value.total) * 100)
})

// 中文格式化（与单词列表一致）
const formatMeaning = (meaning) => {
  if (!meaning) return ''
  let s = meaning.replace(/<br\s*\/?>/g, '<br>')
  const posPattern =
    /(n\.|v\.|vi\.|vt\.|adj\.|adv\.|prep\.|conj\.|pron\.|auxv\.)/g
  let isFirst = true
  s = s.replace(posPattern, (match) => {
    if (isFirst) {
      isFirst = false
      return match
    }
    return '<br>' + match
  })
  return s
}

// 拆出音标
const normalizeWords = (data) => {
  return data.map((w) => {
    let displayWord = w.word
    let phonetic = ''
    if (lang.value === 'EN' && typeof w.word === 'string') {
      const idx = w.word.indexOf('[')
      if (idx > -1) {
        displayWord = w.word.slice(0, idx).trim()
        phonetic = w.word.slice(idx).trim()
      }
    }
    return { ...w, word: displayWord, phonetic }
  })
}

// 拉取单词
const fetchWords = async () => {
  loading.value = true
  msg.value = ''
  try {
    if (!bookId.value) {
      router.push('/book-select')
      loading.value = false
      return
    }
    const resp = await fetch(
      `/api/words/day/${day.value}?lang=${lang.value}&bookId=${bookId.value}`
    )
    const data = await resp.json()
    words.value = Array.isArray(data) ? normalizeWords(data) : []
    resetInputs()
    resetHardFade()
    await fetchHardList()
  } catch (e) {
    msg.value = '获取单词失败'
    words.value = []
  }
  loading.value = false

  // 单词加载完之后，再决定是否显示“回到顶部”按钮，并在 DOM 更新后计算位置
  updateBackToTopVisibility()
  await nextTick()
  updateBackTopPosition()
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

const resetHardFade = () => {
  hardFade.value = Array(words.value.length).fill(false)
}

// 顽固单词
const fetchHardList = async () => {
  const username = localStorage.getItem('username') || ''
  if (!username) {
    hardList.value = []
    return
  }
  try {
    const resp = await fetch(
      `/api/hard-words/${username}?lang=${lang.value}`
    )
    const data = await resp.json()
    hardList.value = Array.isArray(data) ? data : []
  } catch {
    hardList.value = []
  }
}

const isHard = (item) => {
  if (!Array.isArray(hardList.value)) return false
  return hardList.value.some((h) => h.id === item.id)
}

const markHard = async (idx) => {
  const wordId = words.value[idx].id
  hardFade.value[idx] = true

  const username = localStorage.getItem('username') || ''
  if (!username) return

  await fetch('/api/hard-words/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `username=${encodeURIComponent(username)}&wordId=${wordId}`
  })

  await fetchHardList()

  setTimeout(() => {
    hardFade.value[idx] = false
  }, 1000)
}

// 听写控制
const startDictation = () => {
  wrongIds.value = []
  dictationStarted.value = true
  dictationFinished.value = false
  msg.value = ''
}

const normalizeAnswer = (s) => (s || '').trim().toLowerCase()

const finishDictation = () => {
  dictationFinished.value = true
  wrongIds.value = words.value
    .filter(
      (w) =>
        normalizeAnswer(inputs.value[w.id]) !==
        normalizeAnswer(w.word)
    )
    .map((w) => w.id)
}

// 错词本：带上 day 和 lang
const addToWrongBook = async () => {
  const username = localStorage.getItem('username') || ''
  if (!username || wrongIds.value.length === 0) {
    return
  }
  await fetch(
    `/api/wrongWords/add?username=${encodeURIComponent(
      username
    )}&day=${day.value}&lang=${lang.value}`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(wrongIds.value)
    }
  )
  msg.value = '已加入错词本'
}

// 发音
const speak = (text) => {
  if (!window.speechSynthesis) return
  const utter = new window.SpeechSynthesisUtterance(text)
  const curLang = localStorage.getItem('wordLang') || 'EN'
  const targetLang = curLang === 'KO' ? 'ko-KR' : 'en-US'
  utter.lang = targetLang

  const voices = window.speechSynthesis.getVoices()
  const matched = voices.find(
    (v) =>
      v.lang && v.lang.startsWith(curLang === 'KO' ? 'ko' : 'en')
  )
  if (matched) utter.voice = matched

  window.speechSynthesis.cancel()
  window.speechSynthesis.speak(utter)
}

// 回到顶部
const updateBackToTopVisibility = () => {
  // 只控制显隐，位置单独用 updateBackTopPosition 算
  showBackToTop.value = words.value.length > 20
}


const updateBackTopPosition = () => {
  const table = document.querySelector('.word-list-root.dictation-mode table')
  if (!table || !showBackToTop.value) {
    backTopLeft.value = 0
    return
  }

  const rect = table.getBoundingClientRect()
  const viewportWidth =
    window.innerWidth || document.documentElement.clientWidth
  const buttonWidth = 110

  // 默认：紧贴单词列表右边沿外 8px
  let left = rect.right + 8

  // 防止跑出屏幕右侧
  const maxLeft = viewportWidth - buttonWidth - 8
  if (left > maxLeft) left = maxLeft

  backTopLeft.value = left
}


const handleResize = () => {
  updateBackTopPosition()
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 顶部导航跳转
const goHome = () => {
  router.push('/home')
}
const goDaySelect = () => {
  router.push('/select-day')
}
const goHardWords = () => {
  router.push({
    path: '/hard-words',
    query: { day: day.value, lang: lang.value }
  })
}
const goTodayWords = () => {
  router.push(`/words/${day.value}`)
}
const goWrongBook = () => {
  router.push({
    path: '/wrongbook',
    query: { day: day.value, lang: lang.value }
  })
}

onMounted(() => {
  fetchWords()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.word-list-root {
  max-width: 1000px;
  margin: 36px auto 0 auto;
  padding: 0 0 36px 0;
  color: var(--text-color);
}

/* 顶部按钮 */
.top-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-top: 10px;
  margin-bottom: 16px;
}

.nav-btn {
  background: #1e90ff;
  color: #fff;
  border: none;
  border-radius: 7px;
  padding: 6px 22px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}

.nav-btn:hover {
  background: #256fff;
}

/* 听写控制 + 正确率 */
.dictation-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.ctrl-btn {
  background: #222;
  color: #fff;
  border-radius: 7px;
  border: 1px solid #1e90ff;
  padding: 4px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.ctrl-btn:hover {
  background: #1e90ff;
  color: #fff;
}

.ctrl-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.stats {
  margin-left: 16px;
  font-weight: 700;
  color: #ffd54f;
}

h1 {
  color: var(--text-color);
  font-size: 2rem;
  margin: 18px 0 20px 0;
  text-align: left;
  font-weight: 700;
}

/* 表格 */
table {
  border-collapse: collapse;
  width: 100%;
  background: var(--main-bg, #1a222d);
  margin-bottom: 12px;
}

th,
td {
  padding: 8px;
  border: 1px solid #444;
  vertical-align: middle;
  text-align: left;
}

/* 单词列样式 */
.word-main {
  display: inline-block;
}

.word-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.word-phonetic-row {
  margin-top: 2px;
}

.word-phonetic {
  display: block;
  font-size: 12px;
  color: #b0bec5;
  margin-top: 2px;
}

.word-text {
  color: #0af;
  text-decoration: underline;
  cursor: pointer;
  transition: color 0.2s;
}

.word-text:hover {
  color: #5cf;
}

.hard-btn {
  background: #880;
  color: #fff;
  border: none;
  border-radius: 4px;
  margin-left: 8px;
  cursor: pointer;
  font-size: 13px;
  padding: 2px 10px;
  transition: background 0.2s, opacity 0.6s;
}

.hard-btn:hover {
  background: #ff9800;
  color: #222;
}

.hard-check {
  margin-left: 10px;
  color: #fff;
  background: #22b573;
  border-radius: 50%;
  padding: 2px 8px;
  font-size: 16px;
  font-weight: bold;
  opacity: 1;
  display: inline-block;
}

.fadeout-enter-active,
.fadeout-leave-active {
  transition: opacity 1s;
}

.fadeout-leave-to {
  opacity: 0;
}

/* 听写输入框 */
.spell-input {
  width: 100%;
  padding: 6px 8px;
  border-radius: 4px;
  border: 1px solid #3c4b60;
  background: #0f1620;
  color: #f5f7fa;
  box-sizing: border-box;
}

.is-wrong {
  color: #ff6b6b;
  border-color: #ff6b6b;
}

/* 隐藏占位 */
.hidden-text {
  color: #425168;
}

/* 中文 */
.chinese {
  white-space: pre-line;
}

/* 信息提示 */
.msg {
  margin-bottom: 12px;
  color: #ffae00;
}

/* 回到顶部按钮 */
.back-top-btn {
  position: fixed;
  bottom: 32px;
  left: 0;
  padding: 6px 14px;
  border-radius: 16px;
  border: none;
  background: rgba(30, 144, 255, 0.9);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  z-index: 50;
}
</style>
