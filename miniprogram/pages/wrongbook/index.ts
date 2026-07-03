import {
  fetchWrongbook,
  fetchWrongbookChapters,
  getStoredToken,
  markWrongMastered,
  setAnswerContext,
  setPendingAiPrompt,
  WrongQuestionItem,
  WrongbookChapter
} from "../../utils/api";

interface WrongCardView {
  id: number;
  questionId: number;
  chapterId: number;
  title: string;
  stem: string;
  chapter: string;
  wrongCount: number;
  errorReason: string;
  status: string;
  statusText: string;
  statusClass: string;
}

const ALL_CHAPTER: WrongbookChapter = { chapterId: 0, chapterTitle: "全部", count: 0 };

Page({
  data: {
    loading: false,
    errorMessage: "",
    pendingCount: 0,
    masteredCount: 0,
    chapters: [ALL_CHAPTER] as WrongbookChapter[],
    heatmapChapters: [] as WrongbookChapter[],
    filterModes: ["按章节", "按错误原因"],
    activeMode: "按章节",
    activeChapterId: 0,
    wrongItems: [] as WrongCardView[],
    visibleItems: [] as WrongCardView[]
  },

  onShow() {
    if (!getStoredToken()) {
      wx.redirectTo({ url: "/pages/onboarding/index" });
      return;
    }
    this.refreshWrongbook();
  },

  refreshWrongbook() {
    this.setData({ loading: true, errorMessage: "" });
    Promise.all([fetchWrongbook(), fetchWrongbookChapters()])
      .then(([response, chapters]) => {
        const wrongItems = response.items.map(toWrongCardView);
        const allChapter: WrongbookChapter = { ...ALL_CHAPTER, count: wrongItems.length };
        this.setData({
          pendingCount: response.pendingCount,
          masteredCount: response.masteredCount,
          chapters: [allChapter, ...chapters],
          heatmapChapters: chapters.slice(0, 5),
          wrongItems,
          activeChapterId: 0
        });
        this.applyFilter(0);
      })
      .catch(() => {
        this.setData({ errorMessage: "加载失败，请重试。" });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  applyFilter(chapterId: number) {
    const visibleItems = chapterId === 0
      ? this.data.wrongItems
      : this.data.wrongItems.filter((item) => item.chapterId === chapterId);
    this.setData({ activeChapterId: chapterId, visibleItems });
  },

  onFilterTap(event: any) {
    const chapterId = Number(event.currentTarget.dataset.chapterId);
    this.applyFilter(chapterId);
  },

  onModeTap(event: any) {
    this.setData({ activeMode: String(event.currentTarget.dataset.mode) });
  },

  onRedo(event: any) {
    const id = Number(event.currentTarget.dataset.id);
    const item = this.data.wrongItems.find((candidate) => candidate.id === id);
    if (item) {
      setAnswerContext({
        questionId: item.questionId,
        wrongQuestionId: item.id,
        title: item.title,
        chapter: item.chapter,
        summary: item.stem,
        tags: [item.errorReason]
      });
    }
    wx.navigateTo({ url: `/pages/answer/index?from=wrongbook&wrongQuestionId=${id}&questionId=${item ? item.questionId : 0}` });
  },

  onMarkMastered(event: any) {
    const id = Number(event.currentTarget.dataset.id);
    markWrongMastered(id)
      .then(() => this.refreshWrongbook())
      .catch(() => {
        wx.showToast({ title: "操作失败", icon: "none" });
      });
  },

  onAskAi(event: any) {
    const id = Number(event.currentTarget.dataset.id);
    const item = this.data.wrongItems.find((candidate) => candidate.id === id);
    if (item) {
      setPendingAiPrompt(`请帮我解释这道错题：${item.stem}\n知识点：${item.title}\n章节：${item.chapter}\n错误原因倾向：${item.errorReason}\n请给出正确思路和一道变式题。`);
    }
    wx.switchTab({ url: "/pages/ai-chat/index" });
  }
});

function toWrongCardView(item: WrongQuestionItem): WrongCardView {
  return {
    id: item.id,
    questionId: item.questionId,
    chapterId: item.chapterId,
    title: item.knowledgeTitle,
    stem: item.stem,
    chapter: item.chapterTitle,
    wrongCount: item.wrongCount,
    errorReason: errorReasonOf(item.wrongCount, item.status),
    status: item.status,
    statusText: statusText(item.status),
    statusClass: statusClass(item.status)
  };
}

function errorReasonOf(wrongCount: number, status: string): string {
  if (status === "RETRYING") {
    return "中断";
  }
  if (wrongCount >= 2) {
    return "概念";
  }
  return "辨析";
}

function statusText(status: string): string {
  if (status === "OPEN") {
    return "待重做";
  }
  if (status === "RETRYING") {
    return "重做中";
  }
  if (status === "MASTERED") {
    return "已掌握";
  }
  return "已归档";
}

function statusClass(status: string): string {
  if (status === "OPEN") {
    return "status-open";
  }
  if (status === "RETRYING") {
    return "status-retrying";
  }
  return "status-mastered";
}
