<template>
  <div class="page">
    <h1>听写模式 Demo</h1>

    <!-- 单词列表页 -->
    <div v-if="mode === 'list'">
      <div class="toolbar">
        <button @click="goToDictation">听写模式</button>
        <button @click="goToWrongBook">错词本</button>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>原词</th>
            <th>中文</th>
            <th>拓展词</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="w in words" :key="w.id">
            <td>{{ w.word }}</td>
            <td>{{ w.meaning }}</td>
            <td>{{ w.extension }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 听写模式（基于单词列表） -->
    <div v-else-if="mode === 'dictation'">
      <div class="toolbar">
        <button @click="goToList">返回单词列表</button>

        <button
          v-if="!mainDictationStarted"
          @click="startMainDictation"
        >
          开始听写
        </button>

        <button
          v-else
          @click="finishMainDictation"
          :disabled="mainDictationFinished"
        >
          听写完成
        </button>

        <button
          v-if="mainDictationFinished && mainWrongIds.length > 0"
          @click="addWrongToBook"
        >
          加入错词本
        </button>

        <button
          style="margin-left: auto;"
          @click="goToWrongBook"
        >
          错词本
        </button>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>原词</th>
            <th>拼写</th>
            <th>中文</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="w in words" :key="w.id">
            <!-- 原词列：听写期间隐藏 -->
            <td>
              <span v-if="!mainDictationStarted || mainDictationFinished">
                {{ w.word }}
              </span>
              <span v-else class="hidden-text">••••••</span>
            </td>

            <!-- 拼写列：只在听写期间允许输入，完成后变为只读并标错 -->
            <td>
              <input
                class="spell-input"
                v-model="mainInputs[w.id]"
                :disabled="!mainDictationStarted || mainDictationFinished"
                :class="{
                  'is-wrong': mainDictationFinished && mainWrongIds.includes(w.id)
                }"
                placeholder="请输入拼写"
              />
            </td>

            <!-- 中文列：听写期间隐藏 -->
            <td>
              <span v-if="!mainDictationStarted || mainDictationFinished">
                {{ w.meaning }}
              </span>
              <span v-else class="hidden-text">••••••</span>
            </td>
          </tr>
        </tbody>
      </table>

      <div
        v-if="mainDictationFinished"
        class="stats"
      >
        正确 {{ mainStats.correct }} / {{ mainStats.total }} ，正确率
        {{ mainAccuracy }}%
      </div>
    </div>

    <!-- 错词本页面（同样有听写功能，但不再有“加入错词本”按钮） -->
    <div v-else-if="mode === 'wrongBook'">
      <div class="toolbar">
        <button @click="goToList">返回单词列表</button>

        <button
          v-if="wrongWords.length > 0 && !wrongDictationStarted"
          @click="startWrongDictation"
        >
          开始听写
        </button>

        <button
          v-if="wrongWords.length > 0 && wrongDictationStarted"
          @click="finishWrongDictation"
          :disabled="wrongDictationFinished"
        >
          听写完成
        </button>
      </div>

      <div v-if="wrongWords.length === 0" class="empty-tip">
        当前没有错词。
      </div>

      <div v-else>
        <table class="table">
          <thead>
            <tr>
              <th>原词</th>
              <th>拼写</th>
              <th>中文</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="w in wrongWords" :key="w.id">
              <!-- 原词：听写期间隐藏 -->
              <td>
                <span v-if="!wrongDictationStarted || wrongDictationFinished">
                  {{ w.word }}
                </span>
                <span v-else class="hidden-text">••••••</span>
              </td>

              <!-- 拼写 -->
              <td>
                <input
                  class="spell-input"
                  v-model="wrongInputs[w.id]"
                  :disabled="!wrongDictationStarted || wrongDictationFinished"
                  :class="{
                    'is-wrong':
                      wrongDictationFinished &&
                      wrongWrongIds.includes(w.id)
                  }"
                  placeholder="请输入拼写"
                />
              </td>

              <!-- 中文：听写期间隐藏 -->
              <td>
                <span v-if="!wrongDictationStarted || wrongDictationFinished">
                  {{ w.meaning }}
                </span>
                <span v-else class="hidden-text">••••••</span>
              </td>

              <!-- 移出错词本 -->
              <td>
                <button @click="removeFromWrongBook(w.id)">移除</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div
          v-if="wrongDictationFinished"
          class="stats"
        >
          正确 {{ wrongStats.correct }} / {{ wrongStats.total }} ，正确率
          {{ wrongAccuracy }}%
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 页面模式：list = 单词列表；dictation = 听写模式；wrongBook = 错词本
const mode = ref('list')

// 示例单词数据（模拟单词列表页面接口返回的内容）
const words = ref([
  { id: 1, word: 'banana', meaning: '香蕉', extension: 'fruit' },
  { id: 2, word: 'apple', meaning: '苹果', extension: 'fruit' },
  { id: 3, word: 'orange', meaning: '橙子', extension: 'fruit' },
  { id: 4, word: 'computer', meaning: '电脑', extension: 'device' }
])

// 错词本数据
const wrongWords = ref([])

// --------------------- 主列表听写模式状态 ---------------------
const mainDictationStarted = ref(false)
const mainDictationFinished = ref(false)
const mainInputs = ref({}) // { [wordId]: '用户输入' }
const mainWrongIds = ref([]) // [wordId...]
const mainStats = ref({ correct: 0, total: words.value.length })

const mainAccuracy = computed(() => {
  if (!mainStats.value || mainStats.value.total === 0) return 0
  return Math.round(
    (mainStats.value.correct / mainStats.value.total) * 100
  )
})

function resetMainDictationState() {
  mainDictationStarted.value = false
  mainDictationFinished.value = false
  mainInputs.value = {}
  mainWrongIds.value = []
  mainStats.value = { correct: 0, total: words.value.length }
}

function startMainDictation() {
  resetMainDictationState()
  mainDictationStarted.value = true
}

function finishMainDictation() {
  if (!mainDictationStarted.value || mainDictationFinished.value) return

  let correct = 0
  const wrongIds = []

  words.value.forEach((w) => {
    const input = (mainInputs.value[w.id] || '').trim()
    if (input === w.word) {
      correct++
    } else {
      wrongIds.push(w.id)
    }
  })

  mainWrongIds.value = wrongIds
  mainStats.value = { correct, total: words.value.length }
  mainDictationFinished.value = true
}

// 把当前听写错的单词加入错词本（去重）
function addWrongToBook() {
  const existingIds = wrongWords.value.map((w) => w.id)
  mainWrongIds.value.forEach((id) => {
    if (!existingIds.includes(id)) {
      const word = words.value.find((w) => w.id === id)
      if (word) {
        wrongWords.value.push({ ...word })
      }
    }
  })
}

// --------------------- 错词本听写模式状态 ---------------------
const wrongDictationStarted = ref(false)
const wrongDictationFinished = ref(false)
const wrongInputs = ref({}) // { [wordId]: '用户输入' }
const wrongWrongIds = ref([]) // [wordId...]
const wrongStats = ref({ correct: 0, total: 0 })

const wrongAccuracy = computed(() => {
  if (!wrongStats.value || wrongStats.value.total === 0) return 0
  return Math.round(
    (wrongStats.value.correct / wrongStats.value.total) * 100
  )
})

function resetWrongDictationState() {
  wrongDictationStarted.value = false
  wrongDictationFinished.value = false
  wrongInputs.value = {}
  wrongWrongIds.value = []
  wrongStats.value = { correct: 0, total: wrongWords.value.length }
}

function startWrongDictation() {
  resetWrongDictationState()
  if (wrongWords.value.length === 0) return
  wrongDictationStarted.value = true
}

function finishWrongDictation() {
  if (!wrongDictationStarted.value || wrongDictationFinished.value) return
  if (wrongWords.value.length === 0) return

  let correct = 0
  const wrongIds = []

  wrongWords.value.forEach((w) => {
    const input = (wrongInputs.value[w.id] || '').trim()
    if (input === w.word) {
      correct++
    } else {
      wrongIds.push(w.id)
    }
  })

  wrongWrongIds.value = wrongIds
  wrongStats.value = { correct, total: wrongWords.value.length }
  wrongDictationFinished.value = true
}

// 移除错词本中的单词
function removeFromWrongBook(id) {
  wrongWords.value = wrongWords.value.filter((w) => w.id !== id)
  // 顺便清理该单词的输入状态
  const copy = { ...wrongInputs.value }
  delete copy[id]
  wrongInputs.value = copy
}

// --------------------- 页面切换 ---------------------
function goToList() {
  mode.value = 'list'
}

function goToDictation() {
  mode.value = 'dictation'
  resetMainDictationState()
}

function goToWrongBook() {
  mode.value = 'wrongBook'
  resetWrongDictationState()
}
</script>

<style scoped>
.page {
  max-width: 900px;
  margin: 24px auto;
  padding: 16px;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI',
    sans-serif;
  font-size: 14px;
}

h1 {
  font-size: 22px;
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

button {
  padding: 4px 10px;
  font-size: 14px;
  cursor: pointer;
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.table th,
.table td {
  border: 1px solid #ddd;
  padding: 6px 8px;
  text-align: left;
}

.table th {
  background: #f7f7f7;
}

.hidden-text {
  color: #ccc;
}

.spell-input {
  width: 100%;
  box-sizing: border-box;
  padding: 4px 6px;
  font-size: 14px;
}

.spell-input.is-wrong {
  color: red;
}

.stats {
  margin-top: 8px;
  font-size: 14px;
}

.empty-tip {
  margin-top: 12px;
}
</style>
