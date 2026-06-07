# Ivy Wallet 个人维护分支重构计划

这个仓库现在作为个人使用和个性化开发用途维护，不再面向上游协作、社区运营、应用商店发布或多人开发流程。后续重构的目标不是重写 Ivy Wallet，而是在不影响当前已有功能的前提下，把项目逐步整理成模块结构清晰、职责分配合理、依赖方向明确、适合单人长期维护的 Android 项目。

## 当前状态摘要

已经完成的清理可以概括为几类：

- 删除社区协作、开源展示和发布流程相关内容：GitHub workflow、Issue/PR 模板、开发规范、Fastlane、发布日志、贡献者、开源致谢、投票问卷、原仓库入口、分享 Ivy、Google Play 评分、Telegram/推广文案等。
- 删除 Google/Firebase/商店发布相关接线：Google Services、Crashlytics、Google Play Review、Firebase Firestore、GitHub 自动备份迁移残留等。
- 删除不再需要的功能模块和入口：contributors、releases、attributions、poll、disclaimer、onboarding、widget，以及第三方 App 导入模板和教程。
- 整顿设置页：合并原高级特性页，改成个人偏好设置；重排设置分组；删除匿名账户入口和首页问候语。
- 精简测试和预览基础设施：删除 Paparazzi 截图测试、快照图片、仅服务 IDE 的 Compose `@Preview` 示例函数和预览 helper。
- 持续清理 `temp:legacy-code` 中确认无引用的旧代码、工具、组件和残留模型。
- 删除空的 `:shared:data:core-testing` 模块，并把测试专用 `FakeRepositoryMemo` 从生产源码移入测试源集。
- 删除未引用的第三方导入 logo、widget 预览/图标、推广/分享/捐赠图片，以及 `help_us_grow` 多语言推广文案。
- 删除 `:temp:old-design` 模块；旧设计 API 先迁入 `shared:ui:core` 作为兼容层，随后已逐步归位到更明确的 `legacy.ui.theme`/`legacy.ui.theme.system` 包名。
- 删除 `:temp:legacy-code` 模块；剩余旧全局上下文暂时收敛在 `shared:ui:legacy`，并已把周期状态、文件选择、日期选择、主 Tab 状态等职责逐步拆出。
- 整理部分 Gradle 约定插件：基础 shared 模块、数据核心模块和 domain 模块已经开始脱离面向页面的 `ivy.feature` 配置。
- 开始拆分 `RootActivity` 平台能力：文件创建/打开、Material 日期选择器、生物识别弹窗、浏览器/商店跳转和 CSV/zip 分享已经移入 `app` 的 platform 边界；`RootScreen` 已从 domain 移到 UI platform 接口。

当前仍保留：

- 真实记账功能、数据模型、数据库、数据迁移、备份恢复、CSV 导入导出、汇率同步、功能测试。
- Gradle wrapper、本地 Android SDK 配置、VS Code 开发所需配置。
- 运行时仍被引用的旧 UI 组件、旧领域逻辑和旧设计系统桥接层。

## 核心目标

重构完成后的代码应该满足这些条件：

- **功能不回退**：现有首页、账户、交易、分类、预算、借贷、计划付款、报表、饼图、汇率、搜索、设置、备份恢复、CSV 导入导出继续可用。
- **模块职责清楚**：每个模块有明确边界，不再让 `temp`、legacy、旧设计系统承担事实上的公共基础设施职责。
- **依赖方向单向**：UI 不反向污染 domain，domain 不直接依赖 Android UI，测试 helper 不进入生产源码。
- **保留必要抽象，删除协作噪音**：去掉为多人协作、开源运营、远程发布和上游维护服务的抽象或流程；保留能降低个人维护成本的抽象。
- **逐步迁移，不大爆炸式重写**：每一轮都可编译、可安装、可回滚，优先通过迁移和收敛来消除旧模块。

## 目标模块结构

最终建议收敛到下面的结构。短期不一定一次性做到，但每轮重构都应朝这个方向推进。

| 模块 | 目标职责 | 不应包含 |
| --- | --- | --- |
| `:app` | Android 应用壳、`Activity`、启动流程、系统权限、生物识别、文件选择、分享、根导航宿主 | 业务计算、数据库访问、旧 UI 组件、跨 feature 业务状态 |
| `:shared:base` | 纯基础能力：时间、线程、基础 Result/Failure、资源抽象、轻量工具 | `SharedPrefs` 业务 key、UI 组件、Room、Ktor、Compose |
| `:shared:data:model` | 纯数据模型和值对象：Account、Transaction、Category、Tag、ExchangeRate、primitive value object | Room DAO、Repository、Android Context、UI 文案 |
| `:shared:data:core` | 数据实现：Room、DataStore、FileSystem、Repository、备份恢复、远程汇率数据源 | UI 状态、Compose、测试 fake、feature 专用逻辑 |
| `:shared:domain` | 业务 use case：余额、统计、CSV 导出、汇率换算、设置/偏好业务规则 | Room 插件、Ktor 具体实现、Android Activity 接口、UI 资源 |
| `:shared:ui:core` | Material3 主题、通用 Compose 组件、图标、时间/金额 UI 格式化、UI CompositionLocal | 数据库、Repository 实现、业务写入逻辑 |
| `:shared:ui:navigation` | 页面 route、导航状态、导航容器 | domain use case、数据层实现、feature 业务逻辑 |
| `:feature:*` | 用户可感知功能页面和 ViewModel | 公共基础设施、跨模块全局状态、临时兼容代码 |
| `:temp:*` | 迁移过程中的临时兼容层 | 最终应清空并删除 |

长期可以进一步简化 feature 模块。如果个人维护更重视低心智负担，可以把多个小 feature 合并，最终保留少量大模块。

## 当前主要问题

### 1. `temp` 模块已经变成事实公共层

现状：

- `feature:*` 和 `app` 对 `:temp:legacy-code` 的直接依赖已经迁走，Gradle 中不再 include 旧 `temp` 模块。
- `:temp:legacy-code` 模块已经删除；旧全局上下文入口暂时迁入 `shared:ui:legacy`，后续继续拆内部职责。
- 旧设计系统源码已经迁入 `shared:ui:core` 作为兼容层，并已从 `com.ivy.legacy.design.*` 进一步归位到 `com.ivy.legacy.ui.theme.system`；旧根包装器已迁到 `com.ivy.ui.LegacyUiRoot`，旧 `LegacyTheme` 等概念仍存在，但不再伪装成正式设计系统。

问题：

- 新代码很容易继续引用 legacy API。
- 无法从模块依赖上判断哪些是真正公共能力，哪些只是迁移残留。
- `shared:ui:legacy` 现在承担了迁移期公共 UI/旧上下文职责，仍会让新代码误用旧 API。

目标：

- 把仍有价值的代码迁到正确模块。
- 把旧名字、旧 package、旧设计系统概念逐步消掉。
- 最终删除 `:temp:legacy-code`，并把 `shared:ui:core` 里的旧设计兼容包替换为更清晰的 Material3/UI core API。

### 2. 构建约定插件过度通用

现状：

- `ivy.feature` 被 app 以外几乎所有 Android library 复用。
- `shared:base`、`shared:data:core`、`shared:domain` 这类非 feature 模块也使用 feature 插件。
- 一些模块拿到了并不需要的 Compose、Hilt、Room、Ktor 或测试配置。

问题：

- 模块职责从 Gradle 文件上看不出来。
- 非 UI 模块容易意外依赖 UI/Compose。
- 这是典型多人项目模板化残留，对个人维护不够直观。

目标：

- 拆分成更直接的约定插件，例如：
  - `ivy.android-library`
  - `ivy.android-compose`
  - `ivy.hilt-library`
  - `ivy.room-library`
  - `ivy.tested-library`
- 每个模块显式声明自己需要的能力。

### 3. 测试 helper 已基本移出生产源码

现状：

- `Fake*Dao` 已在 `shared:data:core/src/test`。
- `TestDispatchersProvider`、`TestResourceProvider`、`TestTimeConverter` 已在 `shared:base-testing`。
- `FakeRepositoryMemo` 已移到 test/androidTest 源集。
- 生产源码中的空 `TestIdlingResource` 计数器已删除，调用方不再插入测试空闲计数。

问题：

- 仍需要继续防止新的测试 helper 回流到 `src/main`。
- 功能测试如果后续重新需要空闲同步，应在 androidTest 专用边界实现，而不是回到生产 ViewModel。

目标：

- 生产 main 源集不承载 fake DAO、test dispatcher、test resource provider 或空闲同步计数器。
- 测试支持能力集中在测试源集或 `shared:*testing` 模块。

### 4. 数据层仍有同步、登录和云端历史痕迹

现状：

- Room entity 里仍有 `isSynced`、`isDeleted`、`lastSyncedTime` 等历史同步字段。
- `UserEntity/UserDao` 仍存在，主要被 `LogoutLogic` 的清空流程调用。
- `SettingsEntity` 仍是旧设置模型，部分偏好又在 `SharedPrefs/DataStore` 中。

问题：

- 个人本地使用不需要云同步语义。
- 数据模型承担了历史兼容和当前功能两种职责。
- 删除字段/表会影响 Room schema 和历史数据库迁移，不能粗暴删除。

目标：

- 先让运行时代码不再依赖旧同步/用户表概念。
- 再通过明确 Room migration 删除或废弃字段。
- 备份恢复格式同步调整，并保留测试覆盖。

### 5. 平台能力集中在 `RootActivity`

现状：

- `RootActivity` 仍负责主题、根导航、应用锁生命周期和根部 Compose 宿主。
- 文件创建/打开、CSV/zip 分享、Material 日期选择器、生物识别弹窗、浏览器跳转、Google Play 跳转已经拆到 `app/src/main/java/com/ivy/wallet/platform`。
- `RootScreen` 已从 `shared:domain` 移到 `shared:ui:core` 的 `com.ivy.ui.platform` 包。

问题：

- Activity 仍偏重，应用锁生命周期和根导航还混在一起。
- feature 仍通过大而全的 `RootScreen` 直接拿 Activity 能力，边界不够窄。

目标：

- 把平台能力拆成小 service/controller：
  - `FilePickerService`
  - `ShareService`
  - `BiometricLockController`
  - `DateTimePickerHost`
  - `ExternalIntentLauncher`
- `RootScreen` 移出 domain，或者拆成更窄的 UI/platform interface。

### 6. feature 模块数量对个人项目偏多

现状：

- 仍有 16 个 feature 模块。
- 很多 feature 文件很少，但都重复依赖 shared、temp、navigation、old design。

问题：

- 模块数量本身成为心智负担。
- 改一个横向 UI/状态逻辑需要跨很多模块。
- 单人开发未必需要这么细的多人协作边界。

目标：

- 先消除 feature 对 `temp` 的依赖。
- 再按真实个人维护习惯合并模块。

## 分阶段重构路线

### 阶段 0：工作方式固定下来

每一轮重构遵守：

- 只处理一个主题，例如“资源清理”“Gradle 插件整理”“迁移颜色常量”。
- 保持提交粒度清晰。
- 默认不编译；涉及 Gradle、Room、依赖迁移、跨模块移动时再编译。
- 涉及数据库迁移、备份恢复、导入导出时必须运行相关测试或至少编译验证。
- 误删可以回滚，但不主动动个人数据相关逻辑，除非已经明确迁移策略。

建议验证级别：

- 纯 README/资源删除：`git diff --check`
- Kotlin 源码移动：`./gradlew.bat :app:assembleDemo`
- 测试 helper 迁移：相关模块 `test` 或 `assembleDemo`
- Room schema 变化：迁移测试、备份恢复测试、demo 安装验证

### 阶段 1：资源和文案瘦身

目标：先删不会影响业务逻辑的资源和文案。

已完成：

- 删除未引用的第三方导入 logo、widget 预览/图标、推广/分享/捐赠图片。
- 删除 `help_us_grow` 及各语言翻译。
- 保留动态分类/账户图标资源，以及首页仍在使用的 `did_you_know` 提示卡文案。

候选内容：

- 未引用的第三方导入 logo：
  - `monefy_logo`
  - `moneymanager_logo`
  - `spendee_logo`
  - `wallet_by_budgetbakers_logo`
- 未引用的 widget 预览和 widget 图标：
  - `preview_widget_add_trn*`
  - `preview_widget_wallet_balance`
  - `ic_widget_*`
  - `shape_widget_background`
  - `income_shape_widget_background`
  - `expense_shape_widget_background`
- 未引用的推广/分享/捐赠资源：
  - `donate_illustration`
  - `home_more_menu_share`
  - `questions`
  - `didyouknow`
  - `help_us_grow` 及各语言翻译
- 如果确认只面向中文个人使用，可以进一步只保留：
  - `values`
  - `values-zh-rCN`

注意：

- 自定义分类/账户图标不能直接批量删。它们通过 `Resources.getIdentifier()` 动态查找，静态搜索可能看不到引用。
- `ic_custom_*` 和 `ic_vue_*` 大量图标仍支撑图标选择器，不能直接当无引用资源删除。

### 阶段 2：整理 Gradle 约定插件

目标：让模块声明更符合职责，减少模板化残留。

建议步骤：

1. 新增或重命名约定插件：
   - `ivy.android-library`：Android library 基础配置、Kotlin、min/compile SDK。
   - `ivy.compose-library`：仅给 Compose UI 模块使用。
   - `ivy.hilt-library`：仅给需要 DI 的模块使用。
   - `ivy.room-library`：仅给 Room 模块使用。
   - `ivy.tested-library`：测试依赖和测试 JVM 配置。
2. 先迁移低风险模块：
   - `shared:base`
   - `shared:data:model`
   - `shared:data:model-testing`
3. 再迁移数据和 domain：
   - `shared:data:core`
   - `shared:domain`
4. 最后迁移 feature 和 temp。

目标结果：

- 非 UI 模块不默认启用 Compose。
- 非 Room 模块不应用 Room 插件。
- `shared:domain` 不直接引入 Ktor 和 Room 插件，除非当前迁移未完成且有明确说明。

当前进展：

- 新增 `ivy.android-library` 作为更清晰的 Android library 基础约定，旧 `ivy.kotlin-android` 暂时保留为兼容别名。
- `shared:base`、`shared:data:model`、`shared:data:model-testing` 已从 `ivy.feature` 迁出，不再默认启用完整 Compose UI 配置。
- `shared:base` 仍显式保留 Hilt、kotlinx serialization 和轻量 `compose-runtime`，因为当前源码仍包含 DI 绑定、序列化器和 `@Immutable` 注解。
- `shared:data:model` 仍显式保留轻量 `compose-runtime`，后续可以把模型层的 Compose 注解替换掉，再彻底移除。
- 新增 `ivy.compose-runtime`，只提供 `@Composable` 编译和 `compose-runtime/ui` 最小依赖，用于当前仍包含 `LocalContext`、`collectAsState` 等轻量 Compose API 的非页面模块。
- `ivy.integration.testing` 已从 `ivy.feature` 改为基于 `ivy.android-library`，避免因为集成测试配置把完整 Compose UI 配置带入数据层。
- `shared:data:core`、`shared:domain` 已从 `ivy.feature` 迁出，改为显式声明基础 Android library、轻量 Compose runtime、Hilt、Room 或集成测试等各自实际需要的能力。
- `shared:domain` 已移除 `ivy.room` 插件；主源码只显式保留 Hilt，当前只有 androidTest 中的汇率同步测试需要 Room runtime/testing 来创建内存数据库。
- `ivy.room` 已从 `ivy.module` 改为基于 `ivy.android-library`，不再隐式带入 Hilt 和 kotlinx serialization；`shared:data:core` 改为显式声明这两个依赖。

### 阶段 3：测试支持代码归位

目标：生产源码不再包含测试 fake。

候选迁移：

- `shared:data:core/src/main/java/com/ivy/data/db/dao/fake/Fake*Dao.kt`
- `shared:base/src/main/java/com/ivy/base/TestDispatchersProvider.kt`
- `shared:base/src/main/java/com/ivy/base/resource/TestResourceProvider.kt`
- `shared:base/src/main/java/com/ivy/base/time/impl/TestTimeConverter.kt`

可选方案：

1. 简单方案：复制到各自模块的 `src/test` 和 `src/androidTest`。
2. 中等方案：创建 `:shared:test-support`，仅测试依赖它。
3. 更完整方案：使用 Android Gradle test fixtures，但会增加配置复杂度。

个人维护推荐：

- 优先采用 `:shared:base-testing` 这类测试支持模块，名字直接、职责清晰。
- 不为了“标准化”引入过复杂的 test fixtures 配置。

当前进展：

- 新增 `:shared:base-testing`，用于承载跨模块复用的基础测试 helper。
- `shared:data:core`、`shared:domain`、`shared:ui:core` 的测试源集改为显式依赖 `shared:base-testing`。
- `Fake*Dao`、`FakeRepositoryMemo` 已归位到测试源集；生产源码不再包含这些 fake。
- 已删除生产源码中的空 `TestIdlingResource` 以及 Root/Main/Import/Loans ViewModel 中的空调用。

### 阶段 4：消灭 `temp:old-design`

目标：先迁移仍有价值的 18 个文件，再删除模块。

迁移分组：

1. 颜色与主题
   - `Colors.kt`
   - `IvyColors.kt`
   - `IvyTheme.kt`
   - `DefaultLegacyDesign.kt`（已由旧多实现设计抽象收敛为内部默认配置）
   - 目标：迁入 `shared:ui:core`，逐步合并到 Material3 theme。
2. 颜色选择器常量
   - `IVY_COLOR_PICKER_COLORS_*`
   - 目标：迁入 `shared:ui:core` 或更明确的 `ColorPalette.kt`。
   - 注意：CSV 导入、借贷逻辑和旧颜色选择器仍在用。
3. 基础 building block
   - `IvyText`（已删除，调用方改用 Material3 `Text`）
   - `IvyIcon/IvyIconScaled`（已删除，调用方改用 Material3 `Icon`、`Image` 或本地小函数）
   - `SpacerHor/SpacerVer`（已删除，调用方改用 Compose 原生 `Spacer`）
   - `DividerW/DividerH`（已删除未使用的旧分隔包装，少量调用方改为本地 `Spacer` 分隔线）
   - `ColumnRoot`（已删除，调用方改用 Compose 原生 `Column`）
   - 目标：旧 `l1_buildingBlocks` 包已清空，后续继续收敛旧主题包名。
4. Compose helper
   - `thenIf`
   - `thenWhen`
   - `densityScope`
   - `rememberInteractionSource`
   - `hideKeyboard`
   - 目标：UI helper 进 `shared:ui:core`，平台键盘行为保持在 UI 层。

当前进展：

- 已把 `utils/Compose.kt`、`utils/Keyboard.kt`、`Spacers.kt`、`ColumnRoot.kt`、`IvyText.kt` 从 `temp:old-design` 移到 `shared:ui:core`；其中 `Spacers.kt`、`ColumnRoot.kt` 和未使用的旧分隔组件后续已替换为 Compose 原生写法并删除。
- 这些文件已归到明确的 legacy UI theme 包名；后续继续替换 `LegacyTheme` 和旧颜色/组件。
- 在 `shared:ui:core` 补齐 `colorControlNormal` attr，让仍使用该 attr 的旧 drawable 可以通过独立资源校验。
- 已把剩余旧设计 Kotlin 源码整体迁入 `shared:ui:core`，并删除 `:temp:old-design` 模块依赖。
- 旧模块中的字体是 `shared:ui:core` 的重复资源；旧模块中的未引用 drawable 不迁移。

完成标准：

- `rg "projects.temp.oldDesign" -g "build.gradle.kts"` 无 feature/app 依赖。
- `rg "com.ivy.design"` 已经从源码中清空。
- 删除 `:temp:old-design` include 和目录。

### 阶段 5：拆解 `temp:legacy-code`

目标：按职责迁移，不按旧目录整体搬家。

当前进展：

- 已把只被 `RootActivity` 使用的 Activity result launcher helper 从 `temp:legacy-code` 迁到 `app` 的 `com.ivy.wallet.platform` 包。
- 已把只被锁屏界面使用的设备锁屏检查从 legacy 通用工具迁到 `app` 的 `com.ivy.wallet.platform` 包。
- 已把 legacy 状态暴露和线程切换 helper 从 `temp:legacy-code` 迁到 `shared:base` 的 `com.ivy.base.legacy` 包。
- 已把 legacy 字符串空白校验 helper 从 `temp:legacy-code` 迁到 `shared:base` 的 `com.ivy.base.legacy` 包。
- 已把 legacy 通用本地化字符串、默认货币、随机数和列表交换 helper 从 `temp:legacy-code` 迁到 `shared:base` 的 `com.ivy.base.legacy` 包。
- 已把余额正负号 helper 从 `temp:legacy-code` 迁到 `shared:base` 的 `com.ivy.base.legacy` 包。
- 已把 `IvyCurrency` 和金额输入/显示格式 helper 从 `temp:legacy-code` 迁到 `shared:data:model` 的 `com.ivy.data.model.currency` 包。
- 已新增 `shared:ui:legacy` 过渡模块，并把旧 Compose/UI helper、手势 helper 和动画 helper 从 `temp:legacy-code` 迁入其中。
- 已拆分 legacy 日期 helper：纯时间计算迁到 `shared:base`，日期展示格式迁到 `shared:ui:legacy`。
- 已把旧 FRP 组合 helper、`FPAction`、`Res` 和测试空闲计数器从 `temp:legacy-code` 迁到 `shared:base`，并移除重复的 legacy `onScreenStart`。
- 已把 Android 通知封装和交易提醒 WorkManager 逻辑从 `temp:legacy-code` 迁到 `app`，保留原包名和提醒功能。
- 已把首次启动默认数据初始化迁到 `app`，并用 `ResetWalletDataUseCase` 接口替代设置页直接注入旧 `LogoutLogic`。
- 已拆分 legacy 根目录中的周期 helper、借贷类型显示和交易页常量：纯时间计算进 `shared:base`，周期文案进 `shared:ui:legacy`，借贷显示逻辑进 `feature:loans`，交易页常量本地化到 `feature:transactions`。
- 已把不依赖 legacy datamodel/domain 的部分交易分隔组件从 `temp:legacy-code` 迁入 `shared:ui:legacy`，并去掉它们对 temp 旧 theme 组件的依赖。
- 已把旧 theme 的颜色常量和 30 个基础组件从 `temp:legacy-code` 迁入 `shared:ui:legacy`；仍与 modal、wallet、legacy datamodel 耦合的少数组件暂留 temp。
- 已把旧弹窗基础层继续收敛到 `shared:ui:legacy`：`IvyModal`、通用 modal action、删除/进度/货币/图标/起始日弹窗、排序弹窗、金额展示、预算/缓冲条、交易类型选择、分类选择和部分通用输入弹窗已迁出 `temp:legacy-code`。
- 已把旧排序接口从 `com.ivy.wallet.domain.data.Reorderable` 收敛到 `shared:data:model` 的 `com.ivy.data.model.Reorderable`，避免 UI legacy 为了排序弹窗反向依赖 domain。
- 已把计划付款复用的 `RecurringRuleModal` 通过外部 `pickDate` 回调与 `IvyWalletCtx` 解耦，并迁入 `shared:ui:legacy`。
- 已把旧时间范围兼容模型迁出 `temp:legacy-code`：纯 `ClosedTimeRange`、收入/支出统计值对象迁入 `shared:data:model`；`FromToTimeRange`、overdue/upcoming 过滤函数和 `AccountData` 已迁入 `shared:domain`；仍带 UI 文案/格式化职责的旧 `TimePeriod`、`Month`、`LastNTimeRange`、`MainTab` 暂时保留在 `shared:ui:legacy` 的 legacy model 区。
- 已把 `FromToTimeRange.toDisplay(...)` 从 domain 模型上拆成 `shared:ui:legacy` 的 UI 扩展，避免 `shared:domain` 依赖 `TimeFormatter`。
- 已把 `Month.incrementMonthPeriod` 改成只返回新周期，不再直接更新 `IvyWalletCtx`；各页面/ViewModel 在调用处显式保存选中周期，副作用更清楚。
- 已把 `ChoosePeriodModal` 和 `PeriodSelector` 迁入 `shared:ui:legacy`，并通过外部 `saveSelectedPeriod`、`pickDate`、`startDateOfMonth` 参数替代内部直接读取 `IvyWalletCtx`。
- 已把金额输入弹窗、计算器弹窗和缓冲金额弹窗迁入 `shared:ui:legacy`。其中金额键盘仍通过 Hilt EntryPoint 读取“标准键盘布局”偏好，因此 `shared:ui:legacy` 暂时显式依赖 `shared:domain` 和 `keval`；后续偏好设置重构时应改为由调用方或 CompositionLocal 提供键盘布局。
- 已把旧 `legacy.datamodel` 整体迁入 `shared:domain`，把旧创建参数模型迁入 `shared:domain`，并把账户/分类/借贷创建参数里的颜色从 Compose `Color` 改为普通 ARGB `Int`，由 UI 弹窗在边界处转换。
- 已把旧颜色选择器、账户弹窗、分类弹窗、借贷弹窗和借贷记录弹窗迁入 `shared:ui:legacy`。颜色选择器移除了旧付费锁显示分支，不再依赖会员状态。
- 已把旧 UI 状态模型 `AppBaseData`、`LegacyDueSection`、`BufferInfo`、`EditTransactionDisplayLoan` 迁入 `shared:ui:legacy`，作为迁移期的 UI 兼容数据。
- 已把搜索框、收入/支出卡片、详情工具栏、标签弹窗、交易卡片和交易列表组件迁入 `shared:ui:legacy`；交易卡片查找账户/分类时改为只使用调用方传入的数据，去掉了对 `IvyWalletCtx` 缓存的读取。
- 已把 `SortOrder`、`CustomExchangeRateState`、`TransactionHistoryDateDivider` 迁入 `shared:domain`，它们本来已经以 `com.ivy.wallet.domain.data` 包名被 feature 使用。
- 已把编辑交易/计划付款复用的底部表单组件迁入 `shared:ui:legacy`；`EditBottomSheet` 改用 Compose 屏幕高度，不再为了底部操作条位置读取 `IvyWalletCtx`。
- 已把旧 domain 层对 `IvyWalletCtx` 的直接依赖拆掉：账户/分类缓存 action 已删除，起始日 action 只负责读写偏好，调用方显式更新旧 UI 上下文；借贷交易逻辑去掉固定为 true 的付费判断分支。
- 已把旧 `domain/action`、`domain/pure`、旧汇率换算逻辑、账户数据 action、交易范围过滤 action 迁入 `shared:domain`。
- 已把旧 creator、计划付款逻辑、标题建议、账户/分类统计逻辑和借贷交易联动逻辑迁入 `shared:domain`；其中 `AccountCreator`、`BudgetCreator` 也统一改到 `com.ivy.wallet.domain.deprecated.logic` 包名。
- 已把仍依赖 Android 字符串资源的 `PreloadDataLogic` 从 `temp:legacy-code` 移到 app 默认数据初始化边界，避免 `temp` 继续承载旧业务逻辑。
- 已把旧全局上下文入口 `IvyWalletCtx`、`ivyWalletCtx()` 和 `rootScreen()` 迁入 `shared:ui:legacy`，随后继续拆分；目前仅保留仍有调用方的 `rootScreen()`。
- 已继续缩小 `shared:ui:legacy` 的旧全局 API：删除无调用方的 `rootView()`、时间选择器桥接、Google 登录入口和固定为 true 的会员状态；RootActivity 使用 `LegacyUiRoot` 作为旧 UI 兼容入口。
- 已删除 `IvyWalletCtx` 中无实际写入路径的账户/分类缓存、列表滚动状态缓存和未被调用的 `reset()`；相关页面改为只使用已有的 `rememberScrollPositionListState(key = ...)` 保存滚动位置。
- 已把备份/恢复/CSV 导入导出使用的文件创建和文件打开能力从 `IvyWalletCtx` 拆到 `shared:ui:core` 的 `FilePicker` 窄接口，由 app 侧 `ActivityResultFilePicker` 负责注册 Android Activity Result；同时删除没有读取方的 `dataBackupCompleted` 旧状态。
- 已把旧日期选择器桥接从 `IvyWalletCtx` 拆到 `shared:ui:core` 的 `DatePicker` 窄接口，并通过 `LocalDatePicker` 暂时提供给旧 Compose 页面；app 侧仍使用 MaterialDatePicker，只是注册位置改到 `ActivityDatePicker`。
- 已把主界面首页/账户页的 Tab 状态从 `IvyWalletCtx` 拆到 `shared:ui:navigation` 的 `MainTabState`，并删除首页更多菜单在旧上下文中的全局展开状态。
- 已把起始日和选中周期从 `IvyWalletCtx` 拆到 `shared:ui:legacy` 的 `PeriodState`，由 app 根部提供 `LocalPeriodState`，首页、交易、余额、饼图、报表、账户、分类、预算和设置页都改用这个明确状态。
- 已把旧全局屏幕宽高从 `IvyContext` 删除：首页更多菜单、主底部栏、借贷底部栏和交易列表底部留白改用当前 Compose `BoxWithConstraintsScope` 的 `maxWidth/maxHeight` 计算布局，根部 UI 包装器不再向全局上下文写入屏幕尺寸。
- 已把旧主题状态从 `IvyContext` 拆到 `shared:ui:core` 的 `ThemeState`，`RootViewModel` 初始化运行时主题，首页和设置页切换主题时更新同一个状态；旧设计 `context()`、`IvyContext`、`IvyWalletCtx` 和 `ivyWalletCtx()` 已删除。
- 已把旧主题访问入口从泛化的 `UI.colors/typo/shapes` 重命名为 `LegacyTheme.colors/typo/shapes`，功能和视觉不变，但调用点会明确标识这是旧主题兼容层。
- 已把 app 侧首次启动默认数据逻辑从 `com.ivy.wallet.domain.deprecated.logic` 迁到 `com.ivy.wallet.domain.startup`，并把交易提醒 WorkManager 调度/Worker 从 `domain.deprecated.logic.notification` 迁到 `com.ivy.wallet.notification.reminder`；这些代码仍保留旧实现，但不再伪装成共享 domain 逻辑。
- 已删除旧 building block 中最薄的 `SpacerVer/SpacerHor/SpacerWeight`、`ColumnRoot`、`DividerW/DividerH/DividerV/DividerSize`，相关调用方已改用 Compose 原生 `Spacer`、`Column` 和本地分隔线。
- 已删除旧 `IvyText` 包装，剩余调用方改用 Material3 `Text`。
- 已删除旧 `IvyIcon/IvyIconScaled/IconScale` 包装，剩余调用方改用 Material3 `Icon`、`Image` 或本地小函数；`shared:ui:core` 的旧 `l1_buildingBlocks` 包已清空。
- 已把旧颜色常量、`Gradient` 和颜色对比/转换 helper 从 `com.ivy.wallet.ui.theme` 迁到 `com.ivy.legacy.ui.theme`，避免通用旧 UI 工具继续挂在 Wallet 产品包名下；旧 `components/modal/wallet` 子包后续再分组迁移。
- 已把旧通用 UI 组件从 `com.ivy.wallet.ui.theme.components` 迁到 `com.ivy.legacy.ui.component`，包括旧按钮、工具栏、输入框、余额行、排序弹窗、底部栏和图标组件；功能和视觉保持不变。
- 已把旧弹窗层从 `com.ivy.wallet.ui.theme.modal` 迁到 `com.ivy.legacy.ui.modal`，`modal.edit` 同步迁到 `com.ivy.legacy.ui.modal.edit`；账户/分类/金额/周期/货币/借贷等旧弹窗仍保留实现，后续再按功能边界下沉。
- 已把旧 `wallet` UI 子包里的金额/货币展示和周期选择组件并入 `com.ivy.legacy.ui.component`；`com.ivy.wallet.ui.theme.*` 包名已经从源码中清空。
- 已把编辑交易/计划付款复用的旧底部表单组件从 `com.ivy.wallet.ui.edit.core` 迁到 `com.ivy.legacy.ui.edit.core`；除 app 自身锁屏包名外，旧 shared/feature UI 不再使用 `com.ivy.wallet.ui.*`。
- 已清理迁移过程中留下的 `com.ivy.legacy.legacy.ui.theme.*` 双重 legacy 包名：预算进度条和日期时间行归入 `com.ivy.legacy.ui.component`，弹窗名称输入归入 `com.ivy.legacy.ui.modal`。
- 已把 `SortOrder`、`CustomExchangeRateState`、`TransactionHistoryDateDivider` 从旧 `com.ivy.wallet.domain.data` 迁到 `com.ivy.legacy.domain.data`；这些类型仍然服务旧页面状态和旧交易列表，先明确标记为 legacy domain 数据。
- 已把 `CreateAccountData`、`CreateBudgetData`、`CreateCategoryData`、`CreateLoanData`、`CreateLoanRecordData`、`EditLoanRecordData` 从旧 `com.ivy.wallet.domain.deprecated.logic.model` 迁到 `com.ivy.legacy.domain.model`；这些仍是旧创建/编辑流程的参数对象，但不再挂在 deprecated logic 包名下。
- 已把 `shared:domain` 中剩余旧业务逻辑从 `com.ivy.wallet.domain.deprecated.logic` 迁到 `com.ivy.legacy.domain.logic`，包括账户/分类/预算/借贷 creator、计划付款逻辑、标题建议、账户/分类统计、汇率换算和借贷交易联动；同时把拼写错误的 `loantrasactions` 包名改为 `loantransactions`。
- 已把旧 FPAction/use-case 与 pure helper 从 `com.ivy.wallet.domain.action/pure` 迁到 `com.ivy.legacy.domain.action/pure`，并同步迁移 `ClosedTimeRange`、`IncomeExpensePair`、`IncomeExpenseTransferPair` 的旧统计值对象包名；这些代码仍是旧 domain 兼容层，但不再占用正式 Wallet 产品包名。
- 已把 `com.ivy.legacy.datamodel.temp` 中的旧实体/新模型 mapper 扩展函数迁到 `com.ivy.legacy.domain.mapper`；这些文件仍服务旧数据模型兼容，但不再使用含糊的 `temp` 包名。
- 已把旧兼容模型本体从 `com.ivy.legacy.datamodel` 迁到 `com.ivy.legacy.domain.model`，包括旧账户、分类、预算、借贷、计划付款规则、设置和旧交易实体转换扩展；字段和 `toEntity()` 映射保持不变。
- 已把 `FromToTimeRange` 和 `AccountData` 从跨模块混用的 `com.ivy.legacy.data.model` 拆到 `com.ivy.legacy.domain.model`；UI 侧 `TimePeriod/Month/LastNTimeRange` 暂时保留在旧 UI 包，因为它们仍依赖 UI 文案和时间格式化。
- 已把剩余 UI 兼容状态模型从 `com.ivy.legacy.data` 迁到 `com.ivy.legacy.ui.model`，并把周期选择模型迁到 `com.ivy.legacy.ui.model.period`；`com.ivy.legacy.data.*` 包名已经从源码中清空。
- 已把旧周期状态入口从 `com.ivy.legacy` 根包迁到 `com.ivy.legacy.ui.state`，并把旧 `rootScreen()` 桥接函数迁到 `com.ivy.legacy.ui.platform`；`shared:ui:legacy` 不再通过根包暴露迁移期 API。
- 已把旧设计兼容层从 `com.ivy.design.*` 迁到 `com.ivy.legacy.design.*`，包括旧 `LegacyTheme`、颜色常量、Compose helper 和 Material3 theme 包装；功能和视觉保持不变。
- 已把旧设计包里的通用 Compose helper 迁到 `com.ivy.ui.compose`，并把键盘隐藏 helper 迁到 `com.ivy.ui.platform`；这些工具不再带旧设计系统的过时标记。
- 已把当前仍在使用的主题状态 `ThemeState/LocalThemeState` 和 Material3 theme 包装迁到 `com.ivy.ui.theme`；旧 `LegacyTheme/IvyTheme` 继续作为兼容层调用它。
- 已把 `LocalDatePicker` 迁到 `com.ivy.ui.platform`，把 `LocalTimeConverter/LocalTimeProvider/LocalTimeFormatter` 迁到 `com.ivy.ui.time`；根部 UI 包装器只负责提供这些平台和时间 Local，不再定义它们。
- 已把旧 `IvyUI` 根包装器迁到 `com.ivy.ui.LegacyUiRoot` 并改名，`com.ivy.legacy.design.api` 包已经清空。
- 已把旧颜色选择器常量从 `com.ivy.legacy.design` 根包迁到 `com.ivy.legacy.ui.theme`，CSV 导入和旧颜色选择器继续使用同一组颜色值。
- 已把旧主题兼容层从 `com.ivy.legacy.design.l0_system` 迁到 `com.ivy.legacy.ui.theme.system`，旧设计包目录已经清空；功能和视觉保持不变。
- 已删除旧设计接口和默认设计外部传参，旧主题兼容层直接使用内部默认配置，去掉了无实际扩展点的设计系统抽象。
- 已删除 `:temp:legacy-code` 的 Gradle include、模块 build 文件，以及所有 app/feature 对 `projects.temp.legacyCode` 的依赖声明。
- 阶段 5 的模块拆解目标已经完成：仓库中不再有被 Gradle include 的 `temp:*` 模块。后续工作转为拆除 `shared:ui:legacy` 中剩余旧上下文、旧设计 API 和旧 UI 兼容模型。

迁移分组：

1. 旧数据模型与 mapper
   - `legacy.datamodel`
   - `datamodel.temp.*`
   - 目标：能替换为 `shared:data:model` 的就替换；仍需兼容旧 UI 的放到明确的 `legacy-model` 包，避免散落。
2. 旧 domain action
   - `FPAction`
   - `then/thenMap/thenFilter/thenSum`
   - `domain/action/*`
   - 目标：改成普通 use case 或直接内联到 ViewModel/domain use case。
3. 旧业务逻辑
   - `AccountCreator`
   - `CategoryCreator`
   - `BudgetCreator`
   - `LoanCreator`
   - `LoanTransactions*`
   - `ExchangeRatesLogic`
   - 目标：迁入 `shared:domain` 或对应 feature 的 domain 子包。
4. 旧 UI 组件和 modal
   - `AccountModal`
   - `CategoryModal`
   - `ChoosePeriodModal`
   - `CalculatorModal`
   - `CurrencyPicker`
   - `ReorderModal`
   - 目标：通用组件进 `shared:ui:core`；功能专用组件进对应 feature。
5. 工具函数
   - 纯 Kotlin：进 `shared:base`。
   - Compose/UI：进 `shared:ui:core`。
   - Android 平台能力：进 `app` 或 platform service。
6. 启动和全局上下文
   - `IvyWalletCtx`（已删除，后续继续处理其原先承载的主题和平台能力替代层）
   - `IvyComposeApp`
   - `InitialDataSetup`
   - 目标：拆成明确的 app startup、preferences、theme state、date/time picker host。

完成标准：

- feature 不再依赖 `projects.temp.legacyCode`。
- app 不再依赖 `projects.temp.legacyCode`。
- 删除 `:temp:legacy-code` include 和目录。

### 阶段 6：偏好设置与本地配置重构

目标：把 `SharedPrefs` 和零散 DataStore key 收敛成明确 repository。

建议建立：

- `PreferencesRepository`
- `ThemePreference`
- `PrivacyPreference`
- `NotificationPreference`
- `DisplayPreference`
- `StartupPreference`

迁移内容：

- app lock enabled
- show notifications
- hide current balance
- hide income
- transfers as income/expense
- theme
- initial setup completed
- backup completed
- start day of month

当前进展：

- 已新增 `com.ivy.domain.preferences.AppPreferences` 作为 `SharedPrefs` 的业务语义封装，底层仍使用原有 `ivy_wallet_prefs` 文件和原 key，暂不改变存储格式、备份格式或恢复逻辑。
- 已把设置页、根启动流程、首次启动默认数据、交易提醒、起始日 action、隐藏余额/收入 action，以及账户/交易/饼图/旧账户逻辑里的全局偏好读取迁到 `AppPreferences`。
- 已把分类排序、最近选择账户和客户旅程卡片关闭状态迁到 `AppPreferences`，feature 层不再直接注入 `SharedPrefs`。
- 已把重置钱包流程改为通过 `AppPreferences.clearAll()` 清空 legacy 偏好；app/feature 层不再直接注入 `SharedPrefs`。
- 备份恢复仍保留原始 `SharedPrefs` 访问；它需要处理全部历史 key 和外部备份格式，后续与备份格式重构一起处理。

目标：

- feature 不直接读写 `SharedPrefs`。
- `SharedPrefs` 最终删除或降级为 DataStore 的内部兼容实现。
- `shared/domain/features` 改名或收敛为 `preferences`，避免“高级特性”历史语义。

### 阶段 7：数据层和数据库遗留清理

目标：删除云同步、用户表和旧设置表的历史负担，但必须谨慎。

候选内容：

- `UserEntity`
- `UserDao`
- `users` table
- `SettingsEntity`
- `isSynced`
- `isDeleted`
- `lastSyncedTime`
- `LogoutLogic.cloudLogout`

建议顺序：

1. 把 `LogoutLogic` 改成 `ResetAllDataUseCase`。
2. 让清空本地数据流程不再依赖 `UserDao`。
3. 新增 Room migration 删除或废弃 `users` 表。
4. 评估 `isDeleted/isSynced` 字段：
   - 如果只是历史同步残留，写迁移删除。
   - 如果仍被软删除逻辑使用，先替换成直接删除或明确本地删除语义。
5. 更新备份恢复数据结构和测试。

风险：

- 会影响已有安装数据。
- 会影响备份文件兼容性。
- 必须单独提交、单独验证。

### 阶段 8：平台能力拆分

目标：减轻 `RootActivity` 和 `RootViewModel`。

建议拆分：

- `FilePickerService`
  - create document
  - open document
- `ShareService`
  - share CSV
  - share ZIP
- `BiometricLockController`
  - app lock 状态
  - biometric prompt
  - inactive timer
- `DateTimePickerHost`
  - date picker
  - time picker
- `ExternalIntentLauncher`
  - open browser
  - open market page

同时调整：

- `RootScreen` 已从 `shared:domain` 移到 `shared:ui:core/platform`。
- 与 Activity 强绑定的实现放到 `app` 的 platform 包。
- feature 后续通过窄接口表达需求，不继续依赖大而全的 `RootScreen`。

当前进展：

- `ActivityDatePickerHost` 承接 Material date picker 注册，`RootActivity` 不再直接构造 `MaterialDatePicker`。
- `ActivityFilePickerHost` 承接 Activity Result 文件创建/打开注册，`RootActivity` 不再保存 launcher 和文件回调。
- `ExternalIntentLauncher` 承接浏览器跳转、Google Play 跳转、CSV 分享和 zip 分享。
- `BiometricAuthenticator` 承接系统生物识别 Prompt 构造。
- `RootScreen` 已移到 `com.ivy.ui.platform.RootScreen`，domain 不再暴露 Android `Uri` 平台接口。

### 阶段 9：feature 模块收敛

目标：减少个人维护心智负担。

短期保持现有 feature 模块，先去掉 temp 依赖。长期可以合并：

候选合并方向：

- 核心记账：
  - `home`
  - `main`
  - `accounts`
  - `transactions`
  - `edit-transaction`
  - `search`
- 统计分析：
  - `balance`
  - `reports`
  - `piechart`
- 扩展记账：
  - `budgets`
  - `loans`
  - `planned-payments`
  - `exchange-rates`
- 数据与设置：
  - `settings`
  - `import-data`

如果后续只追求最简单，可以最终合并为：

- `:feature:wallet`
- `:feature:analytics`
- `:feature:settings`

但合并模块应放在 temp 依赖消除之后，否则只是把旧耦合搬到更大的模块里。

### 阶段 10：最终依赖方向

目标依赖方向：

```text
app
  -> feature:*
  -> shared:ui:navigation
  -> shared:ui:core
  -> shared:domain
  -> shared:data:core
  -> shared:data:model

feature:*
  -> shared:ui:core
  -> shared:ui:navigation
  -> shared:domain
  -> shared:data:model

shared:domain
  -> shared:base
  -> shared:data:model

shared:data:core
  -> shared:base
  -> shared:data:model

shared:ui:core
  -> shared:base
  -> shared:domain only if unavoidable
```

不希望出现：

- `shared:domain -> shared:ui:*`
- `shared:domain -> Android Activity/platform interface`
- `feature -> temp:*`
- `shared:data:core/src/main -> fake/test helper`
- `app -> every low-level library because of convenience`

## 高风险区域

这些内容可以重构，但不要和普通清理混在同一提交里：

- Room schema、migration、entity 字段删除。
- 备份恢复格式变化。
- CSV 导入导出字段变化。
- `InitialDataSetup` 和首次启动默认数据。
- 账户余额、转账、借贷、预算统计逻辑。
- 汇率同步和历史汇率换算。
- App lock、生物识别、文件选择和分享。
- 动态图标资源，例如 `ic_custom_*`、`ic_vue_*`。

## 近期推荐执行顺序

推荐从低风险到高风险这样推进：

1. **资源和文案瘦身**
   - 删除未引用的第三方导入 logo、widget 预览图、推广/分享文案。
   - 可选：只保留中文和默认资源。
2. **构建约定插件整理**
   - 先让 `shared:base`、`shared:data:model` 不再使用 `ivy.feature`。
   - 再处理 `shared:data:core`、`shared:domain`。
3. **测试 helper 归位**
   - 新建或整理 `shared:test-support`。
   - 移动 fake DAO、test dispatcher、test resource provider、test time converter。
4. **收尾旧设计兼容层**
   - `:temp:old-design` 已删除。
   - 后续替换 `LegacyTheme`、旧颜色常量和旧组件。
5. **迁移 `temp:legacy-code`**
   - 从工具函数和 modal 开始。
   - 再处理旧 domain action。
6. **偏好设置重构**
   - 建立 `PreferencesRepository`。
   - 消除 feature 直接访问 `SharedPrefs`。
7. **平台能力拆分**
   - 拆 `RootActivity`。
   - 移动 `RootScreen`。
8. **数据库遗留迁移**
   - 用户表、同步字段、旧设置表单独处理。
9. **feature 模块合并**
   - 在 temp 依赖减少后再做。

## 每轮提交建议

提交粒度建议：

- `chore: 删除未引用资源残留`
- `build: 拆分 Android library 构建约定`
- `test: 移动数据层 fake DAO 到测试支持模块`
- `refactor: 迁移旧颜色常量到 ui core`
- `refactor: 迁移旧账户弹窗到 accounts feature`
- `refactor: 建立偏好设置仓库`
- `refactor: 拆分 RootActivity 平台能力`
- `data: 删除 legacy users 表`

每轮提交说明应包含：

- 删除/迁移了什么。
- 保留了什么。
- 是否编译。
- 是否安装。
- 是否跑测试。

## 当前下一步

下一步建议执行：

1. 继续收窄 `RootScreen`：把分享文件、打开外部链接、构建版本信息拆成更小的 UI/platform 接口，逐步减少 feature 对 Activity 大接口的依赖。
2. 继续减轻 `RootActivity`：把应用锁窗口保护、暂停/恢复计时和生物识别触发逻辑整理成更明确的 app lock controller。
3. 平台层稳定后再进入数据库遗留清理：先梳理 `UserEntity/UserDao`、`SettingsEntity`、同步字段和备份恢复格式，不直接改 schema。
