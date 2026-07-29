# 開發者文件

基於 [CatVod](https://github.com/CatVodTVOfficial/CatVodTVJarLoader) 的開源 Android TV 影音應用程式（Leanback 專版），透過外部 JSON 配置靈活擴展片源。

[討論群組](https://t.me/fongmi_official) | [發布頻道](https://t.me/fongmi_release)

[![Star History Chart](https://api.star-history.com/svg?repos=FongMi/TV&type=Date)](https://www.star-history.com/#FongMi/TV&Date)

---

## 目錄

- [專案架構](#專案架構)
- [播放器](#播放器)
- [點播功能](#點播功能)
- [爬蟲引擎](#爬蟲引擎)
- [網路功能](#網路功能)
- [遠端控制](#遠端控制)
- [配置說明](#配置說明)
- [延伸閱讀](#延伸閱讀)

---

## 專案架構

| 項目      | 值                                      |
|---------|----------------------------------------|
| package | `com.fongmi.android.tv`                |
| minSdk  | 24（Android 7.0 Nougat）                 |
| abi     | `arm64-v8a`、`armeabi-v7a`（依電視架構二選一） |
| flavor  | `leanback`（Android TV）                 |

```
TV/
├── app/            主應用程式（Leanback UI）
├── catvod/         爬蟲抽象層（Spider 介面、OkHttp 網路棧）
└── quickjs/        QuickJS JavaScript 引擎
```

**本 fork 精簡項：**

- 僅保留 TV 版（`leanback`），提供兩種 ABI 變體：
  - `arm64_v8a`：較新電視、盒子（64 位 ARM）
  - `armeabi_v7a`：較舊電視（32 位 ARM，例如部分小米電視）
- 內建精簡 Vod 配置（`assets/config/vod.json`），`spider.jar` 與 Vosk 語音模型從 CDN 按需下載
- 支援掃碼 / URL 換源
- 配置與首頁推薦磁碟快取，命中後先展示、後台刷新
- 已移除：手機版、直播、DLNA、Android Auto、Python、Thunder/Jianpian 協議、MPV 播放器

---

## 播放器

- **核心**：ExoPlayer（Media3），硬解優先
- **渲染**：SurfaceView / TextureView
- **DRM**：Widevine、PlayReady、ClearKey，支援 `#KODIPROP` 宣告
- **字幕**：SRT / SSA / ASS 外掛字幕、系統 CaptioningManager、遠端即時注入
- **其他**：倍速、多縮放比例、背景音訊、片頭 / 片尾自動跳過

---

## 點播功能

- 多站點分類瀏覽，Filter 篩選（年份 / 地區 / 類型等）
- 多站點**並行搜尋**，關鍵字自動繁轉簡提升相容性
- 播放失敗自動換源：解析器 → 線路 → 搜尋其他站 → 下一站點
- 觀看記錄（保留 60 天）、收藏、無痕模式
- 遙控器操作首頁、搜尋、播放全流程

---

## 爬蟲引擎

支援兩種語言撰寫爬蟲：

- Java JAR（DexClassLoader）
- JavaScript（QuickJS）

透過 `api` 欄位指定爬蟲，`ext` 欄位傳入初始化參數。完整 API 規格見 [SPIDER.md](docs/SPIDER.md)。

---

## 網路功能

- **DoH**：DNS over HTTPS，支援 Bootstrap IP
- **代理**：HTTP / HTTPS / SOCKS4 / SOCKS5，依 host 正則規則動態選擇
- **Hosts**：DNS 解析覆蓋，支援萬用字元 `*`
- **CORS 注入**：依 host 規則在回應中注入自訂標頭
- **廣告攔截**：`ads` 黑名單，符合域名直接攔截
- **WebView 嗅探**：Sniffer 以 regex 攔截媒體 URL；支援 UA 偽裝

---

## 遠端控制

應用啟動後綁定本地 HTTP 伺服器（NanoHTTPD）：

- **本機服務**（`127.0.0.1`）：爬蟲代理、解析、資源讀取等內部能力
- **遠端服務**（區域網 IP）：手機掃碼推送，需 6 位配對碼（URL 自帶 `token` 參數）

埠號從 **9978** 起自動偵測。完整端點說明見 [LOCAL.md](docs/LOCAL.md)。

---

## 配置說明

Vod 配置為應用主要入口：

- **內建**：`assets://config/vod.json`（首次安裝自動使用）
- **換源**：設定頁掃碼或輸入外部 JSON URL

頂層欄位定義點播站點（`sites`）、解析規則（`parses`）、網路設定（`doh`、`proxy`、`hosts`、`ads`）等。完整欄位說明見 [CONFIG.md](docs/CONFIG.md)。

**構建命令：**

首次構建需先編譯 Media3 AAR（約 15 分鐘，僅需執行一次）：

```bash
scripts/build_media_aars.sh
```

依電視架構選擇對應變體：

```bash
# 64 位電視 / 盒子
./gradlew assembleLeanbackArm64_v8aRelease

# 32 位電視（如部分小米電視）
./gradlew assembleLeanbackArmeabi_v7aRelease
```

產物位於 `Release/apk/`：

| APK | 適用設備 |
|-----|---------|
| `leanback-arm64_v8a.apk` | `arm64-v8a` |
| `leanback-armeabi_v7a.apk` | `armeabi-v7a` |

本地調試（自帶 debug 簽名，無需配置 keystore）：

```bash
./gradlew assembleLeanbackArmeabi_v7aDebug   # 或 Arm64_v8aDebug
adb install -r app/build/outputs/apk/leanbackArmeabi_v7a/debug/app-leanback-armeabi_v7a-debug.apk
```

不確定電視架構時，可在已連接 adb 的設備上執行：

```bash
adb shell getprop ro.product.cpu.abi
```

Release 簽名需在 `local.properties` 配置 `storeFile`、`keyAlias`、`storePassword`；未配置時 release 包無法直接安裝。

---

## 延伸閱讀

| 文件                          | 說明                   |
|-----------------------------|----------------------|
| [CONFIG.md](docs/CONFIG.md) | Vod 完整配置欄位說明       |
| [SPIDER.md](docs/SPIDER.md) | Spider 所有方法規格與回傳格式   |
| [LOCAL.md](docs/LOCAL.md)   | 本地 HTTP API 所有端點完整說明 |
