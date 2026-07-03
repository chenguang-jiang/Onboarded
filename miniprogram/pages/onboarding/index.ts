import { getStoredSettings, getStoredToken, loginWithWechat, saveStudySettings } from "../../utils/api";

Page({
  data: {
    examDate: "2026-11-08",
    reminderTime: "08:30",
    targetOptions: [10, 15, 20, 25, 30],
    targetIndex: 1,
    loading: false,
    errorMessage: ""
  },

  onLoad() {
    if (getStoredToken() && getStoredSettings()?.onboardingCompleted) {
      wx.switchTab({ url: "/pages/today/index" });
      return;
    }

    this.bootstrapLogin();
  },

  bootstrapLogin() {
    this.setData({ loading: true, errorMessage: "" });
    loginWithWechat()
      .then((loginResult) => {
        if (!loginResult.onboardingRequired && getStoredSettings()) {
          wx.switchTab({ url: "/pages/today/index" });
        }
      })
      .catch(() => {
        this.setData({ errorMessage: "登录失败。" });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  onDateChange(event: any) {
    this.setData({ examDate: String(event.detail.value) });
  },

  onTimeChange(event: any) {
    this.setData({ reminderTime: String(event.detail.value) });
  },

  onTargetChange(event: any) {
    this.setData({ targetIndex: Number(event.currentTarget.dataset.index) });
  },

  onSubmit() {
    const dailyTarget = this.data.targetOptions[this.data.targetIndex];
    this.setData({ loading: true, errorMessage: "" });

    saveStudySettings({
      examDate: this.data.examDate,
      dailyTarget,
      reminderTime: this.data.reminderTime
    })
      .then(() => {
        wx.switchTab({ url: "/pages/today/index" });
      })
      .catch(() => {
        this.setData({ errorMessage: "保存失败。" });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  }
});
