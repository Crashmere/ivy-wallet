# Ivy Wallet 个人维护分支重构计划

这个仓库现在作为个人使用和个性化开发用途维护，不再面向上游协作、社区运营、应用商店发布或多人开发流程。后续重构的目标不是重写 Ivy Wallet，而是在不影响当前已有功能的前提下，把项目逐步整理成模块结构清晰、职责分配合理、依赖方向明确、适合单人长期维护的 Android 项目。

## 当前状态摘要

已经完成的清理可以概括为几类：

- 删除社区协作、开源展示和发布流程相关内容：GitHub workflow、Issue/PR 模板、开发规范、Fastlane、发布日志、贡献者、开源致谢、投票问卷、原仓库入口、分享 Ivy、Google Play 评分、Telegram/推广文案等。
- 删除 Google/Firebase/商店发布相关接线：Google Services、Crashlytics、Google Play Review、Firebase Firestore、GitHub 自动备份迁移残留等。
- 删除不再需要的功能模块和入口：contributors、releases、attributions、poll、disclaimer、onboarding、widget，以及第三方 App 导入模板和教程。
- 删除失去实现意义的云端删除入口：设置页不再显示“删除云端数据”链路，`ResetWalletDataUseCase` 不再保留空的 cloud reset 方法。
- 整顿设置页：合并原高级特性页，改成个人偏好设置；重排设置分组；删除匿名账户入口和首页问候语。
- 精简测试和预览基础设施：删除 Paparazzi 截图测试、快照图片、仅服务 IDE 的 Compose `@Preview` 示例函数和预览 helper。
- 持续清理 `temp:legacy-code` 中确认无引用的旧代码、工具、组件和残留模型。
- 删除空的 `:shared:data:core-testing` 模块，并把测试专用 `FakeRepositoryMemo` 从生产源码移入测试源集。
- 删除未引用的第三方导入 logo、widget 预览/图标、推广/分享/捐赠图片，以及 `help_us_grow` 多语言推广文案。
- 删除无代码引用的 `data_synced_to_cloud` 多语言云同步完成文案。
- 删除 `:temp:old-design` 模块；旧设计 API 先迁入 `shared:ui:core` 作为兼容层，随后已逐步归位到更明确的 `legacy.ui.theme`/`legacy.ui.theme.system` 包名。
- 删除 `:temp:legacy-code` 模块；剩余旧全局上下文暂时收敛在 `shared:ui:legacy`，并已把周期状态、文件选择、日期选择、主 Tab 状态等职责逐步拆出。
- 整理部分 Gradle 约定插件：基础 shared 模块、数据核心模块和 domain 模块已经开始脱离面向页面的 `ivy.feature` 配置。
- 开始拆分 `RootActivity` 平台能力：文件创建/打开、Material 日期选择器、生物识别弹窗和 CSV/zip 分享已经移入 `app` 的 platform 边界；`RootScreen` 大接口已删除，feature 改为依赖更窄的 UI platform 接口。
- 继续清理历史包名：旧 Room migration 和 type converter 已从 `com.ivy.domain.db` 归位到 `com.ivy.data.db`，旧 UI 组件中误挂到 domain 包名下的 `ListItem/IvyColorPicker` 已归位到 `com.ivy.legacy.ui.component`。

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
| `:shared:base` | 纯基础能力：时间、线程等少量跨层端口 | `SharedPrefs` 业务 key、资源抽象、UI 组件、Room、Ktor、Compose |
| `:shared:data:model` | 纯数据模型和值对象：Account、Transaction、Category、Tag、ExchangeRate、primitive value object | Room DAO、Repository、Android Context、UI 文案 |
| `:shared:data:core` | 数据实现：Room、DataStore、FileSystem、Repository、备份恢复、远程汇率数据源 | UI 状态、Compose、测试 fake、feature 专用逻辑 |
| `:shared:domain` | 业务 use case：余额、统计、CSV 导出、汇率换算、设置/偏好业务规则 | Room 插件、Ktor 具体实现、Android Activity 接口、UI 资源 |
| `:shared:ui:core` | Material3 主题、通用 Compose 组件、图标、资源端口、时间/金额 UI 格式化、UI CompositionLocal | 数据库、Repository 实现、业务写入逻辑 |
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
- `TestDispatchersProvider`、`TestTimeConverter` 已在 `shared:base-testing`；`TestResourceProvider` 已随资源端口迁到 `shared:ui:core/src/test`。
- `FakeRepositoryMemo` 已移到 test/androidTest 源集。
- 生产源码中的空 `TestIdlingResource` 计数器已删除，调用方不再插入测试空闲计数。

问题：

- 仍需要继续防止新的测试 helper 回流到 `src/main`。
- 功能测试如果后续重新需要空闲同步，应在 androidTest 专用边界实现，而不是回到生产 ViewModel。

目标：

- 生产 main 源集不承载 fake DAO、test dispatcher、test resource provider 或空闲同步计数器；资源测试替身留在 UI 测试源集。
- 测试支持能力集中在测试源集或 `shared:*testing` 模块。

### 4. 数据层仍有同步、登录和云端历史痕迹

现状：

- 当前主实体已经删除 `isSynced`、`lastSyncedTime` 等运行时云同步字段；`isDeleted` 仍作为本地软删除过滤字段保留。
- 历史 `users` 表已通过 130 -> 131 Room migration 删除，`UserEntity/UserDao` 和重置流程里的用户表清空依赖已移除。
- `SettingsEntity` 仍是旧设置模型，部分偏好又在 `SharedPrefs/DataStore` 中。

问题：

- 个人本地使用不需要云同步语义。
- 数据模型承担了历史兼容和当前功能两种职责。
- 删除字段/表会影响 Room schema 和历史数据库迁移，不能粗暴删除。

目标：

- 先让运行时代码不再依赖旧同步/用户表概念。
- 继续通过明确 Room migration 删除或废弃字段。
- 备份恢复格式同步调整，并保留测试覆盖。

### 5. 平台能力集中在 `RootActivity`

现状：

- `RootActivity` 仍负责主题、根导航、应用锁生命周期和根部 Compose 宿主。
- 文件创建/打开、CSV/zip 分享、Material 日期选择器和生物识别弹窗已经拆到 `app/src/main/java/com/ivy/wallet/platform`。
- 构建版本信息、文件分享和应用语言设置跳转已拆成 `BuildInfoProvider`、`FileSharer`、`LocaleSettingsLauncher` 这类窄接口，放在 `shared:ui:core` 的 `com.ivy.ui.platform` 包。
- 原 `RootScreen` 大接口已经删除。

问题：

- Activity 仍偏重，应用锁生命周期和根导航还混在一起。
- feature 已不再通过 `LocalContext.current as ...` 强转 Activity 获取平台能力；剩余平台行为主要集中在 app/platform、数据文件读写和 Compose UI 边界。

目标：

- 把平台能力拆成小 service/controller：
  - `FilePickerService`
  - `ShareService`
  - `BiometricLockController`
  - `DateTimePickerHost`
- 删除无调用方的外部链接/商店跳转平台方法。
- 保持 feature 只依赖 `FileSharer`、`BuildInfoProvider` 等窄接口。

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

- 新增 `ivy.android-library` 作为更清晰的 Android library 基础约定；旧 `ivy.kotlin-android` 兼容别名已经删除，当前没有模块使用它。
- `shared:base`、`shared:data:model`、`shared:data:model-testing` 已从 `ivy.feature` 迁出，不再默认启用完整 Compose UI 配置。
- `shared:base` 仍显式保留 Hilt，因为当前源码仍包含基础 DI 绑定；Compose runtime 和 kotlinx serialization 已经移除。
- `shared:data:model` 已移除轻量 `compose-runtime`，纯数据模型不再依赖 UI runtime。
- 过渡用的 `ivy.compose-runtime` 插件已经删除；当前非页面模块不再需要轻量 Compose 编译配置。
- `ivy.integration.testing` 已从 `ivy.feature` 改为基于 `ivy.android-library`，避免因为集成测试配置把完整 Compose UI 配置带入数据层。
- `shared:data:core`、`shared:domain` 已从 `ivy.feature` 迁出，改为显式声明基础 Android library、Hilt、Room 或测试配置等各自实际需要的能力。
- `shared:domain` 已移除空 androidTest 源集使用的 `ivy.integration.testing` 插件；domain 当前只保留 JVM 单元测试，Room migration 和备份恢复这类设备测试继续留在 `shared:data:core`。
- `shared:domain` 已移除 `ivy.room` 插件；主源码只显式保留 Hilt，测试也不再为了 domain 行为验证创建内存 Room 数据库。
- `shared:domain` 已移除 Ktor 依赖；汇率同步测试改用 `ExchangeRateStore` fake 验证业务转换与保存行为，真实网络 client 继续留在 data core 实现边界。
- `ivy.room` 已从 `ivy.module` 改为基于 `ivy.android-library`，不再隐式带入 Hilt 和 kotlinx serialization；`shared:data:core` 改为显式声明这两个依赖。
- `shared:ui:core`、`shared:ui:legacy`、`shared:ui:navigation` 已从 `ivy.feature` 迁到 `ivy.compose`；shared UI 模块不再伪装成 feature。
- `ivy.compose` 已收敛为纯 Android Compose 配置，不再隐式套用 `ivy.module` 或引入未使用的 Molecule 插件；feature 模块继续由 `ivy.feature` 组合 Hilt 与 Compose 能力，需要 Hilt Module 的 shared UI 模块才显式声明 `ivy.hilt`。
- 版本目录里的 Compose LiveData 依赖别名已从临时/拼写错误的 `compose-runtime-livedate-temp` 改为 `compose-runtime-livedata`，保留依赖本身不变。
- 删除无运行时调用方的 `FormatMoneyUseCase` 和对应测试，`shared:ui:core` 不再因为这段旧金额格式化草稿依赖 `shared:domain` 或 DataStore；当前实际金额展示继续使用既有 data model currency formatting 与旧 UI 展示逻辑。
- `shared:ui:navigation` 已移除未使用的 `shared:domain` 依赖；导航模块当前只依赖基础类型、UI core 和自身导航状态。
- app 模块已移除自身不再直接使用的 Ktor、Room、OpenCSV、Keval、RecyclerView、AndroidX Security 和 Arrow 依赖；源码层已经不再直接引用 data core DAO/repository，Gradle 层保留对 data-core 的实现依赖以纳入 Hilt 绑定。
- app 模块重新显式依赖 `shared:data:core` 作为运行时数据实现模块；feature/domain 仍只依赖 data 端口，data-core 的 Hilt Module 负责把 Room、DataStore、备份、文件和远程汇率实现绑定进应用图。
- `ivy.android-library` 不再给所有 Android library 默认添加 Arrow；`shared:data:model` 因公开 `Either/Raise` API 显式用 `api` 暴露 Arrow，其他实际直接使用 Arrow 的模块改为各自声明 `implementation(libs.bundles.arrow)`；旧 FRP helper 移出后，`shared:base` 不再需要 Arrow。
- `ivy.android-library` 不再给所有 Android library 默认添加 Timber；domain 汇率同步和饼图点击逻辑中的调试日志已删除，当前只保留 app 日志初始化/锁屏认证日志以及 data core 的网络/导入错误日志。
- `ivy.android-library` 不再给所有 Android library 默认添加整套单元测试依赖；当前有 `src/test` 的 `shared:base`、`shared:data:model`、`shared:data:model-testing`、`shared:data:core`、`shared:domain` 和 `shared:ui:core` 改为在各自模块里显式声明测试 bundle。
- `shared:data:core` 的 DataStore 依赖已从 `api` 收窄为 `implementation`；DataStore 绑定仍由 data core 提供，但不再通过 data core 传递暴露给其他模块。
- `shared:domain` 已移除 AndroidX DataStore 依赖；偏好开关的存储能力抽成 `PreferenceToggleStore` 端口，DataStore 读写和清空由 `shared:data:core` 实现，domain 只保留业务级 `PreferenceToggleRepository` 和开关元数据。
- `shared:data:api` 已显式暴露 Arrow 依赖；`ExchangeRateStore` 的公开签名直接使用 `Either`，不再依赖 `shared:data:model` 间接传递 Arrow。
- `feature:accounts` 已显式声明 Arrow 依赖；账户页源码直接使用 `toOption`，不再靠 `shared:data:model` 传递提供。
- `ivy.module` 不再默认启用 kotlinx serialization；当前 `ivy.feature` 页面模块没有序列化源码引用，序列化能力只保留在 `shared:data:model` 和 `shared:data:core` 等实际需要的模块中。
- app 模块已移除 Kotlin serialization 插件；应用壳本身没有序列化源码，序列化继续由 `shared:data:model` 和 `shared:data:core` 提供。
- 空壳 `ivy.module` 约定插件已删除；`ivy.feature` 现在只保留 `ivy.hilt` 和 `ivy.compose` 两个页面能力入口，不再重复声明基础 Android library 插件。
- app 与 `ivy.android-library` 已移除重复的旧式 `kotlin-android` 插件 ID，只保留正式 `org.jetbrains.kotlin.android` 插件；版本目录中仅包含 Android 协程运行时的依赖 bundle 已改名为 `kotlin-android-runtime`，避免和插件 ID 混淆。
- 根目录 `temp/` 已加入 `.gitignore`；旧 `temp:*` 模块不再被 Gradle include，后续本地残留构建目录不会被误加回版本库。

### 阶段 3：测试支持代码归位

目标：生产源码不再包含测试 fake。

候选迁移：

- `shared:data:core/src/main/java/com/ivy/data/db/dao/fake/Fake*Dao.kt`
- `shared:base/src/main/java/com/ivy/base/TestDispatchersProvider.kt`
- `shared:base/src/main/java/com/ivy/base/resource/TestResourceProvider.kt`（已迁到 `shared:ui:core/src/test`）
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
- 测试源集中的 `FakeSettingsDao`、`FakePlannedPaymentDao`、`FakeLoanRecordDao` 已补齐基础内存行为，不再保留 `TODO("Not yet implemented")` 作为潜在测试崩溃点。
- 已删除生产源码中的空 `TestIdlingResource` 以及 Root/Main/Import/Loans ViewModel 中的空调用。

### 阶段 4：消灭 `temp:old-design`

目标：先迁移仍有价值的 18 个文件，再删除模块。

迁移分组：

1. 颜色与主题
   - `Colors.kt`
   - `IvyColors.kt`
   - `IvyTheme.kt`
   - `LegacyThemeDefaults.kt`（已由旧多实现设计抽象收敛为内部默认配置）
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
- 已把旧 FRP 组合 helper、`FPAction` 和 `Res` 从 `temp:legacy-code` 迁入项目，并移除重复的 legacy `onScreenStart`；旧 UI 测试开关后续已下沉到 `shared:ui:legacy`，FRP helper 后续归位到 `shared:domain`。
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
- 已把金额输入弹窗、计算器弹窗和缓冲金额弹窗迁入 `shared:ui:legacy`。金额键盘的“标准键盘布局”偏好已经改为由 app 根部提供 legacy UI 专用偏好入口，不再把 domain 的 `PreferenceToggles/BoolPreference` 类型暴露给旧 UI；`shared:ui:legacy` 仍因旧账户/借贷弹窗和周期模型暂时依赖 legacy domain 模型，后续按 UI model 边界继续拆。
- 已把旧 `legacy.datamodel` 整体迁入 `shared:domain`，把旧创建参数模型迁入 `shared:domain`，并把账户/分类/借贷创建参数里的颜色从 Compose `Color` 改为普通 ARGB `Int`，由 UI 弹窗在边界处转换。
- 已把旧颜色选择器、账户弹窗、分类弹窗、借贷弹窗和借贷记录弹窗迁入 `shared:ui:legacy`。颜色选择器移除了旧付费锁显示分支，不再依赖会员状态。
- 已把旧 UI 状态模型 `AppBaseData`、`LegacyDueSection`、`BufferInfo`、`EditTransactionDisplayLoan` 迁入 `shared:ui:legacy`，作为迁移期的 UI 兼容数据。
- 已把搜索框、收入/支出卡片、详情工具栏、标签弹窗、交易卡片和交易列表组件迁入 `shared:ui:legacy`；交易卡片查找账户/分类时改为只使用调用方传入的数据，去掉了对 `IvyWalletCtx` 缓存的读取。
- 早期迁入 `shared:domain` 的旧页面状态值对象已继续下沉：`SortOrder` 进入分类 feature，`CustomExchangeRateState` 进入编辑交易 feature，`TransactionHistoryDateDivider` 进入 `shared:base` 的 legacy history item 模型包。
- 已把编辑交易/计划付款复用的底部表单组件迁入 `shared:ui:legacy`；`EditBottomSheet` 改用 Compose 屏幕高度，不再为了底部操作条位置读取 `IvyWalletCtx`。
- 已把旧 domain 层对 `IvyWalletCtx` 的直接依赖拆掉：账户/分类缓存 action 已删除，起始日状态由 `PeriodState` 和正式 settings use case 承接，调用方显式更新旧 UI 上下文；借贷交易逻辑去掉固定为 true 的付费判断分支。
- 已把旧 `domain/action`、`domain/pure`、旧汇率换算逻辑、账户数据 action、交易范围过滤 action 迁入 `shared:domain`。
- 已把旧 creator、计划付款逻辑、标题建议、账户/分类统计逻辑和借贷交易联动逻辑迁入 `shared:domain`；后续再逐步从 legacy 包迁到正式 use case。
- 已把仍依赖 Android 字符串资源的 `PreloadDataLogic` 从 `temp:legacy-code` 移到 app 默认数据初始化边界，避免 `temp` 继续承载旧业务逻辑。
- 已精简 `PreloadDataLogic`：删除没有运行时入口的账户/分类建议列表，默认账户预置直接创建当前 `data.model.Account`，不再通过旧 `legacy.domain.model.Account` 转换；首次启动默认现金、银行账户和默认分类保持不变。
- 已把旧全局上下文入口 `IvyWalletCtx`、`ivyWalletCtx()` 和 `rootScreen()` 迁入 `shared:ui:legacy` 后继续拆分；目前这些旧全局入口都已经删除。
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
- app 主源码不再使用 deprecated 的全局 `stringRes()`；首次启动默认数据、应用锁日志和交易提醒通知文案都改为通过注入式 `ResourceProvider` 读取资源，继续缩小全局 `appContext` 的影响面。
- 交易提醒调度器不再使用 deprecated 的全局 `timeNowLocal()`，改为注入 `TimeProvider` 获取当前本地时间；每天 20:00 的提醒调度语义保持不变。
- 第一批 ViewModel 已停止使用 deprecated 的全局 UTC 时间函数：设置导出文件名、首页/余额/交易/饼图的月份切换、报表 upcoming/overdue 判断和报表导出文件名都改为通过注入式 `TimeProvider` 获取当前时间。
- 第一批非 UI feature 逻辑已停止使用 deprecated 的全局 `stringRes()`：交易页/报表页/饼图中的特殊分类名称，以及首页客户旅程卡片文案都改为通过注入式 `ResourceProvider` 获取字符串资源。
- 第一批 feature 屏幕层已停止使用 deprecated 的全局 `stringRes()`：首页、交易页和报表页的空状态/标签文案改为 Compose 原生 `stringResource()`，列表构建块继续接收普通字符串参数。
- feature 源码中的剩余预算类型和借贷类型显示也已停止使用全局 `stringRes()`；这些文案只在 Composable 调用点使用，因此改为 Composable 格式化函数内部调用 `stringResource()`。
- `shared:ui:legacy` 的收入/支出卡片、统计工具栏和旧交易列表组件已停止使用全局 `stringRes()`；旧交易列表不再提供依赖全局上下文的默认空状态标题，调用方需显式传入普通字符串。
- 旧日期/周期显示链路已停止使用全局 `stringRes()`：月份模型改为只保存 `monthValue`，月份名、interval 单位、Last N 周期和“今天/昨天/明天”文案都在 Composable 显示边界通过 `stringResource()` 获取；无调用方的旧 `stringRes()` 兼容函数已经删除。
- 已删除 `shared:base` 中最后的全局 `appContext` 入口；`IvyAndroidApp` 不再在启动时写入全局 Context，旧 `SharedPrefs` 和平台类继续通过构造参数或 Hilt 注入获取 Context。
- 第一批 UI 层当前时间读取已停止使用 deprecated 的全局时间函数：饼图点击计时改用 `SystemClock.elapsedRealtime()`，旧交易卡片、日期分隔、日期格式化和周期选择弹窗改为通过 `LocalTimeProvider` 获取当前日期/时间，并删除无调用方的 `getTrueDate()` 桥接函数。
- 周期模型和 domain 交易过滤已停止使用 deprecated 的全局当前日期函数：`TimePeriod`、`Month`、`PeriodState`、月份切换和 upcoming/overdue 过滤都通过显式传入的 `TimeProvider` 或 `LocalDate` 计算当前周期与今天边界。
- 已删除 `shared:base` 中旧全局当前时间函数和手写 UTC/local 转换 helper；旧日期展示改为用 `LocalDateTime.toInstant(UTC)` 加标准 `DateTimeFormatter.withZone(...)` 格式化，计划付款页面顺手清理了残留的无用旧时间 import。
- 旧 `DateTimeUtil` 毫秒转换先从 `com.ivy.base.legacy` 迁出，随后已继续下沉到 `shared:data:core` 的 `com.ivy.data.db`；Room type converter 和 LocalDateTime serializer 仍用 `toUtcEpochMilli()` / `epochMilliToUtcLocalDateTime()` 保留原有 UTC 持久化语义。
- 旧 `MVVMExt` 已拆出 `com.ivy.base.legacy`：原 LiveData 只读 helper 已删除，仍使用 LiveData 的 ViewModel 直接暴露 `LiveData<T>` 类型；后续 `StateFlow.readOnly()` 与旧 dispatcher helper 也已分别改为标准库 API 和标准协程调度器调用。
- 字符串本地化大小写/判空 helper 已从 `shared:base:legacy` 拆出；后续无抽象价值的 `com.ivy.base.text` 包也已删除，调用方改用 Kotlin 标准库或文件内局部扩展。默认系统法币 helper 已归位到数据模型层。
- 其余通用 helper 已继续拆出 `shared:base:legacy`：列表交换迁到 `com.ivy.base.collections`，随机区间数迁到 `com.ivy.base.random`，zip/unzip 迁到 `com.ivy.base.io`，余额正负号 helper 迁到 `com.ivy.ui.money`。
- `shared:base` 中拼写错误的 `com.ivy.base.kotlinxserilzation` 包已更正为 `com.ivy.base.kotlinxserialization`；serializer descriptor 和编码方式保持不变。
- 旧函数式 helper 已从顶层 `com.ivy.frp` 归入 `com.ivy.base.frp`；`shared:base` 源码现在只暴露在 `com.ivy.base.*` 根包下。
- 日期、时间范围和 `IntervalType` 周期递增 helper 已迁到 `com.ivy.base.time`；旧交易兼容模型 `Transaction/LegacyTransaction/TransactionHistoryItem/LegacyTag` 已迁到 `com.ivy.base.model.legacy`，继续留在 `shared:base` 以保持现有导航和旧 UI 依赖不变。
- 旧主题枚举已迁到 `com.ivy.data.model.Theme`，数据库仍通过枚举 `name` 持久化，现有设置值不变；旧 `SharedPrefs` 已迁到 `com.ivy.base.prefs.SharedPrefs`，同一个 `ivy_wallet_prefs` 文件名和 key 保持不变。
- `shared:base` 中的 `com.ivy.base.legacy` 包已经清空；后续重点从“迁出 legacy 包名”转向“减少 Android SharedPreferences 对 domain/data 的扩散”。
- 偏好读写已抽出 `PreferenceStore` 接口，`SharedPrefs` 只作为 Android 实现通过 Hilt 绑定；业务 key 集中到 `AppPreferenceKeys`，domain 和数据备份恢复不再直接依赖 `SharedPrefs` 具体类。
- 偏好 toggle 的 UI 读取边界已从 domain 拆到 `shared:ui:legacy`：旧金额键盘只接收 legacy UI 专用的键盘布局 Flow，domain 中的 `BoolPreference` 只保留 key、默认值和分组等元数据。
- `shared:domain` 已删除剩余 legacy 数据模型上的 Compose `@Immutable` 注解，并移除 `ivy.compose-runtime` 插件；domain 不再需要 Compose 编译配置。
- `shared:domain` 已完全移除 Ktor/Room 测试依赖；汇率同步验证改为 JVM 单元测试，domain 只关心 `ExchangeRateStore` 端口行为。
- `shared:data:core` 的 AndroidManifest 已删除被 AGP 忽略的 `package` 属性，命名空间统一由模块 `namespace = "com.ivy.data"` 提供。
- `shared:data:core` 的测试 fake DAO 已停止使用 Compose Locale helper，并移除 `ivy.compose-runtime` 插件；数据层不再为测试字符串处理引入 Compose 配置。
- `shared:data:model` 已删除剩余数据类上的 Compose `@Immutable` 注解，并移除 `compose.runtime` 依赖；纯数据模型不再依赖 UI runtime。
- `shared:base` 已删除基础枚举和旧交易兼容模型上的 Compose `@Immutable` 注解，并移除 `compose.runtime` 依赖；基础层不再依赖 UI runtime。
- `shared:base` 已移除只为旧 LiveData helper 保留的 `androidx.lifecycle:lifecycle-livedata-core` 依赖；基础层目前不再依赖 Lifecycle。
- `shared:ui:navigation` 和 `shared:ui:legacy` 已移除 `ivy.hilt` 插件；它们只保留轻量 `javax.inject` 注解依赖，继续通过 app 的 Hilt 图提供 `Navigation`、`MainTabState` 和 `PeriodState` 单例。
- `shared:ui:legacy` 已移除对 `shared:domain` 的 Gradle 依赖；旧 UI 兼容层只依赖基础模型、数据模型、UI core 和导航，不再反向接触 domain use case。
- `shared:ui:legacy` 已移除显式 Arrow 依赖；分类和标签编辑弹窗不再用 `either/bind` 组装 UI 表单结果，改回直接构造已校验的值对象。
- `shared:ui:navigation` 已从顶层 `com.ivy.navigation` 归入 `com.ivy.ui.navigation`，模块 namespace 与 UI 分层保持一致；路由对象和导航状态行为不变。
- `app` 的 AndroidManifest 已删除被 AGP 忽略的 `package` 属性，应用命名空间继续由模块 `namespace = "com.ivy.wallet"` 提供。
- `app` 内部启动初始化和整库重置实现已从 `com.ivy.wallet.domain.*` 归入 `com.ivy.wallet.startup/reset`；这些类仍只是 app 侧编排和 domain 接口实现，不再伪装成正式领域层。
- 锁屏页已改用 Material3 `Button/MaterialTheme`，不再直接依赖旧 `LegacyTheme/IvyButton`；自动弹出生物识别和手动解锁逻辑保持不变。
- `RootActivity` 已用 `OnBackPressedDispatcher` 承接系统返回键，不再覆盖废弃的 `Activity.onBackPressed()`；旧 `Navigation.onBackPressed()` 仍作为迁移期兼容入口保留。
- 快捷设置磁贴服务已删除空生命周期 override，并把旧系统的 `startActivityAndCollapse(Intent)` 调用隔离为 compat 方法；Android 14+ 继续使用 `PendingIntent` 分支。
- 已删除旧 building block 中最薄的 `SpacerVer/SpacerHor/SpacerWeight`、`ColumnRoot`、`DividerW/DividerH/DividerV/DividerSize`，相关调用方已改用 Compose 原生 `Spacer`、`Column` 和本地分隔线。
- 已删除旧 `IvyText` 包装，剩余调用方改用 Material3 `Text`。
- 已删除旧 `IvyIcon/IvyIconScaled/IconScale` 包装，剩余调用方改用 Material3 `Icon`、`Image` 或本地小函数；`shared:ui:core` 的旧 `l1_buildingBlocks` 包已清空。
- 已把旧颜色常量、`Gradient` 和颜色对比/转换 helper 从 `com.ivy.wallet.ui.theme` 迁到 `com.ivy.legacy.ui.theme`，避免通用旧 UI 工具继续挂在 Wallet 产品包名下；旧 `components/modal/wallet` 子包后续再分组迁移。
- 已把旧通用 UI 组件从 `com.ivy.wallet.ui.theme.components` 迁到 `com.ivy.legacy.ui.component`，包括旧按钮、工具栏、输入框、余额行、排序弹窗、底部栏和图标组件；功能和视觉保持不变。
- 已把遗留误包名 `com.ivy.domain.legacy.ui.*` 清空：`ListItem` 和 `IvyColorPicker` 归入 `com.ivy.legacy.ui.component`，调用方仍使用原组件行为。
- 已把旧弹窗层从 `com.ivy.wallet.ui.theme.modal` 迁到 `com.ivy.legacy.ui.modal`，`modal.edit` 同步迁到 `com.ivy.legacy.ui.modal.edit`；账户/分类/金额/周期/货币/借贷等旧弹窗仍保留实现，后续再按功能边界下沉。
- 已把旧 `wallet` UI 子包里的金额/货币展示和周期选择组件并入 `com.ivy.legacy.ui.component`；`com.ivy.wallet.ui.theme.*` 包名已经从源码中清空。
- 已把编辑交易/计划付款复用的旧底部表单组件从 `com.ivy.wallet.ui.edit.core` 迁到 `com.ivy.legacy.ui.edit.core`；除 app 自身锁屏包名外，旧 shared/feature UI 不再使用 `com.ivy.wallet.ui.*`。
- 已清理迁移过程中留下的 `com.ivy.legacy.legacy.ui.theme.*` 双重 legacy 包名：预算进度条和日期时间行归入 `com.ivy.legacy.ui.component`，弹窗名称输入归入 `com.ivy.legacy.ui.modal`。
- 已把 `TransactionHistoryDateDivider` 从旧 `com.ivy.wallet.domain.data` 迁到 `com.ivy.base.model.legacy`；它仍服务旧交易列表和旧日期分组，并与 `TransactionHistoryItem` 位于同一基础模型包。`SortOrder/CustomExchangeRateState` 已进一步下沉到对应 feature。
- 旧创建/编辑参数已从早期的 `com.ivy.wallet.domain.deprecated.logic.model` 迁出；当前 `CreateAccountData`、`CreateBudgetData`、`CreateCategoryData`、`CreateLoanData`、`CreateLoanRecordData`、`EditLoanRecordData` 等纯参数对象归入 `com.ivy.data.model.legacy`，旧页面和正式 use case 继续使用同名语义。
- 已把 `shared:domain` 中旧业务逻辑从早期 deprecated/legacy logic 包继续迁出：计划付款、账户统计、分类统计、借贷交易联动和旧汇率换算已进入正式 use case 包；当前不再保留 `com.ivy.legacy.domain.logic` 源码。
- 已把旧 FPAction/use-case 与 pure helper 从 `com.ivy.wallet.domain.action/pure` 迁到 `com.ivy.legacy.domain.action/pure`，并同步迁移 `ClosedTimeRange`、`IncomeExpensePair`、`IncomeExpenseTransferPair` 的旧统计值对象包名；这些代码仍是旧 domain 兼容层，但不再占用正式 Wallet 产品包名。
- 已把旧 FRP/action helper 从 `shared:base` 物理下沉到 `shared:domain`，仍保留 `com.ivy.legacy.frp` 包名以避免大面积调用方 import churn；`shared:base` 不再承载这批旧 action 组合工具。
- 旧 UI 专用的 `TestingContext` 已从 `shared:base` 的 FRP 包下沉到 `shared:ui:legacy` 的 `com.ivy.legacy.ui.testing`，基础层不再承载这段只服务旧 Compose UI 测试规避逻辑的全局开关。
- 旧 FRP 组合 helper 已精简为只保留实际使用的重载，删除历史推导注释和“迁到 FP/FPAction”的过时 TODO；业务含义明确的 TODO 继续保留。
- 旧 `Res.tryOp()` 已移除没有收益的 `inline/noinline` 组合，避免迁移后 domain 编译持续产生无意义的 inline 性能警告。
- 旧 domain 逻辑、旧交易模型和旧统计值对象不再用大面积 `@Deprecated` 注解制造编译噪音；当前仍保留这些实现以支撑报表、交易列表、计划付款和旧统计流程，迁移状态通过 `legacy` 包名和 README 计划追踪。
- 旧交易分组仍需要的本地时区转换 helper 已从 `shared:base` 移到 `com.ivy.domain.time`；`shared:base` 不再暴露这段只服务 domain 交易流程的扩展函数。
- 已清理一批低风险编译警告：保留仍被使用的 `LegacyTag` 和客户旅程卡片 provider，但取消误导性废弃标记；Arrow `orNull()`/旧 `option` DSL、旧 Material `Divider` 和 Kotlin `toUpperCase()` 调用已更新到当前 API。
- 继续清理低风险废弃 API：旧货币选择器改用 `String.lowercase(Locale)`，旧排序弹窗改用 `bindingAdapterPosition` 并处理 `NO_POSITION`，汇率页箭头图标改用 AutoMirrored 版本。
- 已把 `com.ivy.legacy.datamodel.temp` 中的旧实体/新模型 mapper 扩展函数迁到 `com.ivy.domain.mapper.legacy`；这些文件仍服务旧数据模型兼容，但不再使用含糊的 `temp` 包名。
- 旧兼容模型已从早期的 `com.ivy.legacy.datamodel` 迁出，并继续按职责下沉；当前模型本体主要归入 `com.ivy.data.model.legacy`，数据库实体转换保留在 `com.ivy.domain.mapper.legacy`。
- 已把跨模块混用的旧模型从 `com.ivy.legacy.data.model` 拆出并继续归位：`FromToTimeRange` 已进入 `com.ivy.data.model.legacy`，账户页展示聚合 `AccountData` 已下沉到 `feature:accounts`；UI 侧 `TimePeriod/Month/LastNTimeRange` 暂时保留在旧 UI 包，因为它们仍依赖 UI 文案和时间格式化。
- 已把 `ClosedTimeRange`、`IncomeExpensePair`、`IncomeExpenseTransferPair` 从旧 `com.ivy.legacy.domain.pure.data` 包归入 `com.ivy.data.model.legacy`；它们仍作为旧统计流程的值对象保留在 `shared:data:model`。
- 旧时间范围值对象 `FromToTimeRange` 已从 `com.ivy.legacy.domain.model` 下沉到 `com.ivy.data.model.legacy`；upcoming/overdue 交易过滤函数已迁到 `com.ivy.domain.time`，UI 和 feature 不再为了这个纯范围对象引用 legacy domain 包名。
- upcoming/overdue 交易日期过滤 helper 已从 `com.ivy.legacy.domain.model` 迁到 `com.ivy.domain.time`；legacy model 包不再承载这类业务过滤函数。
- 通用排序号、Arrow `Option` 归零和 non-empty list 折叠 helper 已从 `com.ivy.legacy.domain.pure.util` 迁到 `com.ivy.domain.util`；这些工具继续服务排序创建、CSV 导入和旧统计折叠，但不再挂在 legacy pure 包下。
- 账户余额过滤、账户币种 fallback 和汇率换算纯函数已从 `com.ivy.legacy.domain.pure.account/exchange` 迁到 `com.ivy.domain.account` 与 `com.ivy.domain.exchange`；它们仍兼容旧账户/汇率模型，但包边界已经按业务职责归位。
- 旧交易纯计算、日期分组和新旧交易值桥接函数已从 `com.ivy.legacy.domain.pure.transaction` 迁到 `com.ivy.domain.transaction.legacy`；当前 `shared:domain` 中的 `com.ivy.legacy.domain` 源码已经清空。
- 账户页展示模型 `AccountData` 和对应 `AccountDataAct` 已从 `shared:domain` 下沉到 `feature:accounts`；账户页专用展示聚合不再占用 shared domain 边界。
- 纯创建参数 `CreateAccountData`、`CreateCategoryData`、`CreateBudgetData` 已从 `com.ivy.legacy.domain.model` 下沉到 `com.ivy.data.model.legacy`；UI 弹窗、feature event 和 domain creator 继续使用同名参数对象，但不再占用 legacy domain model 包。
- 旧预算模型 `Budget` 已从 `com.ivy.legacy.domain.model` 下沉到 `com.ivy.data.model.legacy`；数据库转换 `toEntity()` 已移入 legacy domain mapper，预算页和预算相关 use case 继续使用同一模型语义。
- 旧汇率计算仍使用的兼容模型 `ExchangeRate` 已从 `com.ivy.legacy.domain.model` 下沉到 `com.ivy.data.model.legacy`；数据库转换边界保留在 legacy domain mapper。无调用方的旧 `Category` 兼容模型和 mapper 已删除，分类功能继续使用正式 `com.ivy.data.model.Category`。
- 旧借贷模型 `Loan`、`LoanRecord` 和 `EditLoanRecordData` 已从 `com.ivy.legacy.domain.model` 下沉到 `com.ivy.data.model.legacy`；借贷数据库转换 `toEntity()` 已移入 legacy domain mapper。
- 旧账户模型 `Account`、计划付款规则 `PlannedPaymentRule`、借贷创建参数 `CreateLoanData/CreateLoanRecordData` 已从 `com.ivy.legacy.domain.model` 下沉到 `com.ivy.data.model.legacy`；旧交易 `toEntity()` 已从 model 包合并进 `com.ivy.domain.mapper.legacy.TransactionExt`。`com.ivy.legacy.domain.model` 源码目录已经清空。
- 已把剩余 UI 兼容状态模型从 `com.ivy.legacy.data` 迁到 `com.ivy.legacy.ui.model`，并把周期选择模型迁到 `com.ivy.legacy.ui.model.period`；`com.ivy.legacy.data.*` 包名已经从源码中清空。
- 已把新旧交易模型桥接 helper 从 `com.ivy.data.temp.migration` 改名到 `com.ivy.data.legacy`，它们仍用于预算、报表和旧 domain 统计，但不再伪装成临时 migration 工具。
- 已把旧 UI helper 从 `com.ivy.ui.legacy` 迁到 `com.ivy.legacy.ui`，包括 Compose 扩展、手势、动画、日期/间隔格式化和 Android UI 扩展；功能不变，只让旧 UI 工具回到统一 legacy UI 包根。
- 已把旧主题系统和 `LegacyUiRoot` 从 `shared:ui:core` 下沉到 `shared:ui:legacy`；`ui:core` 继续保留 Material3 主题、平台接口、时间接口和基础 UI 工具，旧设计兼容层归入 legacy 模块。
- 已把旧周期状态入口从 `com.ivy.legacy` 根包迁到 `com.ivy.legacy.ui.state`，并删除旧 `rootScreen()` 桥接函数；`shared:ui:legacy` 不再通过根包暴露迁移期 API。
- 已把旧 `OnboardingButton` 重命名为通用的 `GradientButton`，并把复用的 `ic_onboarding_next_arrow` 资源改名为 `ic_next_arrow`；CSV 导入完成页、分类按钮和标签按钮的视觉保持不变。
- 已删除无调用方的旧 `IvyDividerDot` 组件；仍被页面使用的复选框、分隔线、按钮和开关组件保留。
- 已删除无外部调用的旧金额展示变体 `AmountCurrencyH1/H2Row/Caption`、大号 `ItemIconL` 包装和 `IvyOutlinedTextField`；当前页面仍使用的金额展示、图标和输入组件保留。
- 已把旧设计兼容层从 `com.ivy.design.*` 迁到 `com.ivy.legacy.design.*`，包括旧 `LegacyTheme`、颜色常量、Compose helper 和 Material3 theme 包装；功能和视觉保持不变。
- 已把旧设计包里的通用 Compose helper 迁到 `com.ivy.ui.compose`，并把键盘隐藏 helper 迁到 `com.ivy.ui.platform`；这些工具不再带旧设计系统的过时标记。
- 已把当前仍在使用的主题状态 `ThemeState/LocalThemeState` 和 Material3 theme 包装迁到 `com.ivy.ui.theme`；旧 `LegacyTheme/IvyTheme` 继续作为兼容层调用它。
- 已把 `LocalDatePicker` 迁到 `com.ivy.ui.platform`，把 `LocalTimeConverter/LocalTimeProvider/LocalTimeFormatter` 迁到 `com.ivy.ui.time`；根部 UI 包装器只负责提供这些平台和时间 Local，不再定义它们。
- 已把旧 `IvyUI` 根包装器迁到 `com.ivy.ui.LegacyUiRoot` 并改名，`com.ivy.legacy.design.api` 包已经清空。
- 已把旧颜色选择器常量从 `com.ivy.legacy.design` 根包迁到 `com.ivy.legacy.ui.theme`，CSV 导入和旧颜色选择器继续使用同一组颜色值。
- 已把旧主题兼容层从 `com.ivy.legacy.design.l0_system` 迁到 `com.ivy.legacy.ui.theme.system`，旧设计包目录已经清空；功能和视觉保持不变。
- 已删除旧设计接口和默认设计外部传参，旧主题兼容层直接使用内部默认配置，去掉了无实际扩展点的设计系统抽象。
- 旧 UI 兼容层不再用大面积 `@Deprecated` 注解制造编译噪音；迁移状态通过 `com.ivy.legacy.ui` 包名、README 计划和后续逐步替换来表达。
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
   - `LoanTransactions*` 已迁到 `domain.usecase.loan`
   - `ExchangeRatesLogic` 已迁到 `domain.usecase.exchange.LegacyExchangeRatesUseCase`
   - 目标：继续压缩剩余 legacy 包，只保留真实兼容层。
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
- 已把设置页、根启动流程、首次启动默认数据、交易提醒、起始日偏好、隐藏余额/收入偏好，以及账户/交易/饼图/旧账户逻辑里的全局偏好读取迁到 `AppPreferences`。
- 起始日读写已收敛到 `GetStartDayOfMonthUseCase` 和 `SetStartDayOfMonthUseCase`，设置页的 1..31 校验语义保持不变；旧 `StartDayOfMonthAct` 和 `UpdateStartDayOfMonthAct` 已删除。
- 已把分类排序、最近选择账户和客户旅程卡片关闭状态迁到 `AppPreferences`，feature 层不再直接注入 `SharedPrefs`。
- 已把重置钱包流程改为通过 `AppPreferences.clearAll()` 清空 legacy 偏好；app/feature 层不再直接注入 `SharedPrefs`。
- 功能开关 `BoolFeature` 不再通过 `Context.dataStore` 读写；偏好开关已重命名为普通本地偏好，设置页、编辑交易分类排序、金额格式化和金额键盘都改为使用同一个注入的 `PreferenceToggleRepository`。
- 原 `shared/domain/features` 已迁到 `shared/domain/preferences/toggles`，`Features/BoolFeature/FeatureGroup` 重命名为 `PreferenceToggles/BoolPreference/PreferenceGroup`；底层 DataStore key 仍沿用 `feature_...` 前缀，保证已安装设备上的偏好开关不丢失。
- feature ViewModel 中的偏好开关读取不再依赖 `shared:ui:legacy` 的 CompositionLocal；账户、分类、首页、搜索、交易、报表和编辑交易页改为注入 `PreferenceToggleRepository`，再通过 `shared:ui:core` 的 Flow 状态 helper 在 Compose 状态层读取。
- `PreferenceToggleRepository` 已从具体 DataStore 包装改成 domain 级业务仓库；底层 `PreferenceToggleStore` 端口放在 `shared:data:api`，Android DataStore 实现和全量清空放在 `shared:data:core`。
- 备份恢复仍保留原始 `SharedPrefs` 访问；它需要处理全部历史 key 和外部备份格式，后续与备份格式重构一起处理。`SharedPrefs` 不再用废弃注解制造警告，迁移状态改由本计划追踪。

目标：

- feature 不直接读写 `SharedPrefs`。
- `SharedPrefs` 最终删除或降级为 DataStore 的内部兼容实现。
- 偏好开关继续从旧的“高级特性”语义收敛为普通本地偏好语义，后续可进一步把 DataStore 入口下沉到数据层 repository。

### 阶段 7：数据层和数据库遗留清理

目标：删除云同步、用户表和旧设置表的历史负担，但必须谨慎。

候选内容：

- `SettingsEntity`
- `isSynced`
- `lastSyncedTime`

已完成：

- 删除 `UserEntity`、`UserDao` 和 Hilt DAO provider。
- `ResetWalletDataUseCaseImpl` 不再依赖用户表。
- 新增 `Migration130to131_DropUsers`，数据库版本升到 131，并生成 `131.json` schema；新 schema 不再包含 `users` 表。
- 删除预算、借贷、借贷记录、计划付款和交易 DAO 中无调用方的 `findByIsSyncedAndIsDeleted` 同步查询，以及对应测试 fake override；这一步不改变 Room schema。
- `isDeleted` 暂时保留：当前本地查询过滤和计划付款按账户软删除仍依赖它，不能和纯云同步残留一起批量删除。
- 基础货币读取开始从 `SettingsDao` 直连收敛到 `CurrencyRepository`：编辑交易、借贷、计划付款、饼图统计、CSV 导入、旧分类统计、旧计划付款统计、旧借贷交易联动和 `BaseCurrencyAct` 不再直接读取 `settings` 表。
- 无业务增量的旧 `BaseCurrencyAct` 已删除；余额、账户、预算、分类、搜索、报表和交易页改为通过正式 `GetBaseCurrencyCodeUseCase` 读取基础币种。
- 基础币种读写进一步收敛为正式 domain 用例：`GetBaseCurrencyUseCase`、`GetBaseCurrencyCodeUseCase`、`SetBaseCurrencyUseCase`。Main、汇率、首页、设置、饼图、计划付款、借贷、编辑交易、CSV 导入、首次默认账户预置和旧账户/分类/计划付款/借贷联动逻辑不再为了基础币种读写直接注入 `CurrencyRepository`；当前只有正式 currency use case 仍依赖数据层仓库。
- 新增 `SettingsRepository`，把仍存放在 `settings` 表中的主题和缓冲金额用窄方法包起来；其上方已补齐 `GetThemeUseCase`、`SwitchThemeUseCase`、`GetBufferAmountUseCase`、`SetBufferAmountUseCase` 和 `EnsureSettingsInitializedUseCase`，设置页、首页、根启动流程和首次默认设置初始化不再直接依赖旧 settings 数据仓库。
- 首次启动默认设置初始化已下沉到 `EnsureSettingsInitializedUseCase`；`InitialDataSetup` 不再直接构造 `SettingsEntity` 或注入 `SettingsDao/WriteSettingsDao/SettingsRepository`，只负责启动编排、默认账户/分类预置和提醒调度。
- 分类列表读取开始收敛到正式 domain 用例：新增 `GetCategoriesUseCase`，搜索页、首页、预算页、报表页、计划付款列表和饼图统计 action 不再为了只读分类列表直接注入 `CategoryRepository`；仍需要保存、删除、查询单个分类或创建默认分类的流程暂时保留数据层依赖，后续按写入语义继续拆。
- 继续扩大分类/账户只读列表边界：账户页、分类页、交易页、编辑交易页、计划付款编辑页、CSV 导入/导出和借贷联动逻辑中的普通分类或账户列表读取已改走 `GetCategoriesUseCase/GetAccountsUseCase` 或迁移期的 `GetLegacyAccountsUseCase/GetLegacyAccountUseCase`；旧 `AccountsAct/AccountByIdAct` 已删除，排序保存、首次初始化是否为空和自动创建 Loans 分类仍保留原仓库入口。
- 标签列表读取和文本搜索开始收敛到正式 domain 用例：新增 `GetTagsUseCase` 和 `SearchTagsUseCase`，报表筛选和编辑交易里的普通标签列表/搜索不再直接调用 `TagRepository.findAll()/findByText()`；标签保存、删除、交易关联和按标签反查交易仍保留仓库入口，后续按写入和筛选语义继续拆。
- 账户/分类页面的写入边界继续收窄：新增 `SaveAccountUseCase`、`SaveCategoryUseCase` 和 `ObserveAccountChangesUseCase`，账户排序、分类排序和账户变更刷新不再直接依赖 repository 或 `DataObserver`；`:feature:accounts` 和 `:feature:categories` 已去掉对 `shared:data:core` 的直接 Gradle 依赖。
- 继续清理 feature 的 Gradle 依赖：`:feature:search`、`:feature:piechart`、`:feature:main` 和 `:feature:settings` 已去掉对 `shared:data:core` 的直接依赖；其中 search/main/settings 只补充实际需要的 `shared:data:model` 或 DataStore 依赖，settings 的 ZIP 备份导出改走 `ExportBackupUseCase`。
- 汇率页的数据边界已收敛：新增 `ObserveExchangeRatesUseCase`、`SaveExchangeRateUseCase` 和 `DeleteExchangeRateUseCase`，`:feature:exchange-rates` 不再直接注入 `ExchangeRatesRepository`，并已去掉对 `shared:data:core` 的直接依赖。
- 汇率金额换算入口已收敛到 `ExchangeAmountUseCase`，预算、账户、交易、报表、钱包汇总和首页到期交易统计不再依赖旧 `ExchangeAct`；底层仍复用现有 `ExchangeData` 与换算纯函数，行为保持不变。
- 旧交易桥接函数已从 `shared:data:core` 移到 domain 旧交易纯逻辑包：`getValue/getAccountId/getTransactionType/settleNow` 不再作为数据实现层 API 暴露，预算、报表和旧 domain 逻辑改为从 `com.ivy.domain.transaction.legacy` 使用这些扩展。
- 预算页数据边界已收敛：新增 `GetBudgetsUseCase` 和 `ReorderBudgetsUseCase` 封装预算列表读取与排序保存，旧 `BudgetsAct` 已删除；`:feature:budgets` 不再直接注入 `WriteBudgetDao`，并已去掉对 `shared:data:core` 的直接依赖。
- 预算创建、编辑和删除已从旧 `BudgetCreator` 拆成 `CreateBudgetUseCase`、`UpdateBudgetUseCase` 和 `DeleteBudgetUseCase`；预算页只依赖正式 use case，旧 `BudgetCreator` 已删除。
- 账户创建和编辑已从旧 `AccountCreator` 拆成 `CreateAccountWithBalanceUseCase` 和 `UpdateAccountWithBalanceUseCase`；主页面、编辑交易、计划付款、借贷和交易详情页不再注入旧 creator，账户保存后自动生成余额调平交易的行为保持不变。
- 分类创建和编辑已从旧 `CategoryCreator` 拆成 `CreateCategoryUseCase` 和 `UpdateCategoryUseCase`；分类页、编辑交易、计划付款和交易详情页不再注入旧 creator，分类排序号、图标、颜色和空名称校验保持不变。
- 首页数据边界已收敛：新增 `GetCustomerJourneyStatsUseCase` 封装首页引导卡片需要的交易/计划付款计数，新增 `MapTransactionsToLegacyUseCase` 封装新旧交易模型转换，`:feature:home` 不再直接依赖 `TransactionRepository`、`PlannedPaymentRuleDao` 或 `TransactionMapper`，并已去掉对 `shared:data:core` 的直接依赖。
- 首页的偏好和交易存在性读取继续收窄：新增 `HasTransactionsUseCase` 替代旧 `HasTrnsAct`，隐藏余额/收入状态直接读取 `AppPreferences`，旧 `HasTrnsAct`、`ShouldHideBalanceAct` 和 `ShouldHideIncomeAct` 已删除。
- 首页缓冲金额差值已直接内联为 `balance - bufferAmount`，无业务增量的旧 `CalcBufferDiffAct` 已删除。
- 借贷页数据边界已收敛：新增 `GetLoansUseCase`、`GetLoanUseCase`、`GetLoanRecordsUseCase`、`ReorderLoansUseCase`、`GetLoanTransactionUseCase` 和 `HasLoanRecordTransactionUseCase`，借贷列表和借贷详情不再直接注入 `LoanRecordDao`、`WriteLoanDao`、`TransactionRepository` 或 `TransactionMapper`；旧 `LoansAct/LoanByIdAct` 已删除，`:feature:loans` 已去掉对 `shared:data:core` 的直接依赖。
- 借贷写入边界已收敛：新增 `CreateLoanUseCase`、`UpdateLoanUseCase`、`DeleteLoanUseCase`、`CreateLoanRecordUseCase`、`UpdateLoanRecordUseCase` 和 `DeleteLoanRecordUseCase`；借贷列表和详情页不再注入旧 `LoanCreator/LoanRecordCreator`，关联交易创建、编辑和删除仍保持原有调用顺序。
- 借贷关联交易同步已从 `legacy.domain.logic.loantransactions` 迁到 `domain.usecase.loan`：新增 `LoanTransactionSyncUseCase`、`LoanRecordTransactionSyncUseCase`、`UpdateAssociatedLoanDataUseCase` 和内部 `LoanTransactionSyncCore`；借贷页和编辑交易页不再注入旧 `LoanTransactionsLogic` 聚合器。
- 旧模型仍需使用的汇率换算入口已从 `legacy.domain.logic.currency.ExchangeRatesLogic` 迁到 `domain.usecase.exchange.LegacyExchangeRatesUseCase`；计划付款、分类详情、借贷同步、编辑交易和旧日期分组不再引用 legacy logic 包。
- 计划付款编辑页数据边界已收敛：新增 `GetPlannedPaymentRuleUseCase`、`SavePlannedPaymentRuleUseCase`、`DeletePlannedPaymentRuleUseCase` 和 `GetCategoryUseCase`，计划付款保存仍会生成未来交易、删除仍会清理未发生的生成交易，`:feature:planned-payments` 已去掉对 `shared:data:core` 的直接依赖。
- 计划付款未来交易生成器已从旧 `PlannedPaymentsGenerator` 迁到正式 `GeneratePlannedPaymentTransactionsUseCase`；一次性规则、循环规则、72 条生成上限和跳过已发生交易的规则保持不变。
- 余额页的计划付款区间金额统计已从 `PlannedPaymentsLogic` 拆到 `CalculatePlannedPaymentsAmountForRangeUseCase`；收入计正、支出计负、转账忽略和基础币种折算规则保持不变。
- 计划付款列表页的规则列表和收入/支出汇总已从 `PlannedPaymentsLogic` 拆到 `GetPlannedPaymentsOverviewUseCase`；一次性、循环、月均折算和基础币种换算规则保持不变。
- legacy 交易模型的计划付款支付/跳过处理已从 `PlannedPaymentsLogic` 拆到 `PayOrSkipLegacyPlannedTransactionUseCase` 和 `PayOrSkipLegacyPlannedTransactionsUseCase`；首页、交易详情、编辑交易和报表页不再为了 legacy 交易处理注入旧逻辑。
- 新交易模型的计划付款支付/跳过处理已从 `PlannedPaymentsLogic` 拆到 `PayOrSkipPlannedTransactionUseCase` 和 `PayOrSkipPlannedTransactionsUseCase`；报表页不再注入旧逻辑，`PlannedPaymentsLogic` 已删除。
- 编辑交易页数据边界已收敛：新增 `SaveLegacyTransactionUseCase`、`DeleteTransactionUseCase`、`GetLoanUseCase` 和一组标签读写/关联用例，交易保存、删除、复制、标签创建、标签编辑、标签删除和标签关联不再直接调用数据层 repository/mapper，`:feature:edit-transaction` 已去掉对 `shared:data:core` 的直接依赖。
- 交易详情页数据边界已收敛：新增 `GetAccountUseCase`、`DeleteAccountUseCase`、`DeleteCategoryUseCase` 和 `MapTransactionsToLegacyWithTagsUseCase`，账户详情、分类详情、账户删除、分类删除和带标签历史列表不再直接注入数据层 repository/DAO/mapper，`:feature:transactions` 已去掉对 `shared:data:core` 的直接依赖。
- 报表页数据边界已收敛：新增 `GetTransactionsUseCase` 和 `GetTransactionsByTagsUseCase`，报表筛选不再直接读取 `TransactionRepository/TagRepository`，新旧交易模型转换改走 `MapTransactionsToLegacyUseCase`；`ExportCsvUseCase` 的自定义导出回调不再暴露 `TransactionRepository` receiver，默认全量导出也改走 `GetTransactionsUseCase`，`:feature:reports` 已去掉对 `shared:data:core` 的直接依赖。
- 导入页数据边界已收敛：`ImportResult/ImportCsvRow` 已迁到 `shared:data:model`，备份恢复改走 `ImportBackupUseCase`，手动 CSV 文件读取改走 `ReadTextFileUseCase`，CSV 导入读取/保存改走 `GetLegacyAccountsUseCase/SaveAccountUseCase/SaveCategoryUseCase/SaveLegacyTransactionUseCase`；`:feature:import-data` 已去掉对 `shared:data:core` 的直接依赖。
- 交易读取 action 已进一步收敛：搜索改走 `GetTransactionsUseCase`，预算和历史分组改走 `GetTransactionsBetweenUseCase`，首页到期交易改走 `GetDueTransactionsUseCase`，编辑交易按 ID 读取改走 `GetLegacyTransactionUseCase`，分类/饼图的账户过滤读取改走 `GetLegacyTransactionsForAccountsUseCase`；旧 `AllTrnsAct`、`HistoryTrnsAct`、`DueTrnsAct`、`TrnByIdAct` 和 `TrnsWithRangeAndAccFiltersAct` 已删除。
- 账户交易读取已收敛到 `GetAccountTransactionsUseCase`；账户余额、账户收支、首页钱包收支和交易详情账户历史不再依赖旧 `AccTrnsAct`，金额折算和统计口径保持不变。
- 账户余额和账户收支计算已收敛到 `CalculateAccountBalanceUseCase` 与 `CalculateAccountIncomeExpenseUseCase`；账户页、交易详情、钱包账户逻辑和钱包余额汇总不再依赖旧 `CalcAccBalanceAct/CalcAccIncomeExpenseAct`。
- 账户余额调平和账户详情未来/逾期交易统计已从 `WalletAccountLogic` 拆到 `AdjustAccountBalanceUseCase`、`GetAccountUpcomingTransactionsSummaryUseCase` 和 `GetAccountOverdueTransactionsSummaryUseCase`；账户创建/编辑和交易详情账户页不再注入旧逻辑，`WalletAccountLogic` 已删除。
- 分类详情页的余额、收入、支出、历史列表、未来交易和逾期交易聚合已从 `WalletCategoryLogic` 拆到 `GetCategoryTransactionsSummaryUseCase` 和 `GetUnspecifiedCategoryTransactionsSummaryUseCase`；交易详情分类页不再注入旧逻辑，`WalletCategoryLogic` 已删除。
- 钱包级余额和收支计算已收敛到 `CalculateWalletBalanceUseCase` 与 `CalculateWalletIncomeExpenseUseCase`；首页、余额页和账户页不再依赖旧 `CalcWalletBalanceAct/CalcIncomeExpenseAct`。
- 交易统计和历史列表分组已收敛到普通 use case：搜索、首页、报表、交易详情、分类页和饼图页改用 `BuildTransactionHistoryItemsUseCase`、`BuildLegacyTransactionHistoryItemsUseCase`、`GetTransactionHistoryItemsUseCase`、`CalculateTransactionsIncomeExpenseUseCase`、`CalculateLegacyTransactionsIncomeExpenseUseCase` 和 `CalculateCategoryIncomeWithAccountFiltersUseCase`；旧 `CalcTrnsIncomeExpenseAct`、`TrnsWithDateDivsAct`、`HistoryWithDateDivsAct` 和分类筛选统计 action 已删除。
- 首页到期交易统计已收敛到 `GetUpcomingTransactionsInfoUseCase`、`GetOverdueTransactionsInfoUseCase` 和公共 `CalculateDueTransactionsInfoUseCase`；旧 `DueTrnsInfoAct`、`UpcomingAct` 和 `OverdueAct` 已删除，`shared:domain` 中不再保留旧 `domain/action` 源码。
- feature-local 旧 action 写法继续收敛：账户页 `AccountDataAct` 改为 `BuildAccountDataUseCase`，饼图页 `PieChartAct` 改为 `BuildPieChartDataUseCase`，两者不再继承 `FPAction` 或依赖 `thenMap/thenFilter` 组合工具。
- 未被运行时代码使用的 `legacy.frp.action` 和 `legacy.frp.monad` 已删除，`Composition.kt`/`CompositionN.kt` 中只保留普通函数组合重载；项目中不再存在 `FPAction`/`Action` 抽象。
- feature 层和旧纯交易分组里的 `legacy.frp` 组合函数已改成普通 Kotlin lambda/循环，`Composition.kt`、`Composition2.kt`、`CompositionN.kt` 和 `Utils.kt` 已删除；当前 `legacy.frp` 只剩 `Pure/SideEffect` 注解。
- `Pure/SideEffect` 注解已迁到 `com.ivy.legacy.domain.pure`，未使用的 `Total/Partial` 注解已删除；`legacy.frp` 包和目录彻底移除。
- 仅作源码标记的 `Pure/SideEffect` 注解也已删除，旧纯函数不再保留无运行时价值的自定义文档注解。
- 删除无调用方的 `SettingsAct`、`UpdateSettingsAct`、旧 `Settings` 模型和 `SettingsEntity.toLegacyDomain()` mapper。
- `SettingsEntity` 暂时仍保留：首次默认数据、重置钱包、备份恢复格式，以及 `CurrencyRepository/SettingsRepository` 内部仍依赖这张表。
- `ResetWalletDataUseCaseImpl` 仍保留在 app 层实现：它需要同时编排数据清空、偏好清空、默认数据重建和根导航复位；当前不再用废弃注解制造警告，后续若拆分应先拆出数据清空与 app 导航两部分职责。
- 删除无调用方的 `data_synced_to_cloud` 多语言文案，云同步用户可见入口继续减少。
- 删除标签和标签关联表里的 `lastSyncedTime` 云同步时间字段，新增 `Migration131to132_DropTagSyncTime`，数据库版本升到 132；旧备份里的多余字段可被现有 JSON 配置忽略。
- 删除账户、交易、分类、设置、计划付款、预算、借贷和借贷记录表里的 `isSynced` 云同步状态字段，新增 `Migration132to133_DropIsSynced`，数据库版本升到 133；旧备份里的多余字段继续由 `ignoreUnknownKeys` 兼容。
- 删除设置页“删除云端数据”入口、空的 `resetCloudUserData()` 用例方法，以及对应多语言云端删除文案；当前分支已经没有云端数据实现，这条链路只会误导用户。
- 删除 `settings` 表里的旧 `name` 和 `isDeleted` 字段，新增 `Migration133to134_DropSettingsLegacyFields`，数据库版本升到 134；运行时仍保留 `theme/currency/bufferAmount/id`，旧备份里的多余字段继续由 JSON 配置忽略。
- 已把历史 Room migration 和 `RoomTypeConverters` 的包名从旧的 `com.ivy.domain.db.*` 归位到 `com.ivy.data.db.*`；这一步只调整源码边界，不改变 schema 或 migration 内容。
- 删除未接入运行时、主体仍是 `TODO("Not implemented")` 的新 domain use case 草稿：钱包统计、钱包余额、分类统计、账户余额和汇率换算；保留已有测试覆盖的 `AccountStatsUseCase.calculate(account, transactions)` 聚合逻辑。
- 业务表实体上的 `isDeleted` 已取消“云同步废字段”注解；当前它是仍被 DAO 查询和部分写入逻辑使用的本地软删除字段，不再作为纯云同步残留处理。
- `SettingsEntity` 已取消 `@Deprecated` 注解，改为普通注释标明 legacy 表用途；它当前仍服务主题、基础币种、缓冲金额和备份兼容，不再让 Room database 编译产生误导性警告。
- `Theme` 枚举已取消旧设计系统 `@Deprecated` 注解；它当前是持久化的应用主题偏好，枚举名仍写入 Room 和备份文件。
- 旧首页判断是否存在交易的 DAO 查询已从 `findAll_LIMIT_1()` 改成 `hasAny()`；这一步不改 schema，只让查询语义更直接并避免读取完整实体。
- `DeviceIdUseCase` 已停止引用 data core 的 `IvyDataStore` typealias，改为直接依赖标准 `DataStore<Preferences>`；data core 只保留实际 `Context.dataStore` 提供入口。
- 设置页偏好开关读写已收敛到 `PreferenceToggleRepository`；`:feature:settings` 不再直接注入 `DataStore<Preferences>`，并去掉了自身的 DataStore Gradle 依赖，底层开关 key 和存储文件不变。
- 旧 UI 偏好开关 CompositionLocal 已从 `LocalPreferenceDataStore` 改为 `LocalPreferenceToggleRepository`；`shared:ui:legacy` 不再直接依赖 DataStore，旧 UI 的开关读取仍复用相同 repository 和现有 key。
- 旧 UI 金额输入偏好入口已收窄为 `AmountInputPreferences/LocalAmountInputPreferences`；它现在只表达金额键盘布局偏好，不再使用泛化的 `LegacyUiPreferences` 命名。
- `shared:ui:legacy` 内部主题默认值和渐变按钮命名已收敛为 `LegacyThemeDefaults` 与 `GradientButton`；legacy 属性继续由模块和包名表达，组件名不再重复历史前缀。
- 编辑交易页的分类排序偏好读取已改走 `PreferenceToggleRepository`；`:feature:edit-transaction` 不再直接注入 DataStore，也去掉了自身的 DataStore Gradle 依赖。
- 重置钱包流程的 DataStore 清空已改走 `PreferenceToggleRepository.clearAll()`；app 层不再直接注入 AndroidX DataStore 或命名为 DataStore 的具体 repository，也去掉了自身的 DataStore Gradle 依赖。
- 偏好开关 key helper 已从 data core 移回 domain 的 `BoolPreference` 内部；`feature_` 前缀保持不变，data core 不再暴露只服务 domain 偏好定义的 `DatastoreKeys`。
- 删除无调用方的 `DeviceIdUseCase` 和 `DeviceId` 草稿；偏好开关的底层 DataStore 读写已下沉到 data core 实现，外部调用方只能通过 `PreferenceToggleRepository` 访问。
- 重置钱包流程的数据删除和全量数据变化通知已拆成 `ClearWalletDataUseCase`、`NotifyAllDataChangedUseCase`；app 层的 reset 实现不再直接注入底层 DAO、repository 或 `DataObserver`，只保留重置编排、偏好清空、默认数据重建和导航复位。
- 首次默认账户/分类预置已改走 `GetAccountsUseCase/GetCategoriesUseCase` 和 `SaveAccountUseCase/SaveCategoryUseCase`；app 启动编排不再直接注入账户 DAO 或分类/账户 repository，默认内容、颜色、图标和初始化条件保持不变。
- 交易提醒 Worker 的“今天是否已经记账”判断已改走 `CountTodayTransactionsUseCase`；app 通知 Worker 不再直接注入 `TransactionDao`，数据层新增按时间范围计数查询，避免为了计数加载完整交易列表。
- 编辑交易页的标题建议逻辑已从旧 `SmartTitleSuggestionsLogic` 迁到正式 `SuggestTransactionTitlesUseCase`；按标题、分类和账户使用频次生成建议的规则保持不变。
- 文本文件读写已抽成 `TextFileStore` 基础端口，data core 的 Android `FileSystem` 负责实现；CSV 导出和手动 CSV 读取不再直接依赖 data core 文件类，备份导入导出后续单独拆分。
- 新增薄模块 `shared:data:api` 承载数据层端口；备份导入导出已改为依赖 `BackupStore`，data core 的 `BackupDataUseCase` 只作为实现绑定到该端口，feature 仍只通过 domain use case 使用备份功能。
- `BackupDataUseCase` 的泛化 legacy TODO 已改成明确的备份格式兼容说明；这段实现暂时继续承担旧 ZIP/JSON 备份格式和旧本地数据兼容职责，后续拆分时应围绕该边界处理。
- 基础币种和设置表访问已抽成 `CurrencyStore` 与 `SettingsStore` 端口；domain 的币种/设置 use case 不再直接注入 data core repository，data core 继续保留 Room-backed 实现和内部 mapper 依赖。
- 设置表默认值已集中到 data core 内部 `LocalSettingsDefaults`；`SettingsRepository` 不再引用 `CurrencyRepository` 的默认币种常量，两个 repository 也不再重复构造默认 `SettingsEntity`。
- 汇率读写和远程同步入口已抽成 `ExchangeRateStore` 端口；汇率同步、设置页汇率列表和重置钱包流程不再直接依赖 data core 的 `ExchangeRatesRepository`。
- 汇率单条查询已收敛到 `ExchangeRateStore.findByBaseCurrencyAndCurrency()`；汇率换算 use case 不再直接注入 `ExchangeRatesDao`，legacy 汇率 mapper 也不再依赖 `ExchangeRateEntity`。
- 标签读写、标签关联和标签搜索已抽成 `TagStore` 端口；编辑交易、历史列表、按标签筛选和重置钱包流程不再直接依赖 data core 的 `TagRepository`。
- 标签创建已停止注入 data core 的 `TagMapper`；`CreateTagUseCase` 直接构造 data model `Tag` 并通过 `TagStore` 保存。
- 账户与分类读写已抽成 `AccountStore/CategoryStore` 端口；对应 domain use case、交易历史构建、借贷同步和重置钱包流程不再直接注入 data core repository，`TransactionMapper` 也改为依赖账户端口。
- 交易读写、到期交易、计划付款关联交易和借贷关联交易已抽成 `TransactionStore` 端口；domain 中的账户、分类、首页、计划付款、借贷、重置和交易 use case 不再直接注入 data core 的 `TransactionRepository`。
- 交易剩余读路径已继续收敛到 `TransactionStore`；`hasAny()`、智能标题建议查询、按账户/分类标题计数、旧账户交易列表、计划付款金额统计和借贷同步不再直接注入 `TransactionDao`。
- 交易 legacy/modern 转换已改为基于 data model 和 `AccountStore` 直接完成；`shared:domain` 主源码不再依赖 data core 的 `TransactionMapper` 或 `TransactionEntity`。
- `shared:domain` 的 Gradle 主依赖和测试依赖都已移除 `shared:data:core`；domain 单元测试改用数据端口 mock/fake，不再依赖 repository、Room 或 data core mapper。
- 数据变化事件已抽成 `DataChangePublisher/DataWriteEvent` 端口；domain 中的账户变更观察和全量数据变更通知不再直接依赖 data core 的 `DataObserver` 实现。
- 预算读写已抽成 `BudgetStore` 端口；预算创建、更新、删除、排序、列表读取和重置钱包流程不再直接注入 Room 的 `BudgetDao/WriteBudgetDao`，旧 `BudgetExt` 实体 mapper 已删除。
- 计划付款规则读写已抽成 `PlannedPaymentRuleStore` 端口；首页统计、账户删除、计划付款保存/删除/读取、付或跳过计划付款，以及重置钱包流程不再直接注入 `PlannedPaymentRuleDao/WritePlannedPaymentRuleDao`，旧 `PlannedPaymentRuleExt` 实体 mapper 已删除。
- 借贷和借贷记录读写已抽成 `LoanStore/LoanRecordStore` 端口；借贷 CRUD、借贷记录 CRUD、借贷交易同步和重置钱包流程不再直接注入 `LoanDao/LoanRecordDao/WriteLoanDao/WriteLoanRecordDao`，旧 `LoanExt/LoanRecordExt` 实体 mapper 已删除。
- 设置表清空已收敛到 `SettingsStore.deleteAll()`；重置钱包流程不再直接注入 `WriteSettingsDao`。
- 主题 fallback、首次初始化默认主题和主题切换规则已从 data core 移回 domain：`SettingsStore` 只保留主题读写能力，`GetThemeUseCase/EnsureSettingsInitializedUseCase/SwitchThemeUseCase` 负责系统暗色映射和 LIGHT/DARK/AMOLED/AUTO 循环顺序，并补充了单元测试锁定这些规则。
- 分类排序偏好已收敛到 `GetCategorySortOrderPreferenceUseCase/SetCategorySortOrderPreferenceUseCase`；分类页不再直接注入 `AppPreferences`，底层 key 和排序行为保持不变。
- 上次选择账户偏好已收敛到 `GetLastSelectedAccountIdUseCase/SetLastSelectedAccountIdUseCase`；编辑交易页和借贷页不再直接读写 `AppPreferences.lastSelectedAccountId`，底层字符串 key 和 UUID 解析行为保持不变。
- “转账计入收支”记账规则偏好已收敛到 `GetTransfersAsIncomeExpensePreferenceUseCase/SetTransfersAsIncomeExpensePreferenceUseCase`；账户页、交易页和饼图页只读 domain 用例，设置页通过用例保存该开关，底层 key 不变。
- 隐藏余额和隐藏收入偏好已收敛到独立 domain 用例；首页只读 `GetHideCurrentBalancePreferenceUseCase/GetHideIncomePreferenceUseCase`，设置页通过对应 set 用例保存，底层 key 和短暂显示逻辑保持不变。
- 交易提醒通知开关已收敛到 `GetShowNotificationsPreferenceUseCase/SetShowNotificationsPreferenceUseCase`；设置页、提醒调度逻辑和提醒 Worker 不再直接访问 `AppPreferences.showNotifications`，通知调度与二次检查行为保持不变。
- 应用锁开关已收敛到 `GetAppLockEnabledPreferenceUseCase/SetAppLockEnabledPreferenceUseCase`；设置页和 `AppLockController` 不再直接访问 `AppPreferences.appLockEnabled`，运行时锁定状态仍由 app 层 controller 管理。
- 首次初始化完成和月起始日读取已收敛到 settings domain 用例；`RootViewModel` 和 `InitialDataSetup` 不再直接访问对应的 `AppPreferences` 字段，底层 key 与启动行为保持不变。
- 只写不读的 `data_backup_completed` 旧偏好已删除；备份导出仍直接生成 zip 并触发分享，不再写入没有消费方的完成标记。
- 首页客户旅程卡片关闭状态已收敛到 `IsCustomerJourneyCardDismissedUseCase/DismissCustomerJourneyCardUseCase`；`feature:home` 不再直接拼接或读写客户旅程偏好 key。
- 重置钱包流程中的旧 app 偏好清空已收敛到 `ClearAppPreferencesUseCase`；app 层重置实现继续负责编排，但不再直接注入 `AppPreferences`。
- 业务偏好 key 已从 `shared:base` 迁到 `shared:data:api`；base 不再承载应用锁、通知、隐藏余额等业务 key。
- 旧 `AppPreferences` 具体类已拆成 `AppPreferenceStore` 端口和 `SharedPrefsAppPreferenceStore` 实现；domain 用例只依赖 data-api 端口，SharedPrefs 读写细节下沉到 data-core。
- 备份恢复中的偏好读写已改走 `AppPreferenceStore`；备份 JSON 仍保留原 sharedPrefs key 字符串以兼容旧备份文件，但 `BackupDataUseCase` 不再直接读写通用 `PreferenceStore`。
- 旧 `PreferenceStore/SharedPrefs` 基础层抽象已删除；`SharedPrefsAppPreferenceStore` 在 data-core 内部直接持有 Android SharedPreferences，base 不再暴露偏好存储绑定。
- `AndroidResourceProvider` 已从 base 移到 app 平台层并由 app Hilt 模块绑定；`ResourceProvider` 抽象也已从 base 迁到 `shared:ui:core` 的 `com.ivy.ui.resource` 包。
- `ResourceProvider` 接口已去掉 `@StringRes` 注解；资源 ID 在 UI 端口中只作为普通参数，Android 注解仅保留在 app 实现层。
- 备份 zip/unzip 工具已从 base 下沉到 `shared:data:core` 的备份包；zip 文件读写仍使用 Android `Context/Uri`，但只留在实际负责备份恢复的数据实现层。
- 文本文件读写端口 `TextFileStore` 已从 base 迁到 `shared:data:api:file`；CSV 导出和文本读取用例继续依赖端口，Android `Uri` 读写实现仍在 data-core 的 `FileSystem`。
- 默认法币函数已从 base 移到 `shared:data:model:currency`，和 `IvyCurrency` 放在同一模型边界；贷款、搜索和 legacy 借贷弹窗只更新导入路径，默认币种 fallback 行为保持不变。
- 单一消费方的薄 helper 已清理：`MutableStateFlow.readOnly()` 改为标准 `asStateFlow()`，`MutableList.swap()` 改为 `Collections.swap()`，base 不再保留这两个无抽象价值的扩展。
- Room/备份实体使用的 UUID、Instant、LocalDateTime 序列化器已从 base 下沉到 `shared:data:core` 的 `db.serializer` 包；序列化格式和实体注解保持不变。
- Room 和备份仍需要的 LocalDateTime/UTC epoch millis 转换 helper 已从 `shared:base` 下沉到 `shared:data:core` 的数据库包；base 不再承载持久化格式工具。
- `LoanType/IntervalType` 已从 base 物理归位到 `shared:data:model`；`IntervalType.incrementDate` 也迁到同一模型包，计划付款和 legacy 周期 UI 只更新导入路径。
- `Json` 的 Hilt 提供模块已从 base 移到 `shared:data:core`，由数据层集中配置备份恢复和 Ktor 客户端共用的 kotlinx serialization 行为。
- legacy 交易展示模型、`TransactionType` 和 `LoanRecordType` 已归位到 `shared:data:model`；base 不再承载交易模型类型，也不再应用 kotlinx serialization 构建插件。
- `Theme` 已从 base 归位到 `shared:data:model`；枚举成员和持久化 `name` 不变，Room 与备份中的主题值保持兼容。
- 金额正负号展示 helper 已从 base 迁到 `shared:ui:core` 的 `com.ivy.ui.money`；分类页和交易页继续复用同一显示逻辑，基础层不再承载 UI 展示语义。
- 单一调用方的随机数 helper 已内联到 legacy 重排弹窗；base 不再保留 `random` 包。
- `shared:data:api` 和 `shared:ui:navigation` 已移除不再使用的 `shared:base` Gradle 依赖；两个模块现在只声明实际源码需要的模型、UI 或端口依赖。
- legacy 时间范围模型不再接收 `TimeProvider`，改由 domain 传入当前 `Instant`；data-model 内部保留同值的 legacy 安全时间边界，`shared:data:model` 已脱离 `shared:base` 依赖。
- `shared:data:core` 对 `shared:data:model` 的依赖已从 `api` 收窄为 `implementation`；数据实现层继续使用模型类型，但不再通过自身向外传递暴露模型依赖。
- `feature:main` 和备份导入页面的旧 `LiveData` 状态已改为 `StateFlow`；Compose 不再需要 `runtime-livedata` 适配依赖，版本目录中的 LiveData 运行时别名也已删除。
- 剩余 `uiThread` 调用已改为标准 `withContext(Dispatchers.Main)`，base 中的主线程切换 helper 已删除。
- `feature:main` 和 `feature:search` 已把旧 `ioThread` helper 改为标准 `withContext(Dispatchers.IO)`，并移除对 `shared:base` 的 Gradle 依赖。
- 剩余 `ioThread/scopedIOThread/computationThread` 调用已全部改为标准 `withContext(Dispatchers.IO/Default)`；`shared:base` 中的旧协程 dispatcher helper 文件已删除。
- `com.ivy.base.text` 中的字符串判空、大小写和首字母转换 helper 已删除；调用方改为标准 `isNullOrBlank().not()`、`uppercase/lowercase(Locale.getDefault())` 或局部私有扩展，基础层不再承载通用字符串糖衣。
- `ResourceProvider` 已从 `shared:base` 迁到 `shared:ui:core`，测试替身也随之从 `base-testing` 移到 ui-core 测试源集；base 不再承载 Android 字符串资源端口。
- `shared:base` 中无消费方的 `BaseModule` 和 `@AppCoroutineScope` 已删除；应用级协程 scope 绑定不再作为未使用的全局 DI 暴露。
- 导出 CSV/备份文件名使用的 `yyyyMMdd-HHmm` 时间戳格式已从 `shared:base` 内联到设置页和报表页；基础层不再暴露文件命名专用的时间格式 helper。
- 版本目录中未被任何 Gradle 文件或源码使用的 `mockk-android` 与 `androidx-security` 依赖别名已删除。
- 账户旧读取路径已收敛到 `AccountStore`；旧 legacy 账户模型现在由 data model 账户映射而来，`shared:domain` 主源码不再直接注入 `AccountDao` 或依赖 `AccountEntity` mapper。
- 旧交易卡片已移除重复账户查找 TODO：渲染前先解析来源/目标账户，再复用同一结果处理点击和币种展示，行为不变但 legacy UI 内部职责更清楚。

建议顺序：

1. 继续评估 `isDeleted` 字段：
   - `isSynced` 已确认是云同步残留并删除。
   - `isDeleted` 仍服务本地查询过滤、测试 fake、历史迁移和计划付款按账户软删除；短期应视为本地软删除语义，不再和云同步残留一起批量删除。
2. 继续梳理 `SettingsEntity` 与 `AppPreferences/DataStore` 的职责重叠，下一步重点是把 `theme/currency/bufferAmount` 的存储边界拆清楚。
3. 更新备份恢复数据结构和测试。

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
- 删除无调用方的平台跳转能力
  - open browser
  - open market page

同时调整：

- `RootScreen` 大接口已经删除。
- 与 Activity 强绑定的实现放到 `app` 的 platform 包。
- feature 通过 `FileSharer`、`BuildInfoProvider` 等窄接口表达平台需求。

当前进展：

- `ActivityDatePickerHost` 承接 Material date picker 注册，`RootActivity` 不再直接构造 `MaterialDatePicker`。
- `ActivityFilePickerHost` 承接 Activity Result 文件创建/打开注册，`RootActivity` 不再保存 launcher 和文件回调。
- `ActivityFileSharer` 承接 CSV 分享和 zip 分享，并直接实现 `FileSharer`；`RootActivity` 不再为了分享文件实现 UI 平台接口。
- `AppBuildInfoProvider` 承接版本号、版本名和 debug 状态读取；`RootActivity` 不再为了设置页版本显示实现 `BuildInfoProvider`。
- `AndroidLocaleSettingsLauncher` 承接 Android 13+ 应用语言设置跳转；设置页 ViewModel 不再直接持有 `Context` 或组装平台 Intent。
- 账户页和编辑交易页 ViewModel 的字符串读取已改为注入式 `ResourceProvider`；feature ViewModel 不再为了 `getString(...)` 持有 Android `Context` 或保留 `StaticFieldLeak` 抑制。
- `BiometricAuthenticator` 承接系统生物识别 Prompt 构造。
- `SecureWindowController` 承接应用锁失焦时的 `FLAG_SECURE` 窗口保护。
- `AppLockController` 承接应用锁启用状态、锁定状态、生物识别结果回调和用户非活跃计时，`RootViewModel` 只保留启动编排和委托方法。
- `RootContent` 承接根部 Compose 内容、锁屏/主导航切换、旧 UI root 注入和 Material3 theme 包装，`RootActivity` 主要保留生命周期、平台注册和平台能力委托。
- `RootScreen` 已被 `FileSharer`、`BuildInfoProvider` 拆分替代，首页客户旅程卡片也不再为了未使用的参数依赖 Activity 平台接口。
- `FileSharer` 和 `BuildInfoProvider` 已通过 `LocalFileSharer/LocalBuildInfoProvider` 由 app 根部显式提供；设置页和报表页不再通过 `LocalContext.current as ...` 强转 Activity 获取平台服务。
- `Features` 和功能开关 DataStore 已通过 `LocalFeatures/LocalFeatureDataStore` 由 app 根部显式提供；旧金额键盘不再用 Hilt `EntryPointAccessors` 从 application 反查依赖。
- 锁屏页不再通过 `LocalContext.current` 自行检查系统锁屏状态；`RootActivity` 从 app 平台层提供 `hasLockScreen` 检查函数，UI 只负责触发认证或继续进入应用。
- 根启动 intent 的交易类型解析已改用 `IntentCompat.getSerializableExtra()`，不再直接调用新版 Android 中弃用的 `Intent.getSerializableExtra(String)`。
- 导航返回处理已收窄为 `Navigation.handleRootBack()`、`registerScreenBackHandler()` 和 modal handler 注册方法；页面和旧 modal 不再直接访问导航内部的返回栈和 handler map。
- `LocalTimeConverter/LocalTimeProvider/LocalTimeFormatter` 现在作为根部显式提供的 UI 时间平台入口保留，不再用废弃注解把当前页面的正常调用标成警告。
- `RootContent` 接收的旧 Material 日期选择器已从 app 具体实现 `ActivityDatePicker` 收窄为 UI 层 `DatePicker` 接口；Activity 仍负责注册 FragmentManager 相关实现。
- 交易提醒调度已删除无调用方的 `testNow()` 调试入口和旧 work name 常量，只保留当前实际使用的每日提醒任务。
- Android Toast 封装 `Toaster` 已从 `shared:base` 迁到 `shared:ui:core` 的 `com.ivy.ui.platform`，编辑交易和汇率页继续通过同一注入类型显示提示；基础层不再承载这段 UI 平台能力。

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
   - 删除 `RootScreen` 大接口，改用窄平台接口。
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

1. 继续做 shared 模块依赖审计：优先检查 `shared:domain`、`shared:ui:core`、`shared:ui:legacy` 是否还有可显式化或可移除的非必要 Gradle 依赖。
2. 继续清理明显错位的包名和模块边界，优先处理 `shared:ui:legacy` 与 `shared:data:core` 中仍残留的历史命名，避免新代码继续误用旧 API。
3. 继续评估 `SettingsEntity` 是否可以拆成更明确的本地偏好表或迁入 DataStore；`name/isDeleted` 已删除，剩余 `theme/currency/bufferAmount` 需要和备份恢复格式一起规划。
4. 继续数据库只读审计：`isDeleted` 目前先保留为本地软删除语义；不再把业务表里的 `isDeleted` 当作纯云同步字段批量删除。
