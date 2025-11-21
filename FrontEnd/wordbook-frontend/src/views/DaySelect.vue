<template>
  <div class="select-day-root">
    <h2>
      请选择要学习的天数（{{ langLabel }}<template v-if="bookName"> / {{
        bookName
      }}</template>）
    </h2>

    <div class="day-list">
      <button
        v-for="day in days"
        :key="day"
        class="day-btn"
        @click="goToDay(day)"
      >
        第{{ day }}天
      </button>
    </div>

    <div v-if="loading" class="loading-tip">加载中...</div>
    <div v-if="!loading && days.length === 0" class="none-tip">暂无可选天数</div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const days = ref([])
const loading = ref(true)
const router = useRouter()

// 当前语言：EN 或 KO
const lang = ref(localStorage.getItem('wordLang') || 'EN')
const bookId = ref(localStorage.getItem('wordBookId'))
const bookName = ref(localStorage.getItem('wordBookName') || '')

// 显示在标题里的中文标签
const langLabel = computed(() =>
  lang.value === 'KO' ? '韩语单词' : '英语单词'
)

const goToDay = (day) => {
  router.push(`/words/${day}`)
}

onMounted(async () => {
  if (!bookId.value) {
    loading.value = false
    router.push('/book-select')
    return
  }
  try {
    const resp = await fetch(
      `/api/words/days?lang=${lang.value}&bookId=${bookId.value}`
    )
    const arr = await resp.json()
    days.value = arr
  } catch {
    days.value = []
  }
  loading.value = false
})
</script>

<style scoped>
.select-day-root {
  max-width: 480px;
  margin: 60px auto 0 auto;
  background: var(--main-bg);
  border-radius: 12px;
  box-shadow: 0 2px 12px #0002;
  padding: 32px 26px 28px 26px;
  color: var(--text-color);
  text-align: center;
}
h2 {
  margin-bottom: 26px;
  font-size: 2.1rem;
  font-weight: 600;
}
.day-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  justify-content: center;
  margin-bottom: 18px;
}
.day-btn {
  background: var(--btn-bg);
  color: var(--btn-text);
  border: none;
  border-radius: 8px;
  padding: 16px 32px;
  font-size: 1.18rem;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 1px 6px #0001;
  transition: background 0.14s, color 0.14s, transform 0.13s;
}
.day-btn:hover {
  background: #2e7dff;
  color: #fff;
  transform: scale(1.04);
}
.loading-tip,
.none-tip {
  color: #888;
  font-size: 1.08rem;
  margin-top: 22px;
}
</style>
