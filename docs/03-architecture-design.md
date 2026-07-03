# Onboarding 整体架构方案

版本：v1.0  
日期：2026-07-01  
架构目标：小步快跑、模块清晰、AI 可控、后续可扩展

## 1. 架构结论

Onboarding 首版建议采用：

```text
微信原生小程序 + Spring Boot 模块化单体 + MySQL + Redis + 智谱 GLM-5.2 知识库检索
```

不建议一开始拆微服务。当前复杂度集中在学习闭环、题库质量、AI 检索可信度和推荐策略，不在服务拆分。

## 2. 总体架构图

```text
┌──────────────────────────────────────────┐
│              微信小程序 Onboarding        │
│  今日任务 / 知识卡 / 答题 / AI / 错题本     │
└─────────────────────┬────────────────────┘
                      │ HTTPS
┌─────────────────────▼────────────────────┐
│                Nginx / HTTPS              │
└─────────────────────┬────────────────────┘
                      │
┌─────────────────────▼────────────────────┐
│             Spring Boot Backend           │
│ auth user syllabus question learning      │
│ wrongbook ai notify admin common          │
└───────┬─────────────┬─────────────┬───────┘
        │             │             │
┌───────▼──────┐ ┌────▼─────┐ ┌────▼──────────┐
│   MySQL 8    │ │ Redis 7  │ │ Tencent COS    │
│ 业务数据      │ │ 缓存限流  │ │ 文件资料       │
└──────────────┘ └──────────┘ └───────────────┘
        │
        │
┌───────▼──────────────────────────────────┐
│           智谱开放平台 GLM-5.2             │
│       知识库检索 / 问答生成 / 引用来源       │
└──────────────────────────────────────────┘

┌──────────────────────────────────────────┐
│              微信服务端能力                │
│      code2Session / 订阅消息 / access_token │
└──────────────────────────────────────────┘
```

## 3. 前端架构

### 3.1 技术选型

推荐：

```text
微信原生小程序
TypeScript
TDesign Miniprogram
Skyline 渲染能力
echarts-for-weixin
```

UI 视觉语言以 `docs/05-ui-design-prompt-from-video.md` 为准：小程序前端应采用浅色科技感、轻玻璃卡片、柔和渐变、空间层叠卡组和克制动效。实现时需要把颜色、圆角、阴影、间距和字体抽象为 design tokens，避免页面各自定义风格。

理由：

- 只做微信小程序时，原生小程序最稳定。
- TypeScript 提升接口和状态维护质量。
- TDesign Miniprogram 提供成熟基础组件。
- Skyline 可提升复杂滚动和动效体验。
- echarts-for-weixin 适合掌握度图表和章节热力图。

### 3.2 前端目录建议

```text
miniprogram/
├── app.ts
├── app.json
├── app.wxss
├── pages/
│   ├── today/
│   ├── knowledge-detail/
│   ├── quiz/
│   ├── quiz-result/
│   ├── wrongbook/
│   ├── ai-chat/
│   ├── progress/
│   └── profile/
├── components/
│   ├── progress-ring/
│   ├── knowledge-card/
│   ├── quiz-option/
│   ├── ai-message/
│   └── chapter-heatmap/
├── services/
│   ├── request.ts
│   ├── auth-api.ts
│   ├── today-api.ts
│   ├── quiz-api.ts
│   ├── ai-api.ts
│   └── wrongbook-api.ts
├── stores/
│   ├── user-store.ts
│   └── today-store.ts
└── utils/
```

### 3.3 前端状态管理

MVP 不引入复杂状态库，采用轻量 store：

- `userStore` 保存登录态、用户信息、学习设置。
- `todayStore` 保存今日任务、进度、当前知识点。
- 页面进入时按需刷新，关键操作后局部更新。

### 3.4 前端交互设计

首页：

- 大进度环展示今日 15 个任务进度。
- 知识卡横向滑动。
- 今日薄弱章节以标签展示。
- AI 快问作为悬浮按钮。

知识卡：

- 正面展示核心解释。
- 背面展示易错点和高频考法。
- 完成后触发轻量动画。

答题页：

- 单题沉浸。
- 选项点击后锁定。
- 提交后展示正确、错误、解析。
- 答错时提供“问 AI 解释这题”。

AI 页：

- 聊天流布局。
- 来源引用使用可展开卡片。
- 快捷追问用横向 chips。

错题本：

- 顶部筛选：章节、错误原因、掌握状态。
- 错题卡片展示错误次数和知识点。
- 章节维度用热力图表达薄弱程度。

## 4. 后端架构

### 4.1 工程结构

```text
backend/
├── onboarding-api/
│   ├── src/main/java/com/onboarding/
│   │   ├── auth/
│   │   ├── user/
│   │   ├── syllabus/
│   │   ├── question/
│   │   ├── learning/
│   │   ├── wrongbook/
│   │   ├── ai/
│   │   ├── notify/
│   │   ├── admin/
│   │   └── common/
│   └── src/main/resources/
│       ├── mapper/
│       ├── db/migration/
│       └── application.yml
└── docker-compose.yml
```

### 4.2 分层结构

每个业务模块保持一致分层：

```text
controller -> application service -> domain service -> mapper -> database
```

约束：

- Controller 只做参数校验和响应转换。
- Application Service 编排业务流程。
- Domain Service 处理推荐、掌握度、错题等核心规则。
- Mapper 只负责数据访问。
- AI 和微信调用封装为 client，不在业务层散落 HTTP 细节。

### 4.3 关键后端组件

```text
DailyPlanGenerator
MasteryScoreService
QuestionSelector
WrongBookService
GlmKnowledgeClient
AiChatService
WeChatAuthClient
WeChatSubscribeClient
NotificationScheduler
```

## 5. AI 架构

### 5.1 知识库边界

智谱知识库负责：

- 文档切片。
- 向量化。
- 知识检索。
- 为 GLM-5.2 回答提供上下文。

本地 MySQL 负责：

- 章节。
- 知识点结构。
- 题库。
- 答题记录。
- 错题本。
- 用户掌握度。

不要把所有业务数据都塞进知识库。知识库解决“资料问答”，MySQL 解决“学习状态和业务规则”。

### 5.2 问答链路

```text
用户问题
-> 后端补充上下文，包含章节、知识点、当前题目
-> GLM-5.2 调用知识库检索
-> 模型基于检索片段回答
-> 后端保存回答和引用
-> 前端展示答案和来源
-> 用户可将回答关联知识点并加入复习队列
```

### 5.2.1 AI 回流链路

```text
AI 回答
-> 展示引用来源
-> 用户选择“加入复习”或“出题考我”
-> 写入 ai_action_record
-> 写入 review_queue
-> 今日或次日任务生成时重新参与排序
```

### 5.3 Prompt 模板

```text
你是软考系统架构师备考教练。
请优先根据知识库内容回答用户问题。
如果知识库中没有明确依据，请说明“知识库中没有找到直接依据”。
请避免编造教材、章节和真题来源。
回答结构：
1. 结论
2. 解释
3. 考试记忆点
4. 易错提醒
5. 参考来源
```

### 5.4 成本控制

- 用户级每日 AI 次数限制。
- 单次问题最大长度限制。
- 对重复问题做短期缓存。
- 保存 token 用量。
- 后台统计高频问题，沉淀为 FAQ。

## 6. 数据架构

数据分为 7 类：

```text
用户数据：user_account
引导数据：user_onboarding_state
内容数据：chapter, knowledge_point, question, question_option
学习数据：daily_plan, daily_plan_item, answer_record, user_mastery
错题数据：wrong_question
复习数据：review_queue
AI 数据：ai_chat_session, ai_chat_message, ai_action_record, knowledge_source
导入数据：content_import_job
```

### 6.1 数据生命周期

知识点和题库：

- 管理端维护。
- 低频更新。
- 需要版本记录，MVP 可先不做。
- 导入失败不得影响已经发布的知识点和题目。

复习队列：

- 承接错题、疑难点、未完成任务。
- 每日任务生成后将被选中的队列项标记为计划中。
- 完成学习或重做后更新队列状态。

学习记录：

- 持续增长。
- 可按时间归档。
- 是推荐算法核心输入。

AI 会话：

- 默认保留。
- 后续支持用户删除。
- 引用来源和 token 用量单独保存。

## 7. 部署架构

### 7.1 MVP 部署

```text
腾讯云 CVM / 轻量服务器
Docker Compose
Nginx
Spring Boot Jar
MySQL 8
Redis 7
```

适合早期：

- 成本低。
- 维护简单。
- 部署链路短。

### 7.2 生产增强

用户量上来后升级：

```text
MySQL -> 腾讯云 RDS
Redis -> 腾讯云 Redis
文件 -> 腾讯 COS
日志 -> CLS 或 ELK
监控 -> Prometheus + Grafana
后端 -> 多实例 + 负载均衡
```

### 7.3 环境划分

```text
dev：本地开发
test：联调和体验测试
prod：正式环境
```

配置差异通过环境变量管理：

```text
MYSQL_URL
REDIS_HOST
WECHAT_APP_ID
WECHAT_APP_SECRET
GLM_API_KEY
GLM_KNOWLEDGE_ID
COS_SECRET_ID
COS_SECRET_KEY
```

## 8. 安全架构

### 8.1 认证

- 小程序使用微信登录获取 code。
- 后端使用 code 换取 openid。
- 后端签发短期 token。
- token 中只保存 userId 和会话版本。

### 8.2 权限

- 小程序接口必须登录。
- 管理接口需要管理员角色。
- 业务查询必须使用 token 中的 userId。
- 不允许前端传 userId 后直接信任。

### 8.3 Secret 管理

- GLM API Key 只在后端。
- 微信 AppSecret 只在后端。
- COS 密钥只在后端。
- 生产环境通过环境变量或密钥管理服务注入。

## 9. 高可用与降级

AI 服务不可用：

- 返回清晰提示。
- 不影响今日任务、答题、错题本。

Redis 不可用：

- 基础业务仍可通过 MySQL 完成。
- 限流和缓存能力降级。

每日任务生成失败：

- 用户进入首页时触发懒生成。
- 定时任务下一轮重试。

微信订阅消息失败：

- 记录失败原因。
- 不阻塞学习任务。

## 10. 性能设计

首页接口：

- 使用聚合接口 `/api/today`。
- 返回今日任务、进度、薄弱章节摘要。
- Redis 缓存用户当天首页摘要。

答题接口：

- 单次写入答题记录、错题和掌握度。
- 使用事务保证一致性。

错题本：

- 按章节分页查询。
- 建立 `user_id + chapter_id + mastered` 索引。

AI：

- 单用户限流。
- 超时控制。
- 长回答可后续改流式。

## 11. 可扩展路线

阶段一：模块化单体。

阶段二：拆出管理后台 Web。

阶段三：拆出 AI 服务。

阶段四：增加异步任务队列。

阶段五：增加数据分析和个性化推荐服务。

建议拆分时机：

- AI 请求量高且影响主服务稳定性。
- 管理后台权限和内容流程复杂化。
- 每日任务生成耗时明显变长。
- 用户量增长到单实例难以支撑。

## 12. 技术风险

| 风险 | 影响 | 应对 |
| --- | --- | --- |
| AI 回答幻觉 | 用户学习错误内容 | 强制知识库优先、展示来源、无依据提示 |
| 题库质量不足 | 学习效果差 | 先人工维护高质量题，再做 AI 生成 |
| 订阅消息限制 | 每日提醒不稳定 | 只做一次性提醒，前端引导授权 |
| 推荐算法过早复杂 | 开发成本高 | MVP 使用规则算法，后续数据驱动优化 |
| 知识库资料版权 | 上线风险 | 只导入自有笔记、授权资料或用户自传资料 |

## 13. 参考资料

- 智谱 GLM 知识库检索文档：https://docs.bigmodel.cn/cn/guide/tools/knowledge/retrieval
- 智谱知识处理与检索文档：https://docs.bigmodel.cn/cn/guide/tools/knowledge/process-and-retrieval
- 微信小程序订阅消息文档：https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/subscribe-message.html
