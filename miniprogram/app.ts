App<IAppOption>({
  globalData: {
    apiBaseUrl: "http://localhost:8080"
  },

  onLaunch() {
    wx.setStorageSync("onboarding:lastLaunchAt", Date.now());
  }
});

interface IAppOption {
  globalData: {
    apiBaseUrl: string;
  };
}
