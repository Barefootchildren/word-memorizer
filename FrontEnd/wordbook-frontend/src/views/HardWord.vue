<template>
  <div class="word-list-root">
    <div class="top-btns">
      <button @click="goHome" class="nav-btn">返回主页</button>
      <button @click="goDaySelect" class="nav-btn">返回天数选择</button>
      <button @click="goTodayList" class="nav-btn">返回当天单词列表</button>
      <button @click="toggleExtensions" class="ext-btn">
        {{ showExtensions ? '隐藏拓展词' : '显示拓展词' }}
      </button>
    </div>

    <h1>顽固单词列表（{{ langLabel }}）</h1>
    <div class="day-info">
      当前天数：第 {{ day }} 天
      <button
        class="star-filter-btn"
        @click="toggleStarFilter"
        :class="{ active: showOnlyStar }"
        tabindex="-1"
      >
        只看⭐
      </button>
    </div>

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
          <!-- 和单词列表一样：全局显示或有行按住“拓”时才显示表头 -->
          <th v-if="showExtensions || activeExtRow !== null">拓展词</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <!-- 用 displayWords（带 idx）保证只看⭐时序号从1开始 -->
        <tr v-for="(row, i) in displayWords" :key="row.item.id">
          <td>{{ i + 1 }}</td>

          <!-- 单词列：星标放在单词文本后面，“拓”按住预览当前行拓展词 -->
          <td v-if="wordVisible || (wordSpellMode && wordSpelling[row.idx])">
            <template v-if="wordSpellMode && wordSpelling[row.idx]">
              <input
                class="spell-input"
                v-model="wordInput[row.idx]"
                :disabled="skipWord[row.idx]"
                @keydown.tab.prevent="focusNext('word', row.idx, $event.shiftKey)"
                @keydown.enter.prevent="checkWordSpell(row.idx)"
                @keydown="handleWordPeekKey(row.idx, $event)"
                @keyup="handleWordPeekKeyUp(row.idx, $event)"
                autocomplete="off"
                :id="'word_input_' + row.idx"
              />

              <button
                class="speak-btn"
                @click="speak(row.item.word)"
                title="听发音"
                tabindex="-1"
              >
                🔊
              </button>
              <button
                class="peek-btn"
                @mousedown="peekWord(row.idx)"
                @mouseup="unpeekWord(row.idx)"
                @mouseleave="unpeekWord(row.idx)"
                tabindex="-1"
              >
                👁️
              </button>
              <button
                class="skip-btn"
                :class="{ active: skipWord[row.idx] }"
                @click="toggleSkipWord(row.idx)"
                tabindex="-1"
              >
                {{ skipWord[row.idx] ? '不跳过' : '跳过' }}
              </button>
              <span v-if="wordWrong[row.idx]" class="wrong">✗</span>
            </template>
            <template v-else>
              <span class="word-text" @click="speak(row.item.word)">{{ row.item.word }}</span>
              <!-- 星标：可点击文本，不改变样式，只是鼠标变成 pointer -->
              <span class="star-toggle" @click.stop="toggleStar(row.idx)" tabindex="-1">
                <span v-if="starFlags[row.idx]">⭐</span>
                <span v-else>-</span>
              </span>
              <!-- 行内“拓”按钮：只在未全局显示拓展词，且该单词有拓展词时出现 -->
              <button
                v-if="
                  !showExtensions &&
                  row.item.extensions &&
                  row.item.extensions.length
                "
                :class="['ext-inline-btn', { active: activeExtRow === row.idx }]"
                @mousedown.prevent="handleExtMouseDown(row.idx)"
                tabindex="-1"
              >
                拓
              </button>
            </template>
          </td>
          <td v-else class="word-hidden">---</td>

          <!-- 中文列 -->
          <td v-if="chineseVisible || (chineseSpellMode && chineseSpelling[row.idx])">
            <template v-if="chineseSpellMode && chineseSpelling[row.idx]">
              <div class="edit-area">
                <input
                  class="spell-input"
                  v-model="chineseInput[row.idx]"
                  :disabled="skipChinese[row.idx]"
                  @keydown.tab.prevent="
                    focusNext('chinese', row.idx, $event.shiftKey)
                  "
                  @keydown.enter.prevent="checkChineseSpell(row.idx)"
                  @keydown="handleChinesePeekKey(row.idx, $event)"
                  @keyup="handleChinesePeekKeyUp(row.idx, $event)"
                  autocomplete="off"
                  :id="'chinese_input_' + row.idx"
                />

                <button
                  class="peek-btn"
                  @mousedown="peekChinese(row.idx)"
                  @mouseup="unpeekChinese(row.idx)"
                  @mouseleave="unpeekChinese(row.idx)"
                  tabindex="-1"
                >
                  👁️
                </button>
                <button
                  class="skip-btn"
                  :class="{ active: skipChinese[row.idx] }"
                  @click="toggleSkipChinese(row.idx)"
                  tabindex="-1"
                >
                  {{ skipChinese[row.idx] ? '不跳过' : '跳过' }}
                </button>
                <span v-if="chineseWrong[row.idx]" class="wrong">✗</span>
                <button
                  v-if="!editIndex[row.idx]"
                  class="edit-btn"
                  @click="enableEdit(row.idx)"
                  tabindex="-1"
                >
                  ✎
                </button>
                <span v-else class="edit-actions">
                  <button class="save-btn" @click="saveEdit(row.idx)" tabindex="-1">
                    保存
                  </button>
                  <button
                    class="cancel-btn"
                    @click="cancelEdit(row.idx)"
                    tabindex="-1"
                  >
                    取消
                  </button>
                </span>
              </div>
              <div v-if="editMsg[row.idx]" class="edit-msg">
                {{ editMsg[row.idx] }}
              </div>
            </template>
            <template v-else>
              <div class="edit-area">
                <span
                  class="chinese"
                  v-html="row.item.meaning.replace(/<br\s*\/?>/g, '<br>')"
                ></span>
                <button
                  v-if="!editIndex[row.idx]"
                  class="edit-btn"
                  @click="enableEdit(row.idx)"
                  tabindex="-1"
                >
                  ✎
                </button>
                <span v-else class="edit-actions">
                  <input class="edit-input" v-model="editInput[row.idx]" />
                  <button class="save-btn" @click="saveEdit(row.idx)" tabindex="-1">
                    保存
                  </button>
                  <button
                    class="cancel-btn"
                    @click="cancelEdit(row.idx)"
                    tabindex="-1"
                  >
                    取消
                  </button>
                </span>
              </div>
              <div v-if="editMsg[row.idx]" class="edit-msg">
                {{ editMsg[row.idx] }}
              </div>
            </template>
          </td>
          <td v-else class="chinese-hidden">---</td>

          <!-- 拓展词列：和单词列表逻辑一致 -->
          <td v-if="showExtensions || activeExtRow !== null" class="ext-col">
            <template v-if="showExtensions || activeExtRow === row.idx">
              <!-- SENTENCE 例句：韩文/中文分行 -->
              <div
                v-if="getExtensionsByType(row.item, 'SENTENCE').length"
                class="ext-group"
              >
                <span class="ext-tag ext-tag-sentence">例</span>
                <div class="ext-list">
                  <div
                    v-for="ext in getExtensionsByType(row.item, 'SENTENCE')"
                    :key="ext.id"
                    class="ext-item"
                  >
                    <span class="speak" title="发音" @click="speak(ext.textKor)">
                      🔊
                    </span>
                    <div class="ext-text">
                      <div class="ext-text-kor">{{ ext.textKor }}</div>
                      <div class="ext-text-cn">{{ ext.textCn }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- SIMILAR 近义词 -->
              <div
                v-if="getExtensionsByType(row.item, 'SIMILAR').length"
                class="ext-group"
              >
                <span class="ext-tag ext-tag-similar">近</span>
                <div class="ext-list">
                  <div
                    v-for="ext in getExtensionsByType(row.item, 'SIMILAR')"
                    :key="ext.id"
                    class="ext-item"
                  >
                    <span class="speak" title="发音" @click="speak(ext.textKor)">
                      🔊
                    </span>
                    <span class="ext-text">{{ ext.textKor }} — {{ ext.textCn }}</span>
                  </div>
                </div>
              </div>

              <!-- RELATED 关联词 -->
              <div
                v-if="getExtensionsByType(row.item, 'RELATED').length"
                class="ext-group"
              >
                <span class="ext-tag ext-tag-related">关</span>
                <div class="ext-list">
                  <div
                    v-for="ext in getExtensionsByType(row.item, 'RELATED')"
                    :key="ext.id"
                    class="ext-item"
                  >
                    <span class="speak" title="发音" @click="speak(ext.textKor)">
                      🔊
                    </span>
                    <span class="ext-text">{{ ext.textKor }} — {{ ext.textCn }}</span>
                  </div>
                </div>
              </div>

              <!-- ANTONYM 反义词 -->
              <div
                v-if="getExtensionsByType(row.item, 'ANTONYM').length"
                class="ext-group"
              >
                <span class="ext-tag ext-tag-antonym">反</span>
                <div class="ext-list">
                  <div
                    v-for="ext in getExtensionsByType(row.item, 'ANTONYM')"
                    :key="ext.id"
                    class="ext-item"
                  >
                    <span class="speak" title="发音" @click="speak(ext.textKor)">
                      🔊
                    </span>
                    <span class="ext-text">{{ ext.textKor }} — {{ ext.textCn }}</span>
                  </div>
                </div>
              </div>

              <!-- IDIOM 惯用语：韩文长度>10 按句子分行，否则一行 -->
              <div
                v-if="getExtensionsByType(row.item, 'IDIOM').length"
                class="ext-group"
              >
                <span class="ext-tag ext-tag-idiom">惯</span>
                <div class="ext-list">
                  <div
                    v-for="ext in getExtensionsByType(row.item, 'IDIOM')"
                    :key="ext.id"
                    class="ext-item"
                  >
                    <span class="speak" title="发音" @click="speak(ext.textKor)">
                      🔊
                    </span>
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

          <!-- 操作列：只保留移除 -->
          <td>
            <button class="remove-btn" @click="removeHard(row.idx)" tabindex="-1">
              移除
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 回到顶部按钮：和单词列表页一致，靠在表格右侧 -->
    <button
      v-if="showBackToTop"
      class="back-top-btn"
      @click="scrollToTop"
      :style="{ left: backTopLeft + 'px' }"
      tabindex="-1"
    >
      回到顶部
    </button>

    <div v-if="loading" class="msg">加载中...</div>
    <div v-if="msg" class="msg">{{ msg }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
function goHome() {
  router.push('/home')
}
function goDaySelect() {
  router.push('/select-day')
}
function goTodayList() {
  router.push('/words/' + day.value)
}

// 当前语言 & 天数
const lang = ref(route.query.lang || localStorage.getItem('wordLang') || 'EN')
const day = ref(Number(route.query.day) || 1)
const langLabel = computed(() =>
  lang.value === 'KO' ? '韩语单词' : '英语单词'
)

const words = ref([])
const loading = ref(false)
const msg = ref('')
const showExtensions = ref(false)
const showOnlyStar = ref(false) // 只看⭐开关

// 回到顶部按钮状态
const showBackToTop = ref(false)
const backTopLeft = ref(0)

// 带原始索引的可见行（用于只看⭐）
const displayWords = computed(() =>
  words.value
    .map((w, idx) => ({ item: w, idx }))
    .filter((row) => !showOnlyStar.value || starFlags.value[row.idx])
)

// 当前按住“拓”按钮预览拓展词的行索引
const activeExtRow = ref(null)

// 状态变量
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
const starFlags = ref([])

// 编辑中文
const editIndex = ref([])
const editInput = ref([])
const editMsg = ref([])

const getHardWordsByDay = async (username, day, lang) => {
  const resp = await fetch(
    `/api/hard-words/${encodeURIComponent(username)}/day/${day}?lang=${lang}`
  )
  if (!resp.ok) {
    throw new Error('fetch hard words failed')
  }
  return resp.json()
}

const fetchWords = async () => {
  loading.value = true
  msg.value = ''
  starFlags.value = []
  const username = localStorage.getItem('username') || ''
  if (!username) {
    msg.value = '请先登录'
    loading.value = false
    return
  }
  try {
    const data = await getHardWordsByDay(username, day.value, lang.value)
    words.value = (Array.isArray(data) ? data : [])
      .sort((a, b) => a.id - b.id)
      .map((w) => ({
        ...w,
        extensions: Array.isArray(w.extensions) ? w.extensions : []
      }))
    starFlags.value = words.value.map((w) => w.isStar === 1)
    resetState()
    updateBackToTopVisibility()
  } catch {
    msg.value = '获取单词失败'
    words.value = []
    starFlags.value = []
    updateBackToTopVisibility()
  }
  loading.value = false
}

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
  editIndex.value = Array(n).fill(false)
  editInput.value = words.value.map((w) => w.meaning)
  editMsg.value = Array(n).fill('')
  activeExtRow.value = null
}

// 星标只看模式
function toggleStarFilter() {
  showOnlyStar.value = !showOnlyStar.value
}

// 星标开关
async function toggleStar(i) {
  const item = words.value[i]
  if (!item) return
  starFlags.value[i] = !starFlags.value[i]
  const starVal = starFlags.value[i] ? 1 : 0
  const username = localStorage.getItem('username') || ''

  await fetch('/api/hard-words/star', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `username=${encodeURIComponent(username)}&wordId=${item.id}&star=${starVal}`
  })
}

// 拓展词按类型
function getExtensionsByType(item, type) {
  if (!item || !Array.isArray(item.extensions)) return []
  return item.extensions
    .filter((ext) => ext.type === type)
    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
}

// 是否是句子（韩文长度 > 10 就按句子分行）
function isSentenceExt(ext) {
  if (!ext || !ext.textKor) return false
  const kor = ext.textKor.trim()
  return kor.length > 10
}

// 切换隐藏/拼写
function toggleWord() {
  wordVisible.value = !wordVisible.value
}
function toggleChinese() {
  chineseVisible.value = !chineseVisible.value
}
function toggleExtensions() {
  showExtensions.value = !showExtensions.value
  if (showExtensions.value) {
    activeExtRow.value = null
  }
  // 拓展列宽度变化，重新计算回到顶部按钮位置
  if (showBackToTop.value) {
    nextTick(() => {
      updateBackTopPosition()
    })
  }
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

// 回到顶部
function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 根据单词数量控制按钮是否出现
function updateBackToTopVisibility() {
  showBackToTop.value = words.value.length > 20
  if (showBackToTop.value) {
    nextTick(() => {
      updateBackTopPosition()
    })
  }
}

// 计算“回到顶部”按钮距离右侧的距离，让它紧贴表格最右边那一列
function updateBackTopPosition() {
  const table = document.querySelector('.word-list-root table')
  if (!table) {
    backTopLeft.value = 0
    return
  }

  const rect = table.getBoundingClientRect()
  const viewportWidth = window.innerWidth || document.documentElement.clientWidth

  // 表格右边缘往外 8px
  let left = rect.right + 8

  // 防止按钮跑出屏幕右侧
  const buttonWidth = 110
  const maxLeft = viewportWidth - buttonWidth - 8
  if (left > maxLeft) {
    left = maxLeft
  }

  backTopLeft.value = left
}

// Tab 跳转
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

// 跳过
function toggleSkipWord(idx) {
  skipWord.value[idx] = !skipWord.value[idx]
}
function toggleSkipChinese(idx) {
  skipChinese.value[idx] = !skipChinese.value[idx]
}

// 键盘“瞅一眼”
function handleWordPeekKey(idx, e) {
  if (e.code === 'Backquote') {
    e.preventDefault()
    e.stopPropagation()
    if (!e.repeat) {
      peekWord(idx)
    }
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
    if (!e.repeat) {
      peekChinese(idx)
    }
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
  chineseInput.value[idx] = words.value[idx].meaning.replace(/<br\s*\/?>/g, '\n')
}
function unpeekChinese(idx) {
  chineseInput.value[idx] = peekBufferChinese.value[idx]
}

// 拼写检查
function checkWordSpell(idx) {
  const val = (wordInput.value[idx] || '').trim().toLowerCase()
  if (val === words.value[idx].word.toLowerCase()) {
    wordWrong.value[idx] = false
    wordSpelling.value[idx] = false
    if (wordSpelling.value.every((v) => !v)) {
      wordSpellMode.value = false
    }
  } else {
    wordWrong.value[idx] = true
    wordInput.value[idx] = ''
  }
}
function checkChineseSpell(idx) {
  const val = (chineseInput.value[idx] || '').replace(/\s/g, '')
  const meanings = words.value[idx].meaning
    .replace(/<br\s*\/?>/g, '|')
    .replace(/(n\.|v\.|vt\.|adj\.|adv\.)/g, '')
    .split('|')
    .map((e) => e.trim())
  const ok = meanings.some((m) => m && (m.includes(val) || val.includes(m)))
  if (ok) {
    chineseWrong.value[idx] = false
    chineseSpelling.value[idx] = false
    if (chineseSpelling.value.every((v) => !v)) {
      chineseSpellMode.value = false
    }
  } else {
    chineseWrong.value[idx] = true
    chineseInput.value[idx] = ''
  }
}

// 发音
function speak(text) {
  if (!window.speechSynthesis) {
    return
  }

  const utter = new window.SpeechSynthesisUtterance(text)
  const curLang = localStorage.getItem('wordLang') || 'EN'
  const targetLang = curLang === 'KO' ? 'ko-KR' : 'en-US'
  utter.lang = targetLang

  const voices = window.speechSynthesis.getVoices()
  const matched = voices.find(
    (v) => v.lang && v.lang.startsWith(curLang === 'KO' ? 'ko' : 'en')
  )
  if (matched) {
    utter.voice = matched
  }

  window.speechSynthesis.cancel()
  window.speechSynthesis.speak(utter)
}

// “拓” 按住预览拓展词
function handleExtMouseDown(idx) {
  if (showExtensions.value) return
  activeExtRow.value = idx
  window.addEventListener('mouseup', handleExtMouseUp)
}

function handleExtMouseUp() {
  activeExtRow.value = null
  window.removeEventListener('mouseup', handleExtMouseUp)
}

// 编辑中文
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

// 移除顽固单词
async function removeHard(idx) {
  const username = localStorage.getItem('username') || ''
  const wordId = words.value[idx].id
  if (!username) return
  try {
    const resp = await fetch('/api/hard-words/remove', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: `username=${encodeURIComponent(username)}&wordId=${wordId}`
    })
    if (resp.ok) {
      msg.value = '已移除'
      await fetchWords()
    } else {
      msg.value = '移除失败'
    }
  } catch {
    msg.value = '网络错误'
  }
}

onMounted(fetchWords)

onBeforeUnmount(() => {
  window.removeEventListener('mouseup', handleExtMouseUp)
})
</script>

<style scoped>
.top-btns {
  display: flex;
  gap: 18px;
  margin-bottom: 22px;
  margin-top: 10px;
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

.word-list-root {
  max-width: 1000px;
  margin: 36px auto 0 auto;
  padding: 0 0 36px 0;
  color: var(--text-color);
}

h1 {
  color: var(--text-color);
  font-size: 2rem;
  margin: 22px 0 24px 0;
  text-align: left;
  font-weight: 700;
}

.day-info {
  color: #cfd8dc;
  margin: -12px 0 20px 0;
  font-size: 15px;
}

/* ⭐标单词过滤按钮 */
.star-filter-btn {
  margin-left: 12px;
  padding: 2px 12px;
  border-radius: 12px;
  border: 1px solid #64b5f6;
  background: #42a5f5;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.star-filter-btn.active {
  background: #1e88e5;
  border-color: #1e88e5;
}

table {
  border-collapse: collapse;
  width: 100%;
  background: var(--main-bg);
  margin-bottom: 12px;
}

th,
td {
  padding: 10px;
  border: 1px solid #444;
  vertical-align: middle;
}

td.word-hidden,
td.chinese-hidden {
  color: #fff;
  text-decoration: none;
  cursor: default;
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

.remove-btn {
  background: #f44;
  color: #fff;
  border: none;
  border-radius: 5px;
  font-size: 15px;
  padding: 3px 18px;
  cursor: pointer;
  transition: background 0.15s;
}

.remove-btn:hover {
  background: #d00;
}

/* 星标：可点击文本，外观跟普通文本类似，不改颜色/下划线，只改鼠标形状 */
.star-toggle {
  margin-left: 8px;
  cursor: pointer;
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
  margin-left: 4px;
  padding: 2px 10px;
  border: none;
  border-radius: 5px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.save-btn {
  background: #22b573;
  color: #fff;
}

.save-btn:hover {
  background: #17a05c;
}

.cancel-btn {
  background: #888;
  color: #fff;
}

.cancel-btn:hover {
  background: #bbb;
  color: #222;
}

.edit-msg {
  color: #22b573;
  font-size: 13px;
  margin-left: 8px;
  margin-top: 3px;
}

.msg {
  margin-top: 14px;
  color: #ffae00;
}

.ext-btn {
  margin-left: auto;
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

/* 行内“拓”按钮（和单词列表页一致） */
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

/* 回到顶部按钮：和单词列表页一样 */
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
