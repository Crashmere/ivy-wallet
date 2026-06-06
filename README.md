# Ivy Wallet 个人维护分支开发计划

这个仓库现在作为个人使用和个性化开发用途维护，不再面向上游协作、社区运营、应用商店发布或多人开发流程。

## 当前状态

- 已删除 GitHub workflow、Issue/PR 模板、CI 辅助模块、Fastlane 发布配置、开发规范文档、Detekt 配置、lint baseline、脚本和生成图等外围资产。
- 已移除 Detekt、Kover、模块图、Gradle wrapper 自动升级、Google Services、Crashlytics、Google Play Review、Compose lint 等构建或发布相关接线。
- 已删除 contributors、releases、attributions、poll 等社区、发布说明、致谢和远程反馈模块。
- 已删除原项目开源展示、GitHub 仓库入口、分享 Ivy、Google Play 评分卡、Telegram/推广求助文案和 GitHub 自动备份迁移残留。
- 已将原 `:feature:features` 独立英文页面合并进设置页，改为面向个人使用的偏好设置。
- 已整顿设置页结构：数据管理、记账规则、系统行为保留为一级分组，外观与显示、输入与列表改为二级菜单。
- 已移除设置页顶部匿名账户名称入口和首页问候语。
- 保留应用功能源码、功能测试源码、截图测试源码、Gradle wrapper、本地数据管理能力和当前主要记账功能。
- 当前本机已通过项目本地 Android SDK 编译 demo APK，并成功安装到已连接手机。

## 后续清理原则

- 优先删除和个人记账无关的社区、推广、远程反馈、发布信息和原项目展示功能。
- 保留真实记账功能、数据模型、数据库、导入导出、功能测试和截图测试，除非确认个人使用场景不再需要。
- 每一轮清理后都尽量保持 Gradle module include、app 依赖、导航入口和设置页入口一致，避免留下不可达或不可编译的残留。
- 对启动流程、数据库迁移、导入导出这类会影响个人数据或首次使用体验的内容，先确认使用习惯，再动手删除。

## 已完成的主要清理

### 社区和发布相关内容

- GitHub workflow、Issue/PR 模板、社区规范、开发规范、发布脚本和 Fastlane 配置。
- `:feature:contributors`
- `:feature:releases`
- `:feature:attributions`
- `:feature:poll:impl` 和 `:feature:poll:public`

### 设置页和推广入口

- 设置页贡献者、发布日志、开源致谢、投票问卷、外部推广和原高级特性独立页面。
- 首页 customer journey 中的外部反馈/评分相关卡片。
- 首页更多菜单里的 GitHub 开源卡片和分享 Ivy 入口。
- onboarding 欢迎页里的 `#opensource` 原仓库入口。
- 免责声明页里的开源仓库展示卡片。
- 首次启动免责声明阻塞页 `:feature:disclaimer`，以及对应的启动跳转、导航入口、`LegalRepository` 和 `LocalLegalDataSource`。

### 构建和数据残留

- Google Services、Crashlytics、Google Play Review、Firebase Firestore 相关接线。
- GitHub 自动备份清理迁移、迁移管理器空壳和 `DatastoreKeys.GITHUB_*`。
- `shared/ui/core` 中不再使用的 GitHub 图标、开源卡片组件和对应截图测试。
- 多语言资源中不再使用的开源、分享、评分、Telegram 和推广求助文案。
- Android 桌面小组件模块 `:widget:add-transaction`、`:widget:balance`、`:widget:shared-base`，以及首页小组件引导卡、Manifest receiver、启动广播、余额刷新接线和 Glance 依赖。

## 下一批建议清理：需要按个人使用习惯确认

### `:feature:onboarding`

首次启动引导，包含货币、账户和分类初始化流程。

清理前需要确认：

- 新安装后是否仍需要初始化默认数据。
- 是否改为直接进入主界面。
- 是否依赖导入数据或手动创建账户来完成初始化。

### `:feature:import-data`

CSV 和其他 App 数据导入功能。

清理前需要确认：

- 是否需要迁移历史数据。
- 是否保留 Ivy Wallet 自身备份文件导入。
- 是否保留导入说明里跳转其他 App 商店页的能力。

### `shared:data:core` 中的 backup/import 相关代码

本地备份、zip/json/csv 导入导出能力。该部分和数据安全相关，建议最后处理。

清理前需要确认：

- 是否仍需要导出备份。
- 是否仍需要恢复备份。
- 是否保留相关功能测试和兼容性测试。

## 建议执行顺序

1. 评估是否简化或删除 `onboarding`。
2. 最后评估 `import-data` 和 backup/import 能力。
