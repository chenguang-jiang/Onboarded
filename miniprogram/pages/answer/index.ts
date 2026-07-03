import {
  AnswerResult,
  consumeAnswerContext,
  fetchQuestion,
  fetchWrongQuestion,
  getStoredToken,
  QuestionOption,
  redoWrongQuestion,
  RedoResult,
  setPendingAiPrompt,
  submitAnswer
} from "../../utils/api";

interface OptionView {
  key: string;
  text: string;
  state: "" | "selected" | "correct" | "wrong";
}

Page({
  data: {
    loading: false,
    errorMessage: "",
    mode: "today" as "today" | "wrongbook",
    questionId: 0,
    wrongQuestionId: 0,
    knowledgeTitle: "做题",
    chapterTitle: "系统架构师",
    knowledgeSummary: "先看考点，再做题。",
    trapText: "留意限定词、场景和计算条件。",
    flipped: false,
    stem: "",
    options: [] as OptionView[],
    selectedKey: "",
    submitted: false,
    isCorrect: false,
    correctAnswer: "",
    explanation: "",
    resultBanner: "",
    mastered: false,
    notified: false
  },

  onLoad(query: Record<string, string>) {
    if (!getStoredToken()) {
      wx.redirectTo({ url: "/pages/onboarding/index" });
      return;
    }

    const mode: "today" | "wrongbook" = query.from === "wrongbook" ? "wrongbook" : "today";
    const questionId = Number(query.questionId || 0);
    const wrongQuestionId = Number(query.wrongQuestionId || 0);
    const context = consumeAnswerContext(questionId, wrongQuestionId) || {
      questionId,
      wrongQuestionId,
      title: "做题",
      chapter: "系统架构师",
      summary: "先看考点，再做题。",
      tags: []
    };
    this.setData({
      mode,
      questionId,
      wrongQuestionId,
      knowledgeTitle: context.title,
      chapterTitle: context.chapter,
      knowledgeSummary: context.summary,
      trapText: trapTextOf(context.title, ""),
      loading: true,
      errorMessage: ""
    });

    const loadPromise = mode === "wrongbook"
      ? fetchWrongQuestion(this.data.wrongQuestionId)
      : fetchQuestion(this.data.questionId);

    loadPromise
      .then((payload) => {
        this.setData({
          stem: payload.stem,
          knowledgeTitle: (payload as any).knowledgeTitle || context.title,
          chapterTitle: (payload as any).chapterTitle || context.chapter,
          knowledgeSummary: context.summary,
          trapText: trapTextOf(context.title, payload.stem),
          options: (payload.options as QuestionOption[]).map((opt) => ({
            key: opt.key,
            text: opt.text,
            state: "" as OptionView["state"]
          })),
          loading: false
        });
      })
      .catch(() => {
        this.setData({
          loading: false,
          errorMessage: "题目加载失败。"
        });
      });
  },

  onSelectOption(event: any) {
    if (this.data.submitted) {
      return;
    }
    const key = String(event.currentTarget.dataset.key);
    this.setData({
      selectedKey: key,
      options: this.data.options.map((opt) => ({
        ...opt,
        state: (opt.key === key ? "selected" : "") as OptionView["state"]
      }))
    });
  },

  onFlipKnowledge() {
    this.setData({ flipped: !this.data.flipped });
  },

  onSubmit() {
    if (this.data.submitted || !this.data.selectedKey) {
      return;
    }
    const selected = this.data.selectedKey;

    const handleResult = (result: AnswerResult | RedoResult) => {
      const correctAnswer = result.correctAnswer;
      const options = this.data.options.map((opt) => {
        let state: OptionView["state"] = "";
        if (opt.key === correctAnswer) {
          state = "correct";
        } else if (opt.key === selected && !result.isCorrect) {
          state = "wrong";
        }
        return { ...opt, state };
      });
      this.setData({
        submitted: true,
        isCorrect: result.isCorrect,
        correctAnswer,
        explanation: result.explanation,
        options,
        resultBanner: this.bannerFor(result),
        mastered: this.data.mode === "wrongbook" && (result as RedoResult).mastered === true
      });
    };

    const failed = () => {
      this.setData({ errorMessage: "提交失败。" });
    };

    if (this.data.mode === "wrongbook") {
      redoWrongQuestion(this.data.wrongQuestionId, { selectedAnswer: selected })
        .then(handleResult)
        .catch(failed);
    } else {
      submitAnswer(this.data.questionId, { selectedAnswer: selected })
        .then(handleResult)
        .catch(failed);
    }
  },

  onBack() {
    this.notifyOpener();
    wx.navigateBack();
  },

  onUnload() {
    this.notifyOpener();
  },

  notifyOpener() {
    if (!this.data.submitted || this.data.notified) {
      return;
    }
    this.setData({ notified: true });
    const getChannel = (this as any).getOpenerEventChannel;
    const channel = typeof getChannel === "function" ? getChannel.call(this) : null;
    if (channel && typeof channel.emit === "function") {
      if (this.data.mode === "wrongbook") {
        channel.emit("redone", {
          wrongQuestionId: this.data.wrongQuestionId,
          mastered: this.data.mastered
        });
      } else {
        channel.emit("answered", {
          questionId: this.data.questionId,
          isCorrect: this.data.isCorrect
        });
      }
    }
  },

  onAskAi() {
    setPendingAiPrompt(`请解释这道软考题：${this.data.stem}\n我的选择：${this.data.selectedKey || "未选择"}\n知识点：${this.data.knowledgeTitle}\n请说明易错点和正确解题路径。`);
    wx.switchTab({ url: "/pages/ai-chat/index" });
  },

  bannerFor(result: AnswerResult | RedoResult): string {
    if (this.data.mode === "today") {
      return result.isCorrect ? "答对了" : "答错了 · 已记录";
    }
    const redo = result as RedoResult;
    if (!redo.isCorrect) {
      return "答错了";
    }
    if (redo.autoMastered || redo.mastered) {
      return "已掌握";
    }
    return "再对 1 次即可掌握";
  }
});

function trapTextOf(title: string, stem: string): string {
  if (title.includes("流水线")) {
    return "常见陷阱是把吞吐率和加速比混用。稳定吞吐率看最慢流水段，不是所有流水段平均。";
  }
  if (title.includes("Cache")) {
    return "常见陷阱是只看命中率。平均访问时间还要同时看命中时间和失效代价。";
  }
  if (stem.includes("不需要考虑")) {
    return "题干出现“不需要”“不正确”时，先圈出否定词，再逐项排除正向概念。";
  }
  return "先判断题目考的是定义、场景还是计算，再回到知识点里的关键词，不要被无关技术名词带偏。";
}
