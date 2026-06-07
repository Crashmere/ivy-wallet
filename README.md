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
- 已精简数据导入页：删除第三方 App 导入模板和教程，只保留 Ivy 备份恢复与手动 CSV 导入。
- 已清理 `temp:legacy-code` 中一批无引用旧代码：Crashlytics 空壳、旧 FRP ViewModel/Composable、孤立旧 modal、旧图表/输入组件和无引用工具类型。
- 已删除 `temp:legacy-code` 中仅服务 IDE 预览的 Compose `@Preview` 示例函数。
- 已删除 `temp:legacy-code` 旧 Compose App 包装中无引用的 `rootActivity` 和组件预览包装 helper。
- 已精简 `temp:legacy-code` 旧工具包，删除无引用的 Fragment helper、部分动画 preset、URL helper、日期格式化 helper 和金额拆分格式化 helper。
- 已删除 app/feature 模块中仅服务 IDE 预览的 Compose `@Preview` 示例函数。
- 已删除 app/feature 模块和 `shared/ui/core` 中的 Paparazzi 截图测试入口、快照图片和截图测试目录。
- 已删除 `temp:old-design` 旧设计组件中仅服务 IDE 预览或截图测试的 Compose 预览 helper。
- 已删除 `temp:old-design` 中确认无外部引用的旧组件：`l2_components`、`l3_ivyComponents` 和旧 shape building block。
- 已删除 `temp:old-design` 中无引用的旧 `Background`、`IvyPadding` 和 padding helper，并简化仍被调用的 `IvyText`。
- 已删除 `temp:old-design` 中无引用的旧 Android、动画、窗口 inset 和 dp/px 工具，并精简 Compose/键盘工具 helper。
- 已继续精简 `temp:legacy-code`，删除无引用的旧 DataStore 包装、旧账户余额模型、分类纯函数对象，并移除部分新旧并存文件中的未接入新模型残留。
- 保留应用功能源码、功能测试源码、Gradle wrapper、本地数据管理能力和当前主要记账功能。
- 当前本机已通过项目本地 Android SDK 编译 demo APK，并成功安装到已连接手机。

## 后续清理原则

- 优先删除和个人记账无关的社区、推广、远程反馈、发布信息和原项目展示功能。
- 保留真实记账功能、数据模型、数据库、导入导出和功能测试，除非确认个人使用场景不再需要。
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
- 首次启动 onboarding 模块 `:feature:onboarding`，改为自动初始化默认设置、账户和分类后直接进入主页。

### 构建和数据残留

- Google Services、Crashlytics、Google Play Review、Firebase Firestore 相关接线。
- GitHub 自动备份清理迁移、迁移管理器空壳和 `DatastoreKeys.GITHUB_*`。
- `shared/ui/core` 中不再使用的 GitHub 图标、开源卡片组件和对应截图测试。
- 多语言资源中不再使用的开源、分享、评分、Telegram 和推广求助文案。
- Android 桌面小组件模块 `:widget:add-transaction`、`:widget:balance`、`:widget:shared-base`，以及首页小组件引导卡、Manifest receiver、启动广播、余额刷新接线和 Glance 依赖。
- `:feature:import-data` 中的第三方 App 来源列表、导入说明页、旧 CSV 模板映射、第三方 App logo 和教程文案；保留备份恢复与手动 CSV 映射导入。
- `temp:legacy-code` 中无引用的 Crashlytics 工具空壳、旧 FRP View 层封装、旧名称/月选择弹窗、旧折线图、旧 checklist 输入框和无引用工具类型。
- `temp:legacy-code` 中的 Compose `@Preview` 示例函数；保留真实运行时组件。
- `temp:legacy-code` 旧 Compose App 包装中的无引用 Activity helper 和组件预览包装 helper；保留运行时仍使用的 `ivyWalletCtx`、`rootView`、`rootScreen` 和 `appDesign`。
- `temp:legacy-code` 旧工具包中无引用的 Fragment 参数/Activity Result helper、动画 preset、URL 打开 helper、日期时间格式化 helper 和金额拆分格式化 helper。
- app/feature 模块中的 Compose `@Preview` 示例函数和 Paparazzi 截图测试入口；保留功能代码和功能测试。
- `temp:old-design` 旧设计组件中的 Compose `@Preview` 示例函数和截图测试预览 helper；保留真实组件。
- `temp:old-design` 中无引用的旧 l2/l3 组件和旧 shape building block；保留仍被当前功能引用的旧设计基础能力。
- `temp:old-design` 中无引用的旧 `Background`、`IvyPadding` 和 padding helper；保留仍被 CSV 导入、借贷逻辑和颜色选择器使用的旧颜色列表常量。
- `temp:old-design` 中无引用的旧 Android、动画、窗口 inset、dp/px 和 Compose helper；保留仍被页面调用的 `thenIf`、`thenWhen`、`densityScope`、`rememberInteractionSource` 和 `hideKeyboard`。
- `temp:legacy-code` 中无引用的旧 `IvyDataStore` 包装、旧 `AccountBalance` 模型、分类纯函数对象，以及未被接入的非 legacy 分类统计 action 和新 `DueSection` 数据残留。

## 已确认保留

- `shared:data:core` 中的本地备份、恢复、zip/json/csv 导入导出能力。
- `:feature:import-data` 中的手动 CSV 导入流程。
- `temp:old-design` 中的 `IVY_COLOR_PICKER_COLORS_*` 旧颜色列表常量；当前仍被 CSV 导入、借贷逻辑和旧颜色选择器使用。
- 功能测试源码。

## 建议执行顺序

1. 继续检查 `temp:legacy-code` 中剩余的旧 UI 组件和旧领域逻辑，优先处理确认无引用的部分。
2. 继续缩小 `temp:old-design` 的保留范围，优先从只有少量引用的旧 building block 和 feature 侧直接依赖入手。
