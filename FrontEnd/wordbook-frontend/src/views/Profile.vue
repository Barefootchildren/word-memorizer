<!-- src/views/Profile.vue -->
<template>
    <div class="profile-container">
      <h2>个人信息</h2>
      <div v-if="!username">
        <div class="msg">请先登录。</div>
        <router-link to="/login">去登录</router-link>
      </div>
      <div v-else>
        <div v-if="user">
          <div class="info-item"><strong>用户名：</strong>{{ user.username }}</div>
          <div class="info-item"><strong>注册时间：</strong>{{ formatTime(user.createdAt) }}</div>
        </div>
        <div v-else>
          <button @click="fetchUserInfo">刷新</button>
          <span class="msg">加载中…</span>
        </div>
  
        <!-- 修改密码功能 -->
        <div class="change-password">
          <h3>修改密码</h3>
          <input type="password" v-model="oldPassword" placeholder="原密码" class="input" />
          <input type="password" v-model="newPassword" placeholder="新密码" class="input" />
          <button @click="changePassword" :disabled="loading">提交</button>
          <div v-if="pwdMsg" class="msg">{{ pwdMsg }}</div>
        </div>
  
        <button class="logout-btn" @click="logout">退出登录</button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  
  const username = localStorage.getItem('username') || ''
  const user = ref(null)
  const router = useRouter()
  
  const oldPassword = ref('')
  const newPassword = ref('')
  const loading = ref(false)
  const pwdMsg = ref('')
  
  function formatTime(str) {
    if (!str) return '-'
    const d = new Date(str)
    return d.toLocaleString()
  }
  
  const fetchUserInfo = async () => {
    if (!username) return
    try {
      const resp = await fetch(`/api/user/${username}`)
      user.value = await resp.json()
    } catch {
      user.value = null
    }
  }
  
  const changePassword = async () => {
    if (!oldPassword.value || !newPassword.value) {
      pwdMsg.value = "请输入原密码和新密码"
      return
    }
    loading.value = true
    pwdMsg.value = ''
    try {
      // 最简单的修改密码实现：直接POST到一个（需后端实现的）修改密码API
      const resp = await fetch('/api/user/change-password', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          username,
          oldPassword: oldPassword.value,
          newPassword: newPassword.value
        })
      })
      const text = await resp.text()
      pwdMsg.value = text
      if (text.includes('成功')) {
        oldPassword.value = ''
        newPassword.value = ''
      }
    } catch {
      pwdMsg.value = '修改失败'
    }
    loading.value = false
  }
  
  const logout = () => {
    localStorage.removeItem('username')
    router.push('/login')
  }
  
  onMounted(fetchUserInfo)
  </script>
  
  <style scoped>
  .profile-container {
    max-width: 480px;
    margin: 45px auto;
    background: #262d3a;
    padding: 30px 30px 20px 30px;
    border-radius: 10px;
    color: #fff;
  }
  .info-item {
    margin-bottom: 16px;
    font-size: 1.12em;
  }
  .change-password {
    margin: 30px 0 16px 0;
  }
  .input {
    display: block;
    margin: 8px 0;
    padding: 7px 13px;
    border: 1px solid #777;
    border-radius: 4px;
    width: 100%;
    box-sizing: border-box;
    background: #222;
    color: #eee;
  }
  button {
    background: #2980b9;
    color: #fff;
    border: none;
    padding: 7px 16px;
    border-radius: 4px;
    cursor: pointer;
    margin: 8px 0 0 0;
  }
  button:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
  .logout-btn {
    background: #e74c3c;
    margin-top: 30px;
    width: 100%;
  }
  .msg {
    color: #ffd700;
    margin-top: 10px;
  }
  </style>
  