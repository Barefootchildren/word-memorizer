<template>
  <div class="word-list-root">
    <div class="top-btns">
      <button class="nav-btn">返回主页</button>
      <button class="nav-btn">返回天数选择</button>
      <button class="nav-btn">顽固单词列表</button>

      <!-- 右侧：拓展词列开关 -->
      <button class="ext-toggle-btn" @click="showExtensions = !showExtensions">
        {{ showExtensions ? '隐藏拓展词' : '显示拓展词' }}
      </button>
    </div>

    <h1>第1天（英语单词）</h1>

    <table>
      <thead>
        <tr>
          <th>序号</th>
          <th>
            单词
            <!-- 这里只是展示样子，不绑定逻辑 -->
            <button class="toggle-btn">隐藏</button>
            <button class="toggle-btn">拼写</button>
          </th>
          <th>
            中文
            <button class="toggle-btn">隐藏</button>
            <button class="toggle-btn">拼写</button>
          </th>
          <th v-if="showExtensions" class="ext-head">拓展词</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, idx) in words" :key="item.id">
          <!-- 序号 -->
          <td class="idx-col">{{ idx + 1 }}</td>

          <!-- 单词列：保持你现在的感觉 -->
          <td>
            <span class="word-text">{{ item.word }}</span>
            <button class="hard-btn">顽固</button>
          </td>

          <!-- 中文列：保持“中文 + 编辑小按钮”的结构 -->
          <td>
            <div class="edit-area">
              <span class="chinese">{{ item.meaning }}</span>
              <button class="edit-btn">✎</button>
            </div>
          </td>

          <!-- 拓展词列：仅在 showExtensions 为 true 时显示 -->
          <td v-if="showExtensions" class="ext-col">
            <!-- 近义词 -->
            <div v-if="item.extensions.similar.length" class="ext-group">
              <span class="ext-label ext-label-sim">近</span>
              <div class="ext-items">
                <div
                  v-for="(ext, i) in item.extensions.similar"
                  :key="'sim-' + i"
                  class="ext-item"
                >
                  <span class="ext-sound">🔊</span>
                  <span class="ext-kor">{{ ext.kor }}</span>
                  <span class="ext-cn">— {{ ext.cn }}</span>
                </div>
              </div>
            </div>

            <!-- 关联词 -->
            <div v-if="item.extensions.related.length" class="ext-group">
              <span class="ext-label ext-label-rel">关</span>
              <div class="ext-items">
                <div
                  v-for="(ext, i) in item.extensions.related"
                  :key="'rel-' + i"
                  class="ext-item"
                >
                  <span class="ext-sound">🔊</span>
                  <span class="ext-kor">{{ ext.kor }}</span>
                  <span class="ext-cn">— {{ ext.cn }}</span>
                </div>
              </div>
            </div>

            <!-- 惯用语 -->
            <div v-if="item.extensions.idiom.length" class="ext-group">
              <span class="ext-label ext-label-idm">惯</span>
              <div class="ext-items">
                <div
                  v-for="(ext, i) in item.extensions.idiom"
                  :key="'idm-' + i"
                  class="ext-item"
                >
                  <span class="ext-sound">🔊</span>
                  <span class="ext-kor">{{ ext.kor }}</span>
                  <span class="ext-cn">— {{ ext.cn }}</span>
                </div>
              </div>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <p class="tip">
      当前组件只是静态展示：按钮和发音图标都没有真实逻辑，只用于预览“拓展词”列的样子和开关效果。
    </p>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 进入页面时默认不显示“拓展词”这一列
const showExtensions = ref(false)

// 示例数据
const words = [
  {
    id: 1,
    word: 'apple',
    meaning: 'n. 苹果',
    extensions: {
      similar: [
        { kor: '사과', cn: '苹果' },
        { kor: '과일', cn: '水果' }
      ],
      related: [{ kor: '주스', cn: '果汁' }],
      idiom: [{ kor: '하루 한 개의 사과', cn: '每天一个苹果' }]
    }
  },
  {
    id: 2,
    word: 'banana',
    meaning: 'n. 香蕉',
    extensions: {
      similar: [{ kor: '바나나', cn: '香蕉' }],
      related: [
        { kor: '열대 과일', cn: '热带水果' },
        { kor: '과일 가게', cn: '水果店' }
      ],
      idiom: [{ kor: '바나나 껍질을 밟다', cn: '踩到香蕉皮' }]
    }
  },
  {
    id: 3,
    word: 'shop',
    meaning: 'n. 商店',
    extensions: {
      similar: [
        { kor: '가게', cn: '店铺' },
        { kor: '상점', cn: '商店' }
      ],
      related: [{ kor: '시장', cn: '市场' }],
      idiom: []
    }
  }
]
</script>

<style scoped>
.word-list-root {
  max-width: 1000px;
  margin: 36px auto 0 auto;
  padding: 0 0 36px 0;
  color: var(--text-color, #fff);
}

/* 顶部三个导航按钮 + 右侧拓展词开关 */
.top-btns {
  display: flex;
  gap: 18px;
  margin-bottom: 22px;
  margin-top: 10px;
}
.nav-btn {
  background: #1e90ff;
  color: #fff;
  border: none;
  border-radius: 7px;
  padding: 6px 22px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}
.nav-btn:hover {
  background: #256fff;
}
.ext-toggle-btn {
  margin-left: auto;
  background: transparent;
  color: #1e90ff;
  border-radius: 22px;
  border: 1px solid #1e90ff;
  padding: 6px 18px;
  font-size: 14px;
  cursor: pointer;
}
.ext-toggle-btn:hover {
  background: rgba(30, 144, 255, 0.15);
}

h1 {
  color: var(--text-color, #fff);
  font-size: 2rem;
  margin: 22px 0 24px 0;
  text-align: left;
  font-weight: 700;
}

/* 表格整体样式，贴近你现在的页面 */
table {
  border-collapse: collapse;
  width: 100%;
  background: var(--main-bg, #222);
  margin-bottom: 12px;
}
th,
td {
  padding: 10px;
  border: 1px solid #444;
  vertical-align: middle;
}
th {
  background: #333;
  font-weight: 600;
}
tbody tr:nth-child(even) {
  background: #262626;
}
tbody tr:nth-child(odd) {
  background: #202020;
}
.idx-col {
  width: 60px;
  text-align: center;
}

/* 表头里的隐藏/拼写按钮，保持原有感觉 */
button.toggle-btn {
  background: #222;
  color: #fff;
  border: 1px solid #555;
  border-radius: 5px;
  padding: 4px 12px;
  margin-left: 8px;
  cursor: pointer;
  font-size: 13px;
}

/* 单词列：蓝色可点击文本 + 顽固按钮 */
.word-text {
  color: #0af;
  text-decoration: underline;
  cursor: pointer;
  transition: color 0.2s;
}
.word-text:hover {
  color: #5cf;
}
.hard-btn {
  background: #880;
  color: #fff;
  border: none;
  border-radius: 4px;
  margin-left: 8px;
  cursor: pointer;
  font-size: 13px;
  padding: 2px 10px;
}

/* 中文列：维持“内容 + 编辑按钮”的结构 */
.edit-area {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.chinese {
  font-size: 15px;
}
.edit-btn {
  border: none;
  background: #1e90ff;
  color: #fff;
  border-radius: 4px;
  padding: 2px 8px;
  cursor: pointer;
  font-size: 13px;
}

/* 拓展词列样式 */
.ext-head {
  width: 250px;
}
.ext-col {
  font-size: 13px;
  vertical-align: top;
}
.ext-group {
  display: flex;
  align-items: flex-start;
  margin-bottom: 6px;
}
.ext-label {
  flex-shrink: 0;
  display: inline-block;
  margin-right: 6px;
  padding: 2px 7px;
  border-radius: 999px;
  font-size: 12px;
  color: #fff;
  text-align: center;
}
.ext-label-sim {
  background: #2f7dd8;
}
.ext-label-rel {
  background: #9c6bdf;
}
.ext-label-idm {
  background: #e6a23c;
}
.ext-items {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.ext-item {
  display: flex;
  align-items: center;
  line-height: 1.3;
}
.ext-sound {
  margin-right: 4px;
}
.ext-kor {
  margin-right: 4px;
}
.ext-cn {
  color: #ddd;
}

.tip {
  margin-top: 10px;
  font-size: 12px;
  color: #bbbbbb;
}
</style>
