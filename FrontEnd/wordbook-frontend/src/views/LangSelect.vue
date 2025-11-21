<template>
  <div class="lang-root">
    <h2>请选择单词语言</h2>

    <div class="btn-group">
      <button class="lang-btn" @click="choose('EN')">英语单词</button>
      <button class="lang-btn" @click="choose('KO')">韩语单词</button>
    </div>

    <p class="tip">（登录后请先选择语言）</p>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const choose = (lang) => {
  // 语言写入本地存储
  localStorage.setItem('wordLang', lang)

  // 选择完语言后进入主页面
  router.push('/book-select')
}
</script>

<style scoped>
.lang-root {
  max-width: 480px;
  margin: 100px auto 0 auto;
  background: var(--main-bg);
  padding: 40px 34px;
  border-radius: 12px;
  box-shadow: 0 2px 12px #0002;
  text-align: center;
  color: var(--text-color);
}

h2 {
  font-size: 2rem;
  margin-bottom: 30px;
  font-weight: 600;
}

.btn-group {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 20px;
}

.lang-btn {
  padding: 14px 34px;
  font-size: 1.2rem;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  background: var(--btn-bg);
  color: var(--btn-text);
  box-shadow: 0 1px 6px #0003;
  transition: background 0.2s, color 0.2s, transform 0.15s;
}

.lang-btn:hover {
  background: #2e7dff;
  color: #fff;
  transform: scale(1.06);
}

.tip {
  margin-top: 14px;
  font-size: 0.9rem;
  color: #888;
}
</style>
