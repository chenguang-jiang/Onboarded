const TOKEN_KEY = "onboarding:token";
const SETTINGS_KEY = "onboarding:settings";
const PENDING_AI_PROMPT_KEY = "onboarding:pending-ai-prompt";
const ANSWER_CONTEXT_KEY = "onboarding:answer-context";

interface AppWithGlobalData {
  globalData: {
    apiBaseUrl: string;
  };
}

interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

export interface QuestionOption {
  key: string;
  text: string;
}

export interface LoginResponse {
  userId: number;
  openid: string;
  token: string;
  onboardingRequired: boolean;
}

export interface StudySettings {
  examDate: string;
  dailyTarget: number;
  reminderTime: string;
  onboardingCompleted?: boolean;
}

export interface KnowledgeBrief {
  id: number;
  title: string;
  summary: string;
  difficulty: string;
  tags: string[];
}

export interface QuestionBrief {
  id: number;
  stem: string;
  options: QuestionOption[];
}

export interface DailyPlanItem {
  itemId: number;
  knowledgePointId: number;
  chapterId: number;
  chapterTitle: string;
  knowledge: KnowledgeBrief;
  question: QuestionBrief | null;
  status: string;
  source: string;
}

export interface TodayPlan {
  planId: number;
  planDate: string;
  totalCount: number;
  completedCount: number;
  items: DailyPlanItem[];
}

export function getStoredToken(): string {
  return wx.getStorageSync(TOKEN_KEY) || "";
}

export function getStoredSettings(): StudySettings | null {
  return wx.getStorageSync(SETTINGS_KEY) || null;
}

export function clearSession() {
  wx.removeStorageSync(TOKEN_KEY);
  wx.removeStorageSync(SETTINGS_KEY);
}

export function loginWithWechat(): Promise<LoginResponse> {
  return new Promise((resolve, reject) => {
    wx.login({
      success: (loginResult) => {
        if (!loginResult.code) {
          reject(new Error("微信登录 code 为空"));
          return;
        }

        request<LoginResponse>({
          url: "/api/auth/wx-login",
          method: "POST",
          data: { code: loginResult.code }
        }).then((response) => {
          wx.setStorageSync(TOKEN_KEY, response.token);
          resolve(response);
        }).catch(reject);
      },
      fail: reject
    });
  });
}

export function saveStudySettings(settings: StudySettings): Promise<StudySettings> {
  return request<StudySettings>({
    url: "/api/users/me/study-settings",
    method: "PUT",
    data: settings,
    auth: true
  }).then((response) => {
    wx.setStorageSync(SETTINGS_KEY, response);
    return response;
  });
}

export function fetchTodayPlan(): Promise<TodayPlan> {
  return request<TodayPlan>({
    url: "/api/today",
    auth: true
  });
}

export function startPlanItem(itemId: number): Promise<TodayPlan> {
  return request<TodayPlan>({
    url: `/api/today/items/${itemId}/start`,
    method: "POST",
    auth: true
  });
}

export function completePlanItem(itemId: number): Promise<TodayPlan> {
  return request<TodayPlan>({
    url: `/api/today/items/${itemId}/complete`,
    method: "POST",
    auth: true
  });
}

export interface QuestionDetail {
  id: number;
  knowledgePointId: number;
  stem: string;
  options: QuestionOption[];
}

export interface AnswerSubmission {
  selectedAnswer: string;
  durationSec?: number;
}

export interface AnswerResult {
  isCorrect: boolean;
  correctAnswer: string;
  explanation: string;
  wrongQuestionId?: number;
  status?: string;
  nextStep?: string;
}

export interface WrongQuestionItem {
  id: number;
  questionId: number;
  chapterId: number;
  chapterTitle: string;
  knowledgePointId: number;
  knowledgeTitle: string;
  stem: string;
  options: QuestionOption[];
  wrongCount: number;
  status: string;
  lastWrongAt?: string;
}

export interface WrongbookResponse {
  items: WrongQuestionItem[];
  pendingCount: number;
  masteredCount: number;
}

export interface WrongbookChapter {
  chapterId: number;
  chapterTitle: string;
  count: number;
}

export interface RedoResult {
  isCorrect: boolean;
  correctAnswer: string;
  explanation: string;
  status: string;
  autoMastered: boolean;
  mastered: boolean;
}

export interface MasteredResult {
  id: number;
  status: string;
  mastered: boolean;
}

export function fetchQuestion(questionId: number): Promise<QuestionDetail> {
  return request<QuestionDetail>({
    url: `/api/questions/${questionId}`,
    auth: true
  });
}

export function submitAnswer(
  questionId: number,
  submission: AnswerSubmission
): Promise<AnswerResult> {
  return request<AnswerResult>({
    url: `/api/questions/${questionId}/answer`,
    method: "POST",
    data: submission,
    auth: true
  });
}

export function fetchWrongbook(): Promise<WrongbookResponse> {
  return request<WrongbookResponse>({
    url: "/api/wrongbook",
    auth: true
  });
}

export function fetchWrongbookChapters(): Promise<WrongbookChapter[]> {
  return request<WrongbookChapter[]>({
    url: "/api/wrongbook/chapters",
    auth: true
  });
}

export function fetchWrongQuestion(wrongQuestionId: number): Promise<WrongQuestionItem> {
  return request<WrongQuestionItem>({
    url: `/api/wrongbook/${wrongQuestionId}`,
    auth: true
  });
}

export function redoWrongQuestion(
  wrongQuestionId: number,
  submission: AnswerSubmission
): Promise<RedoResult> {
  return request<RedoResult>({
    url: `/api/wrongbook/${wrongQuestionId}/redo`,
    method: "POST",
    data: submission,
    auth: true
  });
}

export function markWrongMastered(wrongQuestionId: number): Promise<MasteredResult> {
  return request<MasteredResult>({
    url: `/api/wrongbook/${wrongQuestionId}/mastered`,
    method: "POST",
    auth: true
  });
}

export interface ChapterProgress {
  chapterId: number;
  chapterTitle: string;
  totalKnowledgePoints: number;
  studiedCount: number;
  masteredCount: number;
  weakCount: number;
  averageScore: number;
  weak: boolean;
}

export interface ProgressOverview {
  totalKnowledgePoints: number;
  studiedCount: number;
  masteredCount: number;
  weakCount: number;
  averageScore: number;
}

export function fetchProgressChapters(): Promise<ChapterProgress[]> {
  return request<ChapterProgress[]>({
    url: "/api/progress/chapters",
    auth: true
  });
}

export function fetchProgressOverview(): Promise<ProgressOverview> {
  return request<ProgressOverview>({
    url: "/api/progress/overview",
    auth: true
  });
}

export interface AiChatSession {
  id: number;
  title: string;
  createdAt: string;
}

export interface AiChatMessage {
  id: number;
  role: string;
  content: string;
  references: string[];
  tokensUsed: number;
  createdAt: string;
}

export interface AiReviewItem {
  id: number;
  messageId: number;
  status: string;
  createdAt: string;
}

export interface SubscriptionResult {
  id: number;
  templateId: string;
  scene: string;
  accepted: boolean;
  status: string;
  updatedAt: string;
}

export interface AnswerContext {
  questionId: number;
  wrongQuestionId?: number;
  title: string;
  chapter: string;
  summary: string;
  tags?: string[];
}

export function fetchAiSessions(): Promise<AiChatSession[]> {
  return request<AiChatSession[]>({
    url: "/api/ai/sessions",
    auth: true
  });
}

export function createAiSession(title?: string): Promise<AiChatSession> {
  return request<AiChatSession>({
    url: "/api/ai/sessions",
    method: "POST",
    data: title ? { title } : {},
    auth: true
  });
}

export function fetchAiMessages(sessionId: number): Promise<AiChatMessage[]> {
  return request<AiChatMessage[]>({
    url: `/api/ai/sessions/${sessionId}/messages`,
    auth: true
  });
}

export function askAi(sessionId: number, question: string): Promise<AiChatMessage> {
  return request<AiChatMessage>({
    url: `/api/ai/sessions/${sessionId}/ask`,
    method: "POST",
    data: { question },
    auth: true
  });
}

export function addAiMessageToReview(messageId: number): Promise<AiReviewItem> {
  return request<AiReviewItem>({
    url: `/api/ai/messages/${messageId}/review-items`,
    method: "POST",
    auth: true
  });
}

export function saveSubscriptionDecision(payload: {
  templateId: string;
  scene: string;
  accepted: boolean;
}): Promise<SubscriptionResult> {
  return request<SubscriptionResult>({
    url: "/api/notifications/subscription",
    method: "PUT",
    data: payload,
    auth: true
  });
}

export function setPendingAiPrompt(prompt: string) {
  wx.setStorageSync(PENDING_AI_PROMPT_KEY, prompt);
}

export function consumePendingAiPrompt(): string {
  const prompt = wx.getStorageSync(PENDING_AI_PROMPT_KEY) || "";
  if (prompt) {
    wx.removeStorageSync(PENDING_AI_PROMPT_KEY);
  }
  return prompt;
}

export function setAnswerContext(context: AnswerContext) {
  wx.setStorageSync(ANSWER_CONTEXT_KEY, context);
}

export function consumeAnswerContext(questionId: number, wrongQuestionId = 0): AnswerContext | null {
  const context = wx.getStorageSync(ANSWER_CONTEXT_KEY) as AnswerContext | "";
  if (!context) {
    return null;
  }
  if (questionId && context.questionId !== questionId) {
    return null;
  }
  if (wrongQuestionId && context.wrongQuestionId !== wrongQuestionId) {
    return null;
  }
  wx.removeStorageSync(ANSWER_CONTEXT_KEY);
  return context;
}

let reloginPromise: Promise<void> | null = null;

// 后端重启或 token 过期时，并发 401 共用同一次重新登录，避免刷出多个登录请求。
function ensureRelogin(): Promise<void> {
  if (reloginPromise) {
    return reloginPromise;
  }
  clearSession();
  const promise = loginWithWechat().then(() => {});
  reloginPromise = promise;
  promise.then(
    () => { reloginPromise = null; },
    () => { reloginPromise = null; }
  );
  return promise;
}

function request<T>(options: {
  url: string;
  method?: "GET" | "POST" | "PUT";
  data?: unknown;
  auth?: boolean;
}): Promise<T> {
  const app = getApp<AppWithGlobalData>();

  const buildHeader = (): Record<string, string> => {
    const header: Record<string, string> = {
      "content-type": "application/json"
    };
    if (options.auth) {
      header.Authorization = `Bearer ${getStoredToken()}`;
    }
    return header;
  };

  if (options.auth && !getStoredToken()) {
    return Promise.reject(new Error("登录态已失效"));
  }

  const send = (allowRelogin: boolean): Promise<T> => {
    return new Promise<T>((resolve, reject) => {
      wx.request({
        url: `${app.globalData.apiBaseUrl}${options.url}`,
        method: options.method || "GET",
        data: options.data,
        header: buildHeader(),
        success: (res) => {
          // Token 失效：清旧 token、重新登录后带新 token 重试一次。
          if (res.statusCode === 401 && options.auth && allowRelogin) {
            ensureRelogin()
              .then(() => send(false))
              .then(resolve, reject);
            return;
          }

          if (res.statusCode < 200 || res.statusCode >= 300) {
            reject(new Error(`请求失败：${res.statusCode}`));
            return;
          }

          const body = res.data as ApiResponse<T>;
          if (!body.success) {
            reject(new Error(body.message || "请求失败"));
            return;
          }

          resolve(body.data);
        },
        fail: reject
      });
    });
  };

  return send(true);
}
