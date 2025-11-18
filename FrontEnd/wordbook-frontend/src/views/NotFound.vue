<!-- src/views/NotFound.vue -->
<template>
    <div class="notfound-container">
      <h1>404</h1>
      <p class="tip">抱歉，页面未找到</p>
      <router-link to="/" class="home-link">返回首页</router-link>
    </div>
  </template>
  
  <script setup>
  </script>
  
  <style scoped>
  .notfound-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 60vh;
    color: #fff;
    background: #222c36;
    border-radius: 12px;
    margin: 50px auto;
    max-width: 480px;
    box-shadow: 0 2px 18px 0 #19202a44;
  }
  h1 {
    font-size: 4em;
    margin-bottom: 8px;
    letter-spacing: 4px;
  }
  .tip {
    margin-bottom: 30px;
    font-size: 1.3em;
    color: #ffb600;
  }
  .home-link {
    display: inline-block;
    background: #2980b9;
    color: #fff;
    padding: 10px 34px;
    border-radius: 6px;
    text-decoration: none;
    font-size: 1.1em;
    transition: background 0.22s;
  }
  .home-link:hover {
    background: #15618c;
  }
  </style>
  