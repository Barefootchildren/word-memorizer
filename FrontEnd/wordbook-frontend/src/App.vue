<!-- src/App.vue -->
<template>
  <div :class="['app-root', theme]">
    <!-- 右上角的主题切换按钮 -->
    <div
      class="theme-switcher-float"
      @click="toggleTheme"
      @mouseenter="showTip = true"
      @mouseleave="showTip = false"
    >
      <img
        v-if="theme === 'light'"
        :src="moon"
        class="theme-icon"
        alt="切换到黑夜"
      />
      <img
        v-else
        :src="sun"
        class="theme-icon"
        alt="切换到白天"
      />
      <div class="theme-tip" v-if="showTip">
        {{ theme === 'light' ? '切换到黑夜' : '切换到白天' }}
      </div>
    </div>
    <!-- 页面内容 -->
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import moon from './assets/icons/moon.svg'
import sun from './assets/icons/sun.svg'

const theme = ref(localStorage.getItem('theme') || 'light')
const showTip = ref(false)

const toggleTheme = () => {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
  localStorage.setItem('theme', theme.value)
}
watch(theme, (val) => {
  document.documentElement.setAttribute('data-theme', val)
}, { immediate: true })
</script>

<style>
:root[data-theme='light'] {
  --bg-color: #fff;
  --text-color: #222;
  --main-bg: #fff;
}
:root[data-theme='dark'] {
  --bg-color: #18191c;
  --text-color: #fff;
  --main-bg: #232323;
}

.app-root {
  background: var(--bg-color);
  color: var(--text-color);
  min-height: 100vh;
  width: 100vw;
}

/* 右上角悬浮按钮 */
.theme-switcher-float {
  position: fixed;
  top: 18px;
  right: 28px;
  z-index: 20;
  cursor: pointer;
  border-radius: 50%;
  padding: 3px;
  background: none;
  transition: background 0.2s;
  user-select: none;
}
.theme-switcher-float:hover {
  background: rgba(100, 100, 255, 0.08);
}
.theme-icon {
  width: 28px;
  height: 28px;
  display: block;
}
.theme-tip {
  position: absolute;
  right: 0;
  top: 38px;
  background: #222c;
  color: #fff;
  padding: 4px 12px;
  font-size: 14px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,.15);
  white-space: nowrap;
  pointer-events: none;
}

.app-main {
  min-height: 100vh;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  background: var(--main-bg);
  /* 可视需要加padding或不加 */
}
html, body, #app, .app-root {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  background: var(--bg-color);
}
body {
  display: block !important;
  place-items: unset !important;
}

</style>
