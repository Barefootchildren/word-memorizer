<template>
  <div v-if="loading" class="global-msg">加载中...</div>
  <div v-if="msg" class="global-msg">{{ msg }}</div>

  <div class="word-list-root" :style="{ opacity: isLayoutReady ? 1 : 0 }">
    <div class="top-btns">
      <button @click="goHome" class="nav-btn">主页</button>
      <button @click="goDaySelect" class="nav-btn">天数选择</button>
      <button @click="goHardWords" class="nav-btn">顽固单词</button>
      <button @click="goDictation" class="nav-btn">听写模式</button>
      <button @click="goTodayWords" class="nav-btn">当天单词</button>
      <button @click="toggleExtensions" class="ext-btn">
        {{ showExtensions ? '隐藏拓展词' : '显示拓展词' }}
      </button>
    </div>

    <h1>第{{ day }}天（{{ langLabel }}/错词本）</h1>

    <table>
      <thead>
        <tr>
          <th>序号</th>
          <th>
            单词
            <button class="toggle-btn" @click="toggleWord" tabindex="-1">
              {{ wordVisible ? '隐藏' : '显示' }}
            </button>
            <button class="toggle-btn" @click="toggleWordSpell" tabindex="-1">
              {{ wordSpellMode ? '退出拼写' : '拼写' }}
            </button>
          </th>
          <th>
            中文
            <button class="toggle-btn" @click="toggleChinese" tabindex="-1">
              {{ chineseVisible ? '隐藏' : '显示' }}
            </button>
            <button class="toggle-btn" @click="toggleChineseSpell" tabindex="-1">
              {{ chineseSpellMode ? '退出拼写' : '拼写' }}
            </button>
          </th>
          <th>操作</th>
          <th v-if="showExtensions || activeExtRow !== null">拓展词</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="(item, i) in words" :key="item.id">
          <td>{{ i + 1 }}</td>

          <td v-if="wordVisible || (wordSpellMode && wordSpelling[i])">
            <template v-if="wordSpellMode && wordSpelling[i]">
              <input
                class="spell-input"
                v-model="wordInput[i]"
                :disabled="skipWord[i]"
                @keydown.tab.prevent="focusNext('word', i, $event.shiftKey)"
                @keydown.enter.prevent="checkWordSpell(i)"
                @keydown="handleWordPeekKey(i, $event)"
                @keyup="handleWordPeekKeyUp(i, $event)"
                autocomplete="off"
                :id="'word_input_' + i"
              />

              <button class="speak-btn" @click="speak(item.word)" title="听发音" tabindex="-1">🔊</button>
              <button
                class="peek-btn"
                @mousedown="peekWord(i)"
                @mouseup="unpeekWord(i)"
                @mouseleave="unpeekWord(i)"
                tabindex="-1"
              >
                👁️
              </button>
              <button class="skip-btn" :class="{ active: skipWord[i] }" @click="toggleSkipWord(i)" tabindex="-1">
                {{ skipWord[i] ? '不跳过' : '跳过' }}
              </button>
              <span v-if="wordWrong[i]" class="wrong">✗</span>
            </template>

            <template v-else>
              <template v-if="lang === 'EN'">
                <div class="word-main">
                  <div class="word-row">
                    <span class="word-text" @click="speak(item.word)">{{ item.word }}</span>
                    <transition name="fadeout">
                      <span v-if="hardFade[i]" class="hard-check">✔</span>
                    </transition>
                    <button v-if="!isHard(item) && !hardFade[i]" class="hard-btn" @click="markHard(i)" tabindex="-1">
                      顽固
                    </button>
                  </div>

                  <div v-if="item.phonetic" class="word-row word-phonetic-row">
                    <span class="word-phonetic">{{ item.phonetic }}</span>
                    <button
                      v-if="!showExtensions && item.extensions && item.extensions.length"
                      :class="['ext-inline-btn', { active: activeExtRow === i }]"
                      @mousedown.prevent="handleExtMouseDown(i)"
                      tabindex="-1"
                    >
                      拓
                    </button>
                  </div>
                </div>
              </template>

              <template v-else>
                <span class="word-text" @click="speak(item.word)">{{ item.word }}</span>
                <transition name="fadeout">
                  <span v-if="hardFade[i]" class="hard-check">✔</span>
                </transition>
                <button v-if="!isHard(item) && !hardFade[i]" class="hard-btn" @click="markHard(i)" tabindex="-1">
                  顽固
                </button>

                <button
                  v-if="!showExtensions && item.extensions && item.extensions.length"
                  :class="['ext-inline-btn', { active: activeExtRow === i }]"
                  @mousedown.prevent="handleExtMouseDown(i)"
                  tabindex="-1"
                >
                  拓
                </button>
              </template>
            </template>
          </td>
          <td v-else class="word-hidden">---</td>

          <td v-if="chineseVisible || (chineseSpellMode && chineseSpelling[i])">
            <template v-if="chineseSpellMode && chineseSpelling[i]">
              <div class="edit-area">
                <input
                  class="spell-input"
                  v-model="chineseInput[i]"
                  :disabled="skipChinese[i]"
                  @keydown.tab.prevent="focusNext('chinese', i, $event.shiftKey)"
                  @keydown.enter.prevent="checkChineseSpell(i)"
                  @keydown="handleChinesePeekKey(i, $event)"
                  @keyup="handleChinesePeekKeyUp(i, $event)"
                  autocomplete="off"
                  :id="'chinese_input_' + i"
                />
                <button
                  class="peek-btn"
                  @mousedown="peekChinese(i)"
                  @mouseup="unpeekChinese(i)"
                  @mouseleave="unpeekChinese(i)"
                  tabindex="-1"
                >
                  👁️
                </button>
                <button
                  class="skip-btn"
                  :class="{ active: skipChinese[i] }"
                  @click="toggleSkipChinese(i)"
                  tabindex="-1"
                >
                  {{ skipChinese[i] ? '不跳过' : '跳过' }}
                </button>
                <span v-if="chineseWrong[i]" class="wrong">✗</span>

                <button v-if="!editIndex[i]" class="edit-btn" @click="enableEdit(i)" tabindex="-1">✎</button>
                <span v-else class="edit-actions">
                  <button class="save-btn" @click="saveEdit(i)" tabindex="-1">保存</button>
                  <button class="cancel-btn" @click="cancelEdit(i)" tabindex="-1">取消</button>
                </span>
              </div>
              <div v-if="editMsg[i]" class="edit-msg">{{ editMsg[i] }}</div>
            </template>

            <template v-else>
              <div class="edit-area">
                <span class="chinese" v-html="formatMeaning(item.meaning)"></span>
                <button v-if="!editIndex[i]" class="edit-btn" @click="enableEdit(i)" tabindex="-1">✎</button>
                <span v-else class="edit-actions">
                  <input class="edit-input" v-model="editInput[i]" />
                  <button class="save-btn" @click="saveEdit(i)" tabindex="-1">保存</button>
                  <button class="cancel-btn" @click="cancelEdit(i)" tabindex="-1">取消</button>
                </span>
              </div>
              <div v-if="editMsg[i]" class="edit-msg">{{ editMsg[i] }}</div>
            </template>
          </td>
          <td v-else class="chinese-hidden">---</td>

          <td>
            <button class="cancel-btn" @click="removeWrongWord(item.id)">移除</button>
          </td>

          <td v-if="showExtensions || activeExtRow !== null" class="ext-col">
            <template v-if="showExtensions || activeExtRow === i">
              <div v-if="getExtensionsByType(item, 'SENTENCE').length" class="ext-group">
                <span class="ext-tag ext-tag-sentence">例</span>
                <div class="ext-list">
                  <div v-for="ext in getExtensionsByType(item, 'SENTENCE')" :key="ext.id" class="ext-item">
                    <span class="speak" title="发音" @click="speak(ext.textKor)">🔊</span>
                    <div class="ext-text">
                      <div class="ext-text-kor">{{ ext.textKor }}</div>
                      <div class="ext-text-cn">{{ ext.textCn }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="getExtensionsByType(item, 'SIMILAR').length" class="ext-group">
                <span class="ext-tag ext-tag-similar">近</span>
                <div class="ext-list">
                  <div v-for="ext in getExtensionsByType(item, 'SIMILAR')" :key="ext.id" class="ext-item">
                    <span class="speak" title="发音" @click="speak(ext.textKor)">🔊</span>
                    <span class="ext-text">{{ ext.textKor }} — {{ ext.textCn }}</span>
                  </div>
                </div>
              </div>

              <div v-if="getExtensionsByType(item, 'RELATED').length" class="ext-group">
                <span class="ext-tag ext-tag-related">关</span>
                <div class="ext-list">
                  <div v-for="ext in getExtensionsByType(item, 'RELATED')" :key="ext.id" class="ext-item">
                    <span class="speak" title="发音" @click="speak(ext.textKor)">🔊</span>
                    <span class="ext-text">{{ ext.textKor }} — {{ ext.textCn }}</span>
                  </div>
                </div>
              </div>

              <div v-if="getExtensionsByType(item, 'ANTONYM').length" class="ext-group">
                <span class="ext-tag ext-tag-antonym">反</span>
                <div class="ext-list">
                  <div v-for="ext in getExtensionsByType(item, 'ANTONYM')" :key="ext.id" class="ext-item">
                    <span class="speak" title="发音" @click="speak(ext.textKor)">🔊</span>
                    <span class="ext-text">{{ ext.textKor }} — {{ ext.textCn }}</span>
                  </div>
                </div>
              </div>

              <div v-if="getExtensionsByType(item, 'IDIOM').length" class="ext-group">
                <span class="ext-tag ext-tag-idiom">惯</span>
                <div class="ext-list">
                  <div v-for="ext in getExtensionsByType(item, 'IDIOM')" :key="ext.id" class="ext-item">
                    <span class="speak" title="发音" @click="speak(ext.textKor)">🔊</span>
                    <div class="ext-text">
                      <template v-if="isSentenceExt(ext)">
                        <div class="ext-text-kor">{{ ext.textKor }}</div>
                        <div class="ext-text-cn">{{ ext.textCn }}</div>
                      </template>
                      <template v-else>
                        {{ ext.textKor }} — {{ ext.textCn }}
                      </template>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </td>
        </tr>
      </tbody>
    </table>

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
import { ref, onMounted, onBeforeUnmount, watch, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

function goHome() {
  router.push('/home')
}
function goDaySelect() {
  router.push('/select-day')
}
function goHardWords() {
  router.push({
    path: '/hard-words',
    query: { day: day.value, lang: lang.value }
  })
}
function goDictation() {
  const bookId = localStorage.getItem('wordBookId') || ''
  router.push({
    path: '/dictation',
    query: {
      day: day.value,
      lang: lang.value,
      bookId
    }
  })
}
function goTodayWords() {
  router.push(`/words/${day.value}`)
}

// 当前第几天（错词本用 query.day 或 currentDay）
const day = ref(Number(route.query.day) || Number(localStorage.getItem('currentDay')) || 1)

// 当前语言（允许从 query.lang 覆盖）
const qLang = route.query.lang
const initLang = typeof qLang === 'string' && qLang ? qLang : localStorage.getItem('wordLang') || 'EN'
const lang = ref(initLang)

const langLabel = computed(() => (lang.value === 'KO' ? '韩语单词' : '英语单词'))

const words = ref([])
const loading = ref(false)
const msg = ref('')

const showExtensions = ref(false)
const activeExtRow = ref(null)

const showBackToTop = ref(false)
const backTopLeft = ref(0)
const isLayoutReady = ref(false)

// 状态变量（与单词列表一致）
const wordVisible = ref(true)
const chineseVisible = ref(true)
const wordSpellMode = ref(false)
const chineseSpellMode = ref(false)
const wordSpelling = ref([])
const chineseSpelling = ref([])
const wordInput = ref([])
const chineseInput = ref([])
const wordWrong = ref([])
const chineseWrong = ref([])
const skipWord = ref([])
const skipChinese = ref([])
const peekBufferWord = ref([])
const peekBufferChinese = ref([])

// 编辑中文
const editIndex = ref([])
const editInput = ref([])
const editMsg = ref([])

// 顽固单词
const hardList = ref([])
const hardFade = ref([])

async function updateLayout() {
  const table = document.querySelector('.word-list-root table')
  if (!table) {
    backTopLeft.value = 0
    return
  }
  const rect = table.getBoundingClientRect()
  backTopLeft.value = rect.right + 8

  setTimeout(() => {
    isLayoutReady.value = true
  }, 50)
}

function updateBackTopPosition() {
  const table = document.querySelector('.word-list-root table')
  if (!table || !showBackToTop.value) {
    backTopLeft.value = 0
    return
  }
  const rect = table.getBoundingClientRect()
  backTopLeft.value = rect.right + 8
}

function handleResize() {
  if (isLayoutReady.value) {
    updateBackTopPosition()
  }
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function updateBackToTopVisibility() {
  showBackToTop.value = words.value.length > 20
}

function toggleExtensions() {
  showExtensions.value = !showExtensions.value
  if (showExtensions.value) activeExtRow.value = null
  nextTick(() => updateLayout())
}

// ===== 错词本接口：获取列表 =====
const fetchWrongWords = async () => {
  loading.value = true
  msg.value = ''
  isLayoutReady.value = false

  const username = localStorage.getItem('username') || ''
  try {
    const resp = await fetch(
      `/api/wrongWords?username=${encodeURIComponent(username)}&day=${day.value}&lang=${lang.value}`
    )
    const data = await resp.json()

    words.value = Array.isArray(data)
      ? data.map((w) => {
          let displayWord = w.word
          let phonetic = ''

          if (lang.value === 'EN' && typeof w.word === 'string') {
            const idx = w.word.indexOf('[')
            if (idx > -1) {
              displayWord = w.word.slice(0, idx).trim()
              phonetic = w.word.slice(idx).trim()
            }
          }

          return {
            ...w,
            word: displayWord,
            phonetic,
            extensions: Array.isArray(w.extensions) ? w.extensions : []
          }
        })
      : []

    resetState()
    updateBackToTopVisibility()
    await fetchHardList()
  } catch {
    msg.value = '获取错词失败'
    words.value = []
    updateBackToTopVisibility()
  }

  loading.value = false
  nextTick(() => updateLayout())
}

// ===== 错词本接口：移除 =====
async function removeWrongWord(id) {
  const username = localStorage.getItem('username') || ''
  try {
    await fetch(
      `/api/wrongWords/remove/${id}?username=${encodeURIComponent(username)}&day=${day.value}&lang=${lang.value}`,
      { method: 'DELETE' }
    )
  } catch {}
  await fetchWrongWords()
}

// ===== 顽固单词 =====
const fetchHardList = async () => {
  const username = localStorage.getItem('username') || ''
  if (!username) {
    hardList.value = []
    return
  }
  try {
    const resp = await fetch(`/api/hard-words/${username}?lang=${lang.value}`)
    const data = await resp.json()
    hardList.value = Array.isArray(data) ? data : []
  } catch {
    hardList.value = []
  }
}

function isHard(item) {
  if (!Array.isArray(hardList.value)) return false
  return hardList.value.some((h) => h.id === item.id)
}

async function markHard(idx) {
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

// ===== 状态初始化 =====
function resetState() {
  const n = words.value.length
  wordSpelling.value = Array(n).fill(wordSpellMode.value)
  chineseSpelling.value = Array(n).fill(chineseSpellMode.value)
  wordWrong.value = Array(n).fill(false)
  chineseWrong.value = Array(n).fill(false)
  wordInput.value = Array(n).fill('')
  chineseInput.value = Array(n).fill('')
  skipWord.value = Array(n).fill(false)
  skipChinese.value = Array(n).fill(false)
  peekBufferWord.value = Array(n).fill('')
  peekBufferChinese.value = Array(n).fill('')
  hardFade.value = Array(n).fill(false)

  editIndex.value = Array(n).fill(false)
  editInput.value = words.value.map((w) => w.meaning)
  editMsg.value = Array(n).fill('')

  activeExtRow.value = null
}

// ===== 切换隐藏/拼写 =====
function toggleWord() {
  wordVisible.value = !wordVisible.value
}
function toggleChinese() {
  chineseVisible.value = !chineseVisible.value
}
function toggleWordSpell() {
  wordSpellMode.value = !wordSpellMode.value
  wordSpelling.value = Array(words.value.length).fill(wordSpellMode.value)
  wordWrong.value = Array(words.value.length).fill(false)
  if (wordSpellMode.value) setTimeout(() => focusInput('word', 0), 50)
}
function toggleChineseSpell() {
  chineseSpellMode.value = !chineseSpellMode.value
  chineseSpelling.value = Array(words.value.length).fill(chineseSpellMode.value)
  chineseWrong.value = Array(words.value.length).fill(false)
  if (chineseSpellMode.value) setTimeout(() => focusInput('chinese', 0), 50)
}

function focusInput(type, idx) {
  const id = type === 'word' ? `word_input_${idx}` : `chinese_input_${idx}`
  const el = document.getElementById(id)
  if (el) el.focus()
}

function focusNext(type, idx, shift) {
  const arr = type === 'word' ? wordSpelling.value : chineseSpelling.value
  const skipArr = type === 'word' ? skipWord.value : skipChinese.value
  let next = idx + (shift ? -1 : 1)
  while (next >= 0 && next < arr.length && (!arr[next] || skipArr[next])) {
    next += shift ? -1 : 1
  }
  if (next >= 0 && next < arr.length && arr[next] && !skipArr[next]) {
    setTimeout(() => focusInput(type, next), 10)
  }
}

function toggleSkipWord(idx) {
  skipWord.value[idx] = !skipWord.value[idx]
}
function toggleSkipChinese(idx) {
  skipChinese.value[idx] = !skipChinese.value[idx]
}

// ===== 键盘「瞅一眼」 =====
function handleWordPeekKey(idx, e) {
  if (e.code === 'Backquote') {
    e.preventDefault()
    e.stopPropagation()
    if (!e.repeat) peekWord(idx)
  }
}
function handleWordPeekKeyUp(idx, e) {
  if (e.code === 'Backquote') {
    e.preventDefault()
    e.stopPropagation()
    unpeekWord(idx)
  }
}
function handleChinesePeekKey(idx, e) {
  if (e.code === 'Backquote') {
    e.preventDefault()
    e.stopPropagation()
    if (!e.repeat) peekChinese(idx)
  }
}
function handleChinesePeekKeyUp(idx, e) {
  if (e.code === 'Backquote') {
    e.preventDefault()
    e.stopPropagation()
    unpeekChinese(idx)
  }
}

function peekWord(idx) {
  peekBufferWord.value[idx] = wordInput.value[idx]
  wordInput.value[idx] = words.value[idx].word
}
function unpeekWord(idx) {
  wordInput.value[idx] = peekBufferWord.value[idx]
}
function peekChinese(idx) {
  peekBufferChinese.value[idx] = chineseInput.value[idx]
  chineseInput.value[idx] = (words.value[idx].meaning || '').replace(/<br\s*\/?>/g, '\n')
}
function unpeekChinese(idx) {
  chineseInput.value[idx] = peekBufferChinese.value[idx]
}

// ===== 拼写检查 =====
function checkWordSpell(idx) {
  const val = (wordInput.value[idx] || '').trim().toLowerCase()
  if (val === String(words.value[idx].word || '').toLowerCase()) {
    wordWrong.value[idx] = false
    wordSpelling.value[idx] = false
    if (wordSpelling.value.every((v) => !v)) wordSpellMode.value = false
  } else {
    wordWrong.value[idx] = true
    wordInput.value[idx] = ''
  }
}

function checkChineseSpell(idx) {
  const val = (chineseInput.value[idx] || '').replace(/\s/g, '')
  const meanings = String(words.value[idx].meaning || '')
    .replace(/<br\s*\/?>/g, '|')
    .replace(/(n\.|v\.|vt\.|adj\.|adv\.)/g, '')
    .split('|')
    .map((e) => e.trim())
  const ok = meanings.some((m) => m && (m.includes(val) || val.includes(m)))
  if (ok) {
    chineseWrong.value[idx] = false
    chineseSpelling.value[idx] = false
    if (chineseSpelling.value.every((v) => !v)) chineseSpellMode.value = false
  } else {
    chineseWrong.value[idx] = true
    chineseInput.value[idx] = ''
  }
}

// ===== 发音 =====
function speak(text) {
  if (!window.speechSynthesis) return

  const utter = new window.SpeechSynthesisUtterance(text)
  const curLang = localStorage.getItem('wordLang') || lang.value || 'EN'
  const targetLang = curLang === 'KO' ? 'ko-KR' : 'en-US'
  utter.lang = targetLang

  const voices = window.speechSynthesis.getVoices()
  const matched = voices.find((v) => v.lang && v.lang.startsWith(curLang === 'KO' ? 'ko' : 'en'))
  if (matched) utter.voice = matched

  window.speechSynthesis.cancel()
  window.speechSynthesis.speak(utter)
}

// ===== “拓” 按住预览 =====
function handleExtMouseDown(idx) {
  if (showExtensions.value) return
  activeExtRow.value = idx
  window.addEventListener('mouseup', handleExtMouseUp)
}
function handleExtMouseUp() {
  activeExtRow.value = null
  window.removeEventListener('mouseup', handleExtMouseUp)
}

// ===== 拓展词 =====
function getExtensionsByType(item, type) {
  if (!item || !Array.isArray(item.extensions)) return []
  return item.extensions.filter((ext) => ext.type === type)
}
function isSentenceExt(ext) {
  if (!ext || !ext.textKor) return false
  return ext.textKor.trim().length > 10
}

// ===== 编辑中文 =====
function enableEdit(idx) {
  editIndex.value[idx] = true
  editInput.value[idx] = words.value[idx].meaning
  editMsg.value[idx] = ''
}
function cancelEdit(idx) {
  editIndex.value[idx] = false
  editInput.value[idx] = words.value[idx].meaning
  editMsg.value[idx] = ''
}
async function saveEdit(idx) {
  editMsg.value[idx] = '保存中...'
  try {
    const resp = await fetch(`/api/words/${words.value[idx].id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ...words.value[idx],
        meaning: editInput.value[idx]
      })
    })
    if (resp.ok) {
      words.value[idx].meaning = editInput.value[idx]
      editMsg.value[idx] = '保存成功'
      setTimeout(() => {
        editMsg.value[idx] = ''
        editIndex.value[idx] = false
      }, 800)
    } else {
      editMsg.value[idx] = '保存失败'
    }
  } catch {
    editMsg.value[idx] = '网络错误'
  }
}

// ===== 中文多词性分行显示 =====
function formatMeaning(meaning) {
  if (!meaning) return ''
  let s = meaning.replace(/<br\s*\/?>/g, '<br>')
  const posPattern = /(n\.|v\.|vi\.|vt\.|adj\.|adv\.|prep\.|conj\.|pron\.|auxv\.)/g
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

// ===== 路由变化 =====
watch(
  () => route.query.day,
  (newDay) => {
    day.value = Number(newDay) || Number(localStorage.getItem('currentDay')) || 1
    fetchWrongWords()
  }
)

watch(
  () => route.query.lang,
  (newLang) => {
    if (typeof newLang === 'string' && newLang) {
      lang.value = newLang
      localStorage.setItem('wordLang', newLang)
      fetchWrongWords()
    }
  }
)

onMounted(() => {
  fetchWrongWords()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('mouseup', handleExtMouseUp)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.word-list-root {
  width: -moz-fit-content;
  width: fit-content;
  margin: 36px auto 0 auto;
  padding: 0 0 36px 0;
  color: var(--text-color);
  transition: opacity 0.3s ease-in-out;
  min-width: 600px;
}

.top-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
  margin-bottom: 22px;
  justify-content: flex-start;
  width: 0;
  min-width: 100%;
}

.global-msg {
  text-align: center;
  margin-top: 50px;
  color: #ffae00;
  font-size: 1.2rem;
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

h1 {
  color: var(--text-color);
  font-size: 2rem;
  margin: 22px 0 24px 0;
  text-align: left;
  font-weight: 700;
}

table {
  border-collapse: collapse;
  max-width: 100%;
  background: var(--main-bg);
  margin-bottom: 12px;
}

th,
td {
  padding: 8px;
  border: 1px solid #444;
  vertical-align: middle;
}

td.word-hidden,
td.chinese-hidden {
  color: #fff;
  text-decoration: none;
  cursor: default;
}

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

button.toggle-btn {
  background: #222;
  color: #fff;
  border: 1px solid #555;
  border-radius: 5px;
  padding: 4px 12px;
  margin-left: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

button.toggle-btn:hover {
  background: #333;
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

.ext-inline-btn {
  margin-left: 6px;
  padding: 2px 8px;
  border-radius: 4px;
  border: none;
  background: rgb(17, 165, 199);
  color: #fff;
  cursor: pointer;
  font-size: 12px;
  transition: background 0.15s;
}

.ext-inline-btn:hover {
  filter: brightness(1.1);
}

.ext-inline-btn.active {
  background: rgb(10, 130, 155);
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

.spell-input,
.edit-input {
  background: #111;
  color: #fff;
  border: 1px solid #555;
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 15px;
  width: 90px;
  margin-right: 5px;
}

.speak-btn {
  background: #0af;
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  font-weight: bold;
  font-size: 14px;
  cursor: pointer;
  vertical-align: middle;
  margin-left: 2px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.speak-btn:hover {
  background: #5cf;
}

.peek-btn,
.skip-btn {
  background: #222;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 15px;
  margin-left: 4px;
  padding: 2px 7px;
  transition: background 0.2s;
  vertical-align: middle;
}

.peek-btn:hover {
  background: #333;
}

.skip-btn {
  background: #666;
  color: #fff;
  font-size: 13px;
}

.skip-btn.active {
  background: #fff7b2;
  color: #886200;
}

.skip-btn:hover {
  background: #bbb;
  color: #333;
}

.wrong {
  color: #f44;
  font-size: 18px;
  margin-left: 8px;
}

.chinese {
  white-space: pre-line;
}

.edit-area {
  display: flex;
  align-items: center;
  gap: 6px;
}

.edit-btn {
  margin-left: 10px;
  background: #1e90ff;
  color: #fff;
  border: none;
  border-radius: 5px;
  padding: 1px 10px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.edit-btn:hover {
  background: #256fff;
}

.edit-input {
  width: 220px;
  margin-left: 4px;
  font-size: 15px;
}

.save-btn,
.cancel-btn {
  background: #f44;
  color: #fff;
  border: none;
  border-radius: 5px;
  font-size: 15px;
  padding: 3px 18px;
  cursor: pointer;
  transition: background 0.15s;
}

.save-btn {
  background: #22b573;
  color: #fff;
}

.save-btn:hover {
  background: #17a05c;
}

.cancel-btn {
  background: #f44;
  color: #fff;
}

.cancel-btn:hover {
  background: #d00;
  color: #222;
}

.edit-msg {
  color: #22b573;
  font-size: 13px;
  margin-left: 8px;
  margin-top: 3px;
}

.ext-btn {
  padding: 6px 18px;
  border-radius: 7px;
  border: 1px solid #1e90ff;
  color: #1e90ff;
  background: transparent;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.ext-btn:hover {
  background: #1e90ff;
  color: #fff;
}

.ext-col {
  font-size: 13px;
  color: #cfd8dc;
}

.ext-group {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  margin-bottom: 8px;
}

.ext-tag {
  display: inline-block;
  min-width: 26px;
  padding: 2px 8px;
  border-radius: 12px;
  color: #fff;
  font-size: 12px;
  text-align: center;
}

.ext-tag-similar {
  background: #2196f3;
}

.ext-tag-sentence {
  background: #26c6da;
}

.ext-tag-related {
  background: #9c27b0;
}

.ext-tag-idiom {
  background: #ff9800;
}

.ext-tag-antonym {
  background: #e53935;
}

.ext-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ext-item {
  display: flex;
  gap: 6px;
  align-items: flex-start;
  max-width: 250px;
  word-break: break-all;
}

.ext-text {
  display: inline-block;
  max-width: 250px;
  overflow-wrap: break-word;
}

.ext-text-kor {
  margin-bottom: 2px;
}

.ext-text-cn {
  font-size: 12px;
  color: #b0bec5;
}

.speak {
  cursor: pointer;
}

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
