# 《今天去哪？》開發進度

## 2026-11-15 日常無模擬器開發

- 最新工作方式：日常不啟動 Android Studio GUI 或模擬器；依功能需要以 `--no-daemon --max-workers=1` 分階段建構與單元測試，必要時集中裝置測試。高記憶體工作前檢查可用記憶體至少 2 GB，且不得並行 Gradle 建構。此規則取代下方先處理模擬器離線再開發的舊下一步。
- 路線 `RouteFlowTest` 仍為**待裝置驗證**；目前不反覆啟動或修復模擬器。保留既有 QA build type，不修改既有模擬器資料及帳號。
- QA 測試相容性：修改 ExampleInstrumentedTest，以 instrumentation APK 的 package 與目標 App package 的關係驗證，避免硬編碼正式 applicationId 導致 QA 版本必然失敗。一般 Debug 與 QA 測試 APK 分別建構成功；此測試仍待裝置執行，不代表路線或 QA 隔離已通過裝置驗收。
- 發現 VS Code 啟動的 Gradle daemon 處理本專案工作時先等待；確認日誌標記 idle 後才依序啟動自己的建構，沒有終止該程序。
- 外部地圖入口：**已實作**各景點開啟外部地圖、無可用 App 提示；導航由外部 App 設定，並非應用程式自行導航或多點路線。**建構通過**：CLI Debug 與測試 APK `BUILD SUCCESSFUL in 38s`，18 tasks 執行；新 Debug APK 8,965,727 bytes，22:01:30。**單元測試通過**：本次 24 項、0 failures/errors/skipped，新增座標 URI 格式測試含不同語系及南半球座標。**待裝置驗證**：按鈕顯示、各景點目標、無地圖 App 提示、返回及 Activity 重建；路線原有裝置驗證亦待完成。
- 介面串接在原先已存在的未提交 PlanningActivity、版面及字串檔中，不將整檔誤認為本次新作；保留全部既有修改。ExampleInstrumentedTest 的 QA 斷言同樣尚待裝置驗證。
- 僅將已通過單元測試且不依賴未提交介面的 `ExternalMap.kt`、`ExternalMapTest.kt` 建立本機 commit `eb30a9b`；畫面串接及 QA 測試修改保留未提交、待裝置驗證，沒有 push。
- 已在既有 `RouteFlowTest` 加入景點地圖按鈕數量、名稱與 Activity 重建後數量的斷言；隔離 QA App 與測試 APK 再次編譯成功（`BUILD SUCCESSFUL in 34s`，18 tasks 執行），這只證明編譯，不代表裝置測試已執行。

## 2026-11-15 低記憶體恢復及隔離 QA 準備

- 使用者要求後續不自行啟動 Android Studio GUI；以 Wrapper 命令列、單一 worker、分階段建構與單一既有 CLI 模擬器驗證。每次啟動高記憶體工作前確認可用 RAM 至少 2 GB；不清除 App 資料、登出或重設模擬器。
- 檢查：Wrapper 9.5.0、JBR 25.0.2、SDK adb 和 emulator CLI 可用；既有 Pixel_8 設為 2048 MB RAM、4 CPU。CIM 查詢遭拒，但 Windows Memory 計數器可讀取，啟動建構前可用約 6472 MB。
- 使用既有 JBR 與 wrapper jar、`--no-daemon --max-workers=1`，在可存取既有 Gradle 快取的環境中執行 `:app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest`：`BUILD SUCCESSFUL in 39s`，5 executed；單元測試本次執行，XML 共 23 項、0 failures/errors/skipped。Debug APK 7,600,331 bytes，時間 2026-11-15 21:47:20；測試 APK 僅組裝，未執行裝置測試。
- `app/build.gradle.kts` 增加以 debug 為基礎的 qa build type，applicationId suffix 為 `.qa`；僅 `-PisolatedQa=true` 時將 instrumentation target 設為 qa。命令列 `:app:assembleQa :app:assembleQaAndroidTest` 顯示 `BUILD SUCCESSFUL in 1m 14s`，66 tasks 執行；輸出 metadata 確認 package 為 `com.example.wheretogo.qa` 與 `com.example.wheretogo.qa.test`。此設定尚未完成裝置驗證，不視為驗收完成；該檔亦含任務前既有未提交變更，未提交。
- 測試相容性：RouteFlowTest 與 PlanningFlowTest 讀取 targetContext 的 session，獨立 QA 需要自己的帳號和 session；不得把正數 ID 當成已驗證有效帳號。ExampleInstrumentedTest 硬編碼正式 package，不能直接宣稱整套 QA instrumentation 相容；須修正測試或明確只執行路線測試並分開驗證。
- 建構結束後確認約 6615 MB 可用、無模擬器；以 Emulator CLI 啟動現有 Pixel_8（`-no-snapshot-save`，本次 launcher PID 22644、qemu PID 5540）。ADB 顯示 emulator-5554 offline，重查仍 offline；qemu 存在但 CPU 增長很少。未安裝 APK、未建立 QA 帳號、未執行 RouteFlowTest，未重試啟動、清除、登出或重設。模擬器可能含未儲存狀態，未自行關閉。
- 下一步：先釐清模擬器 offline 原因且保留資料，再核對裝置上 QA package 是否已存在；僅在確認隔離範圍後安裝並建立獨立 QA session，執行路線兩案例，核對 passed/failed/skipped。未提交或 push。

## 2026-11-15 安全恢復與建構檢查（21:34）

- 已讀取規則與進度、檢查 Git 狀態及近期歷史；HEAD 為 `f4b4dc8`，既有會員、規劃與路線變更仍未提交，全部保留；未修改功能程式或建立 commit。
- `git diff --check` 通過。程式已有路線排序、限制檢查和按需地圖，但路線裝置驗證及線上搜尋、行程儲存等仍未完成，不視為完整功能。
- Gradle wrapper 為 9.5.0，daemon criteria 為 JetBrains Java 25；已確認 Android Studio JBR 的 java、javac、jlink 及 local.properties 所指 SDK 的 adb 存在。程序詳細命令列的 CIM 查詢遭拒；Android Studio 顯示先前建構已結束，未觀察到並行建構。
- 本次在 Android Studio 執行 `:app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest`，21:34 顯示 `BUILD SUCCESSFUL in 1s`，71 項均 up-to-date；Debug APK 存在（8,894,607 bytes，2026-11-14 22:02:04），非本次新產物。既有單元測試 XML 共 23 項、零失敗及略過；因工作均 up-to-date，不能稱為本次重新執行測試。測試 APK 僅完成組裝。
- 啟動既有 Pixel 8 模擬器，發現目前登入帳號並非前次記錄的 QA 帳號；保留現有 session，未執行要求 QA session 的 `RouteFlowTest`，也未清除資料、登出或刪除帳號。裝置測試及人工操作驗收尚未通過。
- 下一步：由使用者將模擬器切換至先前保留的 QA 帳號後，再驗證路線流程；通過後才能可靠地在其上繼續下一階段。未 push。

## 2026-11-15 接續檢查

- 工作目錄已有未提交的會員、規劃與地圖程式，均予保留。既有程式已包含路線排序、估算及按需載入 OSM 底圖，完整 E 階段仍未完成。
- 本次 Android Studio 執行 `:app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest`，顯示 `BUILD SUCCESSFUL in 52s`，71 項均 up-to-date，APK 存在。先前單元測試 XML 共 23 項、零失敗與略過，為 2026-11-14 產物，並非本次重新執行。
- 本次未執行裝置測試或人工操作；線上搜尋、休息點、行程詳情與 Room 儲存等仍未完成。下一步應啟動既有模擬器並安全確認 QA session，驗證路線流程後再繼續開發。
- 本次未修改功能程式、建立 commit 或 push。

更新日期：2026-11-14（Asia/Taipei）

## 目前階段

登入／註冊閃退修正已通過 Android 編譯、單元測試與裝置迴歸測試。會員完整流程已通過前階段模擬器自動操作驗收。現已接上 B 首頁規劃入口、C 常用出發地／時間／預算／偏好畫面，以及 D 本機推薦、搜尋與勾選；這些是分階段實作，並非 B～D 全部功能完成。線上搜尋、地圖、行程儲存與其他功能仍未完成，使用者本次人工驗收尚未回報。

## 本次完成

- 已重新閱讀根目錄 AGENTS.md、目前程式、Git 狀態，以及正式企畫書全文（12 頁）與第 5～12 頁 A～H 畫面。
- AGENTS.md 已整合新的自主開發、條件式 commit、禁止 push、驗證與進度保存授權。
- `MainActivity.configureForm` 的 submit 按鈕位於 `MaterialButton.apply` 接收者範圍內。原本三個未限定的 `findViewById` 會從按鈕查找旁邊的輸入框，回傳 null，造成點擊時 NPE。三行現已明確使用 `this@MainActivity.findViewById`；登入與註冊共用此修正。
- 已檢查 main 原始碼內其他 findViewById 與接收者範圍，未發現相同問題；未新增 nullable View 容錯或 catch。
- 新增 `AuthFormClickTest.kt`：登入及註冊各有一個空白表單點擊迴歸案例，直接執行真實 listener，驗證可到達帳號錯誤訊息。測試不建立帳號、不清除資料、不登出既有 session；已有登入狀態時明確略過。
- 新增 `docs/Verify-AuthLogic.ps1`：使用既有本機依賴執行兩個純 JVM 測試類別，不啟動 Gradle、不搬動或修改其快取；測試產物寫入系統暫存目錄。預設依賴版本與 JDK 路徑僅對應此電腦，缺少依賴時會明確失敗。

## 本次驗證證據

| 類別 | 結果 | 範圍與限制 |
| --- | --- | --- |
| 靜態檢查 | 檢查通過 | 三個 Activity 接收者、表單及 session 所需 View ID；15 個 main XML 均可解析；MainActivity 的 git diff --check 通過 |
| 測試腳本 | 檢查通過 | PowerShell AST 無語法錯誤，且完整執行腳本成功，exit code 0 |
| 純 JVM 單元測試 | 本次執行通過 | Kotlin 2.2.10、JDK 25.0.2、JUnit 4.13.2；AccountRulesTest + PasswordHasherTest：`OK (4 tests)`，腳本執行顯示 Time: 6.594 秒 |
| Android 編譯／APK | 本次 IDE 驗證通過 | 20:20 執行 assembleDebug：BUILD SUCCESSFUL in 37s；20:22 Run 再次建構成功並保留資料更新安裝；APK 存在，7,525,796 bytes |
| Gradle 單元測試／測試 APK | 本次 IDE 驗證通過 | 20:27 執行 testDebugUnitTest + assembleDebugAndroidTest：BUILD SUCCESSFUL in 33s；5 tests passed；三個 JUnit XML 的 timestamp 均為 2026-11-14T12:27:23Z，failures/errors 均 0；測試 APK 已產生 |
| 裝置測試 | 本次執行通過 | 既有 Pixel 8 API 35，emulator-5554；更新測試 APK 後，以 AndroidJUnitRunner 執行 AuthFormClickTest（2）+ AccountDatabaseTest（2）：OK (4 tests)，Time: 59.928，全部 status code 0，無 skipped；Room 使用 in-memory DB |
| 模擬器自動操作 | 本次流程驗證通過 | 同一 qa_964ce922bddc：空白登入／註冊、確認密碼不一致、新建帳號、大小寫重複帳號、錯誤密碼、正確登入、程序重啟保留登入、登出後重啟未登入、再登入原帳號均通過；最後為未登入，帳號保留 |
| 使用者人工操作 | 尚未回報 | 不把 Codex 的自動操作當作使用者本次人工驗收 |

### 出遊條件模型階段（同日 20:39）

- `:app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest` 已透過 IDE 完成：`BUILD SUCCESSFUL in 16s`，71 actionable tasks（11 executed、60 up-to-date）。
- 本次 10 項 Gradle 單元測試通過：新增 TripConditionsTest 5 項＋既有 5 項。四份 XML timestamp 為 2026-11-14T12:39:11Z，failures/errors 均 0。
- 新 APK：`app/build/outputs/apk/debug/app-debug.apk`，7,530,387 bytes，檔案時間 20:39:10，SHA256 `7F2E20D63B0B8E3033B211A8BB2E52AEA6C40F1B574E89A2693AC0D24DF3BEDF`。
- 這次新增程式是未接入畫面的純資料與輸入驗證模型，因此以單元測試確認行為；不宣稱 B 首頁或 C 條件畫面已完成。沒有因此重跑與模型無關的整段帳號操作，也未額外建立第二個 QA 帳號。
- 已獨立 commit：`f8dcbbc`（Add validated trip conditions model and unit tests），只有 `TripConditions.kt`、`TripConditionsTest.kt`，共 95 行。沒有 push。

### 首頁與條件設定流程（同日 21:14 起）

實作範圍：

- MainActivity 的 session 畫面改為首頁規劃入口，保留帳號顯示與原有登出功能；未新增不可用的「我的行程」或個人中心入口。
- PlanningActivity 為 non-exported，且需現有本機登入 session；從首頁進入。提供四個常用起點、2／4／8 小時、300／500／1000 元或自訂整數預算、五種可複選偏好。缺少出發地與無效自訂預算會留在條件頁並顯示錯誤。
- 下一步提供三筆經官方來源整理的本機景點，依起點、時間、基本入場預算與任一偏好篩選；可用名稱／地區搜尋、勾選及取消，沒有符合時顯示空結果。每張卡片可開啟官方資訊頁。
- 返回條件頁與 Activity 重建會保留條件、關鍵字、勾選及所處頁面；這是 saved-instance state，不宣稱已做 Room 草稿保存或 App 冷啟動草稿恢復。
- 已處理重複動態 checkbox ID 的自動狀態覆寫問題；勾選由 selectedIds 與 Bundle 管理。視覺檢查發現的重複出發地 hint 已移除，選取色改用既有藍色系。
- 細節來源、座標與估算假設見 `PLANNING_DATA_SOURCES.md`。沒有新增網路權限、費用、定位權限或資料庫 schema 變更。

驗證紀錄：

- 首輪：測試中誤用 AutoCompleteTextView 不支援的 performItemClick，以及 2 小時案例漏算往返造成預期不符；已修正測試，未扭曲產品估算來迎合案例。
- 21:16：IDE `assembleDebug + testDebugUnitTest + assembleDebugAndroidTest` 顯示 `BUILD SUCCESSFUL in 8s`。14 項單元測試通過；更新安裝兩個 APK 成功。
- 會員閃退迴歸：AuthFormClickTest `OK (2 tests)`，Time 8.791，無 skipped。以同一 qa_964ce922bddc 登入更新後首頁成功，沒有建立新帳號。
- 第一輪 PlanningFlowTest：重建／搜尋／保留案例通過後，裝置發生 system_server 原生 SIGSEGV（裝置 logcat 13:18:32）；instrumentation 顯示 System has crashed。未把此輪標為全部通過。
- 系統恢復後，補跑當時尚未完成的首頁來回與預算驗證：`OK (2 tests)`，Time 13.047；這是補測證據，非最終四案例整輪通過。
- 21:23：視覺修正與實際下拉選取測試加入後，IDE 顯示 `BUILD SUCCESSFUL in 12s`，71 tasks（24 executed、47 up-to-date）。14 項單元 XML timestamp 為 2026-11-14T13:23:28Z，零 failures/errors；APK 8,883,679 bytes（21:23:28），測試 APK 607,998 bytes（21:23:30）。
- 這輪 PlanningFlowTest 四項中：重建／搜尋／返回與預算驗證通過，首頁來回與實際下拉案例因 Espresso RootViewWithoutFocusException 失敗（Time 39.851）。裝置同時有其他系統服務 ANR／記憶體壓力紀錄。加入有時限的 resumed Activity 視窗焦點等待後，兩項仍因拿不到視窗焦點失敗（Time 45.302），沒有略過斷言或修改產品邏輯掩蓋。
- 在 IDE Device Manager 停止並 Cold Boot 同一個 Pixel_8，沒有使用 Wipe Data、刪除 AVD 或建立新裝置。冷啟動後 UI 與 dumpsys window 證實 QA session 還在且 MainActivity 取得焦點。
- 冷啟動後補跑：首頁來回通過，實際下拉測試暴露 Espresso 將 AdapterView 搜尋限定在主視窗的選擇器問題。修正為 inRoot(isPlatformPopup())，只重跑受影響案例後 `OK (1 test)`、Time 2.963、status code 0。完整紀錄保留於本機 Temp 的 `wheretogo-dropdown-test-4ce88c5fa87842c08ad88e0ef14086f2.log`。
- 最新 PlanningFlowTest 四個案例均有分批通過證據，沒有 skipped：首頁來回、必填與自訂預算、重建／搜尋／勾選與返回、實際下拉選取。沒有宣稱最後曾完整四項同一輪全過；中斷與失敗紀錄仍保留。
- 視覺修正後正式 App APK SHA256：`9BD28CD5355EDA16E108F17833761B566ACDD03A36CEE805AAFDC509B2E5333F`。其後只有測試焦點與 Popup 定位修改，沒有更改產品程式。

- 最後實際操作：從四個選項的下拉清單點選淡水捷運站，選擇半天／500 元，進入推薦頁，勾選卡片後顯示「已選 1 個景點」，返回條件仍保留起點，再返回首頁並登出；程序重啟後仍顯示登入頁，命令 exit 0。
- 最終畫面檢查：出發地 hint 不重疊、藍色系選取、卡片名稱／地址／距離／費用文字可讀；頁面可垂直捲動到其餘控制項。條件與推薦截圖位於系統 Temp：`wheretogo-final-conditions.png`、`wheretogo-final-spots.png`，沒有把截圖或帳號資料加入 Git。
- 最後 MainActivity.kt SHA256：`E5A382158683D7AE2C487D1CDBCC2965778C0CFA6525A8B0CD57099898A879B6`。QA 帳號 qa_964ce922bddc 保留，App 最終為未登入，沒有新建第二個帳號。

最新狀態：常用起點到本機推薦的實作與本次範圍驗證已完成，14 項單元測試通過，4 個規劃裝置案例分批通過，模擬器操作與視覺檢查完成。這不代表 B／C／D 的完整企畫功能或使用者人工驗收已完成。

腳本重跑方式（專案根目錄）：

```powershell
& .\docs\Verify-AuthLogic.ps1
```

本次腳本產物：`C:\Users\hungg\AppData\Local\Temp\wheretogo-auth-tests-2473fe3047784ca5b9607af8c71136b7.jar`。

閃退修正階段（尚未接首頁時）的 MainActivity.kt SHA256：`7291915420C68AE70AC9D17B1DC3AF62E2A0D89C0565E4896FC13651DAAF83BD`；後續首頁串接已再修改該檔，此值不是最新檔案雜湊。

## 驗收流程與備用手動建構

終端機沙箱限制未變；本次已改用 IDE 的建構介面完成驗證，不需使用者重跑同一建構。以下保留為後續一般 PowerShell 的備用命令：

```powershell
& 'D:\program files\Android Studio\jbr\bin\java.exe' `
  '-Dorg.gradle.java.installations.paths=D:\program files\Android Studio\jbr' `
  '-Dorg.gradle.java.installations.auto-detect=false' `
  '-Dorg.gradle.java.installations.auto-download=false' `
  -jar '.\gradle\wrapper\gradle-wrapper.jar' `
  --no-daemon :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest

if ($LASTEXITCODE -eq 0) {
  Get-Item '.\app\build\outputs\apk\debug\app-debug.apk'
}
```

需保留本次指令成功結束、BUILD SUCCESSFUL 與 APK 存在的輸出。使用 Android Studio 更新執行 APK，保留相同 applicationId、簽章及 App 資料；不要解除安裝、清除儲存空間或執行 pm clear。安裝衝突時回報，不以刪除 App 解決。

在模擬器或裝置上，選擇未使用的測試帳號（4～20 個 ASCII 英數字或底線），執行：

1. 若已有登入狀態，先確認該帳號顯示正常，重開確認保留，再正常按登出。
2. 登入頁空白提交：顯示帳號錯誤、不閃退。
3. 切換註冊頁空白提交：顯示帳號錯誤、不閃退。
4. 有效新帳號＋8～64 字元密碼；確認密碼不一致時顯示錯誤；兩次一致時註冊成功並回登入頁。
5. 再用相同帳號註冊：顯示重複帳號，原帳號與密碼仍可使用。
6. 錯誤密碼登入：顯示錯誤；正確密碼登入：顯示登入帳號。
7. 關閉並重開 App（必要時強制停止再啟動，保留資料）：仍為相同帳號登入。
8. 按登出後關閉重開：維持未登入；原帳號仍可登入。

完成後在未登入狀態，以 Android Studio 執行 `AuthFormClickTest` 與 `AccountDatabaseTest`，分別回報 passed／failed／skipped。後者使用 in-memory Room，不清除正式 accounts.db。裝置測試成功仍不能替代第 7～8 項的程序重啟驗收。

## 需求對照與後續順序

正式需求來源：本目錄 `資管二丙 第七組 APP程設專題企畫書.pdf`。UI 圖內的景點、天氣、行程名稱均屬示意，不能當作已完成或真實查詢結果。

| 階段 | 已確認方向 | 目前狀態 |
| --- | --- | --- |
| A 會員 | 註冊、登入、登出；既有實作另有登入狀態保留 | 本次建構／單元／裝置／模擬器流程驗收通過；使用者人工驗收尚未回報 |
| B 首頁 | 開始規劃、我的行程等入口；與 A～H 導覽一致 | 已接規劃入口、帳號與登出；我的行程／個人中心等入口未完成，尚非完整 B |
| C 出遊條件 | 出發地；2 小時／半天／一天；預算選項與自訂；可複選偏好 | 模型已 commit；畫面、驗證、返回／重建保存已實作；出發地目前限四個常用地點，任意線上地點查詢未完成 |
| D 景點 | 推薦與線上搜尋、名稱／地址／座標、勾選景點 | 已接三筆本機推薦、名稱／地區搜尋、勾選與空結果；線上搜尋與下一步行程安排未完成 |
| E 地圖與規劃 | 依地理距離建議順序；地圖標記；休息建議由使用者選擇加入 | 未實作；不得將直線距離宣稱實際道路導航 |
| F 行程詳情 | 景點、天氣、行程與休息點；可儲存；實際導航交外部地圖 | 未實作 |
| G 我的行程 | Room 儲存、重新查看及刪除行程 | 未實作 |
| H 個人中心 | 會員資訊、我的行程、意見回饋、App 資訊及登出 | 未實作 |
| 課程元件 | 短時間主動觸發 Service、內部事件 BroadcastReceiver、受控 ContentProvider；主要操作與頁面停留紀錄 | 未實作，依實際功能接入，不製造空殼完成標記 |

技術維持 Kotlin／XML／Room；企畫書指定 OSM、Nominatim、視需要 Overpass、Open-Meteo。接入前查核當時官方使用規範，維持零付費、不綁信用卡，不新增未要求的功能。

## Git 與資料保留

- 本次開始前已有多項 tracked 修改及 untracked auth 程式、測試、資源與企畫書。基底 HEAD 為 `8a60700`；上層另有 `HW.zip`，未讀寫或加入。
- 會員修正雖已通過本次驗證，但 MainActivity 的完整 diff 包含使用者既有未提交的會員實作，不能整檔當作本次三行修正提交。因此會員相關工作仍保留未提交；沒有將既有檔案或 untracked auth 資料整批加入索引。
- 新增的出遊條件資料模型與其測試已獨立提交為 `f8dcbbc`，只加入兩個明確檔案；本次所有操作均未 push 或重寫歷史。
- 本機推薦模型、4 項單元測試及來源文件已獨立提交為 `f4b4dc8`，只有 PlanningCatalog.kt、PlanningCatalogTest.kt、PLANNING_DATA_SOURCES.md，共 129 行。首頁串接涉及仍未提交的既有會員基底，因此介面、Manifest 及規劃裝置測試保留未提交，避免混入使用者原有工作。
- 本次新增的進度文件、兩份驗證腳本與裝置迴歸測試，以及 AGENTS.md 授權更新均保留在工作目錄。未清除 App 資料、未變更資料庫 schema、未重建專案。

## 阻礙與下一步

1. 已透過既有 IDE 與啟動原有 AVD 解決本次驗證途徑；未變更 Gradle 權限、未移動快取、未 wipe AVD、未清除 App 資料。
2. 自動審核最初拒絕建立並保留 QA 帳號，使用者隨後明確答覆「允許建立並保留測試帳號」，阻礙已解除。`docs/Verify-AuthDevice.ps1` 不讀取既有密碼、不刪除帳號，若遇既有登入 session 會停止而不登出。首次執行最後的再次登入遇到前景不再是 App 而中止（exit 1），所以沒有將整段腳本宣稱通過；已補上新 dump 成功與 package 檢查，防止使用舊 XML 或點擊其他 App。
3. 隨後以同一 QA 帳號補驗再次登入，並用新 XML 分別斷言程序重啟後 session_account、登出後 form_title，以及再重啟後沒有 logout 按鈕，全數 exit 0。最後未登入；未刪除 QA 帳號。新版完整腳本尚未重新整段執行，不額外產生第二個帳號。
4. B／C 共用條件模型已驗證：AvailableTime=120／240／480 分鐘（含交通），Departure 必須有有效名稱與座標，五種偏好可複選或空集合。預算以整數新臺幣處理，允許 0 表示免費出遊；空白、負值、小數與溢位輸入不可默默套預設值。
5. 本階段四個規劃裝置案例已分批通過，模型與測試提交範圍已確認，畫面證據已整理，QA 帳號已登出。下一階段處理 D 任意出發地／線上景點搜尋與 E 行程安排，再將條件、景點及行程接入 Room。Nominatim 政策已閱讀，相關條件見 PLANNING_DATA_SOURCES.md；不能用單機節流宣稱已滿足全 App 流量限制，也不把本機資料當線上結果。
6. 最後逐項整合網路失敗／無結果、行程儲存與帳號範圍、程序重啟，以及完整展示流程。此項尚未進行。

本回合結束後不會自動背景繼續開發。
