# 《今天去哪？》開發進度

## 1. 專案整體狀態與最近更新時間

更新：2026-11-15（Asia/Taipei）。Android App 使用 Kotlin、XML、Room；依專案企畫書及 A～H UI 逐步實作。日常採 16 GB RAM 低記憶體模式，不啟動 Android Studio GUI 或模擬器。下表狀態分開記錄實作與各項驗證；**建構通過不等於完成驗收**。完整既有紀錄保存在 [歷史紀錄](DEVELOPMENT_HISTORY_2026-11-15.md)，其中舊下一步與舊 IDE 建議已由本檔和 AGENTS.md 取代。

## 2. 最近完成的工作

- 外部地圖：各景點入口及沒有可用 App 的提示已接入；由外部 App 設定導航，非多點道路導航。URI 邏輯及測試已提交 `eb30a9b`；畫面串接仍是既有未提交檔案的一部分。
- 獨立 QA：`app/build.gradle.kts` 新增 `.qa` build type，只有指定 `-PisolatedQa=true` 時 instrumentation 才指向 QA。Debug 與 QA 測試 APK 均已編譯；QA 尚待裝置驗證。
- 本次整理持久協作規則及狀態入口；保留先前紀錄與所有既有程式變更。
- 路線與規劃裝置測試共用 QA 前提：只允許獨立 QA package、已存在的帳號資料庫及有效的 `qa_` 帳號 session；條件不符時略過，不能當作通過。修改位於先前未追蹤的測試檔及新測試輔助檔，尚待裝置執行。
- 目的地天氣的 WMO 代碼辨識元件已實作，未知代碼保持 UNKNOWN；建構及單元測試通過。完整天氣流程仍待裝置驗證。
- 天氣查詢與行程畫面已實作：使用者主動查詢各景點，顯示查詢時最近整點預報、地點時區、氣溫、天氣及前一小時降雨機率；缺值、失敗、取消、有限快取與重試均有處理。行程尚無出發／抵達時間，不能推稱抵達時預報。來源與授權連結已標示；裝置及實際 UI 驗證仍待完成。
- 天氣資料層、單元測試及 Gson 依賴以獨立 index 精確建立本機 commit `e71d69f`；提交前檢查四個檔案的 staged diff，提交後確認正式 index 無暫存，並比對既有 Gradle、規劃、路線與天氣畫面共八個檔案雜湊不變。未 push。
- 新增獨立抵達時間計算元件：在明確提供出發 epoch 及每段交通／停留分鐘數時，計算各站抵達時間，拒絕負數及運算溢位；已建構與單元測試通過。尚未接入既有行程、出發時間輸入或天氣 UI，不代表抵達時間預報已完成。
- 時間輸入來源與完整性模型已實作：使用者出發時間須含明確時區；各段交通及停留必須標示使用者輸入、外部服務估算或具說明的系統預設。缺項明確回報，不填補預設；完整時沿用既有時間計算並保留來源，溢位明確回報。僅供程式模型使用，未顯示預測抵達時間。
- 天氣預報時間選取已加上 30 分鐘最大距離檢查；當逐時資料有缺口或超出相鄰時間範圍時回報不可用，不以相距過遠的預報冒充目標時段。建構及單元測試通過；裝置驗證仍待集中執行。
- 行程保存第一階段：已實作保存快照與帳號範圍操作介面，沿用現有條件及景點模型，保留景點順序、名稱／座標／地址與估算說明；記憶體替身測試跨帳號讀取／刪除、輸入驗證及快照獨立性。尚未接 Room、session 或 UI，沒有實際持久化。

## 3. 功能進度表

狀態用語：尚未開始、開發中、已實作待測試、建構通過、單元測試通過、裝置驗證通過、完成驗收。下表同列多種狀態，以限定實際驗證範圍。

| 功能 | 狀態與範圍 | 尚缺 |
| --- | --- | --- |
| A 會員 | 已實作；先前建構、單元及裝置流程驗證通過 | 使用者人工驗收；相關程式仍未提交 |
| B 首頁 | 開發中；規劃入口及登出已接 | 我的行程、個人中心等 |
| C 出遊條件 | 部分已實作、建構與單元測試通過；先前部分裝置案例分批通過 | 任意出發地搜尋、整體驗收 |
| D 景點 | 本機推薦、搜尋及勾選已實作，建構／單元通過；舊畫面案例分批通過 | 線上搜尋及完整驗收 |
| E 路線與地圖 | 排序、估算、按需 OSM 底圖已實作；建構／相關單元測試通過 | 路線裝置驗證、休息建議；不代表道路導航 |
| F 行程詳情與外部地圖 | 單點外部地圖與天氣查詢畫面已實作、建構及單元測試通過 | 外部地圖與天氣裝置驗證、抵達時間預報、休息點、行程詳情及儲存 |
| G 我的行程 | 開發中：快照及帳號範圍操作介面已實作，建構／記憶體替身單元測試通過 | Room 實作、與登入 session 綁定、畫面、程序重啟及裝置驗證 |
| H 個人中心 | 開發中；首頁已有帳號顯示及登出 | 其他企畫書項目 |
| 課程元件 | 尚未開始 | 依實際功能接入並驗證 |

## 4. 建構及測試結果

- 2026-11-15：Wrapper CLI `--no-daemon --max-workers=1 :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest`：`BUILD SUCCESSFUL in 38s`，24 項單元測試本次執行、零 failures/errors/skipped；Debug APK 8,965,727 bytes。完整本機 JBR 與 Wrapper 指令見 AGENTS.md，需在參數後加入上述 task。
- 2026-11-15：同一 Wrapper CLI、同樣資源參數，加 `-PisolatedQa=true :app:assembleQa :app:assembleQaAndroidTest`：`BUILD SUCCESSFUL in 34s`；這是編譯，不是裝置測試。
- 2026-11-15：同一 Wrapper CLI、同樣資源參數，加 `-PisolatedQa=true :app:assembleQaAndroidTest`：`BUILD SUCCESSFUL in 35s`，4 tasks 執行；QA session 防護已編譯，未執行裝置測試。此階段只改裝置測試，不重跑不受影響的單元測試。
- 2026-11-15：相同 `:app:assembleQaAndroidTest` 再執行以驗證共用 QA 前提，`BUILD SUCCESSFUL in 41s`，4 tasks 執行；僅編譯，裝置測試仍待執行。
- 2026-11-15：Wrapper CLI 同上，使用 `--no-daemon --max-workers=1 '-Pkotlin.compiler.execution.strategy=in-process' :app:assembleDebug :app:testDebugUnitTest`：`BUILD SUCCESSFUL in 52s`，26 項單元測試、0 failures/errors/skipped；Debug APK 8,965,727 bytes，22:13:56。第一次未引用含點號的 PowerShell 參數，Gradle 誤認為 task、未進編譯；修正引號後通過。
- 2026-11-15：相同資源參數執行 `:app:assembleDebug :app:testDebugUnitTest`，`BUILD SUCCESSFUL in 2m 6s`，41 tasks 執行；29 項單元測試、零 failures/errors/skipped，Debug APK 7,713,555 bytes（22:24:04）。天氣資料層含解析測試；網路與 UI 尚未驗證。
- 2026-11-15：同一 Wrapper CLI 使用 `--no-daemon --max-workers=1 '-Pkotlin.compiler.execution.strategy=in-process' :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest`，`BUILD SUCCESSFUL in 1m 19s`，71 tasks 執行；30 項單元測試零 failures/errors/skipped，測試 APK 僅編譯。之後加入來源及授權連結，以相同資源參數執行 `:app:assembleDebug :app:testDebugUnitTest`，`BUILD SUCCESSFUL in 16s`；單元測試 up-to-date，Debug APK 9,105,991 bytes（22:32:24）。
- 2026-11-15：相同 Wrapper CLI 資源參數執行 `:app:assembleDebug :app:testDebugUnitTest` 驗證抵達時間元件與邊界修正，`BUILD SUCCESSFUL in 36s`，9 tasks 執行；33 項單元測試、零 failures/errors/skipped，Debug APK 9,105,991 bytes（22:41:28）。這是純計算元件，尚未執行裝置測試。
- 2026-11-15：相同 Wrapper CLI、`--no-daemon --max-workers=1 '-Pkotlin.compiler.execution.strategy=in-process' :app:assembleDebug :app:testDebugUnitTest`，`BUILD SUCCESSFUL in 39s`，9 tasks 執行；36 項單元測試、零 failures/errors/skipped，Debug APK 9,105,991 bytes（22:48:30）。來源與缺值模型為純計算程式，未執行裝置測試。
- 2026-11-15：相同 Wrapper CLI 資源參數及 `:app:assembleDebug :app:testDebugUnitTest` 驗證預報時間缺口邊界，`BUILD SUCCESSFUL in 37s`，9 tasks 執行；37 項單元測試、零 failures/errors/skipped，Debug APK 9,105,991 bytes（22:51:00）。未執行裝置測試。
- 2026-11-15：相同 Wrapper CLI、`--no-daemon --max-workers=1 '-Pkotlin.compiler.execution.strategy=in-process' :app:assembleDebug :app:testDebugUnitTest`，`BUILD SUCCESSFUL in 38s`，9 tasks 執行；41 項單元測試、零 failures/errors/skipped，Debug APK 9,105,991 bytes（22:58:34）。行程保存使用記憶體替身測試，Room 與裝置未驗證。
- 使用公開淺水灣景點座標向 Open-Meteo 執行一次 HTTPS 查詢，回傳 Asia/Taipei 和各 48 筆逐時欄位；這是服務回應檢查，不等於 Android 裝置網路驗證。服務免費方案限非商業用途與流量額度，見 [使用條款](https://open-meteo.com/en/terms)及[授權](https://open-meteo.com/en/licence)。
- 先前 23 項通過是新增 ExternalMapTest 前的結果；較新的實際執行共 24 項。歷史會員／規劃裝置結果及中斷紀錄見歷史檔。
- 路線、外部地圖按鈕、QA 隔離與最新測試 APK：**待裝置驗證**。未將使用者人工操作標為通過。

## 5. 已知問題與風險

- Pixel 8 曾顯示 ADB offline；不反覆啟動或修復，集中測試時再安全處理。不得清除、重設、覆蓋資料或登出現有帳號。
- 規劃條件與勾選目前僅為 Activity 暫存，冷啟動或離開流程無法保證草稿保留。
- QA 裝置測試須先確認裝置、套件及獨立 QA session；RouteFlowTest 和 PlanningFlowTest 已共用 QA package 與帳號查核，但該防護尚待裝置驗證。
- 天氣流程及 UI 尚待裝置驗證，包括無網路、部分資料、取消／重建、授權連結與重試；單機間隔及短暫快取不能保證跨裝置遵守服務流量上限。現行預報不是抵達時間預報。
- 天氣資料層的 Gson 依賴已使用獨立 index 安全提交；天氣畫面控制器與資源依賴先前未提交的規劃、路線畫面，仍保留未提交，避免混入歷史工作。
- 個別與整趟估算使用直線距離假設；本機三筆景點並非線上搜尋。線上來源及政策前提見 [資料來源](PLANNING_DATA_SOURCES.md)。
- 抵達時間資料不足：目前沒有使用者出發日期／時刻，交通只有「直線距離 × 1.5、25 km/h」的整趟粗略估算，三個景點的 60 分鐘停留是系統預設，並非使用者輸入或道路服務結果。現階段不可把它們宣稱為可靠抵達時間。
- 會員 `accounts.db` 目前為 Room version 1；行程儲存尚無持久化實作。正式資料庫方案須先設計非破壞性遷移或資料隔離，並確認帳號範圍及備份一致性；不得用 destructive migration 或清除資料避開問題。
- 2026-11-15 靜態資料流檢查：條件 → 本機推薦／勾選 → `RoutePlanner` 距離排序 → 行程清單／地圖／天氣選點已接；條件變更會移除不再符合的勾選。排序仍只從 `PlanningCatalog.attractions` 解析 ID，未來線上搜尋結果不能直接沿用，否則會被當作不存在；行程保存快照尚未接收此路線，行程詳情與「我的行程」尚未完成。這是原始碼檢查，非本次裝置測試。
- Room 方案待使用者決定：①沿用 `accounts.db`，以明確遷移保留會員，會員與行程可共用交易及既有備份排除規則；②獨立資料庫避免改動會員 schema，但需處理帳號 ID 對應、資料庫之間無外鍵及備份／還原一致性。兩者皆需帳號隔離、遷移／重啟測試，不能以破壞性遷移代替。
- 待後續產品決策：可由使用者逐段輸入（控制度高但操作較多）、使用清楚標示的現有粗略估算及預設（容易使用但誤差可能大），或接入符合零付費及使用條件的道路服務（可能受額度、可用性及機車路線支援限制）。需確認選擇與 UI 呈現後才接入抵達時間預報；此階段不擅自選用。

## 6. 尚未提交的變更

依 2026-11-15 的 `git status`：會員、首頁、規劃、路線／地圖、天氣畫面、資源、Gradle／QA 設定及裝置測試均有既有未提交修改與新增檔案；`.vscode/` 亦為既有未追蹤內容。不要整批提交或覆寫；每次重新檢查狀態。專案外 `../HW.zip` 不得加入。

## 7. 下一個優先開發功能

等待會員與行程 Room 方案的選擇後，設計並驗證非破壞性持久化、帳號範圍與備份；不自行清除或重建資料。靜態資料流檢查已完成，線上景點、保存快照接線及行程詳情仍未完成。暫停擴充抵達時間模型及其天氣整合；規劃、路線、天氣與行程儲存列入下一次集中裝置驗證。

## 8. 後續待辦事項

1. 完成需求內其餘功能；避免把假資料或示意畫面當成真實資料。
2. 累積相關功能後集中驗證路線、外部地圖與 QA 版本；先確定裝置／套件及資料隔離。
3. 完成帳號範圍內的行程保存、重新查看、刪除與程序重啟驗證，再做整合及展示流程驗收。
4. 每個功能或重要階段更新此檔，記錄日期、實際指令、結果、風險及下一步；詳細舊證據留在歷史檔。
