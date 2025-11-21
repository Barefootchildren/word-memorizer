<template>
  <div class="word-list-root">
    <div class="top-btns">
      <button @click="goHome" class="nav-btn">返回主页</button>
      <button @click="goDaySelect" class="nav-btn">返回天数选择</button>
    </div>

    <h1>顽固单词列表（{{ langLabel }}）</h1>

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
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, i) in words" :key="item.id">
          <td>{{ i + 1 }}</td>

          <!-- 单词列 -->
          <td v-if="wordVisible || (wordSpellMode && wordSpelling[i])">
            <template v-if="wordSpellMode && wordSpelling[i]">
              <input
                class="spell-input"
                v-model="wordInput[i]"
                :disabled="skipWord[i]"
                @keydown.tab.prevent="focusNext('word', i, $event.shiftKey)"
                @keydown.enter.prevent="checkWordSpell(i)"
                autocomplete="off"
                :id="'word_input_' + i"
              />
              <button
                class="speak-btn"
                @click="speak(item.word)"
                title="听发音"
                tabindex="-1"
              >
                🔊
              </button>
              <button
                class="peek-btn"
                @mousedown="peekWord(i)"
                @mouseup="unpeekWord(i)"
                @mouseleave="unpeekWord(i)"
                tabindex="-1"
              >
                👁️
              </button>
              <button
                class="skip-btn"
                :class="{ active: skipWord[i] }"
                @click="toggleSkipWord(i)"
                tabindex="-1"
              >
                {{ skipWord[i] ? '不跳过' : '跳过' }}
              </button>
              <span v-if="wordWrong[i]" class="wrong">✗</span>
            </template>
            <template v-else>
              <span class="word-text" @click="speak(item.word)">{{ item.word }}</span>
            </template>
          </td>
          <td v-else class="word-hidden">---</td>

          <!-- 中文列 -->
          <td v-if="chineseVisible || (chineseSpellMode && chineseSpelling[i])">
            <template v-if="chineseSpellMode && chineseSpelling[i]">
              <div class="edit-area">
                <input
                  class="spell-input"
                  v-model="chineseInput[i]"
                  :disabled="skipChinese[i]"
                  @keydown.tab.prevent="focusNext('chinese', i, $event.shiftKey)"
                  @keydown.enter.prevent="checkChineseSpell(i)"
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
                <button
                  v-if="!editIndex[i]"
                  class="edit-btn"
                  @click="enableEdit(i)"
                  tabindex="-1"
                >
                  ✎
                </button>
                <span v-else class="edit-actions">
                  <button class="save-btn" @click="saveEdit(i)" tabindex="-1">
                    保存
                  </button>
                  <button class="cancel-btn" @click="cancelEdit(i)" tabindex="-1">
                    取消
                  </button>
                </span>
              </div>
              <div v-if="editMsg[i]" class="edit-msg">{{ editMsg[i] }}</div>
            </template>
            <template v-else>
              <div class="edit-area">
                <span
                  class="chinese"
                  v-html="item.meaning.replace(/<br\s*\/?>/g, '<br>')"
                ></span>
                <button
                  v-if="!editIndex[i]"
                  class="edit-btn"
                  @click="enableEdit(i)"
                  tabindex="-1"
                >
                  ✎
                </button>
                <span v-else class="edit-actions">
                  <input class="edit-input" v-model="editInput[i]" />
                  <button class="save-btn" @click="saveEdit(i)" tabindex="-1">
                    保存
                  </button>
                  <button class="cancel-btn" @click="cancelEdit(i)" tabindex="-1">
                    取消
                  </button>
                </span>
              </div>
              <div v-if="editMsg[i]" class="edit-msg">{{ editMsg[i] }}</div>
            </template>
          </td>
          <td v-else class="chinese-hidden">---</td>

          <td>
            <button class="remove-btn" @click="removeHard(i)" tabindex="-1">
              移除
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="loading" class="msg">加载中...</div>
    <div v-if="msg" class="msg">{{ msg }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
function goHome() {
  router.push('/home')
}
function goDaySelect() {
  router.push('/select-day')
}

// 当前语言
const lang = ref(localStorage.getItem('wordLang') || 'EN')
const langLabel = computed(() => (lang.value === 'KO' ? '韩语单词' : '英语单词'))

const words = ref([])
const loading = ref(false)
const msg = ref('')

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

// 编辑中文
const editIndex = ref([])
const editInput = ref([])
const editMsg = ref([])

const fetchWords = async () => {
  loading.value = true
  msg.value = ''
  const username = localStorage.getItem('username') || ''
  try {
    const resp = await fetch(`/api/hard-words/${username}?lang=${lang.value}`)
    words.value = await resp.json()
    resetState()
  } catch {
    msg.value = '获取单词失败'
    words.value = []
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
}

// 切换隐藏/拼写
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

// Tab 跳转
function focusNext(type, idx, shift) {
  let arr = type === 'word' ? wordSpelling.value : chineseSpelling.value
  let skipArr = type === 'word' ? skipWord.value : skipChinese.value
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

// 瞅一眼
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

  // 当前语言（从 localStorage 读取）
  const curLang = localStorage.getItem('wordLang') || 'EN'
  const targetLang = curLang === 'KO' ? 'ko-KR' : 'en-US'
  utter.lang = targetLang

  // 尝试匹配对应语言的 voice
  const voices = window.speechSynthesis.getVoices()
  const matched = voices.find(v => v.lang && v.lang.startsWith(curLang === 'KO' ? 'ko' : 'en'))
  if (matched) {
    utter.voice = matched
  }

  // 避免多个排队
  window.speechSynthesis.cancel()
  window.speechSynthesis.speak(utter)
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
</style>
