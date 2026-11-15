# 《今天去哪？》開發進度

## 1. 專案整體狀態與最近更新時間

更新：2026-11-15（Asia/Taipei）。Android App 使用 Kotlin、XML、Room；依專案企畫書及 A～H UI 逐步實作。日常採 16 GB RAM 低記憶體模式，不啟動 Android Studio GUI 或模擬器。下表狀態分開記錄實作與各項驗證；**建構通過不等於完成驗收**。完整既有紀錄保存在 [歷史紀錄](DEVELOPMENT_HISTORY_2026-11-15.md)，其中舊下一步與舊 IDE 建議已由本檔和 AGENTS.md 取代。

## 2. 最近完成的工作

- 外部地圖：各景點入口及沒有可用 App 的提示已接入；由外部 App 設定導航，非多點道路導航。URI 邏輯及測試已提交 `eb30a9b`；畫面串接仍是既有未提交檔案的一部分。
- 獨立 QA：`app/build.gradle.kts` 新增 `.qa` build type，只有指定 `-PisolatedQa=true` 時 instrumentation 才指向 QA。Debug 與 QA 測試 APK 均已編譯；QA 尚待裝置驗證。
- 本次整理持久協作規則及狀態入口；保留先前紀錄與所有既有程式變更。
- 路線與規劃裝置測試共用 QA 前提：只允許獨立 QA package、已存在的帳號資料庫及有效的 `qa_` 帳號 session；條件不符時略過，不能當作通過。修改位於先前未追蹤的測試檔及新測試輔助檔，尚待裝置執行。
- 目的地天氣的 WMO 代碼辨識元件已實作，未知代碼保持 UNKNOWN；建構及單元測試通過。尚未連接 API、中文畫面、氣溫或降雨機率，整體天氣功能仍開發中。

## 3. 功能進度表

狀態用語：尚未開始、開發中、已實作待測試、建構通過、單元測試通過、裝置驗證通過、完成驗收。下表同列多種狀態，以限定實際驗證範圍。

| 功能 | 狀態與範圍 | 尚缺 |
| --- | --- | --- |
| A 會員 | 已實作；先前建構、單元及裝置流程驗證通過 | 使用者人工驗收；相關程式仍未提交 |
| B 首頁 | 開發中；規劃入口及登出已接 | 我的行程、個人中心等 |
| C 出遊條件 | 部分已實作、建構與單元測試通過；先前部分裝置案例分批通過 | 任意出發地搜尋、整體驗收 |
| D 景點 | 本機推薦、搜尋及勾選已實作，建構／單元通過；舊畫面案例分批通過 | 線上搜尋及完整驗收 |
| E 路線與地圖 | 排序、估算、按需 OSM 底圖已實作；建構／相關單元測試通過 | 路線裝置驗證、休息建議；不代表道路導航 |
| F 行程詳情與外部地圖 | 單點外部地圖入口已實作、建構／URI 單元測試通過；WMO 代碼元件已實作並通過單元測試 | 裝置跳轉、天氣查詢與顯示、休息點、行程詳情及儲存 |
| G 我的行程 | 尚未開始 | Room 保存、查看與刪除 |
| H 個人中心 | 開發中；首頁已有帳號顯示及登出 | 其他企畫書項目 |
| 課程元件 | 尚未開始 | 依實際功能接入並驗證 |

## 4. 建構及測試結果

- 2026-11-15：Wrapper CLI `--no-daemon --max-workers=1 :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest`：`BUILD SUCCESSFUL in 38s`，24 項單元測試本次執行、零 failures/errors/skipped；Debug APK 8,965,727 bytes。完整本機 JBR 與 Wrapper 指令見 AGENTS.md，需在參數後加入上述 task。
- 2026-11-15：同一 Wrapper CLI、同樣資源參數，加 `-PisolatedQa=true :app:assembleQa :app:assembleQaAndroidTest`：`BUILD SUCCESSFUL in 34s`；這是編譯，不是裝置測試。
- 2026-11-15：同一 Wrapper CLI、同樣資源參數，加 `-PisolatedQa=true :app:assembleQaAndroidTest`：`BUILD SUCCESSFUL in 35s`，4 tasks 執行；QA session 防護已編譯，未執行裝置測試。此階段只改裝置測試，不重跑不受影響的單元測試。
- 2026-11-15：相同 `:app:assembleQaAndroidTest` 再執行以驗證共用 QA 前提，`BUILD SUCCESSFUL in 41s`，4 tasks 執行；僅編譯，裝置測試仍待執行。
- 2026-11-15：Wrapper CLI 同上，使用 `--no-daemon --max-workers=1 '-Pkotlin.compiler.execution.strategy=in-process' :app:assembleDebug :app:testDebugUnitTest`：`BUILD SUCCESSFUL in 52s`，26 項單元測試、0 failures/errors/skipped；Debug APK 8,965,727 bytes，22:13:56。第一次未引用含點號的 PowerShell 參數，Gradle 誤認為 task、未進編譯；修正引號後通過。
- 先前 23 項通過是新增 ExternalMapTest 前的結果；較新的實際執行共 24 項。歷史會員／規劃裝置結果及中斷紀錄見歷史檔。
- 路線、外部地圖按鈕、QA 隔離與最新測試 APK：**待裝置驗證**。未將使用者人工操作標為通過。

## 5. 已知問題與風險

- Pixel 8 曾顯示 ADB offline；不反覆啟動或修復，集中測試時再安全處理。不得清除、重設、覆蓋資料或登出現有帳號。
- 規劃條件與勾選目前僅為 Activity 暫存，冷啟動或離開流程無法保證草稿保留。
- QA 裝置測試須先確認裝置、套件及獨立 QA session；RouteFlowTest 和 PlanningFlowTest 已共用 QA package 與帳號查核，但該防護尚待裝置驗證。
- 個別與整趟估算使用直線距離假設；本機三筆景點並非線上搜尋。線上來源及政策前提見 [資料來源](PLANNING_DATA_SOURCES.md)。

## 6. 尚未提交的變更

依 2026-11-15 的 `git status`：會員、首頁、規劃、路線／地圖、資源、Gradle／QA 設定及裝置測試均有既有未提交修改與新增檔案。不要整批提交或覆寫；每次重新檢查狀態。專案外 `../HW.zip` 不得加入。文件整理完成後僅提交本次明確文件檔。

## 7. 下一個優先開發功能

繼續目的地天氣功能：依 [Open-Meteo 文件](https://open-meteo.com/en/docs) 設計含時區及時間標示的溫度、天氣代碼與降雨機率查詢，處理空值／網路失敗及服務限制；不把逐小時降雨機率誤稱即時觀測。其後完成行程畫面及集中裝置驗證；休息建議與行程儲存仍待完成。

## 8. 後續待辦事項

1. 完成需求內其餘功能；避免把假資料或示意畫面當成真實資料。
2. 累積相關功能後集中驗證路線、外部地圖與 QA 版本；先確定裝置／套件及資料隔離。
3. 完成帳號範圍內的行程保存、重新查看、刪除與程序重啟驗證，再做整合及展示流程驗收。
4. 每個功能或重要階段更新此檔，記錄日期、實際指令、結果、風險及下一步；詳細舊證據留在歷史檔。
