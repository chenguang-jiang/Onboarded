import {
  getStoredSettings,
  getStoredToken,
  saveSubscriptionDecision
} from "../../utils/api";

const DAILY_REMINDER_TEMPLATE_ID = "daily-template";
const SUBSCRIPTION_STATUS_KEY = "onboarding:subscription-status";

Page({
  data: {
    examDate: "2026-11-08",
    dailyTarget: 15,
    reminderTime: "08:30",
    subscriptionStatus: "未授权"
  },

  onShow() {
    if (!getStoredToken()) {
      wx.redirectTo({ url: "/pages/onboarding/index" });
      return;
    }
    const settings = getStoredSettings();
    const subscriptionStatus = wx.getStorageSync(SUBSCRIPTION_STATUS_KEY);
    if (settings) {
      this.setData({
        examDate: settings.examDate,
        dailyTarget: settings.dailyTarget,
        reminderTime: settings.reminderTime,
        subscriptionStatus: subscriptionStatus || this.data.subscriptionStatus
      });
    } else if (subscriptionStatus) {
      this.setData({ subscriptionStatus });
    }
  },

  onSubscribeReminder() {
    wx.requestSubscribeMessage({
      tmplIds: [DAILY_REMINDER_TEMPLATE_ID],
      success: (result) => {
        const accepted = result[DAILY_REMINDER_TEMPLATE_ID] === "accept";
        this.saveSubscription(accepted);
      },
      fail: () => {
        this.saveSubscription(false);
      }
    });
  },

  saveSubscription(accepted: boolean) {
    saveSubscriptionDecision({
      templateId: DAILY_REMINDER_TEMPLATE_ID,
      scene: "DAILY_TASK",
      accepted
    })
      .then((result) => {
        const subscriptionStatus = result.status === "ACCEPTED" ? "已授权" : "已拒绝";
        wx.setStorageSync(SUBSCRIPTION_STATUS_KEY, subscriptionStatus);
        this.setData({ subscriptionStatus });
        wx.showToast({ title: accepted ? "已开启" : "未开启", icon: "none" });
      })
      .catch(() => {
        wx.showToast({ title: "保存失败", icon: "none" });
      });
  }
});
