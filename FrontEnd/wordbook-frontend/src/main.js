//src/main.vue
import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 新增

createApp(App).use(router).mount('#app')
