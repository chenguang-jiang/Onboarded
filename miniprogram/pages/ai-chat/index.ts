import {
  addAiMessageToReview,
  AiChatMessage,
  AiChatSession,
  askAi,
  consumePendingAiPrompt,
  createAiSession,
  fetchAiMessages,
  fetchAiSessions,
  getStoredToken
} from "../../utils/api";

declare const requirePlugin: any;

let voiceManager: any = null;
let voiceReady = false;

Page({
  data: {
    sessions: [] as AiChatSession[],
    activeSessionId: 0,
    activeSessionTitle: "",
    messages: [] as AiChatMessage[],
    inputText: "",
    sending: false,
    loading: false,
    errorMessage: "",
    voiceAvailable: true,
    recording: false,
    recognizing: false,
    voiceHint: "按住说",
    quickPrompts: ["讲简单点", "出题", "举例", "易错点"]
  },

  onLoad() {
    this.setupVoiceRecognition();
  },

  onShow() {
    if (!getStoredToken()) {
      wx.redirectTo({ url: "/pages/onboarding/index" });
      return;
    }
    const pendingPrompt = consumePendingAiPrompt();
    if (pendingPrompt) {
      this.setData({ inputText: pendingPrompt });
    }
    this.loadSessions();
  },

  onHide() {
    this.stopVoiceIfNeeded();
  },

  onUnload() {
    this.stopVoiceIfNeeded();
  },

  setupVoiceRecognition() {
    try {
      const plugin = requirePlugin("WechatSI");
      voiceManager = plugin.getRecordRecognitionManager();
      voiceReady = true;

      voiceManager.onStart = () => {
        this.setData({
          recording: true,
          recognizing: true,
          voiceHint: "聆听中..."
        });
      };

      voiceManager.onRecognize = (result: { result?: string }) => {
        const text = (result.result || "").trim();
        if (text) {
          this.setData({ voiceHint: `识别中：${text}` });
        }
      };

      voiceManager.onStop = (result: { result?: string }) => {
        const text = (result.result || "").trim();
        this.setData({
          recording: false,
          recognizing: false,
          voiceHint: "按住说"
        });

        if (!text) {
          wx.showToast({ title: "未识别", icon: "none" });
          return;
        }

        const base = this.data.inputText.trim();
        this.setData({ inputText: base ? `${base}\n${text}` : text });
        wx.showToast({ title: "已转成文字", icon: "none" });
      };

      voiceManager.onError = () => {
        this.setData({
          recording: false,
          recognizing: false,
          voiceHint: "按住说"
        });
        wx.showToast({ title: "识别失败", icon: "none" });
      };
    } catch (error) {
      voiceReady = false;
      this.setData({
        voiceAvailable: false,
        voiceHint: "语音不可用"
      });
    }
  },

  stopVoiceIfNeeded() {
    if (voiceManager && this.data.recording) {
      voiceManager.stop();
    }
  },

  onToggleVoice() {
    if (!voiceReady || !voiceManager) {
      wx.showToast({ title: "语音未启用", icon: "none" });
      return;
    }

    if (this.data.recording) {
      voiceManager.stop();
      return;
    }

    wx.getSetting({
      success: (setting) => {
        const auth = setting.authSetting["scope.record"];
        const startRecord = () => {
          voiceManager.start({
            lang: "zh_CN",
            duration: 60000
          });
        };

        if (auth) {
          startRecord();
          return;
        }

        if (auth === false) {
          wx.showToast({ title: "开启麦克风", icon: "none" });
          return;
        }

        wx.authorize({
          scope: "scope.record",
          success: startRecord,
          fail: () => {
            wx.showToast({ title: "需要麦克风", icon: "none" });
          }
        });
      },
      fail: () => {
        wx.showToast({ title: "权限异常", icon: "none" });
      }
    });
  },

  loadSessions() {
    this.setData({ loading: true, errorMessage: "" });
    fetchAiSessions()
      .then((sessions) => {
        if (sessions.length === 0) {
          return createAiSession().then((session) => this.activateSession([session]));
        }
        this.activateSession(sessions);
      })
      .catch(() => {
        this.setData({ errorMessage: "会话加载失败。" });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  activateSession(sessions: AiChatSession[]) {
    const active = sessions[0];
    this.setData({
      sessions,
      activeSessionId: active.id,
      activeSessionTitle: active.title,
      messages: []
    });
    this.loadMessages();
  },

  loadMessages() {
    if (!this.data.activeSessionId) {
      return;
    }
    fetchAiMessages(this.data.activeSessionId)
      .then((messages) => {
        this.setData({ messages });
      })
      .catch(() => {
        this.setData({ errorMessage: "消息加载失败。" });
      });
  },

  onInput(event: any) {
    this.setData({ inputText: event.detail.value || "" });
  },

  onQuickPrompt(event: any) {
    const prompt = String(event.currentTarget.dataset.prompt);
    const base = this.data.inputText.trim();
    this.setData({ inputText: base ? `${base}\n${prompt}` : prompt });
  },

  onSend() {
    const question = this.data.inputText.trim();
    if (!question || this.data.sending || !this.data.activeSessionId) {
      return;
    }
    this.setData({ inputText: "", sending: true });
    const userMessage: AiChatMessage = {
      id: Date.now(),
      role: "user",
      content: question,
      references: [],
      tokensUsed: 0,
      createdAt: ""
    };
    askAi(this.data.activeSessionId, question)
      .then((assistant) => {
        this.setData({
          messages: [...this.data.messages, userMessage, assistant],
          sending: false
        });
      })
      .catch(() => {
        this.setData({ sending: false });
        wx.showToast({ title: "发送失败，请重试", icon: "none" });
      });
  },

  onNewSession() {
    createAiSession()
      .then((session) => {
        this.setData({
          sessions: [session, ...this.data.sessions],
          activeSessionId: session.id,
          activeSessionTitle: session.title,
          messages: []
        });
      })
      .catch(() => {
        wx.showToast({ title: "创建失败", icon: "none" });
      });
  },

  onAddReview(event: any) {
    const messageId = Number(event.currentTarget.dataset.id);
    if (!messageId) {
      return;
    }
    addAiMessageToReview(messageId)
      .then(() => {
        wx.showToast({ title: "已加入", icon: "success" });
      })
      .catch(() => {
        wx.showToast({ title: "加入失败", icon: "none" });
      });
  }
});
