<template>
    <div class="login-root">
      <div class="login-main">
        <h2>用户登录</h2>
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label>用户名：</label>
            <input v-model="form.username" type="text" required />
          </div>
          <div class="form-group">
            <label>密码：</label>
            <input v-model="form.password" type="password" required />
          </div>
          <button type="submit" :disabled="loading">登录</button>
        </form>
        <div v-if="msg" class="msg">{{ msg }}</div>
        <router-link to="/register">没有账号？点我注册</router-link>
      </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const form = ref({
  username: '',
  password: ''
})
const msg = ref('')
const loading = ref(false)
const router = useRouter()

const handleLogin = async () => {
  loading.value = true
  msg.value = ''
  try {
    const resp = await fetch('/api/user/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form.value)
    })
    const text = await resp.text()
    if (text.includes('成功')) {
      // 登录成功，保存用户名
      localStorage.setItem('username', form.value.username)

      // 登录成功后 -> 跳到语言选择页
      router.push('/lang-select')

    } else {
      msg.value = text
    }
  } catch (e) {
    msg.value = '网络异常'
  }
  loading.value = false
}
</script>

<style scoped>
.login-root {
  min-height: calc(100vh - 56px);
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: transparent;
}

.login-main {
  width: 340px;
  padding: 32px 28px;
  border-radius: 10px;
  box-shadow: 0 4px 24px #0001;
  background: var(--main-bg);
  color: var(--text-color);
  box-sizing: border-box;
}

h2 {
  text-align: center;
  margin-bottom: 26px;
  font-weight: 600;
  letter-spacing: 2px;
}

.form-group {
  margin-bottom: 18px;
}
label {
  display: block;
  margin-bottom: 4px;
}
input {
  width: 100%;
  padding: 7px 10px;
  border-radius: 4px;
  border: 1px solid #bbb;
  background: var(--bg-color);
  color: var(--text-color);
  font-size: 16px;
  transition: border 0.2s;
}
input:focus {
  outline: none;
  border-color: #67c23a;
}

button {
  width: 100%;
  padding: 10px;
  background: var(--btn-bg);
  color: var(--btn-text);
  border: none;
  border-radius: 4px;
  font-size: 17px;
  font-weight: 500;
  cursor: pointer;
  margin-top: 10px;
  transition: background 0.18s;
}
button:disabled {
  background: #ccc;
  color: #888;
  cursor: not-allowed;
}

.msg {
  margin-top: 14px;
  color: #ffae00;
  text-align: center;
  font-size: 15px;
}

a {
  color: #409eff;
  display: block;
  text-align: right;
  margin-top: 16px;
  text-decoration: none;
}
a:hover {
  text-decoration: underline;
}
</style>
