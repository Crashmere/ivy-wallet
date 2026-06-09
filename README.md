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
- `shared:ui:core` 承接真正跨页面复用的 UI 基础能力：主题 root 所需状态、时间服务、弹窗、金额展示、交易列表、按钮、图标、搜索框、周期选择、标签入口等。
- feature 模块只保留 app 导航图或主页面需要调用的页面入口；状态、事件、ViewModel、展示模型和内部 UI helper 已大量改为模块内部实现。
- domain use case 仍作为 feature 注入入口保留，但构造函数、内部算法 helper、mapper、汇率换算细节、时间 helper 等已尽量收窄为模块内部实现。
- data-core 的 Room Store、偏好 Store、mapper、文件系统和远程汇率源等实现类继续留在 data-core 内部，外部通过 data-api 端口和 domain use case 使用。
- data-model 金额、时间范围和 primitive 工具已继续清理无调用方公开扩展，基础模型层不再暴露没有业务入口的 helper。

### 交易模型和 legacy 命名

- `shared:data:model` 中旧的 `LegacyTransaction`、`LegacyAccount`、`LegacyTag` 已删除。
- 旧交易保存、旧账户读取、旧账户保存、按旧交易 ID 读取等 use case 已删除或替换为正式模型入口。
- 首页、交易列表、报表、饼图、预算、分类、CSV 导入、计划付款、账户余额调整等路径已逐步改为正式 `Transaction` / `Account` / `Category` 模型，只在必要的统计兼容算法内部做局部适配。
- 当前代码中仍可能出现 `legacy` 字样的地方，应优先判断它是不是数据库历史迁移、测试临时文件名或真正的兼容语义；不再新增旧模块式的 legacy 包和 API。
- 已继续清理误导性历史命名：状态栏兼容函数按 Android R 前后命名，借贷详情里的旧版本兼容注释改为明确说明账户关联兼容语义。

### 资源和文案

- 删除无引用或明显过期的推广、登录、教程、反馈、第三方导入、旧同步、旧默认分类、旧图表标签等字符串与图片资源。
- 继续删除已移除入口遗留的未引用 drawable，例如 Telegram、Toshl、旧同步和时间追踪图标。
- 多语言资源没有为了“看起来干净”而整体删除；只处理明确无引用或明显脱离当前功能的条目。
- 默认分类、预算、周期、图表、备份恢复、CSV、通知等可能承载实际功能的文案继续保留。

## 当前模块分工

根目录下主要模块按职责理解：

- `app`：Android 应用壳层，负责 Activity、Hilt 装配、导航图、平台能力适配、通知、启动和根 UI。
- `feature/*`：具体功能页面，例如首页、账户、分类、预算、交易列表、编辑交易、计划付款、借贷、报表、饼图、搜索、设置、导入和汇率。
- `shared:data:api`：数据端口，只暴露 Store 接口和数据变更事件。
- `shared:data:core`：Room、SharedPreferences、备份恢复、CSV/zip 文件、远程汇率、Store 实现和数据 mapper。
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

- 偏好设置暂时继续使用现有 Store/use case 边界。
- `SettingsEntity`、SharedPreferences、DataStore 或 Room 字段变化必须单独规划迁移。
- `isDeleted` 目前先保留为本地软删除语义，不把所有业务表里的 `isDeleted` 直接当作云同步残留删除。

### 6. 暂缓 feature 模块合并

模块合并不是当前优先事项。

- 只有当 shared/feature 边界继续收窄后，某些 feature 仍然因为实际业务强耦合而难以维护，再考虑合并。
- 合并前必须先确认导航、资源、Hilt、测试和包名边界。

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

## 当前下一步

优先级从高到低：

1. 继续检查代码中的历史命名和注释，处理不再准确的 `legacy/temp/old` 标识。
2. 继续检查 `shared:ui:core`、`shared:domain`、`shared:data:core` 中只在模块内部使用但仍公开的 API。
3. 继续检查 feature 的 Gradle 依赖，删除已经通过 core/domain 门面替代的直接依赖。
4. 继续做无引用资源清理，但避开数据库、备份、CSV、默认数据和统计逻辑。
5. 需要设备确认 UI 行为时再编译安装；普通文档和纯可见性收窄不必每次安装。
