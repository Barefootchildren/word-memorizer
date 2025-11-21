<template>
  <div class="book-root">
    <h2>请选择单词书（{{ langLabel }}）</h2>

    <div class="book-list">
      <button
        v-for="book in books"
        :key="book.id"
        class="book-btn"
        @click="choose(book)"
      >
        {{ book.name }}
      </button>
    </div>

    <div v-if="loading" class="tip">加载中...</div>
    <div v-else-if="books.length === 0" class="tip">暂无单词书</div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const lang = ref(localStorage.getItem('wordLang') || 'EN')
const books = ref([])
const loading = ref(true)

const langLabel = computed(() => (lang.value === 'KO' ? '韩语' : '英语'))

const choose = (book) => {
  localStorage.setItem('wordBookId', book.id)
  localStorage.setItem('wordBookName', book.name)
  router.push('/select-day')
}

onMounted(async () => {
  if (!lang.value) {
    router.push('/lang-select')
    return
  }
  try {
    const resp = await fetch(`/api/books?lang=${lang.value}`)
    const data = await resp.json()
    books.value = Array.isArray(data) ? data : []
  } catch (e) {
    books.value = []
  }
  loading.value = false
})
</script>

<style scoped>
.book-root {
  max-width: 520px;
  margin: 80px auto 0 auto;
  background: var(--main-bg);
  border-radius: 12px;
  box-shadow: 0 2px 12px #0002;
  padding: 36px 30px;
  text-align: center;
  color: var(--text-color);
}

h2 {
  font-size: 2rem;
  margin-bottom: 26px;
  font-weight: 600;
}

.book-list {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  justify-content: center;
}

.book-btn {
  background: var(--btn-bg);
  color: var(--btn-text);
  border: none;
  border-radius: 10px;
  padding: 16px 28px;
  font-size: 1.1rem;
  cursor: pointer;
  box-shadow: 0 1px 6px #0003;
  transition: background 0.14s, color 0.14s, transform 0.12s;
}

.book-btn:hover {
  background: #2e7dff;
  color: #fff;
  transform: scale(1.04);
}

.tip {
  margin-top: 16px;
  color: #888;
}
</style>
