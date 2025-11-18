<template>
  <button v-if="!isLogin" class="back-button" @click="handleBack">
    ← 返回
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isLogin = computed(() => route.path === '/login')

const handleBack = () => {
  if (window.history.length <= 1) {
    router.push('/')
  } else {
    router.back()
  }
}
</script>

<style scoped>
.back-button {
  position: fixed;
  top: 18px;
  left: 16px;
  z-index: 10;
  padding: 6px 14px;
  font-size: 14px;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  border: none;
  border-radius: 16px;
  cursor: pointer;
  backdrop-filter: blur(4px);
  transition: background 0.2s ease;
}

.back-button:hover {
  background: rgba(0, 0, 0, 0.7);
}
</style>
