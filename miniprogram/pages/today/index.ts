import {
  ChapterProgress,
  completePlanItem,
  DailyPlanItem,
  fetchProgressChapters,
  fetchProgressOverview,
  fetchTodayPlan,
  getStoredSettings,
  getStoredToken,
  setAnswerContext,
  setPendingAiPrompt,
  startPlanItem,
  TodayPlan
} from "../../utils/api";

interface KnowledgeCardView {
  itemId: number;
  questionId: number;
  title: string;
  chapter: string;
  summary: string;
  source: string;
  questionStem: string;
  tags: string[];
  status: string;
  statusText: string;
  statusClass: string;
}

Page({
  data: {
    completed: 0,
    total: 15,
    daysLeft: 118,
    streak: 1,
    loading: false,
    errorMessage: "",
    activeCardIndex: 0,
    progressPercent: 0,
    progressStyle: "width:0%",
    knowledgeCards: [] as KnowledgeCardView[],
    studiedCount: 0,
    masteredCount: 0,
    weakCount: 0,
    overallAvg: 0,
    weakChapters: [] as ChapterProgress[]
  },

  onShow() {
    if (!getStoredToken()) {
      wx.redirectTo({ url: "/pages/onboarding/index" });
      return;
    }
    this.refreshToday();
  },

  refreshToday() {
    const settings = getStoredSettings();
    this.setData({
      loading: true,
      errorMessage: "",
      daysLeft: settings?.examDate ? calculateDaysLeft(settings.examDate) : 118
    });

    Promise.all([fetchTodayPlan(), fetchProgressChapters(), fetchProgressOverview()])
      .then(([plan, chapters, overview]) => {
        const weakChapters = chapters
          .filter((chapter) => chapter.weak)
          .sort((a, b) => a.averageScore - b.averageScore)
          .slice(0, 3);
        this.setData({
          knowledgeCards: plan.items.map(toKnowledgeCardView),
          total: plan.totalCount,
          completed: plan.completedCount,
          progressPercent: percent(plan.completedCount, plan.totalCount),
          progressStyle: `width:${percent(plan.completedCount, plan.totalCount)}%`,
          weakChapters,
          studiedCount: overview.studiedCount,
          masteredCount: overview.masteredCount,
          weakCount: overview.weakCount,
          overallAvg: overview.averageScore
        });
      })
      .catch(() => {
        this.setData({ errorMessage: "加载失败，请重试。" });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  applyPlan(plan: TodayPlan) {
    this.setData({
      knowledgeCards: plan.items.map(toKnowledgeCardView),
      total: plan.totalCount,
      completed: plan.completedCount,
      progressPercent: percent(plan.completedCount, plan.totalCount),
      progressStyle: `width:${percent(plan.completedCount, plan.totalCount)}%`
    });
  },

  onSwiperChange(event: any) {
    this.setData({ activeCardIndex: event.detail.current || 0 });
  },

  onAskAi(event?: any) {
    const dataset = event && event.currentTarget ? event.currentTarget.dataset : {};
    const index = Number(dataset.index !== undefined ? dataset.index : this.data.activeCardIndex);
    const card = this.data.knowledgeCards[index];
    if (card) {
      setPendingAiPrompt(`请用软考系统架构师考试视角解释「${card.title}」，所属章节：${card.chapter}。请补充易错考法和一个例题思路。`);
    }
    wx.switchTab({ url: "/pages/ai-chat/index" });
  },

  onStartPractice(event: any) {
    const index = Number(event.currentTarget.dataset.index);
    const card = this.data.knowledgeCards[index];
    if (!card || card.status === "DONE" || !card.questionId) {
      return;
    }
    setAnswerContext({
      questionId: card.questionId,
      title: card.title,
      chapter: card.chapter,
      summary: card.summary,
      tags: card.tags
    });
    startPlanItem(card.itemId)
      .then((plan) => {
        this.applyPlan(plan);
        wx.navigateTo({
          url: `/pages/answer/index?from=today&questionId=${card.questionId}`,
          events: {
            answered: (data: { isCorrect: boolean }) => {
              if (data.isCorrect) {
                completePlanItem(card.itemId).then((updated) => this.applyPlan(updated));
              }
            }
          }
        });
      })
      .catch(() => {
        wx.showToast({ title: "启动失败", icon: "none" });
      });
  }
});

function toKnowledgeCardView(item: DailyPlanItem): KnowledgeCardView {
  return {
    itemId: item.itemId,
    questionId: item.question ? item.question.id : 0,
    title: item.knowledge.title,
    chapter: item.chapterTitle,
    summary: item.knowledge.summary,
    source: planSourceText(item.source),
    questionStem: item.question ? item.question.stem : "",
    tags: item.knowledge.tags,
    status: item.status,
    statusText: statusText(item.status),
    statusClass: statusClass(item.status)
  };
}

function planSourceText(source: string): string {
  if (source === "CARRY_OVER") return "顺延";
  if (source === "WRONG_RELATED") return "错题";
  if (source === "LOW_MASTERY") return "待巩固";
  if (source === "FALLBACK") return "巩固";
  return "新知识";
}

function percent(completed: number, total: number): number {
  if (!total) {
    return 0;
  }
  return Math.round((completed / total) * 100);
}

function statusText(status: string): string {
  if (status === "STUDYING") return "进行中";
  if (status === "DONE") return "已完成";
  return "待学";
}

function statusClass(status: string): string {
  if (status === "STUDYING") return "status-studying";
  if (status === "DONE") return "status-done";
  return "status-pending";
}

function calculateDaysLeft(examDate: string): number {
  const today = new Date();
  const target = new Date(`${examDate}T00:00:00`);
  const oneDay = 24 * 60 * 60 * 1000;
  return Math.max(0, Math.ceil((target.getTime() - today.getTime()) / oneDay));
}
