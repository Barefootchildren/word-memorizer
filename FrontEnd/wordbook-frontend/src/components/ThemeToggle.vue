<!-- src/components/ThemeToggle.vue -->
<template>
    <button
      class="theme-toggle"
      @click="toggleTheme"
      @mouseenter="showTip = true"
      @mouseleave="showTip = false"
    >
      <img
        v-if="isDark"
        src="@/assets/icons/moon.svg"
        alt="黑夜"
        class="theme-icon"
      />
      <img
        v-else
        src="@/assets/icons/sun.svg"
        alt="白天"
        class="theme-icon"
      />
      <span v-if="showTip" class="tip">
        {{ isDark ? '切换到白天' : '切换到黑夜' }}
      </span>
    </button>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  const isDark = ref(false)
  const showTip = ref(false)
  
  const toggleTheme = () => {
    isDark.value = !isDark.value
    document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
  }
  
  onMounted(() => {
    // 默认亮色
    document.documentElement.setAttribute('data-theme', 'light')
  })
  </script>
  
  <style scoped>
  .theme-toggle {
    position: absolute;
    top: 16px;
    right: 20px;
    background: transparent;
    border: none;
    cursor: pointer;
    outline: none;
  }
  
  .theme-icon {
    width: 28px;
    height: 28px;
    vertical-align: middle;
  }
  
  .tip {
    position: absolute;
    top: 40px;
    right: 0;
    background: #333;
    color: #fff;
    padding: 3px 12px;
    border-radius: 6px;
    font-size: 13px;
    white-space: nowrap;
    z-index: 999;
    box-shadow: 0 1px 6px #0003;
  }
  </style>
  