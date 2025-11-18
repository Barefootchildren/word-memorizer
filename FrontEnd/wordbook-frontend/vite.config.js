import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 只要前端请求地址以 /api 开头，就转发到后端
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 如果你的后端没有统一/api前缀，需要加rewrite
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
