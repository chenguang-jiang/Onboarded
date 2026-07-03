# Onboarding 后台设计方案

版本：v1.0  
日期：2026-07-01  
适用范围：微信小程序后端、AI 知识库检索、学习任务、题库、错题本

## 1. 后台目标

后台系统需要支撑以下能力：

- 微信小程序登录与用户身份管理。
- 章节、知识点、题库、解析维护。
- 每日学习任务生成。
- 答题记录和错题本沉淀。
- 用户知识点掌握度计算。
- GLM-5.2 知识库检索问答。
- 微信订阅消息提醒。
- 后续扩展管理后台和资料导入。

## 2. 技术选型

### 2.1 后端语言与框架

推荐：

```text
Java 21
Spring Boot 3.x
Spring Web
Spring Validation
Spring Security 或轻量 JWT 拦截器
MyBatis-Plus
Flyway
```

原因：

- Java 生态适合长期维护和复杂业务建模。
- Spring Boot 对定时任务、HTTP 客户端、配置管理、监控接入成熟。
- MyBatis-Plus 适合业务系统快速开发，同时保留 SQL 可控性。
- Flyway 让数据库结构演进可追踪。

### 2.2 存储

```text
MySQL 8：核心业务数据
Redis 7：缓存、限流、会话态、任务幂等
腾讯 COS：资料文件、题库导入文件、图片资源
智谱知识库：知识文档向量化和检索
```

### 2.3 AI 调用

```text
GLM-5.2
智谱开放平台知识库检索工具
后端代理调用
```

原则：

- 小程序端不保存模型 API Key。
- 后端统一封装 AI 调用、限流、日志、降级。
- AI 回答必须保存引用来源，便于用户复查和问题追踪。

## 3. 后台模块划分

建议采用模块化单体，避免一开始拆微服务。

```text
onboarding-api
├── auth        微信登录、token、会话
├── user        用户资料、学习偏好
├── syllabus    章节、知识点
├── question    题库、选项、解析
├── learning    每日任务、学习记录、掌握度
├── wrongbook   错题本
├── ai          GLM-5.2、知识库检索、会话
├── notify      微信订阅消息
├── admin       管理接口、资料导入
└── common      统一响应、异常、鉴权、工具类
```

### 3.1 auth 模块

职责：

- 微信 `code2Session`。
- openid 绑定用户。
- 生成小程序登录 token。
- 刷新用户会话。

核心接口：

```text
POST /api/auth/wx-login
POST /api/auth/logout
GET  /api/auth/session
```

### 3.2 syllabus 模块

职责：

- 维护软考章节结构。
- 维护知识点内容。
- 提供章节树和知识点详情。

核心接口：

```text
GET /api/chapters
GET /api/chapters/{chapterId}/knowledge-points
GET /api/knowledge-points/{id}
```

### 3.3 question 模块

职责：

- 维护题目、选项、答案、解析。
- 根据知识点获取题目。
- 提交答案并返回判题结果。

核心接口：

```text
GET  /api/questions/next?knowledgePointId={id}
POST /api/questions/{id}/answer
GET  /api/questions/{id}
```

### 3.4 learning 模块

职责：

- 生成每日学习任务。
- 记录知识点学习状态。
- 计算知识点掌握度。
- 输出首页统计。

核心接口：

```text
GET  /api/today
POST /api/today/items/{itemId}/start
POST /api/today/items/{itemId}/complete
GET  /api/progress/overview
GET  /api/progress/chapters
```

### 3.5 wrongbook 模块

职责：

- 自动记录错题。
- 按章节、知识点、错误原因聚合。
- 支持错题重做和标记掌握。

核心接口：

```text
GET  /api/wrongbook
GET  /api/wrongbook/chapters
GET  /api/wrongbook/{id}
POST /api/wrongbook/{id}/redo
POST /api/wrongbook/{id}/mastered
```

### 3.6 ai 模块

职责：

- 封装 GLM-5.2 调用。
- 使用知识库检索工具回答用户问题。
- 保存问答历史和引用来源。
- 对 AI 调用做限流、降级、成本记录。

核心接口：

```text
POST /api/ai/chat
GET  /api/ai/sessions
GET  /api/ai/sessions/{sessionId}/messages
DELETE /api/ai/sessions/{sessionId}
```

MVP 请求示例：

```json
{
  "sessionId": 10001,
  "question": "质量属性场景应该怎么写？",
  "contextType": "knowledge_point",
  "contextId": 20001
}
```

MVP 返回示例：

```json
{
  "answer": "质量属性场景通常包括刺激源、刺激、环境、制品、响应和响应度量...",
  "references": [
    {
      "title": "软件架构评估-质量属性场景",
      "source": "系统架构设计师笔记.md",
      "chapterName": "软件架构设计",
      "snippet": "质量属性场景由六部分组成..."
    }
  ],
  "sessionId": 10001,
  "messageId": 90001
}
```

### 3.7 notify 模块

职责：

- 保存订阅授权记录。
- 调用微信接口发送订阅消息。
- 处理发送失败、过期、频控。

核心接口：

```text
POST /api/notify/subscribe
GET  /api/notify/settings
PUT  /api/notify/settings
```

## 4. 核心业务流程

### 4.0 首次引导流程

```text
小程序 wx-login
-> 后端创建或读取用户
-> 判断 user_onboarding_state 是否完成
-> 未完成则返回 onboardingRequired = true
-> 用户设置考试日期、每日目标、提醒时间
-> 写入用户学习设置
-> 初始化第一天 daily_plan
-> 标记首次引导完成
```

首日任务生成失败时，不影响用户进入首页；首页显示空态和“重新生成今日任务”按钮。

### 4.1 微信登录流程

```text
小程序 wx.login -> 后端 wx-login -> 微信 code2Session -> 获取 openid -> 创建或更新用户 -> 返回 token
```

后端注意：

- 不向前端返回 session_key。
- token 绑定 userId 和 openid。
- Redis 可保存 token 黑名单或会话版本。

### 4.2 每日任务生成流程

```text
定时任务触发
-> 查询用户学习设置
-> 判断当天是否已生成
-> 合并复习队列中的错题、疑难点、未完成项
-> 计算候选知识点
-> 按推荐比例选取 15 个知识点
-> 为每个知识点匹配题目
-> 写入 daily_plan 和 daily_plan_item
-> 缓存首页摘要
-> 发送订阅消息
```

幂等规则：

- `daily_plan(user_id, plan_date)` 唯一。
- 已生成计划不重复生成。
- 手动刷新需要保留已完成项，不覆盖用户进度。
- 前一天未完成项不原样复制，而是进入 `review_queue` 重新排序。
- 候选题目不足时允许生成无题知识点，但计划项状态必须标记 `QUIZ_PENDING`。

### 4.3 答题和错题流程

```text
用户提交答案
-> 后端判题
-> 写 answer_record
-> 更新 daily_plan_item
-> 更新 user_mastery
-> 若错误则写入或更新 wrong_question
-> 返回解析和下一步建议
```

### 4.4 AI 问答流程

```text
用户提问
-> 鉴权
-> 参数校验
-> 用户级限流
-> 构造 GLM-5.2 消息
-> 调用知识库检索
-> 获取回答和来源
-> 保存 ai_chat_message
-> 返回前端
```

建议系统提示词：

```text
你是软考系统架构师备考教练。请优先基于知识库内容回答用户问题。
如果知识库中没有明确依据，请直接说明“知识库中没有找到直接依据”，不要编造。
回答请使用以下结构：结论、解释、考试记忆点、易错提醒、参考来源。
```

### 4.5 AI 回流复习流程

```text
用户点击 AI 回答后的“加入复习”
-> 后端校验 messageId 和用户身份
-> 读取关联知识点或错题
-> 写入 ai_action_record
-> 写入或更新 review_queue
-> 返回“已加入复习”
```

### 4.6 内容导入流程

```text
管理员上传资料或题库文件
-> 创建 content_import_job
-> 文件保存到对象存储或本地临时目录
-> 解析章节、知识点、题目
-> 写入 MySQL 内容表
-> 文档类资料同步到智谱知识库
-> 更新 knowledge_source 和 import job 状态
```

导入失败不能影响已有学习任务，失败原因写入 `content_import_job.error_message`。

## 5. 数据库设计

### 5.1 用户表

```sql
CREATE TABLE user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  openid VARCHAR(64) NOT NULL UNIQUE,
  unionid VARCHAR(64) NULL,
  nickname VARCHAR(64) NULL,
  avatar_url VARCHAR(512) NULL,
  exam_date DATE NULL,
  daily_target INT NOT NULL DEFAULT 15,
  reminder_time TIME NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);
```

### 5.1.1 首次引导状态表

```sql
CREATE TABLE user_onboarding_state (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  onboarding_completed TINYINT NOT NULL DEFAULT 0,
  completed_at DATETIME NULL,
  last_step VARCHAR(64) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_onboarding_user(user_id)
);
```

### 5.2 章节表

```sql
CREATE TABLE chapter (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT NULL,
  name VARCHAR(128) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_chapter_parent_sort(parent_id, sort_no)
);
```

### 5.3 知识点表

```sql
CREATE TABLE knowledge_point (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  chapter_id BIGINT NOT NULL,
  title VARCHAR(256) NOT NULL,
  summary VARCHAR(1024) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  importance TINYINT NOT NULL DEFAULT 2,
  difficulty TINYINT NOT NULL DEFAULT 2,
  tags VARCHAR(512) NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_kp_chapter_importance(chapter_id, importance),
  FULLTEXT INDEX ft_kp_title_content(title, content)
);
```

### 5.4 题目表

```sql
CREATE TABLE question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  chapter_id BIGINT NOT NULL,
  knowledge_point_id BIGINT NOT NULL,
  type VARCHAR(32) NOT NULL,
  stem TEXT NOT NULL,
  answer VARCHAR(512) NOT NULL,
  analysis TEXT NOT NULL,
  difficulty TINYINT NOT NULL DEFAULT 2,
  source VARCHAR(128) NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_question_kp_difficulty(knowledge_point_id, difficulty),
  INDEX idx_question_chapter(chapter_id)
);
```

### 5.5 题目选项表

```sql
CREATE TABLE question_option (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  question_id BIGINT NOT NULL,
  option_key VARCHAR(8) NOT NULL,
  option_text TEXT NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  INDEX idx_option_question(question_id, sort_no)
);
```

### 5.6 每日计划表

```sql
CREATE TABLE daily_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  plan_date DATE NOT NULL,
  total_count INT NOT NULL,
  completed_count INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_daily_plan_user_date(user_id, plan_date)
);
```

### 5.7 每日计划项表

```sql
CREATE TABLE daily_plan_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plan_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  knowledge_point_id BIGINT NOT NULL,
  question_id BIGINT NULL,
  source_type VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  sort_no INT NOT NULL DEFAULT 0,
  completed_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_plan_item_plan_status(plan_id, status),
  INDEX idx_plan_item_user_kp(user_id, knowledge_point_id)
);
```

计划项状态：

```text
PENDING      待学习
STUDYING     学习中
QUIZ_PENDING 已有知识点但缺少可用题
DONE         已完成
SKIPPED      用户主动跳过
EXPIRED      超过保留期后回到普通复习池
```

### 5.8 答题记录表

```sql
CREATE TABLE answer_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  selected_answer VARCHAR(512) NOT NULL,
  is_correct TINYINT NOT NULL,
  duration_sec INT NULL,
  error_reason VARCHAR(64) NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_answer_user_question_time(user_id, question_id, created_at),
  INDEX idx_answer_user_time(user_id, created_at)
);
```

### 5.9 错题表

```sql
CREATE TABLE wrong_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  chapter_id BIGINT NOT NULL,
  knowledge_point_id BIGINT NOT NULL,
  wrong_count INT NOT NULL DEFAULT 1,
  mastered TINYINT NOT NULL DEFAULT 0,
  last_wrong_at DATETIME NOT NULL,
  mastered_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_wrong_user_question(user_id, question_id),
  INDEX idx_wrong_user_chapter_mastered(user_id, chapter_id, mastered),
  INDEX idx_wrong_user_kp(user_id, knowledge_point_id)
);
```

错题状态建议：

```text
OPEN      待重做
RETRYING  已重做但未满足掌握条件
MASTERED  已掌握，默认隐藏
ARCHIVED  用户归档
```

### 5.10 掌握度表

```sql
CREATE TABLE user_mastery (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  knowledge_point_id BIGINT NOT NULL,
  mastery_score INT NOT NULL DEFAULT 0,
  study_count INT NOT NULL DEFAULT 0,
  correct_count INT NOT NULL DEFAULT 0,
  wrong_count INT NOT NULL DEFAULT 0,
  last_review_at DATETIME NULL,
  next_review_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_mastery_user_kp(user_id, knowledge_point_id),
  INDEX idx_mastery_user_next_review(user_id, next_review_at),
  INDEX idx_mastery_user_score(user_id, mastery_score)
);
```

### 5.11 AI 会话表

```sql
CREATE TABLE ai_chat_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_ai_session_user_time(user_id, updated_at)
);
```

```sql
CREATE TABLE ai_chat_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(32) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  references_json JSON NULL,
  token_usage_json JSON NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_ai_message_session_time(session_id, created_at)
);
```

### 5.12 知识库资料表

```sql
CREATE TABLE knowledge_source (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(256) NOT NULL,
  type VARCHAR(64) NOT NULL,
  file_url VARCHAR(512) NULL,
  glm_knowledge_id VARCHAR(128) NULL,
  glm_document_id VARCHAR(128) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_knowledge_source_status(status)
);
```

### 5.12.1 内容导入任务表

```sql
CREATE TABLE content_import_job (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  source_id BIGINT NULL,
  import_type VARCHAR(64) NOT NULL,
  file_name VARCHAR(256) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  total_count INT NOT NULL DEFAULT 0,
  success_count INT NOT NULL DEFAULT 0,
  failed_count INT NOT NULL DEFAULT 0,
  error_message TEXT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_import_job_status(status)
);
```

### 5.12.2 复习队列表

```sql
CREATE TABLE review_queue (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  knowledge_point_id BIGINT NOT NULL,
  source_type VARCHAR(32) NOT NULL,
  source_id BIGINT NULL,
  priority INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  due_date DATE NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_review_user_due_status(user_id, due_date, status),
  INDEX idx_review_user_kp(user_id, knowledge_point_id)
);
```

### 5.12.3 AI 动作记录表

```sql
CREATE TABLE ai_action_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  message_id BIGINT NOT NULL,
  action_type VARCHAR(64) NOT NULL,
  knowledge_point_id BIGINT NULL,
  question_id BIGINT NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_ai_action_user_message(user_id, message_id)
);
```

### 5.13 订阅记录表

```sql
CREATE TABLE subscribe_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  template_id VARCHAR(128) NOT NULL,
  scene VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  expire_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_subscribe_user_scene(user_id, scene, status)
);
```

## 6. 推荐算法 MVP

每日计划生成时按 4 类候选池选取。

```text
新知识点：未出现在 user_mastery 或 study_count = 0
错题关联：wrong_question.mastered = 0
低掌握度：mastery_score < 60
高频重点：importance = 3
```

去重策略：

- 同一个知识点当天只能出现一次。
- 已完成的知识点不被手动刷新覆盖。
- 候选不足时从高频重点补齐。

题目匹配策略：

- 优先选择用户未做过的题。
- 若没有未做题，选择最近 7 天未做过的题。
- 若仍不足，允许复用但标记为复习题。

## 7. AI 调用设计

### 7.1 配置项

```yaml
glm:
  api-key: ${GLM_API_KEY}
  model: glm-5.2
  knowledge-id: ${GLM_KNOWLEDGE_ID}
  timeout-seconds: 60
  max-daily-requests-per-user: 100
```

### 7.2 请求构造

后端封装请求时带上知识库检索工具。

```json
{
  "model": "glm-5.2",
  "messages": [
    {
      "role": "user",
      "content": "质量属性场景怎么写？"
    }
  ],
  "tools": [
    {
      "type": "retrieval",
      "retrieval": {
        "knowledge_id": "ONBOARDING_KNOWLEDGE_ID",
        "prompt_template": "你是软考系统架构师备考教练。优先根据知识库回答。若知识库没有依据，必须说明未找到明确资料。回答格式：结论、解释、考试记忆点、易错提醒、参考来源。知识库内容：{{knowledge}}。用户问题：{{question}}。"
      }
    }
  ]
}
```

### 7.3 降级策略

- GLM 超时：返回“AI 服务暂时繁忙，请稍后再试”。
- 知识库无结果：允许模型解释通用概念，但必须标记“未从知识库找到直接依据”。
- 用户频繁请求：返回限流提示。
- 模型返回空内容：记录异常日志并提示重试。

## 8. 安全与权限

- 所有业务接口必须鉴权。
- 后端校验 userId，不能信任前端传入 userId。
- AI API Key 只保存在服务端环境变量。
- 管理接口单独鉴权。
- 上传文件限制类型和大小。
- AI 输入输出记录敏感词和异常审计。
- 用户级限流防止 AI 成本失控。

## 9. 监控与日志

关键指标：

- 日活用户。
- 每日任务生成成功率。
- 知识点完成率。
- 答题正确率。
- 错题重做率。
- AI 请求次数。
- AI 平均响应时间。
- AI 失败率。
- 订阅消息发送成功率。

日志建议：

- 请求日志。
- 业务异常日志。
- AI 调用日志。
- 定时任务日志。
- 微信接口错误日志。

## 10. 开发优先级

P0：

- 登录。
- 今日任务。
- 知识点。
- 答题。
- 错题本。
- GLM 问答。

P1：

- 掌握度。
- 订阅消息。
- 首页统计。
- 资料导入。

P2：

- 管理后台。
- 模拟考试。
- AI 变式题。
- 真题套卷。

## 11. 参考资料

- 智谱 GLM 知识库检索文档：https://docs.bigmodel.cn/cn/guide/tools/knowledge/retrieval
- 智谱知识处理与检索文档：https://docs.bigmodel.cn/cn/guide/tools/knowledge/process-and-retrieval
- 微信小程序订阅消息文档：https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/subscribe-message.html
