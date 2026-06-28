# Ivy Wallet 个人维护分支

这个仓库是 Ivy Wallet 的个人维护分支，只面向本机编译、个人使用和个性化改造。当前目标不是重写应用，而是在不影响现有记账功能的前提下，把项目整理成更适合单人长期维护的 Android 项目。

本分支不再面向上游协作、社区运营、应用商店发布或多人开发流程。后续修改优先服务三个目标：

- 保留官方已经写好的实际功能。
- 删除或收窄与个人使用无关的发布、协作、推广、测试展示和历史兼容噪音。
- 让模块职责、依赖方向和公开 API 更清楚，减少理解成本。

## 当前状态

已经完成的清理和重构可以概括为以下几类。

### 项目用途收敛

- 删除 GitHub workflow、Issue/PR 模板、贡献规范、社区文档、Fastlane、发布日志、开源展示、贡献者、开源致谢、投票问卷、推广入口、Google Play 评分、Telegram 等与个人维护无关的内容。
- 删除 Google Services、Crashlytics、Firebase Firestore、Google Play Review、GitHub 自动备份迁移残留等发布或云端接线。
- 删除 contributors、releases、attributions、poll、disclaimer、onboarding、widget、第三方 App 导入模板和教程等不再需要的功能入口。
- 保留本地 zip 备份/恢复、手动 CSV 导入/导出、通知、应用锁、汇率、预算、借贷、计划付款、报表等实际使用功能。

### 设置页和 UI 行为

- 设置页已按个人使用重新整理：数据管理、记账规则、系统行为、外观与显示、输入与列表等分组更清楚。
- 原“高级特性”整理为偏好设置，不再按上游产品运营语义组织。
- 删除匿名账户入口、首页问候语、分享 Ivy、评分、推广、云端删除等无实际个人维护价值的入口。
- 二级设置页的滚动状态已经隔离，进入/退出二级菜单不会污染一级设置页滚动位置。

### 构建和本地开发

- 项目已改为适合 VS Code + Gradle + 本地 Android SDK 的开发方式，不依赖 Android Studio。
- release/demo 构建不再依赖外部 keystore 或发布环境变量，当前都使用本地 debug 签名。
- `.gitignore`、`gradle.properties`、lint 配置、Compose compiler metrics/reports、Gradle 仓库配置都已精简到当前项目实际需要的范围。
- JitPack、Timber 运行时依赖、Ktor HTTP body 日志、Firebase/Google 相关构建接线已经删除。

### 旧模块和历史兼容层

- `:temp:*` 模块已经删除。
- `:shared:ui:legacy` 模块已经删除。
- 原旧 UI 组件、弹窗、交易列表、金额展示、周期选择、标签弹窗、账户/分类弹窗、重排弹窗等仍有价值的实现已迁入 `shared:ui:core` 或各 feature 私有边界。
- `RootActivity` 已拆出平台能力：文件选择、文件分享、Material 日期选择器、生物识别、窗口安全、BuildInfo、Locale 设置等保留在 app 壳层或明确的平台适配器中。
- `RootScreen` 大接口已删除，feature 改为依赖更窄的 UI platform 接口或页面入口回调。

### 模块边界

- `shared:ui:navigation` 继续聚焦 route、页面栈、返回栈和 screen scoped ViewModel。
- `shared:ui:navigation` 的返回栈清理继续简化为直接清空内部栈，减少只服务实现细节的私有跳转 helper。
- `shared:ui:core` 承接真正跨页面复用的 UI 基础能力：主题 root 所需状态、时间服务、弹窗、金额展示、交易列表、按钮、图标、搜索框、周期选择、标签入口等。
- `shared:ui:core` 的主题渐变 API 继续收窄，feature 侧只暴露实际使用的绿色/Ivy 色和水平渐变能力，红色/反向橙色等交易卡片内部 token 保留为内部实现细节。
- `shared:ui:core` 的 Material3 内部色表继续删除无调用方色阶，只保留当前主题实际使用的紫、绿、红和中性色。
- 只服务首页更多菜单的颜色插值 helper 已移回 `feature:home` 私有实现，`shared:ui:core` 不再暴露该页面细节。
- 只服务主导航底部栏的快速弹簧动画 helper 已移回 `feature:main` 私有实现，`shared:ui:core` 只保留跨页面复用的通用动画 helper。
- `shared:ui:core` 的金额 API 继续收窄，金额输入解析、输入格式化和币种小数位 helper 只保留为 UI core 内部实现；feature 侧继续使用实际需要的金额展示和币种名称入口。
- `shared:ui:core` 的时间/周期 API 继续收窄，本地日期格式化细节和月份工厂/显示 helper 只保留为 UI core 内部实现；feature 侧继续使用语义化的日期、周期显示入口。
- `shared:ui:core` 的动态图标查找逻辑已合并为模块内部 helper，保留 `ItemIcon*` 和 `getCustomIconIdS` 公开入口，避免旧格式 `ic_custom_*` 和新格式资源解析逻辑重复维护。
- feature 模块只保留 app 导航图或主页面需要调用的页面入口；状态、事件、ViewModel、展示模型和内部 UI helper 已大量改为模块内部实现。
- app 壳层也继续收窄公开面，应用锁屏 UI 等只由根内容调用的 Compose 入口已改为模块内部实现；Android framework 需要实例化的 Activity、Application、Service 保持公开。
- domain use case 仍作为 feature 注入入口保留，但构造函数、内部算法 helper、mapper、汇率换算细节、时间 helper 等已尽量收窄为模块内部实现。
- domain 中 CSV 行构建作用域、汇率同步识别键等嵌套实现类型已继续收窄为私有，use case 对外只暴露实际调用入口和结果模型。
- domain 中偏好开关目录删除无调用方的聚合列表，设置页继续使用显式分组后的具体偏好项。
- data-core 的 Room Store、偏好 Store、mapper、文件系统和远程汇率源等实现类继续留在 data-core 内部，外部通过 data-api 端口和 domain use case 使用。
- data-model 金额、时间范围和 primitive 工具已继续清理无调用方公开扩展和常量，基础模型层不再暴露没有业务入口的 helper。
- CSV 导入、借贷详情和周期工具里继续删除无调用方 helper，保留现有导入、借贷交易同步和周期选择行为。

### 交易模型和 legacy 命名

- `shared:data:model` 中旧的 `LegacyTransaction`、`LegacyAccount`、`LegacyTag` 已删除。
- 旧交易保存、旧账户读取、旧账户保存、按旧交易 ID 读取等 use case 已删除或替换为正式模型入口。
- 首页、交易列表、报表、饼图、预算、分类、CSV 导入、计划付款、账户余额调整等路径已逐步改为正式 `Transaction` / `Account` / `Category` 模型，只在必要的统计兼容算法内部做局部适配。
- 当前代码中仍可能出现 `legacy` 字样的地方，应优先判断它是不是数据库历史迁移、测试临时文件名或真正的兼容语义；不再新增旧模块式的 legacy 包和 API。
- 已继续清理误导性历史命名：状态栏兼容函数按 Android R 前后命名，借贷详情里的旧版本兼容注释改为明确说明账户关联兼容语义。
- 借贷同步里的 `oldLoanRecord*`、`oldLoanAccountId` 命名已改为 `originalLoanRecord*`、`originalLoanAccountId`，避免把“原始值”误读成旧架构兼容层。
- Gradle 版本目录中仍服务当前功能的 Keval 计算器和 OpenCSV 导入/测试依赖不再归到 `Legacy` 分类，避免把实际功能依赖误读成旧架构残留。

### 资源和文案

- 删除无引用或明显过期的推广、登录、教程、反馈、第三方导入、旧同步、旧默认分类、旧图表标签等字符串与图片资源。
- 继续删除已移除入口遗留的未引用 drawable，例如 Telegram、Toshl、旧同步和时间追踪图标。
- 继续删除旧交易扩展入口遗留的未引用 drawable，例如附件、清单、模板、滑动提示和旧循环交易图标；当前计划付款功能本身保留。
- 继续删除旧登录、推广、付费、支持和第三方导入入口遗留的未引用 drawable。
- 继续删除旧弹窗、侧边菜单和控件状态遗留的未引用 drawable。
- 继续删除旧页面尺寸变体和旧功能入口遗留的未引用 drawable，例如无内边距入口图标、旧预算/统计小图标和旧标签/到期日图标。
- 继续删除 `shared:ui:core` 中无引用的旧 logo、旧 launcher drawable 和空账户插画；实际启动图标保留在 `app/src/main/res/mipmap*`。
- 继续删除没有 `R.font` / `@font` 静态引用的 italic、thin、extralight 等字体文件，保留当前主题实际使用的 OpenSans/Raleway 字重。
- 继续删除默认资源和多语言资源中已经没有调用方的孤立字符串。
- 继续清理已确认无使用点的 Kotlin import，减少页面文件顶部噪音。
- 继续收窄 `shared:ui:core` 的公共面，删除未使用动画 helper，并把中号图标的内部包装实现收回文件私有。
- 多语言资源没有为了“看起来干净”而整体删除；只处理明确无引用或明显脱离当前功能的条目。
- 默认分类、预算、周期、图表、备份恢复、CSV、通知等可能承载实际功能的文案继续保留。

## 当前模块分工

根目录下主要模块按职责理解：

- `app`：Android 应用壳层，负责 Activity、Hilt 装配、导航图、平台能力适配、通知、启动和根 UI。
- `feature/wallet`：核心记账与编辑页面——首页、主导航、账户、分类、预算、交易列表、编辑交易、计划付款、借贷、搜索、汇率。
- `feature/analytics`：统计分析页面——余额、报表、饼图。
- `feature/settings`：设置与数据管理页面——设置、CSV 导入。
- `shared:data:api`：数据端口，只暴露 Store 接口和数据变更事件。
- `shared:data:core`：Room、DataStore、备份恢复、CSV/zip 文件、远程汇率、Store 实现和数据 mapper。
- `shared:data:model`：跨层使用的正式业务模型、ID、金额、币种、时间范围和基础模型工具。
- `shared:domain`：用例层，承接 feature 和 data 之间的业务编排、统计、汇率换算、重排、导入导出、重置和偏好读取。
- `shared:ui:core`：跨 feature 复用的 Compose UI、平台 UI 端口、主题状态、时间服务、弹窗、金额展示、交易列表和基础组件。
- `shared:ui:navigation`：导航状态、route、返回栈和页面级 ViewModel scope。
- `shared:test-support`：跨模块测试辅助。
- `buildSrc`：Gradle convention plugins。
- `gradle`：Gradle wrapper 和版本目录。

## 本地开发

常用命令：

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleDemo
.\gradlew.bat :app:installDebug
```

当前分支通常用 `compileDebugKotlin` 作为重构后的快速编译门槛。涉及资源、manifest、安装包、运行行为或设备验证时，再执行 assemble/install。

本机开发依赖：

- JDK，使用 Gradle wrapper 能识别的版本。
- Android SDK command line tools、platform、build-tools。
- `local.properties` 中的 `sdk.dir` 指向本机 Android SDK。
- 通过 USB 调试连接手机时，可用 Gradle install task 或 adb 安装 APK。

## 重构原则

后续改动遵循这些规则：

- 不删除官方已经写好的实际功能，除非明确确认该功能对个人使用没有价值。
- 不为了形式统一批量重构高风险路径。
- 优先删除无调用方、无入口、无业务价值或只服务多人协作/发布流程的内容。
- 优先把单页面 UI、状态和展示模型收回 feature 内部。
- `shared:*` 只保留真正跨模块复用的能力，避免把页面私有细节变成公共 API。
- domain 只暴露 feature 需要的 use case；内部算法、mapper、临时模型和实现构造细节尽量模块内可见。
- data-api 只暴露端口，data-core 保留实现细节。
- 每次提交要尽量能说明：改了什么、保留了什么、为什么行为不变、是否编译。

## 高风险区域

以下内容可以继续重构，但必须单独规划、单独提交，不要混在普通清理里：

- Room schema、migration、entity 字段删除。
- 备份恢复格式变化。
- CSV 导入导出字段变化。
- 首次启动默认数据和初始化流程。
- 账户余额、转账、借贷、预算、计划付款结算和统计逻辑。
- 汇率同步和历史汇率换算。
- App lock、生物识别、文件选择、文件分享、通知。
- 动态图标资源，例如 `ic_custom_*`、`ic_vue_*`。

## 后续计划

### 1. 继续清理历史命名

目标是让代码语义和当前结构一致。

- 搜索并审查 `legacy`、`temp`、`old` 等命名。
- 数据库 migration 中的临时表命名通常不需要改，除非明显误导。
- 业务代码中已经不再表示兼容层的 legacy 命名应改成当前领域含义。

### 2. 继续收窄 shared/feature 公开面

目标是减少跨模块心智负担。

- 查找只在本模块内使用但仍是 public 的函数、类、token 和 helper。
- feature 只保留导航入口、主页面入口或其他模块确实需要的窄接口。
- `shared:ui:core` 中只保留真正复用的 UI 组件；只服务单页面的 UI 移回 feature。
- `shared:domain` 中只保留外部需要注入或调用的 use case；统计 helper、mapper、转换函数继续内收。

### 3. 继续清理无引用资源和文案

目标是删除不会影响功能的资源噪音。

- 优先处理无 `R.string` / `R.drawable` / `R.raw` 调用的资源。
- 对默认分类、备份、CSV、通知、图表、周期、预算等功能相关资源保持保守。
- 多语言资源只跟随默认资源清理，不单独做大规模删除。

### 4. 审计 feature 依赖

目标是让每个 feature 的 Gradle 依赖能解释清楚。

- 删除 feature 中不再直接使用的依赖。
- 避免 feature 直接依赖底层 data-core 实现。
- 避免 feature 直接调用 domain 内部算法模型。
- 如果两个 feature 因页面组合产生依赖，优先确认是否能通过更窄入口表达。

### 5. 谨慎处理偏好和数据库

目标是减少历史字段，但不破坏已有数据。

- 偏好端口（`data-api` 的 Store 接口）和 use case 边界保持不变；偏好的存储后端已从 SharedPreferences 收敛到 DataStore（见第 7 节）。
- `SettingsEntity`（主题、币种、缓冲金额）仍留在 Room，字段变化必须单独规划迁移，避免影响备份格式。
- `isDeleted` 目前先保留为本地软删除语义，不把所有业务表里的 `isDeleted` 直接当作云同步残留删除。

### 6. feature 模块合并（已完成：16 → 3）

原先 16 个 feature 模块已按职责合并为 3 个：

- `feature:wallet`：核心记账与编辑（原 home、main、accounts、transactions、edit-transaction、search、categories、budgets、loans、planned-payments、exchange-rates）。
- `feature:analytics`：统计分析（原 balance、reports、piechart）。
- `feature:settings`：设置与数据管理（原 settings、import-data）。

合并时所有源文件保留原 Kotlin 包名，导航图与调用方无需改动；这些 feature 模块无资源/manifest/额外依赖，合并零资源冲突，`feature:main` 对 accounts/home 的内部依赖随合并自然消除。

### 7. 偏好存储后端收敛（已完成：SharedPreferences → DataStore）

应用偏好原本分散在三种存储机制：SharedPreferences（应用锁、隐藏余额、每月起始日等同步标量）、DataStore（feature 开关）、Room `SettingsEntity`（主题/币种/缓冲金额）。本步将 SharedPreferences 后端整体迁移到 DataStore，存储机制从 3 种降为 2 种。

- 偏好端口仍是同步 API，`AppLockController`、`SecureWindowController` 等生命周期/窗口代码无需改造；新实现 `DataStorePreferenceStore` 用一次性内存快照 + 写穿透在 DataStore 之上保留同步语义。
- 旧 `ivy_wallet_prefs` 的数据通过 DataStore 的 `SharedPreferencesMigration` 在首次访问时自动导入新文件 `ivy_wallet_preferences_v1` 并清理旧文件，老用户偏好零丢失。
- 偏好 DataStore 与 feature 开关 DataStore 分文件存放，`clearAll()` 只清偏好、不动开关，保持原重置语义。
- `SettingsEntity` 仍留在 Room（涉及备份格式与 Room 迁移，单独规划）。
- 已在真机验证：种入 10 项旧偏好（布尔/整型/字符串/动态键）→ 安装新包 → 全部按原值迁移（隐藏余额、每月起始日等行为在 UI 正确生效），旧文件被消费删除。

## 提交习惯

推荐提交粒度：

- `docs: 整理个人维护分支 README`
- `refactor: 收窄某模块公开面`
- `refactor: 移除无调用方 helper`
- `chore: 删除未引用资源`
- `build: 精简某模块依赖`
- `data: 调整某个明确迁移`

提交说明建议包含：

- 删除或迁移了什么。
- 保留了什么行为。
- 是否编译。
- 是否安装。
- 是否跑测试。

## 清理审计（已完成）

对上述低优先级清理项做了一轮数据化审计，结论：项目已基本干净，不为清理而清理。

- 历史命名/注释（legacy/temp/old/TODO/FIXME）：无残留（仅偏好迁移代码中对“旧 SharedPreferences”的准确描述）。
- 死代码：全量重编译 0 警告，无未使用的 private/internal 符号、参数或变量。
- 无引用资源：lint 未报 UnusedResources；`shared:ui:core` 的数百个 `ic_custom_*`/`ic_vue_*` 图标由 `DynamicIconLookup` 经 `getIdentifier` 按名动态加载，属“在用”，不可删（也是 lint 唯一一条 DiscouragedApi 的来源，刻意保留）。
- Gradle 依赖：3 个 feature 模块依赖已最小化；app 依赖逐项核实均在用（WorkManager 用于提醒、Material 用于日期选择、opencsv 用于 CSV 导入）。
- 未使用的公开 API：133 个 use case 全部有注入方（仅测试类为单点引用），29 个 data-api 端口均被使用（最少 7 处引用），无死端口。
- lint 余下 75 项 GradleDependency + 3 项 AGP 版本提示，均为“有更新版本可升级”，属独立升级任务而非清理。

## 当前下一步

优先级从高到低：

1. 依赖与 AGP/Compose/Kotlin/Room 版本升级（lint 提示有较多新版本）——属独立任务，需逐项验证编译与运行，单独规划。
2. `SettingsEntity`（主题/币种/缓冲金额）从 Room 收敛的可行性评估——牵涉备份格式与 Room 迁移，高风险，按需再做。
3. 新增代码时保持既有约定：实现类用 internal、仅端口与 use case 公开；feature 依赖最小化。
4. 需要设备确认 UI 行为时再编译安装；普通文档和纯可见性收窄不必每次安装。
