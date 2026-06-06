# Ivy Wallet 个人维护分支开发计划

这个仓库现在作为个人使用和个性化开发用途维护，不再面向上游协作、社区运营、应用商店发布或多人开发流程。

## 当前状态

- 已删除 GitHub workflow、Issue/PR 模板、CI 辅助模块、Fastlane 发布配置、开发规范文档、Detekt 配置、lint baseline、脚本和生成图等外围资产。
- 已移除 Detekt、Kover、模块图、Gradle wrapper 自动升级、Google Services、Crashlytics、Google Play Review、Compose lint 等构建或发布相关接线。
- 已删除 contributors、releases、attributions、poll 等社区/远程反馈模块。
- 已将原 `:feature:features` 独立英文页面合并进设置页，改为面向个人使用的偏好设置。
- 保留应用功能源码、功能测试源码、截图测试源码和 Gradle wrapper。
- 当前本机已通过项目本地 Android SDK 编译 demo APK，并成功安装到已连接手机。

## 后续清理原则

- 优先删除和个人记账无关的社区、推广、远程反馈、发布信息和原项目展示功能。
- 保留真实记账功能、数据模型、数据库、导入导出、功能测试和截图测试，除非确认个人使用场景不再需要。
- 每一轮清理后都尽量保持 Gradle module include、app 依赖、导航入口和设置页入口一致，避免留下不可达或不可编译的残留。

## 第一批建议清理：基本确定无用

### `:feature:contributors`

贡献者页面，用于展示 GitHub 贡献者和仓库信息。个人使用不需要保留。

清理时需要同步处理：

- 从 `settings.gradle.kts` 移除 `:feature:contributors`
- 从 `app/build.gradle.kts` 移除对应依赖
- 从 `app/src/main/java/com/ivy/IvyNavGraph.kt` 移除 `ContributorsScreen`
- 从 `feature/settings` 中移除 Contributors 设置入口

### `:feature:releases`

版本发布说明页面，会访问 GitHub releases。个人分支不再跟随原项目发布节奏，可删除。

清理时需要同步处理：

- 从 `settings.gradle.kts` 移除 `:feature:releases`
- 从 `app/build.gradle.kts` 移除对应依赖
- 从 `app/src/main/java/com/ivy/IvyNavGraph.kt` 移除 `ReleasesScreen`
- 从 `feature/settings` 中移除 Releases 设置入口

### `:feature:poll:impl` 和 `:feature:poll:public`

投票/问卷功能，当前实现依赖 Firebase Firestore。该功能主要服务原项目远程反馈收集，个人使用不需要。

清理时需要同步处理：

- 从 `settings.gradle.kts` 移除 `:feature:poll:impl` 和 `:feature:poll:public`
- 从 `app/build.gradle.kts` 移除对应依赖
- 从 `feature/home/build.gradle.kts` 移除 `projects.feature.poll.public`
- 从首页 customer journey 逻辑中移除 poll card 和 `PollRepository` 依赖
- 从 `app/src/main/java/com/ivy/IvyNavGraph.kt` 移除 `PollScreen`
- 从 `shared:ui:navigation` 移除 `PollScreen`
- 从 `gradle/libs.versions.toml` 移除 `firebase-firestore`

### `:feature:attributions`

开源库和技术栈致谢页。个人本地使用通常不需要展示该页面。

清理时需要同步处理：

- 从 `settings.gradle.kts` 移除 `:feature:attributions`
- 从 `app/build.gradle.kts` 移除对应依赖
- 从 `app/src/main/java/com/ivy/IvyNavGraph.kt` 移除 `AttributionsScreen`
- 从 `feature/settings` 中移除 Attributions 设置入口

## 第二批建议清理：需要按个人使用习惯确认

### `:feature:disclaimer`

首次启动免责声明确认页。个人使用可以考虑删除，但需要先调整启动流程。

清理前需要确认：

- 是否仍需要首次启动时阻塞进入 App
- 是否保留 `LegalRepository` 和 `LocalLegalDataSource`
- `RootViewModel` 中跳转 `DisclaimerScreen` 的逻辑如何替换

### `:widget:add-transaction`、`:widget:balance`、`:widget:shared-base`

Android 桌面小组件，包括快速添加交易和余额展示。

清理前需要确认：

- 是否使用桌面 widget
- `RootActivity.setupApp()` 中的 widget broadcast 是否删除
- `AndroidManifest.xml` 中相关 receiver/service 声明是否删除

### `:feature:onboarding`

首次启动引导，包含货币、账户和分类初始化流程。

清理前需要确认：

- 新安装后是否仍需要初始化默认数据
- 是否改为直接进入主界面
- 是否依赖导入数据或手动创建账户来完成初始化

### `:feature:import-data`

CSV 和其他 App 数据导入功能。

清理前需要确认：

- 是否需要迁移历史数据
- 是否保留 Ivy Wallet 自身备份文件导入

### `shared:data:core` 中的 backup/import 相关代码

本地备份、zip/json/csv 导入导出能力。该部分和数据安全相关，建议最后处理。

清理前需要确认：

- 是否仍需要导出备份
- 是否仍需要恢复备份
- 是否保留相关功能测试和兼容性测试

## 第三批建议清理：非模块残留

这些不是独立 Gradle 模块，但仍包含原项目社区、推广或远程服务残留。

- `temp/legacy-code/src/main/java/com/ivy/legacy/Constants.kt` 中的 GitHub、Telegram、Sponsor、Google Play、隐私政策链接
- `feature/settings/SettingsScreen.kt` 中可能残留的原项目推广入口
- `shared/ui/core` 中的 GitHub 开源卡片和相关资源
- `app/src/main/java/com/ivy/wallet/migrations/impl/DisableGitHubAutoBackupMigration.kt`
- `shared/data/core/src/main/java/com/ivy/data/datastore/DatastoreKeys.kt` 中的 `GITHUB_*` key

## 建议执行顺序

1. 删除 GitHub、Telegram、Sponsor、Google Play 等 URL 常量和开源卡片。
2. 按个人习惯决定是否删除 `widget/*`。
3. 最后评估 `disclaimer`、`onboarding`、`import-data` 和 backup/import 能力。
