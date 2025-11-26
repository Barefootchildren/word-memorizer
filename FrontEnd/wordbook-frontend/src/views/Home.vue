<!-- src/views/Home.vue -->
<template>
  <div class="home-page">
    <h1>欢迎使用智能单词本</h1>
    <div class="nav-btns">
      <router-link to="/select-day" class="nav-btn">单词列表</router-link>
      <router-link to="/hard-words" class="nav-btn">顽固单词</router-link>
      <!--<router-link to="/review-records" class="nav-btn">复习记录</router-link>-->
      <router-link to="/profile" class="nav-btn">个人中心</router-link>
      <router-link to="/book-select" class="nav-btn">返回单词书选择</router-link>
      <router-link to="/lang-select" class="nav-btn">返回语言选择</router-link>
    </div>
  </div>
</template>

<script setup>
// 无需脚本
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  min-height: 60vh;
  padding-top: 80px;
}

h1 {
  font-size: 2.4rem;
  font-weight: bold;
  margin-bottom: 40px;
}

.nav-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  justify-content: center;
  max-width: 480px;
  /* 让布局更紧凑 */
  margin: 0 auto;
  /* 居中 */
}

.nav-btn {
  display: inline-block;
  padding: 14px 28px;
  font-size: 1.15rem;
  border-radius: 8px;
  text-decoration: none;
  background: var(--btn-bg);
  color: var(--btn-text);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.10);
  transition: background 0.15s, color 0.15s, transform 0.15s;
  font-weight: 500;
}

.nav-btn:hover {
  background: #2e7dff;
  color: #fff;
  transform: translateY(-2px) scale(1.03);
}
</style>