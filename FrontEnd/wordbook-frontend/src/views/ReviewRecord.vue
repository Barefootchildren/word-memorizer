<!-- src/views/ReviewRecord.vue -->
<template>
  <div class="review-container">
    <h2>复习记录（{{ langLabel }}）</h2>

    <div v-if="!username">
      <div class="msg">请先登录。</div>
      <router-link to="/login">去登录</router-link>
    </div>

    <div v-else>
      <button @click="fetchRecords" class="refresh-btn">刷新</button>

      <table class="review-table" v-if="records.length">
        <thead>
          <tr>
            <th>单词</th>
            <th>释义</th>
            <th>状态</th>
            <th>复习次数</th>
            <th>最后复习时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="rec in records" :key="rec.id">
            <td>{{ rec.word.word }}</td>
            <td v-html="rec.word.meaning"></td>
            <td>{{ statusText(rec.status) }}</td>
            <td>{{ rec.reviewTimes }}</td>
            <td>{{ formatTime(rec.lastReview) }}</td>
          </tr>
        </tbody>
      </table>

      <div v-if="!records.length" class="msg">暂无复习记录</div>
      <div v-if="msg" class="msg">{{ msg }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'

const username = localStorage.getItem('username') || ''
const lang = ref(localStorage.getItem('wordLang') || 'EN')
const langLabel = computed(() => (lang.value === 'KO' ? '韩语单词' : '英语单词'))

const records = ref([])
const msg = ref('')

function formatTime(str) {
  if (!str) return '-'
  const d = new Date(str)
  return d.toLocaleString()
}

function statusText(s) {
  if (s === 'new') return '新词'
  if (s === 'familiar') return '熟悉'
  if (s === 'hard') return '顽固'
  if (s === 'mastered') return '已掌握'
  return s || '-'
}

const fetchRecords = async () => {
  if (!username) return
  msg.value = ''
  try {
    const resp = await fetch(`/api/review-record/user/${username}?lang=${lang.value}`)
    records.value = await resp.json()
  } catch {
    msg.value = '获取复习记录失败'
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.review-container {
  max-width: 820px;
  margin: 40px auto;
  background: #24303c;
  padding: 28px 28px 20px 28px;
  border-radius: 10px;
  color: #fff;
}
h2 {
  margin: 0 0 16px 0;
  font-size: 1.7rem;
}
.refresh-btn {
  margin-bottom: 14px;
  padding: 6px 18px;
  border-radius: 6px;
  border: none;
  background: #1e90ff;
  color: #fff;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.15s;
}
.refresh-btn:hover {
  background: #256fff;
}
.review-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}
.review-table th,
.review-table td {
  border: 1px solid #555;
  padding: 9px 11px;
  text-align: left;
}
.review-table th {
  background: #16a085;
  color: #fff;
}
.review-table tr:nth-child(even) {
  background: #26313f;
}
.msg {
  margin: 14px 0 0 0;
  color: #ffc500;
}
</style>
