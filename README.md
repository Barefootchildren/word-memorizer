# 单词记忆器

一个帮助用户记忆单词的全栈应用，包含用户注册登录、单词列表查看、困难单词管理、复习记录等功能。

## 技术栈

- 前端：Vue 3 + Vite
- 后端：Spring Boot (Java)
- 数据库：JPA + MySQL (默认配置)

## 功能特性

- 用户注册与登录
- 按学习天数查看单词列表
- 管理个人困难单词（添加/移除）
- 记录复习情况（状态、复习次数）
- 查看个人复习记录
- 单词数据导入与导出（CSV 格式）

## 安装与启动

### 前端

1. 进入前端目录：`cd FrontEnd/wordbook-frontend`
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`

### 后端

1. 进入后端目录：`cd Word`
2. 使用 Maven 构建：`mvn clean package`
3. 启动应用：`java -jar target/*.jar`

## API 文档

### 用户管理
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/{username}` - 获取用户信息

### 单词管理
- `GET /api/words` - 获取所有单词
- `GET /api/words/day/{day}` - 按学习天数获取单词
- `POST /api/words/import` - 导入单词（CSV 文件）
- `GET /api/words/export/day/{day}` - 导出单词为 CSV

### 困难单词
- `GET /api/hard-words/{username}` - 获取用户的困难单词
- `POST /api/hard-words/add` - 添加困难单词
- `POST /api/hard-words/remove` - 移除困难单词

### 复习记录
- `GET /api/review-record/user/{username}` - 获取用户复习记录
- `POST /api/review-record/save` - 保存复习记录
- `DELETE /api/review-record/{id}` - 删除复习记录

## 许可证

本项目采用 MIT 许可证。详情请查看项目根目录下的 LICENSE 文件。

## 贡献指南

欢迎提交 Pull Request！请遵循以下步骤：
1. Fork 项目
2. 创建新分支 (`git checkout -b feature/new-feature`)
3. 提交更改 (`git commit -m 'Add new feature'`)
4. 推送分支 (`git push origin feature/new-feature`)
5. 创建 Pull Request

我们建议在提交 PR 前运行完整的测试套件并确保代码风格一致。