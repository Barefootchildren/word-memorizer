// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import DaySelect from '../views/DaySelect.vue'
import WordList from '../views/WordList.vue'
import HardWord from '../views/HardWord.vue'
import ReviewRecord from '../views/ReviewRecord.vue'
import Profile from '../views/Profile.vue'
import NotFound from '../views/NotFound.vue'
import LangSelect from '../views/LangSelect.vue'   // 新增：语言选择页

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/lang-select', component: LangSelect }, // 新增：登录后选择语言
  { path: '/home', component: Home },
  { path: '/select-day', component: DaySelect },
  { path: '/words/:day', component: WordList, props: true },
  { path: '/hard-words', component: HardWord },
  { path: '/review-records', component: ReviewRecord },
  { path: '/profile', component: Profile },
  { path: '/:pathMatch(.*)*', component: NotFound }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
