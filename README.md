# Ivy Wallet 个人维护分支重构计划

这个仓库现在作为个人使用和个性化开发用途维护，不再面向上游协作、社区运营、应用商店发布或多人开发流程。后续重构的目标不是重写 Ivy Wallet，而是在不影响当前已有功能的前提下，把项目逐步整理成模块结构清晰、职责分配合理、依赖方向明确、适合单人长期维护的 Android 项目。

## 当前状态摘要

已经完成的清理可以概括为几类：

- 删除社区协作、开源展示和发布流程相关内容：GitHub workflow、Issue/PR 模板、开发规范、Fastlane、发布日志、贡献者、开源致谢、投票问卷、原仓库入口、分享 Ivy、Google Play 评分、Telegram/推广文案等。
- 删除 Google/Firebase/商店发布相关接线：Google Services、Crashlytics、Google Play Review、Firebase Firestore、GitHub 自动备份迁移残留等。
- 删除不再需要的功能模块和入口：contributors、releases、attributions、poll、disclaimer、onboarding、widget，以及第三方 App 导入模板和教程。
- 手动 CSV 导入继续保留，但导入警告文案不再把重复风险归因于“其他 App”，改为说明 CSV 通常缺少 Ivy 交易 UUID。
- 删除失去实现意义的云端删除入口：设置页不再显示“删除云端数据”链路，`ResetWalletDataUseCase` 不再保留空的 cloud reset 方法。
- 整顿设置页：合并原高级特性页，改成个人偏好设置；重排设置分组；删除匿名账户入口和首页问候语。
- 精简测试和预览基础设施：删除 Paparazzi 截图测试、快照图片、仅服务 IDE 的 Compose `@Preview` 示例函数和预览 helper。
- `:temp:*` Gradle 模块已经删除；后续工作集中在继续收窄 `shared:ui:legacy` 中保留下来的旧 UI/旧模型兼容边界。
- 删除空的 `:shared:data:core-testing` 模块，并把测试专用 `FakeRepositoryMemo` 从生产源码移入测试源集。
- 删除未引用的第三方导入 logo、widget 预览/图标、推广/分享/捐赠图片，以及 `help_us_grow` 多语言推广文案。
- 删除无代码引用的 `data_synced_to_cloud` 多语言云同步完成文案。
- 删除 `:temp:old-design` 模块；旧设计 API 先迁入 `shared:ui:core` 作为兼容层，随后已逐步归位到更明确的 `legacy.ui.theme`/`legacy.ui.theme.system` 包名。
- 删除 `:temp:legacy-code` 模块；剩余旧全局上下文暂时收敛在 `shared:ui:legacy`，并已把周期状态、文件选择、日期选择、主 Tab 状态等职责逐步拆出。
- 整理部分 Gradle 约定插件：基础 shared 模块、数据核心模块和 domain 模块已经开始脱离面向页面的 `ivy.feature` 配置。
- 开始拆分 `RootActivity` 平台能力：文件创建/打开、Material 日期选择器、生物识别弹窗和 CSV/zip 分享已经移入 `app` 的 platform 边界；`RootScreen` 大接口已删除，feature 改为依赖更窄的 UI platform 接口。
- 继续清理历史包名：旧 Room migration 和 type converter 已从 `com.ivy.domain.db` 归位到 `com.ivy.data.db`，旧 UI 组件中误挂到 domain 包名下的 `ListItem/IvyColorPicker` 已归位到 `com.ivy.legacy.ui.component`。
- 清理非业务路径上的旧调试输出：预算 ID 解析、币种代码解析、键盘可见性判断、金额输入确认和 CSV 初次解析失败不再打印堆栈，继续按原有空值/无操作策略处理。
- 收窄 UI core 职责：旧交易列表专用的 `LegacyDueSection` 已从 `shared:ui:core` 迁回 `shared:ui:legacy` 的交易组件包。
- 继续收窄 UI core 职责：旧交易列表基础入参 `AppBaseData` 已改名为 `TransactionListData`，并迁入 `shared:ui:legacy` 的交易组件包。
- 继续迁移旧 UI 状态：账户、分类、借贷、借贷记录、缓冲金额、循环规则和周期选择弹窗状态已从 `shared:ui:core` 迁入 `shared:ui:legacy`。
- 精简 Compose 构建约定：删除 Compose compiler metrics/reports 输出配置，减少个人开发构建产物噪音。
- 精简根目录忽略规则：`.gitignore` 已移除 Fastlane、Freeline、Google Services、Android Studio 细项等历史噪音，只保留当前 Android/Gradle/VS Code 本地开发会产生的文件规则。
- 精简本地构建配置：删除 release 签名 keystore/环境变量接线，release/demo 都使用本地 debug 签名；`gradle.properties` 只保留当前构建需要的设置。
- 精简 app lint 配置：删除强制生成根目录 lint HTML/XML 报告和跨依赖 lint 扫描的配置，只保留当前项目仍需要的 lint 抑制与 release 检查策略。
- 清理生产代码中的裸异常堆栈输出：编辑交易、计划付款以及预算/分类/借贷保存用例不再调用 `printStackTrace()`，继续按原有失败返回值处理。
- 收窄 feature 内部 Compose helper：分类排序弹窗和报表标签筛选不再保留未使用的 `modifier` 参数，减少无意义 suppress。
- 补齐 `buildSrc` 根项目名，消除 Gradle type-safe project accessors 针对 buildSrc checkout 路径的缓存警告。
- 收窄 Gradle 仓库配置：普通依赖解析不再使用 Gradle Plugin Portal，`buildSrc` 普通依赖也不再保留 JitPack；插件解析入口继续保留 Gradle Plugin Portal。
- 删除根工程 JitPack 仓库：当前剩余第三方依赖均可从 Google/Maven Central 解析，项目不再依赖额外的 JitPack 仓库入口。
- 收窄导航旧交易参数：`TransactionsScreen` 与 `PieChartStatisticScreen` 不再通过导航携带完整 `LegacyTransaction`，改为传递交易 ID，并由目标 ViewModel 按需读取展示模型。
- 收窄导航交易类型参数：`shared:ui:navigation` 不再依赖 `shared:data:model` 的 `TransactionType`，route 改用轻量 `TransactionRouteType`，并删除无读取点的 `TransactionsScreen.transactionType` 参数。
- 收窄旧交易列表组件职责：`shared:ui:legacy` 的交易列表/交易卡片不再直接构造编辑页或筛选页 route，改由首页、搜索、报表和交易页传入点击回调。
- 收窄旧 toolbar 组件职责：交易统计 toolbar 和编辑页 toolbar 不再直接调用 `nav.back()`，关闭行为由对应 feature 页面传入。
- 收窄导航模块职责：主界面 tab 状态 `MainTab/MainTabState/LocalMainTabState` 已从 `shared:ui:navigation` 迁到 `shared:ui:core` 的 `com.ivy.ui.main` 包，navigation 模块继续聚焦 route、栈和返回处理。
- 收窄主界面 Tab 状态职责：`MainTabState` 不再作为 app Hilt 单例提供，改由 `MainViewModel` 作为主页面状态持有，再通过 `LocalMainTabState` 提供给首页和账户页。
- 收回主界面 Tab 状态边界：`MainTab` 和选中状态已归入 `feature:main`，首页/账户页通过普通回调请求切换 tab，`shared:ui:core` 不再提供 `LocalMainTabState` 全局入口。
- 删除无效 legacy screen 标记：当前所有页面统一走 `LegacyUiRoot` surface，`Screen` 不再暴露 `isLegacy`，`NavigationRoot` 删除只服务非 legacy 分支的 ViewModelStore 清理逻辑。
- 收回旧弹窗状态职责：账户、分类、缓冲金额、周期、借贷、借贷记录和计划付款重复规则的 `*ModalData` 已归回 `shared:ui:legacy` 的 modal 包；`shared:ui:core` 不再保留旧弹窗状态包。
- 收窄导航返回职责：旧弹窗返回键处理改用 Compose `BackHandler`，`Navigation` 删除 modal back handler 栈，只继续处理页面级返回和根返回栈。
- 收窄页面级返回职责：主界面、CSV 恢复页和交易统计页的返回回调改为随 `onScreenStart` 注册/注销；ViewModel 只保留返回行为判断，不再负责把回调长期挂到导航对象上。
- 收窄导入功能导航职责：备份恢复和手动 CSV 导入的 ViewModel 不再注入 `Navigation`，完成/跳过/结果页返回由 Screen 执行，ViewModel 只负责重置导入状态。
- 收窄借贷详情导航职责：删除借贷成功后由 ViewModel 发出一次性 `CloseScreen` UI 事件，Screen 收集事件后执行返回；借贷详情 ViewModel 不再注入 `Navigation`。
- 收窄计划付款编辑导航职责：无账户、保存完成和删除完成后的关闭页面动作改为 `CloseScreen` UI 事件；计划付款编辑 ViewModel 不再注入 `Navigation`。
- 收窄交易统计导航职责：删除账户或分类完成后的关闭页面动作改为 `CloseScreen` UI 事件；交易统计 ViewModel 不再注入 `Navigation`。
- 收窄首页导航职责：余额卡片点击后的页面跳转改为 `OpenBalance/OpenAccountsTab` UI 事件；首页 ViewModel 不再注入 `Navigation` 或持有主 Tab 状态。
- 收窄编辑交易导航职责：保存、删除、复制、计划交易支付和空账户退出后的关闭页面动作改为 `CloseScreen` UI 事件；编辑交易 ViewModel 不再注入 `Navigation`，原先无返回栈时回主界面的语义移到 Screen 执行。
- 收窄重置数据导航职责：`ResetWalletDataUseCaseImpl` 不再注入 `Navigation` 或跳转主界面；设置页在重置完成后通过 `WalletDataReset` UI 事件重置返回栈并回到主界面。
- 收窄首页客户旅程模型职责：客户旅程卡片不再携带 `(Navigation, MainTabState) -> Unit` 回调，改为声明 `CustomerJourneyAction` 意图；Composable 层负责把意图翻译为导航或 Tab 切换。
- 首页客户旅程 UI 组件也已停止直接调用 `navigation()` 或构造 route；`HomeLazyColumn` 负责把 `CustomerJourneyAction` 翻译为打开账户 tab、计划付款编辑或支出饼图。
- 首页 Header 的收入/支出卡片不再直接构造饼图 route；点击动作改为普通回调，由 `HomeTab` 顶层统一执行导航。
- 首页更多菜单不再直接引用各功能 route；菜单按钮改为上报 `MoreMenuDestination`，由 `HomeTab` 统一映射到搜索、设置、分类、计划付款、报表、预算和借贷页面。
- 收窄计划付款列表导航职责：计划付款列表和卡片不再直接调用 `navigation()` 或构造 route，改为向页面入口暴露点击回调；`PlannedPaymentsScreen` 统一把卡片、分类和账户点击翻译为编辑页或交易筛选页导航。
- 切断 legacy UI 对导航模块的依赖：legacy 内部初始化副作用改用 `shared:ui:core` 的 `onCompositionStart()`，`shared:ui:legacy` 不再声明 `shared:ui:navigation` 依赖。
- 继续收窄导航模块依赖：`shared:ui:navigation` 已移除未使用的 `shared:ui:core` 依赖，当前只保留自身导航状态、Compose ViewModel owner 和 route 需要的 immutable collection。
- 集中 ViewModel 获取入口：剩余直接使用 `viewModel()` 的 feature 页面已改用 `screenScopedViewModel()`，`lifecycle-viewmodel-compose` 依赖只保留在 `shared:ui:navigation`。
- 简化导航栈实现：`Navigation` 内部页面返回栈已从 Java `Stack` 换成 Kotlin `ArrayDeque`，保留原有后进先出返回语义。
- 收窄汇率页内部 UI 边界：新增汇率弹窗不再直接回传 ViewModel 事件类型，模块内列表项和弹窗组件也不再作为 public API 暴露。
- 收窄 domain legacy 交易计算公开面：旧交易折叠、汇率换算、日期分组和到期过滤 helper 改为 domain 模块内可见，外部继续通过 use case 使用这些能力。
- 收窄 domain 正式交易计算公开面：交易折叠、钱包/账户统计函数、交易类型过滤和交易汇率换算 helper 改为 domain 模块内可见；当前仍被 feature 使用的交易值扩展暂时保留 public。
- 下沉纯交易类型判断：`getTransactionType()` 归入 `shared:data:model`，预算和报表 feature 不再为了读取交易类型/基础金额直接依赖 `domain.transaction` 的内部扩展。
- 下沉 legacy 账户纯模型 helper：`includedLegacyAccounts()` 和 `legacyAccountCurrency()` 归入 `shared:data:model` 的 `LegacyAccount` 边界，删除 `domain.account.legacy` 小包。
- 收窄汇率换算边界：feature 层不再直接构造 `ExchangeData` 或调用 `transactionCurrency()`，改用 `ExchangeAmountUseCase` 的简单币种入口和 `ExchangeTransactionAmountUseCase`；`ExchangeData`、`exchange()` 与交易币种推断已收回 domain 内部。
- 收窄 domain 时间 helper 边界：`nowUtc()`、日期转换、到期/未来交易过滤等时间工具只作为 domain 内部实现保留，feature 层继续使用各自 UI/页面侧的时间处理入口。
- 删除正式交易日期分组的无调用方扩展：正式交易历史入口继续由 `BuildTransactionHistoryItemsUseCase` 提供，`TransactionHistoryItems` 不再保留一层未使用的 `withDateDividers(...)` 包装。
- 收窄 data-api 公开面：SharedPreferences key 常量已从 `shared:data:api` 移入 `shared:data:core` 的偏好实现包，API 模块继续只暴露 Store 端口。
- 收窄数据写入事件：当前只有账户页订阅账户变更，分类/标签写入事件已从 `DataWriteEvent` 中移除；分类/标签 Store 仍保留本地缓存，但不再发布无人消费的事件。
- 收窄旧主题外部入口：feature 层不再直接导入 `legacy.ui.theme.system` 的 `LegacyTheme/style/colorAs`，改走 `legacy.ui.theme` 门面；`system` 包继续作为 `shared:ui:legacy` 内部实现。
- 收窄旧主题内部调用面：旧 UI 组件和弹窗也已改走 `legacy.ui.theme` 门面，`system.LegacyTheme` 收窄为模块内部实现，并删除重复的 `system` typography 扩展。
- 继续收窄旧主题 system 包：旧 UI 组件不再直接导入 `theme.system` 颜色/工具，外层旧色板补齐仍需公开的颜色和 `asBrush()`；底层 `Colors.kt` 删除重复的 gradient/对比度工具，只保留主题默认值需要的颜色常量。
- 收窄标签弹窗公开面：`AddOrEditTagModal` 改为 `shared:ui:legacy` 内部实现，feature 层继续只通过 `ShowTagModal` 和 `AddTagButton` 访问标签 UI。
- 收窄 data-core 内部远程源：删除只有单实现且只被 `DefaultExchangeRateStore` 使用的 `RemoteExchangeRatesDataSource` 接口；domain 仍只依赖 `ExchangeRateStore`，汇率同步行为不变。
- 清理旧 UI 无效参数：删除 `ShowTagModal` 和 `ModalAmountSection` 中未使用的 `modifier` 参数及对应 suppress，调用方和展示行为不变。
- 继续清理旧 UI 无效参数：删除编辑交易/计划付款共用的 `EditBottomSheet` 中未使用的 `modifier` 参数，编辑入口调用和底部表单展示行为不变。
- 收窄旧 UI 内部构件公开面：`IntervalPickerRow`、小号内部图标、图标选择弹窗、计算器弹窗和弹窗绿色主按钮改为 `shared:ui:legacy` 内部实现，feature 层继续只使用现有页面级弹窗入口。
- 报表筛选弹层专用的全宽描边按钮已移回 `feature:reports` 私有实现；`shared:ui:legacy` 不再导出 `IvyOutlinedButtonFillMaxWidth` 这种单页面组件。
- 饼图页专用的渐变圆形按钮已移回 `feature:piechart` 私有实现；`shared:ui:legacy` 只继续暴露仍被多页面复用的普通圆形按钮。
- 预算页专用的预算电池和编辑交易页专用的自定义汇率卡片已移回各自 feature 私有实现；`shared:ui:legacy` 不再导出这两个单页面业务展示组件。
- 设置页专用的旧开关控件已移回 `feature:settings` 私有实现；`shared:ui:legacy` 不再导出 `IvySwitch`。
- CSV 导入结果页专用的渐变完成按钮已移回 `feature:import-data` 私有实现；`shared:ui:legacy` 不再导出 `GradientButton`。
- 交易页已直接使用 `BalanceRow` 参数表达中号余额样式；`shared:ui:legacy` 删除只剩单次调用的 `BalanceRowMedium` 包装函数。
- CSV 导入结果页的返回按钮已改为 `feature:import-data` 私有实现；`shared:ui:legacy` 的 `BackButton` 收窄为模块内部给旧工具栏使用。
- 饼图页不再直接调用底层 `ItemIconM`，改用带默认图标的包装入口；`shared:ui:legacy` 的 `ItemIconM` 收窄为模块内部实现。
- 计划付款列表改用本 feature 私有的分组分隔条；`shared:ui:legacy` 的交易 `SectionDivider` 收窄为交易列表内部实现。
- 汇率列表项改用本 feature 私有的删除按钮；`shared:ui:legacy` 的 `DeleteButton` 收窄为旧 UI 内部实现。
- 汇率新增弹窗改用本 feature 私有的名称输入框；`shared:ui:legacy` 的 `IvyNameTextField` 收窄为旧弹窗内部实现。
- 报表筛选关键词区域改用本 feature 私有的换行布局；`shared:ui:legacy` 的 `WrapContentRow` 收窄为旧弹窗内部实现。
- 旧工具栏公开 API 不再暴露 `BackButtonType` 枚举；报表页通过 `showCloseButton = true` 请求关闭按钮，旧工具栏内部自行选择返回/关闭图标。
- 首页更多菜单改用本 feature 私有的缓冲金额电池；`shared:ui:legacy` 的 `BufferBattery` 收窄为旧缓冲弹窗内部实现。
- 首页缓冲金额弹窗和弹窗状态已移回 `feature:home` 私有实现；`shared:ui:legacy` 不再导出 `BufferModal`、`BufferModalData` 或缓冲金额电池组件。
- 报表筛选的关键词添加弹窗已移回 `feature:reports` 私有实现；`shared:ui:legacy` 不再导出只服务报表筛选的 `AddKeywordModal`。
- 设置页的每月起始日选择弹窗已移回 `feature:settings` 私有实现；`shared:ui:legacy` 不再导出只服务设置页的 `ChooseStartDateOfMonthModal`。
- 交易列表的删除二次确认弹窗已移回 `feature:transactions` 私有实现；`shared:ui:legacy` 只继续保留多页面复用的普通 `DeleteModal`。
- 编辑交易页的交易时间展示和到期日卡片已移回 `feature:edit-transaction` 私有实现；`shared:ui:legacy` 不再导出只服务该页面的 `TransactionDateTime` 和 `DueDate`。
- 预算弹窗改用本 feature 私有的名称输入、新增/保存和删除按钮；`shared:ui:legacy` 的 `ModalNameInput`、`ModalAddSave`、`ModalDelete` 收窄为旧弹窗内部实现。
- 编辑交易页的计划交易支付/收取确认按钮已改为本页面私有实现；`shared:ui:legacy` 的 `ModalCheck` 收窄为旧弹窗内部实现。
- feature 层不再直接导入 legacy 内部黑色、紫色、深绿和中性色板，也不再使用 `colorAs` 扩展；这些旧主题常量继续只在 `shared:ui:legacy` 内部使用。
- 分类排序弹窗和计划付款编辑页底部操作改用本 feature 私有的 Set 按钮；`shared:ui:legacy` 的 `ModalSet` 收窄为旧弹窗内部实现。
- 计划付款重复规则弹窗和对应弹窗状态已移回 `feature:planned-payments` 私有实现；`shared:ui:legacy` 不再导出 `RecurringRuleModal` 或 `RecurringRuleModalData`。
- 借贷还款记录展示模型中的账户已从完整 `LegacyAccount` 收窄为本 feature 的轻量 `DisplayLoanAccount`，记录卡片只保留跳转和展示所需的账户 ID、名称与图标。
- 导入流程和报表筛选浮层的底部渐隐遮罩已改为各自页面私有实现；`shared:ui:legacy` 删除不再复用的 `GradientCutBottom`。
- 预算分类选择和报表筛选列表项已改为各自 feature 私有实现；`shared:ui:legacy` 删除不再复用的通用 `ListItem`。
- 编辑交易页和计划付款编辑页的“添加计划日期”按钮已改为各自 feature 私有实现；`shared:ui:legacy` 的 `AddPrimaryAttributeButton` 收窄为旧描述组件内部实现。
- 报表金额筛选和饼图分类行直接使用 `AmountCurrencyB1`；`shared:ui:legacy` 删除只剩包装作用的 `AmountCurrencyB1Row`。
- 收窄旧主题色板公开面：颜色选择器专用色板、旧 UI 内部红色/橙色渐变、透明色、底部渐变遮罩、旧弹窗背景模糊和 `asBrush()` 扩展改为 `shared:ui:legacy` 内部实现，feature 层仍可使用现有公开颜色和主题门面。
- 收窄 domain 内部工具公开面：新旧模型 mapper 中只被 domain 使用的转换方向、CSV 导出行模型和 Arrow 数值 helper 改为模块内部实现；CSV 导入仍需要的 `toDomainAccount()` 暂时保留公开入口。
- 收回旧账户保存转换边界：新增 `SaveLegacyAccountUseCase`，CSV 导入 feature 不再直接调用 legacy 账户 mapper；`toDomainAccount()` 改为 domain 内部实现。
- 收回排序号工具边界：CSV 导入用本地私有 helper 计算导入账户/分类排序号，feature 层不再直接引用 `domain.util`；domain 的 `nextOrderNum()` 改为内部工具。
- 收窄 data-model 工具公开面：金额格式化内部常量和 helper 改为文件私有，未使用的 `Value` 模型和 `toCloseTimeRangeUnsafe()` 扩展已删除，`PositiveValue.round()` 的内部 `roundTo()` 改为私有；金额输入、金额展示和时间范围公共 API 保持不变。
- 收窄 UI core 平台工具公开面：WindowInsets、键盘状态、状态栏兼容和安全时间边界中只服务内部实现的 helper 改为私有或模块内部可见；饼图内部尺寸常量改为私有，并删除未使用的 dp 转 px Int 重载。
- 去除汇率远程数据源的旧抽象命名残留：接口已删除后，`RemoteExchangeRatesDataSourceImpl` 重命名为正式的 `RemoteExchangeRatesDataSource`，并移出 `impl` 包；汇率同步行为不变。
- 收窄 data-core mapper 公开面：Room/远程 DTO 与 domain 模型之间的转换函数改为模块内部可见，远程汇率响应 DTO 和抓取函数也不再暴露为跨模块 API；同时删除 `TagMapper` 中无调用方的新建标签 helper。
- 继续拆分 app 壳层职责：新增 `RootAppLockHost` 承接 Activity 生命周期里的应用锁、窗口安全和系统生物识别桥接，`RootActivity` 只保留生命周期转发和根内容装配。
- 精简 Ktor 客户端配置：移除汇率同步 HTTP body 调试日志和 `ktor-client-logging` 依赖，数据层继续保留 JSON 内容协商和现有错误返回。
- 精简应用锁调试路径：删除生物识别成功/失败的 debug 日志、只为日志存在的文案读取依赖，以及无运行时引用的认证结果多语言文案。
- 收窄 UI core 平台 helper：删除无调用方的 Composable `hideKeyboard()` 包装，保留仍被页面返回处理和旧 UI 调用的 `View` 扩展入口。
- 精简 data-core 日志依赖：备份导入失败继续按原逻辑返回空导入结果并发布数据变更事件，但不再为了这一条错误日志依赖 Timber。
- 删除旧同步 UI 文案：无代码引用的 `sync_transactions`、`syncing_transactions`、`bank_sync_enabled`、`syncing`、`tap_to_sync`、`sync_failed` 多语言字符串已移除；汇率同步功能继续使用当前页面自己的状态文案。
- 扩大资源清理：删除无代码引用且属于旧登录、教程、反馈、推广、第三方导入或旧设置入口的多语言字符串；默认分类、预算、图表和周期等可能承载实际功能的文案继续保留。
- 收尾无引用字符串：默认资源中剩余无 `R.string` 调用的旧图表标签、旧周期快捷项、旧客户旅程说明和旧默认分类名已删除；当前初始化数据使用的新默认分类文案继续保留。
- 移除 Timber 运行时依赖：app 不再初始化 DebugTree，通知展示失败继续按原有吞异常策略处理，版本目录和 app 依赖中删除 Timber。
- 收窄饼图页旧交易泄漏：`CategoryAmount` 只向 UI 暴露关联交易的 `id/type` 轻量引用，`PieChartStatisticState` 不再携带完整 `LegacyTransaction` 列表；统计计算内部仍沿用现有旧交易算法。
- 删除无调用方的新模型计划付款付/跳过 use case；当前实际 UI 路径继续使用 legacy 计划付款处理用例。
- 删除偏好开关的旧分组元数据：设置页已经显式组织“外观与显示”“输入与列表”等分组，`BoolPreference` 不再携带无人读取的 `PreferenceGroup`。
- 收窄饼图 feature 公开面：除 app 导航图需要调用的 `PieChartStatisticScreen` 入口外，饼图状态、事件、ViewModel、内部图表组件和构图用例都改为模块内部实现。
- 继续收窄基础 feature 公开面：账户、余额、汇率和搜索模块只保留被主页面或 app 导航图调用的页面入口，状态、事件、ViewModel 和模块内展示模型改为内部实现。
- 继续收窄管理类 feature 公开面：预算和分类模块只保留 app 导航图调用的页面入口，列表状态、事件、弹窗数据、排序枚举、展示模型和局部 UI helper 改为模块内部实现。
- 收窄计划付款 feature 公开面：只保留计划付款列表页和编辑页两个外部入口，列表卡片、底栏、重复规则组件、状态、事件和 ViewModel 都改为模块内部实现。
- 收窄借贷 feature 公开面：只保留借贷列表页和详情页两个外部入口，借贷状态、事件、UI 事件、展示模型、底栏常量、记录列表 helper 和格式化扩展改为模块内部实现。
- 收窄主界面和首页 feature 公开面：只保留 `MainScreen` 与 `HomeTab` 作为外部入口，主 tab 状态、底栏、首页状态/事件、客户旅程模型和内部 UI helper 改为模块内部实现。
- 收窄设置 feature 公开面：只保留 `SettingsScreen` 作为 app 导航入口，设置状态、事件、UI 事件和 ViewModel 改为模块内部实现。
- 收窄报表 feature 公开面：只保留 `ReportScreen` 作为 app 导航入口，报表筛选模型、状态、事件、ViewModel 和筛选浮层 UI 改为模块内部实现。
- 收窄交易列表 feature 公开面：只保留 `TransactionsScreen` 作为 app 导航入口，交易列表状态、事件、UI 事件和 ViewModel 改为模块内部实现。
- 收窄编辑交易 feature 公开面：只保留 `EditTransactionScreen` 作为 app 导航入口，编辑状态、事件、UI 事件、ViewModel 和页面展示模型改为模块内部实现。
- 收窄导入 feature 公开面：只保留 `ImportCSVScreen` 与 `CSVScreen` 两个导航入口，备份恢复流程、CSV 状态/事件、解析模型、导入器和内部 flow UI 改为模块内部实现。
- 收窄旧重排弹窗公开面：feature 层继续使用单类型 `ReorderModalSingleType` 和 `ReorderButton`，底层多类型 `ReorderModal` 收为旧 UI 内部实现。
- 继续收窄旧主题色板公开面：外层旧主题门面中只被 `shared:ui:legacy` 内部使用的 `Blue`、`IvyLight`、`GreenLight`、`RedLight` 和 `IvyDark` 改为模块内部常量。
- 下沉 UI 基础服务装配：`ThemeState`、`PeriodState`、时间服务、日期时间弹窗和 `Toaster` 由 `shared:ui:core` 自己声明 Hilt 绑定，`Navigation` 由 `shared:ui:navigation` 自己声明绑定；app 不再持有这些具体实现类的装配代码。
- 下沉 Android 字符串资源适配器：`AndroidResourceProvider` 从 app 迁入 `shared:ui:core`，app 不再为通用 `ResourceProvider` 保留绑定。
- 收窄根启动快捷方式参数：桌面 shortcut 的“添加交易类型”直接解析为导航层 `TransactionRouteType`，Root 启动事件不再先携带数据层 `TransactionType` 再转换。
- 删除无调用的 UI core Material3 `BackButton` helper；当前实际页面继续使用 legacy 旧组件里的返回按钮。
- 删除无调用的 domain `TimeRange` 模型；当前时间范围功能继续使用实际页面和数据层仍在调用的 `FromToTimeRange`、`ClosedTimeRange` 与 UI period 状态。
- 收窄设置用例构造边界：设置相关 domain use case 继续作为 feature 可注入的公开入口，但它们的 `@Inject` 构造函数收为模块内部实现，避免外部直接依赖底层 Store 装配细节。
- 收窄全部 domain use case 构造边界：外部模块继续注入公开 use case 类型，具体构造函数统一收为 domain 模块内部细节。
- 收窄 data-core 实现构造边界：Room Store、偏好 Store、mapper、文件系统和远程汇率源等实现类继续留在 `shared:data:core` 内部，注入构造函数也统一收为内部细节。
- 收窄 app/feature 内部注入构造边界：ViewModel、页面内部 helper 和平台适配器仍由 Hilt 创建，但手动构造入口不再作为跨模块可见细节暴露。
- 归位 legacy 根 UI 包名：`LegacyUiRoot` 仍作为 app 装配旧 UI 的入口保留，但包名已从 `com.ivy.ui` 调整到 `com.ivy.legacy.ui`，与所在 `shared:ui:legacy` 模块一致。
- 删除 Android 系统自动备份规则：manifest 已保持 `allowBackup=false`，不再保留无实际作用的 `dataExtractionRules/fullBackupContent` 资源；应用内 zip 备份、恢复和 CSV 导入导出不受影响。
- 收窄 Android manifest 平台配置：远程汇率接口全部使用 HTTPS，app 不再允许明文流量；`INTERNET` 权限只由 app 壳层声明，data-core 不再通过库 manifest 暗中合并权限。
- 删除导航全局页面返回处理器：主页面、导入恢复页和交易筛选页改用页面内 `BackHandler` 处理局部返回行为，`Navigation` 只继续负责页面栈。
- 收窄剩余 domain 注入构造边界：偏好开关服务、偏好开关目录和借贷交易同步核心仍可由 Hilt 注入，但构造函数不再作为模块外可手动调用入口。
- 删除对象路由页面的无用入参：Main、Balance、Categories、PlannedPayments、Report、Budget、Loans 和 Search 页面入口不再接收没有数据可读的 `screen` 参数。
- 收窄计划付款 route 职责：`EditPlannedScreen` 只保存导航数据，初始必填项判断移回计划付款编辑页面内部。
- 收窄交易列表 route 数据：`unspecifiedCategory` 不再使用没有语义的可空布尔值，改为明确的 true/false 分支。
- 统一交易列表 route 集合语义：账户筛选列表和旧交易 ID 列表改为 `ImmutableList`，避免 route 持有可变集合接口。
- 收窄旧收支汇总卡片输入：卡片只接收收入/支出交易数量，不再自己解析完整旧交易历史。
- 清理报表页内部事件命名：计划付款交易的支付/收取、跳过和批量跳过事件已经只传交易 ID，不再在事件名中暴露 `LegacyTransaction`。
- 收窄 ui-core 时间格式化内部端口：`DevicePreferences` 只作为 `IvyTimeFormatter` 的本模块适配接口，不再暴露给模块外部。
- 收窄首页状态边界：`HomeState` 和 `HomeViewModel` 不再直接持有旧交易列表 UI 包装类型，只在首页调用旧列表组件时做适配转换。
- 收敛交易列表页到期交易状态：upcoming/overdue 的交易、展开状态和收支统计合并为页面本地 section，旧 UI 的 `LegacyDueSection` 只在组件调用处构造。
- 收敛报表页到期交易状态：报表页同样把 upcoming/overdue 的交易、展开状态和收支统计合并为页面本地 section，减少状态字段数量。
- 收窄报表页顶部摘要状态：报表页不再为了收支卡片计数和饼图跳转保存完整旧交易列表，只保留交易 ID 和收入/支出数量摘要。
- 收窄交易列表页顶部摘要计算：交易列表页顶部收支卡片不再在 Composable 中过滤旧交易对象，收入/支出交易数量由状态层提供。
- 收窄分类页统计加载状态：分类页不再把月度旧交易列表作为 ViewModel 字段长期保存，只在加载分类统计时作为局部输入使用。
- 收窄饼图页输入缓存：饼图 ViewModel 不再长期保存由 route ID 还原出的旧交易对象，只保存交易 ID，并在重算图表时局部读取。
- 收窄借贷详情关联交易缓存：借贷详情不再把贷款关联旧交易对象保存在 ViewModel 字段中，加载时只设置开关状态，编辑时局部读取。

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
| `:shared:data:model` | 纯数据模型和值对象：Account、Transaction、Category、Tag、ExchangeRate、primitive value object | Room DAO、Repository、Android Context、UI 文案 |
| `:shared:data:core` | 数据实现：Room、DataStore、FileSystem、Repository、备份恢复、远程汇率数据源 | UI 状态、Compose、测试 fake、feature 专用逻辑 |
| `:shared:domain` | 业务 use case：余额、统计、CSV 导出、汇率换算、设置/偏好业务规则 | Room 插件、Ktor 具体实现、Android Activity 接口、UI 资源 |
| `:shared:ui:core` | Material3 主题、通用 Compose 组件、图标、资源端口、时间端口/转换/格式化、金额 UI 格式化、UI CompositionLocal | 数据库、Repository 实现、业务写入逻辑 |
| `:shared:ui:navigation` | 页面 route、导航状态、导航容器 | domain use case、数据层实现、feature 业务逻辑 |
| `:feature:*` | 用户可感知功能页面和 ViewModel | 公共基础设施、跨模块全局状态、临时兼容代码 |
| `:temp:*` | 迁移过程中的临时兼容层 | 最终应清空并删除 |

`shared:base` 已经被删除。原先剩余的时间端口只服务 UI 根部、日期选择器和 legacy 周期 UI，已经归入 `shared:ui:core`；后续不再为少量通用 helper 重新建立基础大杂烩模块。

长期可以进一步简化 feature 模块。如果个人维护更重视低心智负担，可以把多个小 feature 合并，最终保留少量大模块。

## 当前主要问题

### 1. `temp` 模块已经变成事实公共层

现状：

- `feature:*` 和 `app` 对 `:temp:legacy-code` 的直接依赖已经迁走，Gradle 中不再 include 旧 `temp` 模块。
- `:temp:legacy-code` 模块已经删除；旧全局上下文入口暂时迁入 `shared:ui:legacy`，后续继续拆内部职责。
- 旧设计系统源码已经迁入 `shared:ui:core` 作为兼容层，并已从 `com.ivy.legacy.design.*` 进一步归位到 `com.ivy.legacy.ui.theme.system`；旧根包装器已归到 `com.ivy.legacy.ui.LegacyUiRoot`，旧 `LegacyTheme` 等概念仍存在，但不再伪装成正式设计系统。

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

- `ivy.feature` 已收窄为只服务页面 feature 模块的组合插件，集中 Compose、Hilt 和 feature 共同 shared 依赖；shared/data/domain 模块不再套用它。
- `shared:data:model`、`shared:data:api`、`shared:domain` 已收敛为 JVM/Kotlin 模块；`shared:data:core` 显式声明 Android library、Room、Hilt 和集成测试能力。
- 大部分公共 bundle 已下放到实际使用方，但仍需要继续检查 Compose、Hilt、testing 等依赖是否还有可收窄空间。

问题：

- 模块职责已经比原始仓库清楚，但个别 bundle 仍可能让模块获得未直接使用的能力。
- feature 模块当前仍统一启用 Compose 和 Hilt；短期合理，后续如果合并或拆小模块，再逐个判断是否需要 Hilt。
- 构建配置仍应保持“模块需要什么就显式声明什么”，避免重新形成大而全的约定插件。

目标：

- 保持 `ivy.android-library`、`ivy.kotlin-library`、`ivy.compose`、`ivy.hilt` 这类仍被多个模块复用的窄约定；只服务单个模块的配置优先内联。
- `ivy.feature` 只表达“这是一个页面 feature 模块”的共同构建能力，不再扩展到 shared/data/domain 这类不同职责模块。

### 3. 测试 helper 已基本移出生产源码

现状：

- `Fake*Dao` 已在 `shared:data:core/src/test`。
- 旧 `TestDispatchersProvider`、`TestTimeConverter` 已随对应端口清理删除；`TestResourceProvider` 已随资源端口迁到 `shared:ui:core/src/test`。
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
- 删除未引用的社区入口文案 `contribute` 和 `ivy_community` 及各语言翻译。
- 删除未引用的分享、贡献者计数、加入社区和旧报表引导文案及各语言翻译。
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

1. 收敛约定插件：
   - `ivy.android-library`：Android library 基础配置、Kotlin、min/compile SDK。
   - `ivy.compose`：仅给 Compose UI 模块使用。
   - `ivy.hilt`：仅给需要 DI 的模块使用。
   - 单模块专用配置不再做成 buildSrc 约定，直接放回对应模块。
2. 先迁移低风险模块：
   - `shared:base`
   - `shared:data:model`
   - `shared:test-support`
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
- `shared:data:model`、`shared:test-support` 已从 `ivy.feature` 迁出，不再默认启用完整 Compose UI 配置。
- `shared:base` 已删除；最后剩余的 `TimeProvider/TimeConverter`、设备时间实现和安全时间边界已归入 `shared:ui:core` 的 `com.ivy.ui.time` 包，原测试也迁到 `shared:ui:core/src/test`。
- `shared:data:model` 已移除轻量 `compose-runtime`，纯数据模型不再依赖 UI runtime。
- 过渡用的 `ivy.compose-runtime` 插件已经删除；当前非页面模块不再需要轻量 Compose 编译配置。
- 只被 data-core 使用的 instrumentation 测试配置已从 `ivy.integration.testing` 插件内联到 `shared:data:core`；buildSrc 不再保留单模块专用约定插件。
- `shared:data:core`、`shared:domain` 已从 `ivy.feature` 迁出；其中 `shared:data:core` 继续作为 Android 数据实现模块显式声明 Room/Hilt 等能力，`shared:domain` 已进一步改成纯 JVM/Kotlin 模块。
- `shared:domain` 已移除空 androidTest 源集；domain 当前只保留 JVM 单元测试，Room migration 和备份恢复这类设备测试继续留在 `shared:data:core`。
- `shared:domain` 已移除 `ivy.room` 和 `ivy.hilt` 插件；主源码只保留 `javax.inject` 构造注入注解供 app 侧 Hilt 图消费，domain 自身不再参与 Hilt 聚合，测试也不再为了 domain 行为验证创建内存 Room 数据库。
- `shared:domain` 已移除 Ktor 依赖；汇率同步测试改用 `ExchangeRateStore` fake 验证业务转换与保存行为，真实网络 client 继续留在 data core 实现边界。
- 只被 data-core 使用的 Room/schema 配置已从 `ivy.room` 插件内联到 `shared:data:core`；KSP 仍由现有 Hilt 约定提供，data-core 自己声明 Room compiler 依赖，buildSrc 不再保留单模块专用 Room 约定。
- `shared:ui:core`、`shared:ui:legacy`、`shared:ui:navigation` 已从 `ivy.feature` 迁到 `ivy.compose`；shared UI 模块不再伪装成 feature。
- `shared:ui:core`、`shared:ui:legacy`、`shared:ui:navigation` 的 Kotlin 源码已从 `src/main/java` 迁到 `src/main/kotlin`，`shared:ui:core` 测试源码同步迁到 `src/test/kotlin`；包名和运行行为不变，只收敛源集结构。
- 所有 `feature:*` 模块的 Kotlin 源码已从 `src/main/java` 迁到 `src/main/kotlin`；页面模块继续保持原包名和行为，只让目录结构匹配 Kotlin-only 代码事实。
- `app` 和 `shared:data:core` 的 Kotlin 源码也已迁到 `src/*/kotlin` 源集；其中 data-core 的 unit test 与 androidTest 同步迁移，持久化、备份和 Room 测试包名不变。
- `ivy.compose` 已收敛为纯 Android Compose 配置，不再隐式套用 `ivy.module` 或引入未使用的 Molecule 插件；feature 模块现在显式同时声明 `ivy.compose` 与 `ivy.hilt`，需要 Hilt Module 的 shared UI 模块才显式声明 `ivy.hilt`。
- `ivy.hilt` 已收敛为纯 Hilt/KSP 配置，不再隐式应用 `ivy.android-library`；需要 Android 基础配置的模块必须通过 `ivy.compose` 或显式 `ivy.android-library` 声明。
- 版本目录里的 Compose LiveData 依赖别名已从临时/拼写错误的 `compose-runtime-livedate-temp` 改为 `compose-runtime-livedata`，保留依赖本身不变。
- 删除无运行时调用方的 `FormatMoneyUseCase` 和对应测试，`shared:ui:core` 不再因为这段旧金额格式化草稿依赖 `shared:domain` 或 DataStore；当前实际金额展示继续使用既有 data model currency formatting 与旧 UI 展示逻辑。
- `shared:ui:navigation` 已移除未使用的 `shared:domain` 依赖；导航模块当前只依赖基础类型、UI core 和自身导航状态。
- app 模块已移除自身不再直接使用的 Ktor、Room、OpenCSV、Keval、RecyclerView、AndroidX Security 和 Arrow 依赖；源码层已经不再直接引用 data core DAO/repository，Gradle 层保留对 data-core 的实现依赖以纳入 Hilt 绑定。
- app 模块重新显式依赖 `shared:data:core` 作为运行时数据实现模块；feature/domain 仍只依赖 data 端口，data-core 的 Hilt Module 负责把 Room、DataStore、备份、文件和远程汇率实现绑定进应用图。
- `ivy.android-library` 不再给所有 Android library 默认添加 Arrow；`shared:data:model` 因公开 `Either/Raise` API 显式用 `api` 暴露 Arrow，其他实际直接使用 Arrow 的模块改为各自声明 `implementation(libs.bundles.arrow)`；旧 FRP helper 移出后，`shared:base` 不再需要 Arrow。
- `ivy.android-library` 不再给所有 Android library 默认添加 Timber；后续 app、data-core 和版本目录中的 Timber 依赖也已删除，项目当前不再依赖 Timber。
- `ivy.android-library` 不再给所有 Android library 默认添加整套单元测试依赖；当前有 `src/test` 的 `shared:data:model`、`shared:test-support`、`shared:data:core`、`shared:domain` 和 `shared:ui:core` 改为在各自模块里显式声明测试 bundle。
- 新增 `ivy.kotlin-library` 作为纯 JVM/Kotlin 模块约定；`shared:data:model`、`shared:test-support`、`shared:data:api` 和 `shared:domain` 已从 Android library 改成 JVM 模块，不再需要 namespace、Android manifest、min/compile SDK 或 Android Kotlin runtime。
- `shared:data:core` 的 DataStore 依赖已从 `api` 收窄为 `implementation`；DataStore 绑定仍由 data core 提供，但不再通过 data core 传递暴露给其他模块。
- `shared:domain` 已移除 AndroidX DataStore 依赖；偏好开关的存储能力抽成 `PreferenceToggleStore` 端口，DataStore 读写和清空由 `shared:data:core` 实现，domain 只保留业务级 `PreferenceToggleService` 和开关元数据。
- `shared:data:api` 已显式暴露 Arrow 依赖；`ExchangeRateStore` 的公开签名直接使用 `Either`，不再依赖 `shared:data:model` 间接传递 Arrow。
- `ExchangeData` 已增加普通字符串工厂方法，账户页和交易页不再为了构造币种 `Option` 直接依赖 Arrow；`feature:accounts` 和 `feature:transactions` 已移除 Arrow Gradle 依赖。
- 汇率页保存/删除手动汇率时已从 `either/bind` DSL 改为普通顺序校验，`feature:exchange-rates` 不再直接声明 Arrow 依赖；数据模型层仍负责暴露值对象校验结果。
- CSV 导入的新分类创建已从单个 `either` 块改为普通值对象解析；`feature:import-data` 移除 Arrow Gradle 依赖后，当前所有 `feature:*` 模块都不再直接声明或导入 Arrow。
- CSV 反读 helper `ReadCsvUseCase` 已从 domain 主源码移到测试源集，OpenCSV 在 `shared:domain` 中收窄为测试依赖；导出 CSV 的字段转义改为本地实现，domain 主源码不再依赖 OpenCSV 或其传递依赖。
- `ivy.module` 不再默认启用 kotlinx serialization；当前页面模块没有序列化源码引用，序列化能力只保留在 `shared:data:model` 和 `shared:data:core` 等实际需要的模块中。
- app 模块已移除 Kotlin serialization 插件；应用壳本身没有序列化源码，序列化继续由 `shared:data:model` 和 `shared:data:core` 提供。
- 空壳 `ivy.module` 约定插件已删除；旧的过宽 `ivy.feature` 用法已从 shared/data/domain 模块中清空，页面 feature 模块后续只通过窄 `ivy.feature` 组合公共构建能力。
- app 与 `ivy.android-library` 已移除重复的旧式 `kotlin-android` 插件 ID，只保留正式 `org.jetbrains.kotlin.android` 插件；版本目录中仅包含 Android 协程运行时的依赖 bundle 已改名为 `kotlin-android-runtime`，避免和插件 ID 混淆。
- app、`ivy.android-library` 和 `ivy.kotlin-library` 已把 Kotlin JVM target 配置从弃用的 `kotlinOptions` 迁到 `compilerOptions`；Java/Kotlin 目标仍保持 17，构建输出不再出现该弃用警告。
- app 当前没有 `src/test` 或 `src/androidTest` 源码，已移除 app 模块中无消费方的通用测试 bundle 和 WorkManager 测试依赖；运行时 WorkManager 依赖保留。
- 版本目录中无引用的 `androidx-work-testing` 依赖别名已删除；交易提醒仍使用运行时 `androidx-work`。
- 根目录 `temp/` 已加入 `.gitignore`；旧 `temp:*` 模块不再被 Gradle include，后续本地残留构建目录不会被误加回版本库。
- 新增 `ivy.feature` 组合约定插件，集中 feature 模块共有的 Compose、Hilt 和 shared 依赖；16 个 `feature:*` 模块改为只声明 `ivy.feature`、namespace 和少量自身额外依赖，页面模块源码与运行行为不变。

### 阶段 3：测试支持代码归位

目标：生产源码不再包含测试 fake。

候选迁移：

- `shared:data:core/src/main/java/com/ivy/data/db/dao/fake/Fake*Dao.kt`
- `shared:base/src/main/java/com/ivy/base/TestDispatchersProvider.kt`
- `shared:base/src/main/java/com/ivy/base/resource/TestResourceProvider.kt`（已迁到 `shared:ui:core/src/test`）
- 旧 `shared:base/src/main/java/com/ivy/base/time/impl/TestTimeConverter.kt` 已删除。

可选方案：

1. 简单方案：复制到各自模块的 `src/test` 和 `src/androidTest`。
2. 中等方案：创建 `:shared:test-support`，仅测试依赖它。
3. 更完整方案：使用 Android Gradle test fixtures，但会增加配置复杂度。

个人维护推荐：

- 优先把测试 helper 放回具体模块的 `src/test` 或 `src/androidTest`；只有多个模块确实复用时再单独建测试支持模块。
- 不为了“标准化”引入过复杂的 test fixtures 配置。

当前进展：

- `:shared:base-testing` 已删除；跨模块复用的数据模型测试生成器、fixture 和近似数值断言已归位到 `:shared:test-support`，不再挂在 `shared:data:model-testing` 名下。
- `:shared:test-support` 只作为测试依赖提供给 data-core 和 domain，包名统一为 `com.ivy.testing`，不进入 app 运行时依赖图。
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
- 已把旧排序接口从 `com.ivy.wallet.domain.data.Reorderable` 迁出 domain，随后继续收窄到 `shared:ui:legacy` 的排序弹窗边界；`Account/Category/Tag` 等正式数据模型不再实现 UI 拖拽排序契约。
- 已把计划付款复用的 `RecurringRuleModal` 通过外部 `pickDate` 回调与 `IvyWalletCtx` 解耦，并迁入 `shared:ui:legacy`。
- 已把旧时间范围兼容模型迁出 `temp:legacy-code`：纯 `ClosedTimeRange`、`FromToTimeRange`、收入/支出统计值对象已进入正式 `com.ivy.data.model`；overdue/upcoming 过滤函数和账户页展示聚合已分别迁入 domain/feature 边界；仍带 UI 文案/格式化职责的旧 `TimePeriod`、`Month`、`LastNTimeRange`、`MainTab` 暂时保留在 `shared:ui:legacy` 的 legacy model 区。
- 已把 `FromToTimeRange.toDisplay(...)` 从 domain 模型上拆成 `shared:ui:legacy` 的 UI 扩展，避免 `shared:domain` 依赖 `TimeFormatter`。
- 已把 `Month.incrementMonthPeriod` 改成只返回新周期，不再直接更新 `IvyWalletCtx`；各页面/ViewModel 在调用处显式保存选中周期，副作用更清楚。
- 已把 `ChoosePeriodModal` 和 `PeriodSelector` 迁入 `shared:ui:legacy`，并通过外部 `saveSelectedPeriod`、`pickDate`、`startDateOfMonth` 参数替代内部直接读取 `IvyWalletCtx`。
- 已把金额输入弹窗、计算器弹窗和缓冲金额弹窗迁入 `shared:ui:legacy`。金额键盘的“标准键盘布局”偏好已经改为由 app 根部提供 legacy UI 专用偏好入口，不再把 domain 的 `PreferenceToggleCatalog/BoolPreference` 类型暴露给旧 UI；`shared:ui:legacy` 仍因旧账户/借贷弹窗和周期模型暂时依赖 legacy domain 模型，后续按 UI model 边界继续拆。
- 已把旧 `legacy.datamodel` 整体迁入 `shared:domain`，把旧创建参数模型迁入 `shared:domain`，并把账户/分类/借贷创建参数里的颜色从 Compose `Color` 改为普通 ARGB `Int`，由 UI 弹窗在边界处转换。
- 已把旧颜色选择器、账户弹窗、分类弹窗、借贷弹窗和借贷记录弹窗迁入 `shared:ui:legacy`。颜色选择器移除了旧付费锁显示分支，不再依赖会员状态。
- 已把旧 UI 状态模型 `AppBaseData`、`LegacyDueSection`、`BufferInfo`、`EditTransactionDisplayLoan` 迁入 `shared:ui:legacy`，作为迁移期的 UI 兼容数据。
- 已把搜索框、收入/支出卡片、详情工具栏、标签弹窗、交易卡片和交易列表组件迁入 `shared:ui:legacy`；交易卡片查找账户/分类时改为只使用调用方传入的数据，去掉了对 `IvyWalletCtx` 缓存的读取。
- 早期迁入 `shared:domain` 的旧页面状态值对象已继续下沉：`SortOrder` 进入分类 feature，`CustomExchangeRateState` 进入编辑交易 feature，`TransactionHistoryDateDivider` 已进一步归位到正式 `com.ivy.data.model`。
- 已把编辑交易/计划付款复用的底部表单组件迁入 `shared:ui:legacy`；`EditBottomSheet` 改用 Compose 屏幕高度，不再为了底部操作条位置读取 `IvyWalletCtx`。
- 已把旧 domain 层对 `IvyWalletCtx` 的直接依赖拆掉：账户/分类缓存 action 已删除，起始日状态由 `PeriodState` 和正式 settings use case 承接，调用方显式更新旧 UI 上下文；借贷交易逻辑去掉固定为 true 的付费判断分支。
- 已把旧 `domain/action`、`domain/pure`、旧汇率换算逻辑、账户数据 action、交易范围过滤 action 迁入 `shared:domain`。
- 已把旧 creator、计划付款逻辑、标题建议、账户/分类统计逻辑和借贷交易联动逻辑迁入 `shared:domain`；后续再逐步从 legacy 包迁到正式 use case。
- 已把仍依赖 Android 字符串资源的默认钱包数据预置逻辑从 `temp:legacy-code` 移到 app 默认数据初始化边界，当前由 `DefaultWalletDataSeeder` 承接，避免 `temp` 继续承载旧业务逻辑。
- 已精简 `DefaultWalletDataSeeder`：删除没有运行时入口的账户/分类建议列表，默认账户预置直接创建当前 `data.model.Account`，不再通过旧 `legacy.domain.model.Account` 转换；首次启动默认现金、银行账户和默认分类保持不变。
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
- 交易提醒调度器不再使用 deprecated 的全局 `timeNowLocal()`，也不再注入 `TimeProvider`；调度本身直接用本地 `LocalDateTime.now()` 计算下一次 20:00 提醒，提醒语义保持不变。
- 第一批 ViewModel 已停止使用 deprecated 的全局 UTC 时间函数：设置导出文件名、首页/余额/交易/饼图的月份切换、报表 upcoming/overdue 判断和报表导出文件名都改为通过注入式 `TimeProvider` 获取当前时间。
- 第一批非 UI feature 逻辑已停止使用 deprecated 的全局 `stringRes()`：交易页/报表页/饼图中的特殊分类名称，以及首页客户旅程卡片文案都改为通过注入式 `ResourceProvider` 获取字符串资源。
- 第一批 feature 屏幕层已停止使用 deprecated 的全局 `stringRes()`：首页、交易页和报表页的空状态/标签文案改为 Compose 原生 `stringResource()`，列表构建块继续接收普通字符串参数。
- feature 源码中的剩余预算类型和借贷类型显示也已停止使用全局 `stringRes()`；这些文案只在 Composable 调用点使用，因此改为 Composable 格式化函数内部调用 `stringResource()`。
- `shared:ui:legacy` 的收入/支出卡片、统计工具栏和旧交易列表组件已停止使用全局 `stringRes()`；旧交易列表不再提供依赖全局上下文的默认空状态标题，调用方需显式传入普通字符串。
- `shared:ui:legacy` 不再重复定义 `densityScope()`，旧 UI 内部密度转换改为复用 `shared:ui:core` 的同名 helper。
- `clickableNoIndication()`、`consumeClicks()` 和 `rememberInteractionSource()` 已从 legacy UI 迁到 `shared:ui:core`；feature 和旧 UI 调用方改为从通用 Compose helper 包导入，减少普通点击交互对 legacy 包的依赖。
- `selectEndTextFieldValue()` 已从 legacy UI 迁到 `shared:ui:core`，交易、分类、预算、搜索、汇率和旧弹窗输入框不再为了光标定位 helper 依赖 legacy 包。
- `drawColoredShadow()` 已从 legacy UI 迁到 `shared:ui:core`，首页、饼图、设置、借贷和旧按钮组件的通用阴影绘制不再挂在 legacy 包下。
- `toDensityPx()/toDensityDp()` 密度换算扩展已从 legacy UI 迁到 `shared:ui:core`，底部栏和旧返回栏不再为了尺寸换算 helper 依赖 legacy 包。
- `windowInsets/statusBarInset/navigationBarInset/navigationBarInsets/keyboardOnlyWindowInsets` 已从 legacy UI 迁到 `shared:ui:core`，搜索页、首页更多菜单和各底部栏改为使用通用窗口 inset helper。
- 页面级 `onScreenStart()` 保留在 `shared:ui:navigation`，跟导航状态保持同一模块；legacy 组件内部不再使用它，改用 `shared:ui:core` 的 `onCompositionStart()` 表达普通组合生命周期副作用。
- 键盘显示监听、隐藏键盘、状态栏深色文字控制和旧日期展示格式化已从 legacy UI 迁到 `shared:ui:core`；搜索、交易、借贷、计划付款和旧弹窗继续使用相同行为，但不再通过 legacy 包拿通用平台/时间 helper。
- 弹簧动画、插值、颜色插值、滑动手势监听、dp 转 px 和 interval 类型文案已从 legacy UI 根包迁到 `shared:ui:core`；首页、主底栏、饼图、报表和旧弹窗保留原交互，但通用动画/手势工具不再挂在 legacy 根包下。
- `SearchInput` 已归入 legacy 组件包，金额输入偏好 CompositionLocal 已归入 `shared:ui:core` 的 preferences 包，`LegacyUiRoot` 对外包名已改为 `com.ivy.legacy.ui`，与 `shared:ui:legacy` 模块保持一致。
- 首页缓冲金额展示模型 `BufferInfo` 和编辑交易借贷提示模型 `EditTransactionDisplayLoan` 已移回各自 feature；`shared:ui:legacy` 不再保存这两段页面私有状态。
- 周期选择模型和状态 `TimePeriod/Month/LastNTimeRange/PeriodState` 已从 legacy UI 迁到 `shared:ui:core` 的 `com.ivy.ui.period`；首页、交易、报表、饼图、预算和根部状态继续共用同一周期语义，但不再依赖 legacy 包。
- 周期模型和弹窗动画时长已分别归入 `shared:ui:core` 的 period/animation 包；周期选择弹窗的 `ChoosePeriodModalData` 作为旧弹窗入参保留在 `shared:ui:legacy`。
- 账户、分类、缓冲金额、借贷、借贷记录、周期选择和计划付款重复规则弹窗的 `*ModalData` 已归回 `shared:ui:legacy`；这些数据对象本质上仍是旧弹窗入参，不再伪装成 UI core 公共 API。
- CSV 导入器的新账户/分类默认颜色已改用导入功能自己的 ARGB 调色板；导入解析逻辑不再为了颜色值依赖 Compose `Color` 或 legacy theme。
- 首页客户旅程卡片模型已改为保存普通 ARGB 背景色，卡片 provider 不再依赖 legacy theme；只有实际 Composable 绘制边界继续把颜色转成旧 UI 渐变。
- 交易、报表和饼图的 ViewModel/UseCase 中用于占位分类的颜色已改成本地 ARGB 常量；非绘制逻辑不再为了 `Color.toArgb()` 依赖 Compose graphics 或 legacy theme。
- 旧交易列表组件的数据契约 `AppBaseData/LegacyDueSection` 已继续提升到 `shared:ui:core` 的 `com.ivy.ui.transaction`；首页、报表、搜索和交易页状态不再为了列表数据契约引用 legacy 交易组件包。
- `getCustomIconIdS()` 已从 legacy 组件包迁到 `shared:ui:core` 的 `com.ivy.ui.icon`；旧图标查找实现的内部类型和 fallback 逻辑已收窄为文件私有，图标选择器的静态图标清单也不再作为 legacy 公共 API 暴露。
- 旧弹窗内部实现细节已继续收窄可见性：周期月份项、图标分组、选择分类新增按钮、金额键盘局部展示组件和 modal action row 不再作为模块外 public API 暴露；跨旧弹窗复用的金额键盘按钮和动态 action helper 仅保留 `shared:ui:legacy` 模块内可见。
- 旧颜色兼容层继续收窄公开面：`dynamicContrast()` 仍是外部可用入口，但 HSV 拆解、亮暗调整和底层 HSV 转换 helper 已改为文件私有实现，避免旧主题算法细节继续作为公共 API 扩散。
- 旧交易卡片的账户/分类查找 helper 已内聚回 `TransactionCard.kt` 并改成私有实现，删除不再提供公共 API 的 `component.transaction.Utils.kt`。
- 旧交易列表的公开边界继续压缩：单卡片 `TransactionCard` 和历史日期分隔 `HistoryDateDivider` 仅保留 legacy 模块内可见，分类 badge 也改为卡片内部私有实现；feature 仍通过列表级 `transactions(...)` 或明确复用的展示组件接入。
- 旧通用组件的底层积木继续降级为模块内 API：`IvyBasicTextField`、`IvyNumberTextField`、`IvyDividerLineRounded`、`AmountCurrencyB2Row` 和无语义的 `CircleButton` 不再直接暴露给 feature，外部继续使用更明确的搜索框、周期输入、底部栏、金额展示和填充圆按钮等入口。
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
- 日期、时间范围和 `IntervalType` 周期递增 helper 已迁到 `com.ivy.base.time`；旧交易兼容模型在 `com.ivy.data.model.legacy` 内已改成真实类 `LegacyTransaction` 加兼容别名 `Transaction`，交易标签 DTO `LegacyTag`、交易历史列表接口 `TransactionHistoryItem` 和日期分隔项已归位到正式 `com.ivy.data.model`。
- 旧主题枚举已迁到 `com.ivy.data.model.Theme`，数据库仍通过枚举 `name` 持久化，现有设置值不变；旧 `SharedPrefs` 已迁到 `com.ivy.base.prefs.SharedPrefs`，同一个 `ivy_wallet_prefs` 文件名和 key 保持不变。
- `shared:base` 中的 `com.ivy.base.legacy` 包已经清空；后续重点从“迁出 legacy 包名”转向“减少 Android SharedPreferences 对 domain/data 的扩散”。
- 偏好读写已抽出窄端口，`SharedPrefs` 只作为 Android 实现通过 Hilt 绑定；业务 key 集中到 `SharedPreferenceKeys`，domain 和数据备份恢复不再直接依赖 `SharedPrefs` 具体类。
- 偏好 toggle 的 UI 读取边界已从 domain 拆到 `shared:ui:legacy`：旧金额键盘只接收 legacy UI 专用的键盘布局 Flow，domain 中的 `BoolPreference` 只保留 key、默认值和分组等元数据。
- `shared:domain` 已删除剩余 legacy 数据模型上的 Compose `@Immutable` 注解，并移除 `ivy.compose-runtime` 插件；domain 不再需要 Compose 编译配置。
- `shared:domain` 已完全移除 Ktor/Room 测试依赖；汇率同步验证改为 JVM 单元测试，domain 只关心 `ExchangeRateStore` 端口行为。
- `shared:data:core` 的 AndroidManifest 已删除被 AGP 忽略的 `package` 属性，命名空间统一由模块 `namespace = "com.ivy.data"` 提供。
- `shared:data:core` 的测试 fake DAO 已停止使用 Compose Locale helper，并移除 `ivy.compose-runtime` 插件；数据层不再为测试字符串处理引入 Compose 配置。
- `shared:data:model` 已删除剩余数据类上的 Compose `@Immutable` 注解，并移除 `compose.runtime` 依赖；纯数据模型不再依赖 UI runtime。
- `shared:base` 已删除基础枚举和旧交易兼容模型上的 Compose `@Immutable` 注解，并移除 `compose.runtime` 依赖；基础层不再依赖 UI runtime。
- `shared:base` 已移除只为旧 LiveData helper 保留的 `androidx.lifecycle:lifecycle-livedata-core` 依赖；基础层目前不再依赖 Lifecycle。
- `shared:ui:navigation` 和 `shared:ui:legacy` 已移除 `ivy.hilt` 插件；它们只保留轻量 `javax.inject` 注解依赖，继续通过 app 的 Hilt 图提供 `Navigation` 和 `PeriodState` 单例；`MainTabState` 后续已收敛为主页面 ViewModel 状态。
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
- 曾把编辑交易/计划付款复用的旧底部表单组件从 `com.ivy.wallet.ui.edit.core` 迁到 `com.ivy.legacy.ui.edit.core`；当前这批编辑页业务 UI 已继续迁回各自 feature 私有边界，旧 shared/feature UI 不再使用 `com.ivy.wallet.ui.*`。
- 已清理迁移过程中留下的 `com.ivy.legacy.legacy.ui.theme.*` 双重 legacy 包名：预算进度条和日期时间行归入 `com.ivy.legacy.ui.component`，弹窗名称输入归入 `com.ivy.legacy.ui.modal`。
- 已把 `TransactionHistoryDateDivider` 从旧 `com.ivy.wallet.domain.data` 归位到正式 `com.ivy.data.model`；它仍服务旧交易列表和旧日期分组，并与 `TransactionHistoryItem` 位于同一模型包。`SortOrder/CustomExchangeRateState` 已进一步下沉到对应 feature。
- 旧创建/编辑参数已从早期的 `com.ivy.wallet.domain.deprecated.logic.model` 迁出；当前 `CreateAccountData`、`CreateBudgetData`、`CreateCategoryData`、`CreateLoanData`、`CreateLoanRecordData`、`EditLoanRecordData` 已归位到正式 `com.ivy.data.model`，旧页面和正式 use case 继续使用同名语义。借贷创建参数仍可引用旧账户模型，但借贷本体和借贷记录模型已经归位。
- 已把 `shared:domain` 中旧业务逻辑从早期 deprecated/legacy logic 包继续迁出：计划付款、账户统计、分类统计、借贷交易联动和旧汇率换算已进入正式 use case 包；当前不再保留 `com.ivy.legacy.domain.logic` 源码。
- 已把旧 FPAction/use-case 与 pure helper 从 `com.ivy.wallet.domain.action/pure` 迁到 `com.ivy.legacy.domain.action/pure`；`ClosedTimeRange`、`FromToTimeRange`、`IncomeExpensePair`、`IncomeExpenseTransferPair` 已进一步归位到正式 data model。旧 action/helper 仍是 domain 兼容层，但不再占用正式 Wallet 产品包名。
- 已把旧 FRP/action helper 从 `shared:base` 物理下沉到 `shared:domain`，仍保留 `com.ivy.legacy.frp` 包名以避免大面积调用方 import churn；`shared:base` 不再承载这批旧 action 组合工具。
- 旧 UI 专用的 `TestingContext` 全局测试开关已删除；它在生产源码中没有写入点，相关分支运行时恒为 false，旧组件直接执行原本的正常滚动逻辑。
- 旧 FRP 组合 helper 已精简为只保留实际使用的重载，删除历史推导注释和“迁到 FP/FPAction”的过时 TODO；业务含义明确的 TODO 继续保留。
- 旧 `Res.tryOp()` 已移除没有收益的 `inline/noinline` 组合，避免迁移后 domain 编译持续产生无意义的 inline 性能警告。
- 旧 domain 逻辑和旧交易模型不再用大面积 `@Deprecated` 注解制造编译噪音；当前仍保留这些实现以支撑报表、交易列表、计划付款和旧统计流程，迁移状态通过 `legacy` 包名和 README 计划追踪。
- 旧交易分组仍需要的本地时区转换 helper 已从 `shared:base` 移到 `com.ivy.domain.time`；`shared:base` 不再暴露这段只服务 domain 交易流程的扩展函数。
- 已清理一批低风险编译警告：保留仍被使用的 `LegacyTag` 和客户旅程卡片 provider，但取消误导性废弃标记；Arrow `orNull()`/旧 `option` DSL、旧 Material `Divider` 和 Kotlin `toUpperCase()` 调用已更新到当前 API。
- 继续清理低风险废弃 API：旧货币选择器改用 `String.lowercase(Locale)`，旧排序弹窗改用 `bindingAdapterPosition` 并处理 `NO_POSITION`，汇率页箭头图标改用 AutoMirrored 版本。
- 已把 `com.ivy.legacy.datamodel.temp` 中的旧实体/新模型 mapper 扩展函数迁到 `com.ivy.domain.mapper.legacy`；这些文件仍服务旧数据模型兼容，但不再使用含糊的 `temp` 包名。
- 旧兼容模型已从早期的 `com.ivy.legacy.datamodel` 迁出，并继续按职责下沉；当前 `com.ivy.data.model.legacy` 只剩旧账户/旧交易兼容层，数据库实体转换保留在 `com.ivy.domain.mapper.legacy`。
- 已把跨模块混用的旧模型从 `com.ivy.legacy.data.model` 拆出并继续归位：`FromToTimeRange` 已进入正式 `com.ivy.data.model`，账户页展示聚合 `AccountData` 已下沉到 `feature:accounts`；UI 侧 `TimePeriod/Month/LastNTimeRange` 暂时保留在旧 UI 包，因为它们仍依赖 UI 文案和时间格式化。
- 已把 `ClosedTimeRange`、`IncomeExpensePair`、`IncomeExpenseTransferPair` 从旧 `com.ivy.legacy.domain.pure.data` 包归入正式 `com.ivy.data.model`；它们仍作为旧统计流程的值对象保留在 `shared:data:model`，但不再使用 legacy 包名。
- 旧时间范围值对象 `FromToTimeRange` 已从 `com.ivy.legacy.domain.model` 归位到正式 `com.ivy.data.model`；upcoming/overdue 交易过滤函数已迁到 `com.ivy.domain.time`，UI 和 feature 不再为了这个纯范围对象引用 legacy domain 包名。
- upcoming/overdue 交易日期过滤 helper 已从 `com.ivy.legacy.domain.model` 迁到 `com.ivy.domain.time`；legacy model 包不再承载这类业务过滤函数。
- 通用排序号、Arrow `Option` 归零和 non-empty list 折叠 helper 已从 `com.ivy.legacy.domain.pure.util` 迁到 `com.ivy.domain.util`；这些工具继续服务排序创建、CSV 导入和旧统计折叠，但不再挂在 legacy pure 包下。
- 账户余额过滤、账户币种 fallback 和汇率换算纯函数已从 `com.ivy.legacy.domain.pure.account/exchange` 迁到 `com.ivy.domain.account` 与 `com.ivy.domain.exchange`；它们仍兼容旧账户/汇率模型，但包边界已经按业务职责归位。
- 旧交易纯计算、日期分组和新旧交易值桥接函数已从 `com.ivy.legacy.domain.pure.transaction` 迁到 `com.ivy.domain.transaction.legacy`；当前 `shared:domain` 中的 `com.ivy.legacy.domain` 源码已经清空。
- 账户页展示模型 `AccountData` 和对应 `AccountDataAct` 已从 `shared:domain` 下沉到 `feature:accounts`；账户页专用展示聚合不再占用 shared domain 边界。
- 纯创建参数 `CreateAccountData`、`CreateCategoryData`、`CreateBudgetData`、`CreateLoanData`、`CreateLoanRecordData` 和借贷记录编辑参数 `EditLoanRecordData` 已从 `com.ivy.legacy.domain.model` 下沉并进一步归位到正式 `com.ivy.data.model`，UI 弹窗、feature event 和 domain creator 继续使用同名参数对象。
- 预算模型 `Budget` 已从 `com.ivy.data.model.legacy` 归位到正式 `com.ivy.data.model`；字段、序列化 ID 字符串、软删除标记和 Room/备份格式保持不变，预算页和预算相关 use case 继续使用同一模型语义。
- 无调用方的旧 `ExchangeRate` 兼容模型已删除，汇率读写、同步和页面展示统一使用正式 `com.ivy.data.model.ExchangeRate`。无调用方的旧 `Category` 兼容模型和 mapper 已删除，分类功能继续使用正式 `com.ivy.data.model.Category`。
- 借贷模型 `Loan`、`LoanRecord` 已从 `com.ivy.legacy.domain.model` 下沉并归位到正式 `com.ivy.data.model`；借贷数据库转换 `toEntity()` 已移入 legacy domain mapper。字段、`isDeleted` 软删除语义、日期类型和 Room/备份格式保持不变。
- 旧账户模型在 `com.ivy.data.model.legacy` 内已改成真实类 `LegacyAccount` 加兼容别名 `Account`；计划付款规则 `PlannedPaymentRule` 已归位到正式 `com.ivy.data.model`。旧交易 `toEntity()` 已从 model 包合并进 `com.ivy.domain.mapper.legacy.TransactionExt`。`com.ivy.legacy.domain.model` 源码目录已经清空。
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
- 已把旧 `IvyUI` 根包装器迁到 `com.ivy.legacy.ui.LegacyUiRoot` 并改名，`com.ivy.legacy.design.api` 包已经清空。
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
   - 纯 Kotlin：优先留在具体消费模块；确实多模块复用时再进入 `shared:domain`、`shared:data:model` 或更明确的业务工具包，不再恢复 `shared:base` 大杂烩。
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

目标：把 `SharedPrefs` 和零散 DataStore key 收敛成明确的偏好端口和业务服务。

建议保留/建立：

- `PreferenceToggleService`
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
- 原 `shared/domain/features` 已迁到 `shared/domain/preferences/toggles`，`Features/BoolFeature` 重命名并收敛为 `PreferenceToggleCatalog/BoolPreference`；旧分组元数据已删除，底层 DataStore key 仍沿用 `feature_...` 前缀，保证已安装设备上的偏好开关不丢失。
- 偏好开关定义不再保留单实现的 `PreferenceToggles` 接口；当前直接注入 `PreferenceToggleCatalog`，设置页和各 feature 仍读取同一批 `BoolPreference` 定义，key 和默认值不变。
- 删除未接入运行时和设置页的 `showDecimalNumber` 偏好定义，以及对应“未来 PR 再打开”的注释；当前可见偏好项和已使用 key 不变。
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
- 旧交易桥接函数已从 `shared:data:core` 移到 domain 旧交易纯逻辑包：`getValue/getAccountId/getTransactionType` 不再作为数据实现层 API 暴露，预算、报表和旧 domain 逻辑改为从 `com.ivy.domain.transaction.legacy` 使用这些扩展。
- 预算页数据边界已收敛：新增 `GetBudgetsUseCase` 和 `ReorderBudgetsUseCase` 封装预算列表读取与排序保存，旧 `BudgetsAct` 已删除；`:feature:budgets` 不再直接注入 `WriteBudgetDao`，并已去掉对 `shared:data:core` 的直接依赖。
- 预算创建、编辑和删除已从旧 `BudgetCreator` 拆成 `CreateBudgetUseCase`、`UpdateBudgetUseCase` 和 `DeleteBudgetUseCase`；预算页只依赖正式 use case，旧 `BudgetCreator` 已删除。
- 账户创建和编辑已从旧 `AccountCreator` 拆成 `CreateAccountWithBalanceUseCase` 和 `UpdateAccountWithBalanceUseCase`；主页面、编辑交易、计划付款、借贷和交易详情页不再注入旧 creator，账户保存后自动生成余额调平交易的行为保持不变。
- 分类创建和编辑已从旧 `CategoryCreator` 拆成 `CreateCategoryUseCase` 和 `UpdateCategoryUseCase`；分类页、编辑交易、计划付款和交易详情页不再注入旧 creator，分类排序号、图标、颜色和空名称校验保持不变。
- 首页数据边界已收敛：新增 `GetCustomerJourneyStatsUseCase` 封装首页引导卡片需要的交易/计划付款计数，新增 `MapTransactionsToLegacyTransactionsUseCase` 封装新旧交易模型转换，`:feature:home` 不再直接依赖 `TransactionRepository`、`PlannedPaymentRuleDao` 或 `TransactionMapper`，并已去掉对 `shared:data:core` 的直接依赖。
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
- 新交易模型的计划付款支付/跳过处理曾从 `PlannedPaymentsLogic` 拆出，但后续 UI 路径已全部回到 legacy 计划付款处理边界；无调用方的 `PayOrSkipPlannedTransactionUseCase` 和 `PayOrSkipPlannedTransactionsUseCase` 已删除，`PlannedPaymentsLogic` 也已删除。
- 编辑交易页数据边界已收敛：新增 `SaveLegacyTransactionUseCase`、`DeleteTransactionUseCase`、`GetLoanUseCase` 和一组标签读写/关联用例，交易保存、删除、复制、标签创建、标签编辑、标签删除和标签关联不再直接调用数据层 repository/mapper，`:feature:edit-transaction` 已去掉对 `shared:data:core` 的直接依赖。
- 交易详情页数据边界已收敛：新增 `GetAccountUseCase`、`DeleteAccountUseCase`、`DeleteCategoryUseCase` 和 `MapTransactionsToLegacyTransactionsWithTagsUseCase`，账户详情、分类详情、账户删除、分类删除和带标签历史列表不再直接注入数据层 repository/DAO/mapper，`:feature:transactions` 已去掉对 `shared:data:core` 的直接依赖。
- 报表页数据边界已收敛：新增 `GetTransactionsUseCase` 和 `GetTransactionsByTagsUseCase`，报表筛选不再直接读取 `TransactionRepository/TagRepository`，新旧交易模型转换改走 `MapTransactionsToLegacyTransactionsUseCase`；`ExportCsvUseCase` 的自定义导出回调不再暴露 `TransactionRepository` receiver，默认全量导出也改走 `GetTransactionsUseCase`，`:feature:reports` 已去掉对 `shared:data:core` 的直接依赖。
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
- 基础币种和设置表访问已抽成 `CurrencyStore` 以及设置相关窄端口；domain 的币种/设置 use case 不再直接注入 data core repository，data core 继续保留 Room-backed 实现和内部 mapper 依赖。
- 设置表默认值已集中到 data core 内部 `LocalSettingsDefaults`；`SettingsRepository` 不再引用 `CurrencyRepository` 的默认币种常量，两个 repository 也不再重复构造默认 `SettingsEntity`。
- 汇率读写和远程同步入口已抽成 `ExchangeRateStore` 端口；汇率同步、设置页汇率列表和重置钱包流程不再直接依赖 data core 的 `ExchangeRatesRepository`。
- 汇率单条查询已收敛到 `ExchangeRateStore.findByBaseCurrencyAndCurrency()`；汇率换算 use case 不再直接注入 `ExchangeRatesDao`，legacy 汇率 mapper 也不再依赖 `ExchangeRateEntity`。
- 标签读写、标签关联和标签搜索已抽成 `TagStore` 端口；编辑交易、历史列表、按标签筛选和重置钱包流程不再直接依赖 data core 的 `TagRepository`。
- 标签创建已停止注入 data core 的 `TagMapper`；`CreateTagUseCase` 直接构造 data model `Tag` 并通过 `TagStore` 保存。
- 账户与分类读写已抽成 `AccountStore/CategoryStore` 端口；对应 domain use case、交易历史构建、借贷同步和重置钱包流程不再直接注入 data core repository，`TransactionMapper` 也改为依赖账户端口。
- 账户、分类和标签 store 共享的内部读取缓存已从早期 `RepositoryMemo/RepositoryCache` 收敛为 `StoreCache`；它只服务 data-core 内部 DAO 读取缓存和数据变化事件发布。
- 交易读写、到期交易、计划付款关联交易和借贷关联交易已抽成 `TransactionStore` 端口；domain 中的账户、分类、首页、计划付款、借贷、重置和交易 use case 不再直接注入 data core 的 `TransactionRepository`。
- 交易剩余读路径已继续收敛到 `TransactionStore`；`hasAny()`、智能标题建议查询、按账户/分类标题计数、旧账户交易列表、计划付款金额统计和借贷同步不再直接注入 `TransactionDao`。
- domain 里的 legacy 模型转换文件已从泛化 `AccountExt.kt`、`TransactionExt.kt` 改名为 `LegacyAccountMapper.kt`、`LegacyTransactionMapper.kt`；转换函数签名不变，只让文件职责更明确。
- 交易 legacy/modern 转换已改为基于 data model 和 `AccountStore` 直接完成；`shared:domain` 主源码不再依赖 data core 的 `TransactionMapper` 或 `TransactionEntity`。
- `shared:domain` 的 Gradle 主依赖和测试依赖都已移除 `shared:data:core`；domain 单元测试改用数据端口 mock/fake，不再依赖 repository、Room 或 data core mapper。
- 数据变化事件已抽成 `DataChangePublisher/DataWriteEvent` 端口；domain 中的账户变更观察和全量数据变更通知不再直接依赖 data core 的 `DataObserver` 实现。
- data-core 中的事件发布实现已从 `DataObserver` 改名为 `DataWriteEventBus`；它负责发出写入事件，不再用“观察者”命名反向暗示数据流。
- `KSerializerLocalDateTime` 的泛化 `TODO` 已改成明确兼容说明：它继续服务旧 Room 实体和备份 JSON 中以 UTC epoch millis 编码的 `LocalDateTime`，不作为未完成的新模型迁移入口。
- `BalanceBuilder` 和 `StatSummaryBuilder` 已从 `com.ivy.domain.usecase` 根包归位到 `com.ivy.domain.model`；对应测试也已移动到 domain model 测试目录。它们是统计聚合辅助对象，不再和可注入业务用例混在同一层级。
- `ResetWalletDataUseCase` 接口已从 `com.ivy.domain.usecase` 根包移入 `com.ivy.domain.usecase.reset`，和 `ClearWalletDataUseCase`、`ClearLocalPreferencesUseCase`、`NotifyAllDataChangedUseCase` 保持同一 reset 边界。
- CSV 导出测试中的反读辅助类已从容易误解为生产用例的 `ReadCsvUseCase` 改名为 `CsvTestReader`，继续只存在于 test 源集。
- `IconAsset` 的测试文件和测试类已从旧名 `IconIdTest` 改为 `IconAssetTest`，和当前模型名保持一致。
- `TransactionTest` 已从 primitive 测试包移动到 `com.ivy.data.model` 测试包；它测试的是正式交易模型扩展，不再混入基础值对象测试目录。
- `IconAsset.exactName` 已从旧标签 `IconId` 改为 `IconAsset`，只影响校验错误消息前缀，模型校验规则不变。
- 通用身份接口 `UniqueId/Identifiable` 已从旧 `com.ivy.data.model.sync` 包迁到 `com.ivy.data.model.identity`；它们只表达本地模型身份约束，不再使用云同步语义包名。
- 主源码中剩余的泛化 `TODO/FIXME/Not implemented` 标记已清空：CSV 导入结果页、交易 tags、金额短格式和 legacy 交易默认值都改成明确的边界说明，不再以待办形式制造认知负担。
- 报表页计算中的 `temp*` 局部变量已改为 `selectedAccounts/historyTransactions/displayIncome/displayExpenses/displayBalance` 等真实含义；交易页中“临时账户转账分类”的注释也改为 synthetic category 说明。
- 预算读写已抽成 `BudgetStore` 端口；预算创建、更新、删除、排序、列表读取和重置钱包流程不再直接注入 Room 的 `BudgetDao/WriteBudgetDao`，旧 `BudgetExt` 实体 mapper 已删除。
- 计划付款规则读写已抽成 `PlannedPaymentRuleStore` 端口；首页统计、账户删除、计划付款保存/删除/读取、付或跳过计划付款，以及重置钱包流程不再直接注入 `PlannedPaymentRuleDao/WritePlannedPaymentRuleDao`，旧 `PlannedPaymentRuleExt` 实体 mapper 已删除。
- 借贷和借贷记录读写已抽成 `LoanStore/LoanRecordStore` 端口；借贷 CRUD、借贷记录 CRUD、借贷交易同步和重置钱包流程不再直接注入 `LoanDao/LoanRecordDao/WriteLoanDao/WriteLoanRecordDao`，旧 `LoanExt/LoanRecordExt` 实体 mapper 已删除。
- 设置表清空已收敛到 `SettingsResetStore.deleteAll()`；重置钱包流程不再直接注入 `WriteSettingsDao`。
- 主题 fallback、首次初始化默认主题和主题切换规则已从 data core 移回 domain：`ThemeStore` 只负责主题读写，`SettingsInitializationStore` 只负责首次初始化，`GetThemeUseCase/EnsureSettingsInitializedUseCase/SwitchThemeUseCase` 负责系统暗色映射和 LIGHT/DARK/AMOLED/AUTO 循环顺序，并补充了单元测试锁定这些规则。
- 分类排序偏好已收敛到 `GetCategorySortOrderPreferenceUseCase/SetCategorySortOrderPreferenceUseCase`；分类页不再直接注入 `AppPreferences`，底层 key 和排序行为保持不变。
- 上次选择账户偏好已收敛到 `GetLastSelectedAccountIdUseCase/SetLastSelectedAccountIdUseCase`；编辑交易页和借贷页不再直接读写 `AppPreferences.lastSelectedAccountId`，底层字符串 key 和 UUID 解析行为保持不变。
- “转账计入收支”记账规则偏好已收敛到 `GetTransfersAsIncomeExpensePreferenceUseCase/SetTransfersAsIncomeExpensePreferenceUseCase`；账户页、交易页和饼图页只读 domain 用例，设置页通过用例保存该开关，底层 key 不变。
- 隐藏余额和隐藏收入偏好已收敛到独立 domain 用例；首页只读 `GetHideCurrentBalancePreferenceUseCase/GetHideIncomePreferenceUseCase`，设置页通过对应 set 用例保存，底层 key 和短暂显示逻辑保持不变。
- 交易提醒通知开关已收敛到 `GetShowNotificationsPreferenceUseCase/SetShowNotificationsPreferenceUseCase`；设置页、提醒调度逻辑和提醒 Worker 不再直接访问 `AppPreferences.showNotifications`，通知调度与二次检查行为保持不变。
- 应用锁开关已收敛到 `GetAppLockEnabledPreferenceUseCase/SetAppLockEnabledPreferenceUseCase`；设置页和 `AppLockController` 不再直接访问 `AppPreferences.appLockEnabled`，运行时锁定状态仍由 app 层 controller 管理。
- 首次初始化完成和月起始日读取已收敛到 settings domain 用例；`RootViewModel` 和 `InitialDataSetup` 不再直接访问对应的 `AppPreferences` 字段，底层 key 与启动行为保持不变。
- 只写不读的 `data_backup_completed` 旧偏好已删除；备份导出仍直接生成 zip 并触发分享，不再写入没有消费方的完成标记。
- 首页客户旅程卡片关闭状态已收敛到 `IsCustomerJourneyCardDismissedUseCase/DismissCustomerJourneyCardUseCase`；`feature:home` 不再直接拼接或读写客户旅程偏好 key。
- 重置钱包流程中的本地 SharedPreferences 清空已收敛到 `ClearLocalPreferencesUseCase`；app 层重置实现继续负责编排，但不再直接注入旧偏好实现。
- 业务偏好 key 已从 `shared:base` 迁到 `shared:data:api`；base 不再承载应用锁、通知、隐藏余额等业务 key。
- 旧 `AppPreferences` 具体类已拆成多个窄 data-api 端口和 `SharedPrefsPreferenceStore` 实现；domain 用例只依赖各自需要的偏好能力，SharedPrefs 读写细节下沉到 data-core。
- 备份恢复中的偏好读写已改走备份专用偏好端口；备份 JSON 仍保留原 sharedPrefs key 字符串以兼容旧备份文件，但 `DefaultBackupStore` 不再直接读写通用 `PreferenceStore`。
- 旧 `PreferenceStore/SharedPrefs` 基础层抽象已删除；`SharedPrefsPreferenceStore` 在 data-core 内部直接持有 Android SharedPreferences，base 不再暴露偏好存储绑定。
- 文件读写和备份恢复端口已用 `ExternalFile` 包装外部文件引用；domain 和 data-api 不再公开 Android `Uri`，UI/platform 仍负责文件选择与分享，data-core 实现边界再转换回 Android `Uri`。`ExternalFile` 已从 data-api 下沉到 `shared:data:model`，直接引用文件句柄的 `feature:import-data`、`feature:reports` 和 `feature:settings` 不再依赖数据端口模块。
- `shared:domain` 对 `shared:data:api` 的 Gradle 依赖已从 `api` 收窄为 `implementation`；feature/app 不再通过 domain 的传递依赖获得数据端口类型。
- App 启动接口 `AppStarter` 已从 domain 下沉到 app 模块；它返回 Android `Intent`，实际只服务通知点击和 app 内启动流程，不再作为共享业务端口暴露。偏好开关目录也已移除单实现接口和 Hilt 绑定，domain 不再为这块保留 Hilt module。
- 币种模型和本地币种默认值读取已从 Android ICU `Currency` 切到 JDK `java.util.Currency`；`shared:data:model` 与 `shared:data:api` 主源码当前不再直接引用 Android API，并已改成更轻的 JVM/Kotlin 模块。
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
- `feature:exchange-rates` 已把保存/删除手动汇率时的 `DispatchersProvider` 注入改为标准 `Dispatchers.IO`，并移除对 `shared:base` 的 Gradle 依赖；汇率同步和手动汇率行为不变。
- `feature:settings` 的导出文件名时间戳已改用 JDK `Instant.now()` 生成 UTC 时间，移除只服务文件命名的 `TimeProvider` 注入和 `shared:base` Gradle 依赖。
- `feature:import-data` 的 CSV 日期导入已用局部 `LocalDateTime.atZone(ZoneId.systemDefault()).toInstant()` 表达原有本地时间转 UTC 规则，移除只服务该转换的 `TimeConverter` 注入和 `shared:base` Gradle 依赖。
- CSV 导入执行器已从历史名 `CSVImporterV2` 改为 `CsvTransactionImporter`，并移除 `CSVRowNew` 迁移期别名；当前只保留这一套 CSV 交易导入实现。
- `feature:planned-payments` 的计划付款编辑和列表展示也已用局部系统时区转换替代 `TimeConverter/LocalTimeConverter`，移除 `shared:base` Gradle 依赖；计划付款起始时间的本地展示和 UTC 存储规则保持不变。
- 剩余 `ioThread/scopedIOThread/computationThread` 调用已全部改为标准 `withContext(Dispatchers.IO/Default)`；`shared:base` 中的旧协程 dispatcher helper 文件已删除。
- `shared:data:core` 的仓库、Store 实现和备份导入导出已改为标准 `Dispatchers.IO`，移除 `DispatchersProvider` 构造参数以及对 `shared:base/base-testing` 的依赖；DAO 访问、缓存写入事件、备份 ZIP/JSON 格式和导入进度逻辑保持不变。
- `shared:domain` 中剩余只服务 use case 外层切线程的 `DispatchersProvider` 注入已移除，账户/分类创建编辑、旧账户/交易读取、计划付款汇总、账户统计和 CSV 导出改用标准 `Dispatchers.IO/Default`；业务日期语义已改用 domain 内部 `java.time` helper。
- 最后一个 UI 调用方 `Toaster` 已改用标准 `Dispatchers.Main`；`DispatchersProvider/IvyDispatchersProvider/TestDispatchersProvider` 和 app 绑定随之删除，基础层不再保留线程调度端口。
- `shared:domain` 的账户统计、分类汇总、计划付款、贷款同步、CSV 导出和旧交易历史分组已改用 domain 内部 `java.time` helper；domain 不再注入 `TimeProvider/TimeConverter`，也不再依赖 `shared:base/base-testing`。
- 无源码消费方的 `:shared:base-testing` 已删除，`shared:ui:core` 移除过时测试依赖；基础测试 helper 不再保留独立模块。
- `PeriodState` 已承接 legacy 周期的当前月、范围解析和月份前后移动逻辑；账户、分类、预算、余额、首页、交易和饼图页不再直接注入 `TimeProvider/TimeConverter`，并移除对 `shared:base` 的 Gradle 依赖。
- `feature:reports` 的筛选周期范围和周期显示已改走 `PeriodState`/legacy 周期显示 helper，CSV 导出文件名使用 JDK `Instant.now()` 生成 UTC 时间戳；报表模块不再直接依赖 `shared:base`。
- `feature:edit-transaction` 的交易日期、时间和 due date 转换已用局部系统时区 `java.time` helper 表达，创建/复制交易默认时间改用 `Instant.now()`；编辑交易模块不再直接依赖 `shared:base`。
- `feature:loans` 的贷款和贷款记录日期选择逻辑已收敛到模块内 `LoanTime` helper；贷款本体继续保留本地 `LocalDateTime` 语义，贷款记录继续保留 UTC `Instant` 语义，贷款模块不再直接依赖 `shared:base`。
- `com.ivy.base.text` 中的字符串判空、大小写和首字母转换 helper 已删除；调用方改为标准 `isNullOrBlank().not()`、`uppercase/lowercase(Locale.getDefault())` 或局部私有扩展，基础层不再承载通用字符串糖衣。
- `ResourceProvider` 已从 `shared:base` 迁到 `shared:ui:core`，测试替身也随之从 `base-testing` 移到 ui-core 测试源集；base 不再承载 Android 字符串资源端口。
- `shared:base` 中无消费方的 `BaseModule` 和 `@AppCoroutineScope` 已删除；应用级协程 scope 绑定不再作为未使用的全局 DI 暴露。
- 导出 CSV/备份文件名使用的 `yyyyMMdd-HHmm` 时间戳格式已从 `shared:base` 内联到设置页和报表页；基础层不再暴露文件命名专用的时间格式 helper。
- `shared:base` 中剩余的薄日期扩展已拆除：UTC epoch 秒、月边界和日结束时间等只在具体调用方保留为私有 helper。
- `BaseHiltBindings` 已迁入 app 的绑定模块，随后 `shared:base` 完成时间端口收敛并删除；最后的 UI 时间入口、设备时间实现、安全时间边界和对应测试迁入 `shared:ui:core`，app 与 `shared:ui:legacy` 不再声明 `projects.shared.base`。
- `TimeProvider/TimeConverter` 的 Hilt 绑定已随实现迁入 `shared:ui:core` 的 `IvyUiBindings`；app 的绑定模块不再负责 UI 时间实现装配。
- 版本目录中未被任何 Gradle 文件或源码使用的 `mockk-android` 与 `androidx-security` 依赖别名已删除。
- Ktor 依赖继续收缩：数据层当前使用 `ContentNegotiation` 与 `ktor-serialization-kotlinx-json`，版本目录已删除旧 `ktor-client-serialization` 依赖别名和 bundle 条目。
- Compose bundle 继续收缩：源码中已无 `@Preview`、Coil Compose 或 WindowSizeClass 使用，版本目录已删除 `compose-tooling`、`compose-coil` 和 `compose-material3-windowsize`。
- `activity-compose` 已从公共 Compose bundle 下放到实际使用方；当前只有 app 的 `setContent`、设置页二级菜单和 legacy 弹窗返回键显式依赖它。
- `lifecycle-viewmodel-compose` 已从公共 Compose bundle 下放到实际使用方；只有需要 `viewModel()` 或 `LocalViewModelStoreOwner` 的 feature/navigation 模块显式依赖它。
- `shared:ui:legacy` 显式声明 `androidx.core:core-ktx`，不再靠 Compose/ViewModel 传递依赖获得 `doOnLayout`。
- `hilt-work` 已从公共 Hilt bundle 下放到 app；当前只有交易提醒 Worker 和 app 的 WorkManager 配置需要它。
- `kotlinx-collections-immutable` 已从公共 Kotlin bundle 移除；它仍由 `shared:data:model` 以 `api` 暴露，因为导入结果和多处 UI 状态仍使用 immutable collection 类型。
- 旧的过宽 `ivy.feature` 用法已删除；当前 `ivy.feature` 只组合 feature 页面模块共同需要的 Compose、Hilt 和 shared 依赖，非 feature 模块继续显式声明自己的构建能力。
- `DateTimePicker` 接口已从 `com.ivy.ui.time.impl` 归位到 `com.ivy.ui.time`；`impl` 包只保留 Android/Material 日期时间选择器实现。
- 生产源码中最后残留的 `Preview` 命名 spacer/helper 已删除；当前没有 Compose 预览专用函数继续留在主源码。
- app 和 `shared:data:core` 已显式声明 `androidx.core:core-ktx`，不再靠 Activity/AppCompat/DataStore 等传递依赖获得 AndroidX Core API。
- `shared:ui:core` 已移除 Hilt 插件和内部 Hilt Module，并显式声明基础 `lifecycle-viewmodel`；主题状态、时间服务、日期时间选择器和 Toaster 的应用级绑定集中到 app 的 DI 模块。
- `shared:ui:navigation` 和 `shared:ui:legacy` 已移除最后的 `javax.inject` 依赖；`Navigation/PeriodState` 作为普通状态类由 app 统一提供单例，`MainTabState` 作为主页面状态由 `MainViewModel` 持有。
- 设置表的 Room 实现已从泛化的 `SettingsRepository` 改名为 `RoomSettingsStore`；先把设置存储边界表达清楚，再逐步拆出更窄的数据端口。
- data-core 的 Store 实现已整体归位到 `com.ivy.data.store`：账户、分类、币种、标签、交易、预算、借贷、计划付款和设置的 Room 实现统一命名为 `Room*Store`，汇率实现命名为 `DefaultExchangeRateStore`。
- data-core 的实体/模型转换器已从历史 `repository.mapper` 包迁到 `com.ivy.data.mapper`；主源码中不再使用 `com.ivy.data.repository` 包。
- 设置表的内部访问已集中到 data-core 的 `SettingsTable`：`RoomSettingsStore` 和 `RoomCurrencyStore` 不再分别持有 `SettingsDao/WriteSettingsDao`，后续拆分 `SettingsEntity` 时只需围绕这一处旧表边界推进。
- `SettingsTable`、`RoomSettingsStore` 和 `RoomCurrencyStore` 已补充单元测试，锁定旧 settings 表的初始化、默认值、upsert、主题、基础币种和缓冲金额行为；测试 fake 的 settings 保存也改为模拟 Room `@Upsert`。
- data-core 内部的备份导入、账户映射和交易仓库已改为依赖 `AccountStore/CurrencyStore/TagStore` 端口，不再直接依赖对应具体 Repository 实现。
- 账户旧读取路径已收敛到 `AccountStore`；旧 legacy 账户模型现在由 data model 账户映射而来，`shared:domain` 主源码不再直接注入 `AccountDao` 或依赖 `AccountEntity` mapper。
- 旧交易卡片已移除重复账户查找 TODO：渲染前先解析来源/目标账户，再复用同一结果处理点击和币种展示，行为不变但 legacy UI 内部职责更清楚。
- 新版交易值读取 helper 和账户统计值函数已从 `com.ivy.domain.transaction.legacy` 迁到正式 `com.ivy.domain.transaction` 包；legacy 包继续只承载仍依赖旧交易/账户模型的兼容逻辑。
- 新版交易筛选和到期判断 helper 已从 legacy 文件拆到 `com.ivy.domain.transaction.TransactionFilters`；旧包内只保留旧交易模型筛选和仍依赖旧账户模型的币种兼容 helper。
- 新版交易折叠/求和 helper 已从 `legacy/FoldTransactions.kt` 拆到正式 `com.ivy.domain.transaction.TransactionFolds`；legacy 文件只保留旧交易模型折叠对象。
- 新版钱包收入/支出汇总函数已从 `legacy/WalletValueFunctions.kt` 拆到正式 `com.ivy.domain.transaction.WalletValueFunctions`；legacy 文件只保留旧交易模型汇总对象。
- 新版交易币种 helper 已从旧 `trnCurrency` 改为正式 `transactionCurrency`，并从 legacy 文件迁到 `com.ivy.domain.transaction.TransactionCurrency`；legacy 包中的 `LegacyTransactionFunctions` 现在只处理旧交易模型。
- 新版交易到旧历史列表 UI item 的桥接函数已从 `legacy/TrnDateDividers.kt` 拆到正式 `com.ivy.domain.transaction.TransactionHistoryItems`；legacy 日期分组入口只保留旧交易模型适配逻辑，列表项模型已归位到正式 data model。
- 旧交易兼容目录中的文件名已和对象名对齐：`LegacyTransactionFunctions`、`LegacyFoldTransactions`、`LegacyWalletValueFunctions`、`LegacyTransactionDateDividers` 均保留在 `com.ivy.domain.transaction.legacy`，目录中不再混用新版语义文件名；交易求和 helper 也从 `sumTrns` 改为 `sumTransactions`。
- 旧钱包收入/支出统计对象已从 `WalletValueFunctionsLegacy` 改为 `LegacyWalletValueFunctions`，和文件名及其他 legacy helper 命名保持一致。
- 交易汇率换算相关旧缩写继续收敛：`ExchangeTrns.kt` 已改为 `ExchangeTransactions.kt`，`ExchangeTrnArgument`/`LegacyExchangeTrns`/`trnCurrency` 改为完整的 `ExchangeTransactionArgument`/`LegacyExchangeTransactions`/`transactionCurrency` 命名；行为不变。
- 旧账户模型 helper 已从泛化 `com.ivy.domain.account.AccountFunctions` 迁到 `com.ivy.domain.account.legacy.LegacyAccountFunctions`，并改名为 `includedLegacyAccounts`/`legacyAccountCurrency`；调用方现在能明确看出这些函数仍依赖 legacy 账户模型。
- 旧交易 due date 筛选已从 `com.ivy.domain.time.TransactionDateFilters` 拆到 `com.ivy.domain.transaction.legacy.LegacyTransactionDateFilters`；`domain.time` 只保留新版交易日期筛选和通用时间转换。
- 旧交易 due/overdue 筛选函数已从 `filterUpcomingLegacy()/filterOverdueLegacy()` 改为 `filterUpcomingLegacyTransactions()/filterOverdueLegacyTransactions()`，名称直接表达处理对象是旧交易列表。
- 核心汇率换算函数已从 legacy `ExchangeRate` 对象依赖改成只接收 `BigDecimal` 汇率值，`ExchangeRateExt.toLegacyDomain()` 随之删除；汇率数据模型到算法的边界更窄。
- 旧交易汇率换算重载和 `LegacyExchangeTransactions` 已从正式 `domain.exchange.ExchangeTransactions` 迁到 `domain.transaction.legacy.LegacyExchangeTransactions`；exchange 包继续保留通用换算与新版交易入口。
- `shared:domain` 内部旧账户/旧交易兼容别名已显式化：domain helper、use case、汇率换算和旧历史列表桥接代码不再从 `com.ivy.data.model.legacy.Account/Transaction` 兼容别名导入，而是直接使用 `LegacyAccount/LegacyTransaction`；这一步只改变类型命名，不改业务计算和数据结构。
- `shared:ui:legacy`、`shared:ui:navigation` 和借贷创建参数里的旧账户/旧交易类型也已显式成 `LegacyAccount/LegacyTransaction`；旧交易卡片、旧交易列表、旧到期分组和导航 route 参数继续保持原行为，但调用方不再借助 `Account/Transaction` 兼容别名隐藏 legacy 边界。
- feature 页面层的旧账户/旧交易类型已完成显式化：首页、交易列表、编辑交易、分类、预算、借贷、计划付款、饼图、报表、搜索和 CSV 导入页面不再引用 `com.ivy.data.model.legacy.Account/Transaction` 兼容别名，全部直接使用 `LegacyAccount/LegacyTransaction`。
- `LegacyAccount.kt` 和 `LegacyTransaction.kt` 中暂留的 `typealias Account/Transaction` 已删除；旧模型现在只能通过真实类名访问，避免新代码继续无意写回旧兼容命名。
- `LegacyAccount.toDomainAccount()` 已从 data model 类成员迁到 `com.ivy.domain.mapper.legacy` 扩展函数；旧账户模型本体现在只保留旧字段，正式账户转换由 domain mapper 负责。
- 剩余旧模型全限定类型写法已收敛：报表页面状态/事件和 legacy 交易 helper 不再散落 `com.ivy.data.model.legacy.LegacyTransaction` FQN，而是统一通过 import 表达旧模型边界。
- 正式账户到旧账户的 mapper 已从泛化 `toLegacyDomain()` 改名为 `toLegacyAccount()`，调用方现在能直接看出这是账户模型兼容转换，而不是泛化 legacy domain 转换。
- 正式交易到旧交易的 mapper 已从泛化 `toLegacy()`/`toDomain()` 改名为 `toLegacyTransaction()`/`toTransaction()`；调用方现在能明确区分正式交易模型和旧交易兼容模型之间的转换，避免和 data-core 的实体 `toDomain()` 命名混在一起。
- 交易列表、首页和报表使用的新旧交易批量转换 use case 已从 `MapTransactionsToLegacy*` 改名为 `MapTransactionsToLegacyTransactions*`，避免把 legacy 误读成整套旧 domain 边界。
- 功能开关偏好门面已从 `PreferenceToggleRepository` 改名为 `PreferenceToggleService`：它只负责把 domain 层 `BoolPreference` 映射到底层 `PreferenceToggleStore`，不再用 repository 命名暗示数据仓库职责。
- 旧 `Logic` 注入变量名已继续收敛：`LegacyExchangeRatesUseCase` 的调用方统一使用 `exchangeRatesUseCase`，首页客户旅程卡片也改用 `customerJourneyCardsProvider` 命名，避免把 provider/use case 误读成旧 logic 层。
- 旧到期交易 UI 模型 `LegacyDueSection` 的 `trns` 字段已改为 `transactions`，legacy 交易列表内部私有 `trnItems/trnCount` 也改为完整命名；首页、报表和交易页调用方同步更新，展示行为不变。
- CSV 导入页面的交易类型元数据已从 `TrnTypeMetadata` 展开为 `TransactionTypeMetadata`，对应事件 `TypeMetaChange/DataMetaChange` 也改为 `TypeMetadataChange/DateMetadataChange`；导入解析规则和 CSV 字段映射行为不变。
- app 启动、首页到期交易加载、编辑交易删除弹窗、交易类型 lambda、客户旅程计数、账户统计和 CSV 导出中的局部 `trn/trans` 缩写已展开为 `transaction*` 命名；只改局部符号，不改业务计算。
- 报表筛选模型中的 `trnTypes/trnType/trnAmountBaseCurrency` 已展开为 `transactionTypes/transactionType/transactionAmountBaseCurrency`；筛选规则和 UI 行为不变。
- 生产代码中剩余的交易缩写已继续收尾：`StatSummary.trnCount` 改为 `transactionCount`，`RoomTransactionStore.retrieveTrns()` 改为 `retrieveTransactions()`，旧迁移类 `Migration105to106_TrnRecurringRules` 改为完整命名；对应测试局部变量同步展开。
- 借贷交易同步中的局部 `transType/transCategoryId` 已展开为 `transactionType/transactionCategoryId`；报表页计划付款事件也从 `...Legacy` 后缀改为明确的 `...LegacyTransaction(s)` 命名。
- domain use case 中注入的 `TransactionStore` 不再命名为 `transactionRepository`，统一改为 `transactionStore`；这是命名层面的边界收敛，读写行为不变。
- `RoomTransactionStoreTest` 的被测对象也从 `repository/newRepository` 改为 `store/newStore`，避免测试代码继续传播旧仓库命名。
- data-core 里的备份实现已从 `BackupDataUseCase` 改名为 `DefaultBackupStore`，并继续通过 `BackupStore` 暴露给 domain；ZIP/JSON 备份格式和导入导出行为不变。
- `TransactionStore` 删除计划付款未来交易的方法已从过去式 `deletedByRecurringRuleIdAndNoDateTime` 改为命令式 `deleteByRecurringRuleIdAndNoDateTime`；DAO SQL 和调用语义不变。
- 设置初始化链路中的基础币种参数已从泛化 `currencyCode` 改为 `baseCurrencyCode`；这一步不改 `settings.currency` 数据库列，只让初始化边界语义更明确。
- 设置表端口已拆成 `SettingsInitializationStore`、`SettingsResetStore`、`ThemeStore` 与 `BufferAmountStore`；初始化、重置、主题和缓冲金额 use case 只依赖各自需要的能力，底层仍由 `RoomSettingsStore` 读写同一张 `settings` 表，数据库 schema 和备份格式不变。
- `RoomCurrencyStore` 已删除进程内基础币种缓存，基础币种读取始终以 `SettingsTable` 当前内容为准；这避免重置、恢复或其他 settings 写入后同一 store 实例继续返回旧币种。
- data-core 的单绑定 Hilt 模块已收敛到 `DataBindingsModule`：store、备份、文本文件和远程汇率数据源绑定集中在一处，减少只为一个接口存在的装配文件。
- 偏好开关 DataStore 不再通过独立 Hilt module 暴露裸 `DataStore<Preferences>`；`DataStorePreferenceToggleStore` 在 data-core 内部直接使用应用 `Context` 取得同一个 `ivy_wallet_datastore_v1` 文件。
- 首次启动完成状态已从旧泛化 app 偏好端口拆到 `InitialSetupStore`；启动流程只依赖初始化状态端口，底层仍读写同一个 SharedPreferences key。
- 最后选择账户 ID 已从旧泛化 app 偏好端口拆到 `LastSelectedAccountStore`；账户选择用例只依赖自己的偏好端口，原 SharedPreferences key 不变。
- 分类排序偏好和首页客户旅程卡片 dismissed 状态已分别拆到 `CategorySortOrderStore` 与 `CustomerJourneyCardStore`；分类/首页用例不再依赖完整 app 偏好端口。
- 本地偏好清空能力已从旧泛化 app 偏好端口拆到 `LocalPreferenceResetStore`；重置用例不再依赖设置开关读取能力。
- 设置开关端口已继续拆成 `AppLockPreferenceStore`、`NotificationPreferenceStore`、`BalancePrivacyPreferenceStore`、`StartDayOfMonthStore`、`TransferBehaviorPreferenceStore` 和 `BackupSettingsPreferenceStore`；`SettingsPreferenceStore` 已删除，备份和设置用例只依赖自己需要的偏好能力。

建议顺序：

1. 继续评估 `isDeleted` 字段：
   - `isSynced` 已确认是云同步残留并删除。
   - `isDeleted` 仍服务本地查询过滤、测试 fake、历史迁移和计划付款按账户软删除；短期应视为本地软删除语义，不再和云同步残留一起批量删除。
2. 继续评估 `SettingsEntity` 是否需要真正拆表或迁移到 DataStore；`theme/currency/bufferAmount` 的代码端口已拆清，后续若改 schema 必须单独迁移并验证备份兼容性。
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
- `BiometricAuthenticator` 承接系统生物识别 Prompt 构造，并在 platform 层内部创建 AndroidX `BiometricPrompt.AuthenticationCallback`。
- `SecureWindowController` 承接应用锁失焦时的 `FLAG_SECURE` 窗口保护。
- `RootActivity` 已去掉仅转调生物识别认证的私有中转方法，锁屏 UI 回调直接调用 `BiometricAuthenticator`，Activity 不再额外暴露 `BiometricPrompt` 类型。
- `AppLockController` 承接应用锁启用状态、锁定状态、生物识别结果处理和用户非活跃计时，`RootViewModel` 只暴露普通成功/失败事件方法，不再把 AndroidX 生物识别 callback 类型带出 platform 层。
- `RootContent` 承接根部 Compose 内容、锁屏/主导航切换、旧 UI root 注入和 Material3 theme 包装，`RootActivity` 主要保留生命周期、平台注册和平台能力委托。
- `RootScreen` 已被 `FileSharer`、`BuildInfoProvider` 拆分替代，首页客户旅程卡片也不再为了未使用的参数依赖 Activity 平台接口。
- `FileSharer` 和 `BuildInfoProvider` 已通过 `LocalFileSharer/LocalBuildInfoProvider` 由 app 根部显式提供；设置页和报表页不再通过 `LocalContext.current as ...` 强转 Activity 获取平台服务。
- `fileSharer()` 和 `buildInfoProvider()` 薄 helper 已从 `shared:ui:legacy` 移到 `shared:ui:core`；设置页和报表页不再为了平台服务 helper 依赖 legacy 桥接包，旧 UI 模块只保留真正的旧组件和旧主题兼容层。
- app 平台实现的公开面已收窄：Activity result launcher helper、文件选择/日期选择注册入口、文件分享器、构建信息 provider、生物识别封装、Secure Window 控制器和设备锁屏检查都改为 app 模块内可见；Hilt 绑定类和 `shared:ui:core` 暴露的窄接口保持不变。
- app 根部公开面继续收窄：`RootContent` 只作为 `RootActivity` 的内部 Compose 内容函数，`RootIntentExtras` 只作为 app 内部启动参数常量；Hilt ViewModel 和绑定实现暂时保持 public 以避免影响生成代码。
- 已删除只有单实现且只被提醒 worker 使用的 `AppStarter` 接口；随后继续删除只包装 `RootActivity` intent 的 `RootIntentFactory`，提醒通知和快捷磁贴现在共用 app 内部 `Context.createRootIntent()` helper。
- 旧 UI 组件的公开面继续收窄：`IvyCheckbox` 改为文字版 checkbox 的私有实现，`PrimaryAttributeColumn`、`IconNameRow`、`DateTimeRow`、`CurrencyPicker`、`IvyBorderButton`、`IvyColorPicker`、`IvyTitleTextField` 和 `IvyDescriptionTextField` 都只作为 `shared:ui:legacy` 内部构件保留；feature 层仍可调用实际页面正在使用的旧组件入口。
- 旧颜色选择器内部色板不再用 `FREE/PREMIUM` 命名；基础色和浅/深变体全部作为普通可选颜色保留，展示顺序不变。
- 旧主题的 `theme.system` 进一步收敛为内部实现层：feature 层不再直接导入其中的具体颜色常量，内部色板、系统 Gradient、CompositionLocal 和颜色算法 helper 均改为 `shared:ui:legacy` 内可见；外部继续通过 `LegacyTheme`、`style()` 和外层 `legacy.ui.theme` 色板使用旧样式。
- 交易提醒通知封装继续收窄：`IvyNotification`、`IvyNotificationChannel` 和 `NotificationService` 的通知构建/展示方法只作为 app 内部实现使用，并删除未被调用的通知 dismiss helper；提醒 worker 的实际通知行为不变。
- 锁屏生物识别链路已删除空错误回调转发和未使用的成功回调 lambda 参数；现在根 Activity 只传递真正有行为的成功/失败事件，错误事件保持原有的无额外处理行为。
- app 通知服务里的异常输出已从裸 `printStackTrace()` 逐步清掉；当前通知展示失败继续按原策略吞异常，项目已不再依赖 Timber。
- `Features` 和功能开关 DataStore 已通过 `LocalFeatures/LocalFeatureDataStore` 由 app 根部显式提供；旧金额键盘不再用 Hilt `EntryPointAccessors` 从 application 反查依赖。
- 锁屏页不再通过 `LocalContext.current` 自行检查系统锁屏状态；`RootActivity` 从 app 平台层提供 `hasLockScreen` 检查函数，UI 只负责触发认证或继续进入应用。
- 根启动 intent 的交易类型解析已改用 `IntentCompat.getSerializableExtra()`，不再直接调用新版 Android 中弃用的 `Intent.getSerializableExtra(String)`。
- `RootViewModel` 的首次初始化判断已去掉同名私有包装函数，注入字段改为 `isInitialSetupCompletedUseCase`，启动编排直接调用 use case，避免函数和依赖同名造成误读。
- `RootViewModel.start()` 不再用外层 `Dispatchers.IO` 包住 UI 状态更新和导航；主题读取、默认数据初始化等耗时工作由各自用例/初始化器处理，`ThemeState/PeriodState/Navigation` 更新留在主协程。
- 根启动 Intent extra 已从 `RootViewModel` companion 移到 `RootIntentExtras`；`IvyAppStarter` 不再为了启动协议依赖 ViewModel 常量。
- 根启动导航职责已从 `RootViewModel` 移到 `RootContent`：ViewModel 不再接收 Android `Intent` 或注入 `Navigation`，启动目标通过 `RootUiEvent` 发给 UI 根部执行；启动 Intent 的交易类型解析留在 app 平台边界。
- 导航返回处理已收窄为 `Navigation.handleRootBack()`、`registerScreenBackHandler()` 和 `unregisterScreenBackHandler()`；旧弹窗改用 Compose `BackHandler`，页面级返回回调随页面生命周期注册/注销，旧 modal 不再直接访问导航内部的返回栈和 handler map。
- `LocalTimeConverter/LocalTimeProvider/LocalTimeFormatter` 现在作为根部显式提供的 UI 时间平台入口保留，不再用废弃注解把当前页面的正常调用标成警告。
- `RootContent` 接收的旧 Material 日期选择器已从 app 具体实现 `ActivityDatePicker` 收窄为 UI 层 `DatePicker` 接口；Activity 仍负责注册 FragmentManager 相关实现。
- 交易提醒调度已删除无调用方的 `testNow()` 调试入口和旧 work name 常量，只保留当前实际使用的每日提醒任务。
- 交易提醒调度不再通过 Hilt 注入 `TimeProvider`，而是在 app 调度器内部直接读取本地当前时间；app 的提醒流程不再为了一个本地时间读取依赖 `shared:base` 时间端口。
- app 层剩余的泛化 `*Logic` 命名已继续收敛：首次默认账户/分类预置从 `PreloadDataLogic` 改为 `DefaultWalletDataSeeder`，交易提醒调度从 `TransactionReminderLogic` 改为 `TransactionReminderScheduler`；行为不变，只让启动编排中的职责更直接。
- Android Toast 封装 `Toaster` 已从 `shared:base` 迁到 `shared:ui:core` 的 `com.ivy.ui.platform`，编辑交易和汇率页继续通过同一注入类型显示提示；基础层不再承载这段 UI 平台能力。
- UI 平台层新增安全的 `Context.findActivity()` helper；状态栏旧版兼容逻辑不再直接强转 Activity，旧主题设置状态栏时也复用同一 Activity 查找入口。
- 清空钱包的 app 层实现从泛化 `ResetWalletDataUseCaseImpl` 改名为 `AppResetWalletDataUseCase`；接口继续作为设置 feature 到 app 编排层的边界，行为不变。
- 根导航装配 `IvyNavGraph` 从顶层 `com.ivy` 包移动到 `app` 的 `com.ivy.wallet.navigation` 边界，并改为 app 模块内部函数；`RootContent` 仍以同一导航状态渲染各 feature 页面。
- app 内部 Hilt 绑定模块、平台适配器、启动默认数据编排、应用锁控制器和提醒调度器继续收为 app 模块内部实现；`RootViewModel` 的注入构造和启动事件流也只在 app 根部可见，Activity/Worker 等 Android 入口保持原有入口职责。
- `RootViewModel`、交易提醒 `TransactionReminderWorker` 和 `NotificationService` 也收为 app 模块内部类；外部仍只通过 Android 入口、WorkManager 调度和 feature 级窄接口触达这些能力。
- Android Manifest 删除无源码调用的旧权限和旧外部存储兼容标记；当前只保留通知、开机后恢复提醒和网络访问所需权限，文件导入/导出继续走系统文档选择器。
- 文件创建/打开的 Activity Result host 删除旧外部存储初始目录 hint，不再调用 `Environment.getExternalStoragePublicDirectory()`；系统文档选择器继续负责实际文件位置。
- Lint 配置删除已经不存在的 `ComposeViewModelInjection` issue 禁用项；快捷磁贴的 Android 14 以下兼容分支显式标注旧 API suppress，保留 Android 14+ 的 `PendingIntent` 启动路径。
- adaptive launcher icon 从多余的 `mipmap-anydpi-v26` 合并到 `mipmap-anydpi`；当前 minSdk 28 下不再需要 v26 资源限定目录。
- Android 12+ 的设备迁移规则和 Android 11 及以下的完整备份规则已补齐；两套配置都排除本地文件、数据库、偏好和外部目录，继续匹配当前 `allowBackup=false` 的不备份策略。

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

当前进展：

- 饼图统计页已继续收窄 legacy 交易状态：从导航传入的交易列表只作为重新计算图表的输入保留在 ViewModel 私有字段中，不再作为 Compose state 或页面状态暴露；分类关联交易仍使用更小的 `AssociatedTransaction` 展示/导航模型。
- 饼图 feature 的内部公开面已收窄：`BuildPieChartDataUseCase`、`PieChartData`、`CategoryAmount`、`AssociatedTransaction`、`SelectedCategory`、状态、事件、ViewModel 和底部栏/图表组件都改为模块内部可见；app 仍只通过页面入口参与导航装配。
- 账户、余额、汇率和搜索 feature 的公开面也已收窄：`AccountsTab`、`BalanceScreen`、`ExchangeRatesScreen` 和 `SearchScreen` 继续作为外部入口，其余状态、事件、ViewModel、展示模型和模块内部 helper 不再作为跨模块 API 暴露。
- 预算和分类 feature 的公开面继续收窄：`BudgetScreen` 和 `CategoriesScreen` 继续作为外部入口，其余状态、事件、ViewModel、弹窗数据、排序枚举、展示模型、预算类型 helper 和局部分类 UI helper 都改为模块内部可见。
- 计划付款 feature 的公开面已收窄：`PlannedPaymentsScreen` 和 `EditPlannedScreen` 继续作为外部入口，其余列表/编辑页状态、事件、UI 事件、ViewModel、列表卡片、底栏、重复规则组件和重复规则判断 helper 都改为模块内部可见。
- 借贷 feature 的公开面已收窄：`LoansScreen` 和 `LoanDetailsScreen` 继续作为外部入口，其余列表/详情页状态、事件、UI 事件、ViewModel、展示模型、tab 枚举、底栏尺寸常量、记录列表 helper 和 `humanReadableType()` 格式化扩展都改为模块内部可见。
- 主界面和首页 feature 的公开面已收窄：`MainScreen` 与 `HomeTab` 继续作为外部入口，其余 `MainTab/MainViewModel/BottomBar`、首页状态/事件/ViewModel、客户旅程模型、更多菜单、现金流信息和首页列表内部构件都改为模块内部可见；底栏尺寸常量改为文件私有实现细节。
- 设置 feature 的公开面已收窄：`SettingsScreen` 继续作为 app 导航入口，`SettingsState/SettingsEvent/SettingsUiEvent/SettingsViewModel` 都改为模块内部可见；设置页内部菜单和 section 组件本来已保持私有。
- 报表 feature 的公开面已收窄：`ReportScreen` 继续作为 app 导航入口，`ReportFilter/ReportScreenState/ReportScreenEvent/ReportViewModel` 和筛选浮层相关 Composable 都改为模块内部可见。
- 交易列表 feature 的公开面已收窄：`TransactionsScreen` 继续作为 app 导航入口，`TransactionsState/TransactionsEvent/TransactionsUiEvent/TransactionsViewModel` 都改为模块内部可见。
- 编辑交易 feature 的公开面已收窄：`EditTransactionScreen` 继续作为 app 导航入口，`EditTransactionViewState/EditTransactionViewEvent/EditTransactionUiEvent/EditTransactionViewModel`、自定义汇率状态和借贷提示展示模型都改为模块内部可见。
- 导入 feature 的公开面已收窄：`ImportCSVScreen` 和 `CSVScreen` 继续作为 app 导航入口，`ImportViewModel/ImportStep`、CSV 状态/事件/映射模型、CSV 解析函数、导入器和内部 flow Composable 都改为模块内部可见；CSV 页面内部列表 helper 改为私有，只保留结果页复用的 `Spacer8` 为模块内部 helper。
- 首页计划付款付/跳过事件已从传递完整 `LegacyTransaction` 收窄为传递交易 ID；旧交易对象仍只保留在列表展示状态和 ViewModel 内部执行边界。
- 交易列表页计划付款付/跳过事件也已收窄为传递交易 ID；跳过全部弹窗只保存待确认的交易 ID 列表，ViewModel 在执行前从当前 due 状态解析旧交易对象。
- 报表页计划付款付/跳过事件同样收窄为传递交易 ID，并删除未被 UI 触发的新模型计划交易事件分支及对应 use case 注入。
- 无调用方的新模型计划付款付/跳过 use case 已删除；后续如果正式 `Transaction` 路径重新接管 UI，再按真实调用点重建用例。
- 交易列表页事件不再反复携带 `TransactionsScreen` route；ViewModel 在 `start()` 时记录当前页面参数，翻月、删除、编辑账户和计划付款刷新都复用当前 route。
- 饼图页删除仅用于启动转发的 `OnStart` 事件；页面直接调用 ViewModel 启动方法，其余用户交互事件保持不变。
- legacy 汇率求和扩展继续收窄：交易求和扩展改为 domain 内部可见，未使用的计划付款求和扩展已删除。
- domain 内部链路继续收窄：账户 upcoming/overdue 基础查询和计划付款生成用例只服务同模块上层 use case，已改为 `internal`；对应上层公共 use case 只保留公共类型，注入构造函数收为模块内部细节。
- UI core 的 Material3 色板实现继续收窄：`IvyColors` 与 `ColorShades` 只服务本模块主题实现，已改为 `internal`。
- data-model 删除未使用的 `PositiveValue.round()` 扩展；金额值对象本身和现有格式化/计算入口保持不变。
- 批量 legacy 计划付款付/跳过用例删除未使用的 `copy/map` 计算，并把局部命名从 `paidTransactions` 收敛为 `dueTransactions`；当前批量 UI 路径仍只执行跳过全部，行为不变。
- data-core 的 Hilt 绑定模块、Room/Ktor/序列化 provider 模块和数据写入事件总线改为模块内部可见；domain 里的账户变更观察用例显式把事件流降维为 `Unit` 通知，语义不变。
- data-core 继续收窄实现边界：Room store、mapper、SharedPrefs/DataStore 适配器、文件系统适配器、远程汇率数据源、备份实现和 store cache 都改为模块内部可见；跨模块仍只暴露 `shared:data:api` 中的 store/backup/file 接口。
- DataStore 的 `Context.dataStore` 扩展属性也收为 data-core 内部实现细节，只服务本模块的偏好开关适配器。
- data-core 的 Room 数据库、DAO、entity、migration、serializer 和 type converter 也收为模块内部可见；外部模块不再能直接依赖数据库结构，只能通过 data-api 与 data-model 交互，本模块测试辅助同步收窄。
- domain 中当前只被本模块统计测试覆盖、尚未作为 feature 入口使用的账户统计用例、统计 summary 和 builder 收为模块内部可见；代码保留，避免把未接入 UI 的统计草稿继续暴露为跨模块 API。
- 借贷交易同步的 `LoanTransactionSyncCore` 收为 domain 内部实现；上层 `LoanTransactionSyncUseCase` 和 `LoanRecordTransactionSyncUseCase` 继续作为 feature 可注入入口，但构造函数不再暴露内部 core 类型。
- 首页到期/逾期交易信息的底层 `CalculateDueTransactionsInfoUseCase` 收为 domain 内部实现；`GetUpcomingTransactionsInfoUseCase` 和 `GetOverdueTransactionsInfoUseCase` 仍作为 feature 入口保留，构造函数不再暴露内部计算器。
- 账户余额调整的 `AdjustAccountBalanceUseCase` 收为 domain 内部协作实现；创建/更新账户用例继续作为 feature 可注入入口，但构造函数不再暴露内部调整器。
- 到期交易基础查询 `GetDueTransactionsUseCase` 也收为 domain 内部实现；首页仍只通过 upcoming/overdue 两个面向 feature 的入口读取统计结果。
- 旧交易列表组件的计划付款事件继续收窄：支付/收款、跳过和跳过全部回调现在只向页面层传交易 ID；交易卡片仍用 `LegacyTransaction` 渲染和处理点击编辑，避免把完整旧模型继续用于简单事件分发。
- 旧交易列表组件的交易点击事件也继续收窄：页面层只接收编辑页导航需要的交易 ID 和交易类型；完整 `LegacyTransaction` 继续限定在交易卡片渲染边界内。
- 旧交易列表组件的账户和分类点击事件同样收窄为只传 ID；页面层筛选导航不再接收完整 `LegacyAccount/Category` 对象。
- 交易列表编辑账户事件也已收窄为只传账户 ID 和新余额；ViewModel 从当前账户状态解析旧账户对象，完整 `LegacyAccount` 不再穿过事件层。
- 编辑交易页的账户选择事件已收窄为只传账户 ID；旧表单组件仍可用完整账户对象渲染和选择，但 Screen 到 ViewModel 的事件边界不再传递 `LegacyAccount`。
- 计划付款编辑页的账户选择事件同样收窄为只传账户 ID；规则保存和币种更新继续使用原账户 ID 语义。
- 编辑交易页和计划付款编辑页的分类选择事件已收窄为只传分类 ID；ViewModel 从当前分类列表解析 UI 所需对象，分类创建/编辑仍保留真实保存数据。
- 饼图分类点击事件已收窄为只传分类 ID；ViewModel 从当前分类金额列表解析选中项，完整 `Category` 不再穿过事件层。
- 编辑交易页的标签选择、取消选择和删除事件已收窄为只传 `TagId`；标签编辑事件删除未使用的旧标签参数，只保存更新后的标签。
- 预算页删除事件已收窄为只传预算 ID；ViewModel 从当前预算列表解析实体后调用删除用例，编辑预算仍保留弹窗返回的更新后预算对象。
- 借贷详情页的还款记录点击和删除事件已收窄为只传记录 ID；ViewModel 从当前记录列表解析弹窗展示数据或删除实体，创建/编辑记录继续保留弹窗返回的数据。
- 账户、预算和借贷列表的重排事件已收窄为只传排序后的 ID 列表；拖拽弹窗继续使用展示模型渲染，ViewModel 负责从当前状态解析实体并调用保存/重排用例。
- 借贷交易同步内部参数拼写已从 `oldLonRecordConvertedAmount` 修正为 `oldLoanRecordConvertedAmount`，并把 `newLoanRecordAccountID` 收敛为 `newLoanRecordAccountId`；只收敛命名，不改变换算逻辑。
- 旧重排弹窗不再要求业务展示模型实现 legacy 接口；账户、分类、预算和借贷页面改为向 `ReorderModalSingleType` 传入排序号读取和复制逻辑，feature 模型不再依赖 `shared:ui:legacy` 的重排数据契约。
- 旧主题外层色板继续收窄：`Blue`、`IvyLight`、`GreenLight`、`RedLight` 和 `IvyDark` 没有模块外调用点，已改为 `shared:ui:legacy` 内部常量；feature 层继续通过 `LegacyTheme` 和仍公开的实际使用色板访问旧主题。
- 计划付款列表状态中的账户和分类已换成本 feature 的轻量展示模型，只保留 ID、名称、图标、币种和颜色等渲染/导航字段；完整 `LegacyAccount/Category` 只在 ViewModel 加载时转换，不再进入页面状态和列表 UI 组件。
- 预算页面状态和预算弹窗数据删除未使用的账户列表字段；账户数据仍在 ViewModel 内部用于预算已花费金额换算和已有账户过滤条件计算，但不再暴露给不使用账户选择的弹窗 UI。
- 报表筛选条件中的分类选择已从完整 `Category` 列表收为分类 ID 列表；筛选弹层继续用完整分类列表渲染按钮，ViewModel 在过滤时把本地“未指定分类” ID 映射回空分类语义，统计结果不变。

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
  -> shared:data:model

shared:data:core
  -> shared:data:model

shared:ui:core
  -> shared:data:model only if needed for UI value formatting
```

不希望出现：

- `shared:domain -> shared:ui:*`
- `shared:domain -> Android Activity/platform interface`
- `feature -> temp:*`
- `shared:data:core/src/main -> fake/test helper`
- `app -> every low-level library because of convenience`

当前进展：

- UI 基础状态和默认实现的 Hilt 装配已下沉到对应 shared 模块：`shared:ui:core` 提供主题、周期、时间、日期时间弹窗和 toast 服务，`shared:ui:navigation` 提供 `Navigation` 单例。
- `com.ivy.ui.time.impl` 下的默认时间实现已收为 `shared:ui:core` 内部实现；app 继续只注入 `TimeProvider`、`TimeConverter`、`TimeFormatter`、`DateTimePicker` 等接口。
- Android 字符串资源适配器已迁入 `shared:ui:core`，作为 `ResourceProvider` 的默认实现供 Hilt 图使用。
- 根启动快捷方式参数已改用 `TransactionRouteType`，shortcut XML 的字符串值不变，打开添加收入/支出/转账入口的行为不变。
- UI core 中未接入任何页面的 Material3 `BackButton` helper 已删除；返回按钮的实际 UI 仍来自当前 legacy 组件。
- domain 中孤立的 `TimeRange` 草稿模型已删除；没有调用点，实际筛选/周期逻辑仍走现有时间范围模型。
- 设置相关 domain use case 的构造函数已收为 `internal`；feature 层仍通过公开 use case 类型注入和调用设置能力，底层偏好 Store 依赖不再作为外部可构造细节暴露。
- domain 下剩余 use case 的 `@Inject` 构造函数也已统一收为 `internal`；模块外只依赖 use case 能力本身，不再依赖构造细节。
- data-core 内部实现类的 `@Inject` 构造函数已统一收为 `internal`；模块外继续只依赖 `shared:data:api` 暴露的 Store 端口。
- app 和 feature 中已是 `internal` 的注入类也统一收窄构造函数；导航入口和 Composable 页面 API 不变。
- `LegacyUiRoot` 已归入 `com.ivy.legacy.ui` 包；app 仍保留对 `shared:ui:legacy` 的真实依赖，但不再把 legacy 根入口伪装成 ui-core 包名。
- Android manifest 已删除系统自动备份规则引用和对应 XML；当前分支继续通过 Ivy 自己的数据管理入口处理 zip 备份、恢复和 CSV 导入导出。
- Android manifest 已删除明文流量开关，汇率同步继续使用现有 HTTPS 接口；`INTERNET` 权限保留在 app 壳层，`shared:data:core` 不再携带库 manifest 权限声明。
- 导航模块已删除全局页面返回处理器 Map；主页面账户 Tab 返回、导入恢复流程内部返回和交易页状态栏恢复都由页面内 `BackHandler` 承担。
- domain 中剩余的偏好开关服务、偏好开关目录和内部借贷交易同步核心构造函数已收为 `internal`；模块外继续只依赖它们的注入类型和公开方法。
- 对象路由页面入口已删除无用 `screen` 参数；仍携带 ID、筛选条件或初始值的 route 页面继续接收对应 `screen` 数据。
- `EditPlannedScreen` route 已删除页面校验 helper；计划付款编辑页在本模块内判断初始金额和账户是否足够进入后续流程。
- `TransactionsScreen.unspecifiedCategory` 已从 `Boolean?` 收为 `Boolean`，交易列表初始化分支不再携带不存在的 null 状态。
- `TransactionsScreen.accountIdFilterList` 和 `transactionIds` 已收为 `ImmutableList<UUID>`，与饼图 route 的集合语义保持一致。
- 旧收支汇总卡片不再接收完整交易历史，只接收页面已经算好的收入/支出交易数量；通用 UI 组件不再依赖 `LegacyTransaction` 或 `TransactionHistoryItem`。
- 报表页内部支付/收取和跳过计划交易的事件名已去掉 `Legacy`，事件层继续只传交易 ID，旧模型查找限制在 ViewModel 私有实现内。
- `DevicePreferences` 已收为 `shared:ui:core` 内部接口；模块外继续只注入公开的 `TimeFormatter`。
- 首页状态层已切换为 feature 本地展示模型；`TransactionListData` 和 `LegacyDueSection` 只在 `HomeTab` 调用旧交易列表组件的适配层出现。
- 首页加载链路已用私有命名输入对象替换 `Pair/Triple` 中间数据；偏好、时间范围、账户列表、余额和历史加载参数不再靠位置传递，页面数据加载顺序和结果不变。
- 交易列表页的到期/逾期交易状态已合并为 feature 本地 section；页面状态不再把交易列表、展开状态和收支统计拆成多组并行字段。
- 报表页的到期/逾期交易状态也已合并为 feature 本地 section；旧 UI 的 `LegacyDueSection` 继续限制在 `ReportScreen` 组件适配层。
- 报表页顶部收支卡片已改用轻量 `ReportTransactionSummary`；完整旧交易列表不再进入 `ReportScreenState.transactions` 这类页面级状态字段。
- 报表页的加载结果写入已从十几个散列参数收为私有 `ReportValues`；收入/支出、到期/逾期、账户、筛选和汇总结果作为一次加载结果传递，状态写入行为不变。
- 交易列表页顶部收支卡片的收入/支出交易数量已从 UI 过滤逻辑移回状态层；`TransactionsScreen` 不再为了计数直接引用 `LegacyTransaction`。
- 分类页月度统计所需的账户和旧交易列表已收敛为一次加载流程的局部输入，避免把旧交易列表挂在 ViewModel 长期可变字段上。
- 饼图页从报表/交易列表进入时只在 ViewModel 中保留输入交易 ID；`LegacyTransaction` 列表只在 `BuildPieChartDataUseCase` 调用前局部加载。
- 借贷详情页的贷款关联交易对象也已改为按需读取；ViewModel 长期状态只保留是否创建关联交易的布尔开关。
- 借贷详情页的还款记录汇总已收为私有 `LoanRecordTotals`；已还本金、已还利息和贷款总额一次遍历得出，再写回页面状态，统计语义不变。
- 报表筛选条件中的账户选择已从完整 `LegacyAccount` 列表收为账户 ID 列表；报表页继续保留全部账户列表用于交易卡片展示和币种换算，筛选条件只表达“选中了哪些账户”。
- 计划付款编辑页不再长期保存选中 `LegacyAccount` 对象；ViewModel 保存账户 ID，UI 所需的账户对象从当前账户列表推导，计划付款规则保存行为不变。
- 借贷列表页的新建借贷默认账户也已从长期 `LegacyAccount` 状态收为账户 ID；打开旧借贷弹窗时再按当前账户列表解析对象，页面状态不再暴露未使用的选中账户字段。
- 借贷详情页的贷款账户也已收为账户 ID 状态；详情展示、编辑贷款弹窗和新增还款记录弹窗继续按当前账户列表推导所需 `LegacyAccount` 对象。
- 交易列表页的账户详情状态也已收为账户 ID；页面状态继续向旧 UI 暴露推导后的 `LegacyAccount`，但 ViewModel 不再注入单账户 legacy 查询 use case。
- CSV 导入器已去掉导入器实例上的 `lateinit` 账户/分类缓存；每次导入创建独立 `CsvImportContext` 保存账户、分类、基础货币和新建颜色游标，导入结果统计和保存逻辑不变。
- 借贷交易同步核心已去掉实例级基础币种缓存和初始化协程；需要换算时直接读取当前基础币种，交易创建、删除和保存入口不变。
- CSV 日期解析已删除从未写入成功值的文件级格式缓存；解析仍按原有日期格式列表逐项尝试。
- 借贷详情 ViewModel 不再通过 `lateinit screen` 保存整个导航 route；页面入口显式传入 `loanId` 启动加载，详情展示和编辑流程不变。
- 交易列表 ViewModel 不再长期保存完整 `TransactionsScreen` route；翻月、删除和刷新只复用本地 `TransactionsQuery` 中的账户、分类、未分类标记和交易 ID 筛选参数。
- feature 内部展示模型包名继续统一：借贷展示模型已从列表页子包 `loans.loan.data` 归位到 `loans.model`，汇率页 `RateUi` 也从 `exchangerates.data` 归位到 `exchangerates.model`，避免和真正数据层命名混淆。
- UI core 删除通用 `Modifier.thenWhen` helper；唯一遗留调用点已改为 `ItemIcon` 私有 padding helper，保留仍被页面和 legacy 组件使用的 `thenIf`、点击、阴影和密度转换工具。
- `ivy.feature` 约定插件不再隐式给所有 feature 注入 `shared:ui:legacy`；当前仍使用旧 UI 的 feature 已在各自 `build.gradle.kts` 中显式声明 legacy 依赖，依赖图不变但边界来源更清楚。
- 饼图统计 ViewModel 不再接收导航 route；页面入口负责把 `PieChartStatisticScreen` 拆成交易类型、账户筛选、交易 ID 和偏好参数，ViewModel 只处理统计加载。
- 计划付款编辑 ViewModel 不再接收 `EditPlannedScreen` 导航 route；页面入口负责拆出计划规则 ID、交易类型、金额、账户、分类、标题和描述，编辑/新建行为不变。
- 编辑交易 ViewModel 不再接收 `EditTransactionScreen` 导航 route；页面入口负责拆出初始交易 ID、交易类型、账户和分类参数，已有交易编辑和新建交易默认账户选择逻辑不变。
- 编辑交易 ViewModel 的基础币种缓存已从 `lateinit` 改为显式的启动期可空缓存；正常流程仍在页面启动时读取基础币种，换汇计算不再依赖 Kotlin 未初始化属性异常表达生命周期。
- 交易列表 ViewModel 完全脱离导航 route 类型；`TransactionsScreen` 到本地 `TransactionsQuery` 的转换下沉到页面入口，ViewModel 只复用查询参数执行加载、翻月和刷新。
- 交易列表内部查询参数继续去 legacy 命名：本地 `TransactionsQuery` 使用 `transactionIds`，加载流程用 `inputTransactions` 表达从 ID 局部读取出的交易；编辑交易和报表的标签搜索 debounce 常量也修正为 `Millis` 命名。
- 交易列表和饼图导航 route 的交易 ID 参数也已从 `legacyTransactionIds` 改为 `transactionIds`；route 只表达 ID 列表，不再暗示跨页面传递完整旧交易模型。
- 饼图导航 route 的账户筛选参数已从含糊的 `accountList` 改为 `accountIdFilterList`，与交易列表 route 和 ViewModel 内部查询命名保持一致。
- 饼图数据构建和借贷列表金额汇总中的内部 `Pair` 已改成私有命名结果对象；账户过滤集合、已还金额和贷款总额不再靠 `first/second` 或解构位置表达。
- 报表导出事件不再携带 `FileSharer` 平台分享器；ViewModel 只生成 CSV 并发出 `ShareCsvFile` UI 事件，页面入口负责调用平台分享能力。
- 报表页 Toolbar 不再直接调用 `navigation()` 执行关闭；返回动作由报表页面入口传入，Toolbar 保持为普通 UI 组件。
- 设置页导出 CSV 和备份 zip 也不再通过事件传递 `FileSharer`；ViewModel 写入文件后发出分享 UI 事件，Screen 统一调用平台分享能力。
- 报表页面事件统一为 `sealed interface`，与其他 feature 的事件定义风格保持一致，减少无意义的 `ReportScreenEvent()` 继承样板。
- 借贷创建和还款记录创建数据已从携带完整 `LegacyAccount` 改为只携带账户 ID；旧弹窗仍用完整账户对象渲染选择项，domain 创建和交易同步只接收 ID，`shared:data:model` 不再因为这两个创建 DTO 依赖 legacy 账户模型。
- 借贷弹窗和还款记录弹窗的 modal data 也已从完整选中账户收窄为账户 ID；旧弹窗继续根据当前账户列表解析展示对象，`shared:ui:core` 的借贷弹窗状态不再依赖 legacy 账户模型。
- 旧弹窗状态包已整体从 `shared:ui:core` 迁回 `shared:ui:legacy`；账户、分类、缓冲金额、周期、借贷、借贷记录和重复规则弹窗继续用同名数据对象传参，但 UI core 不再暴露旧 modal data API。
- 交易页和饼图页的周期选择弹窗状态已从 ViewModel/State/Event 移回 Screen 本地状态；ViewModel 只处理周期切换和数据加载，不再为了打开旧弹窗依赖 legacy modal data。
- 分类页和计划付款编辑页的新增/选择类旧弹窗状态也已移回 Screen 本地状态；ViewModel 继续处理创建账户、创建/编辑分类和重复规则保存，不再承担纯 UI 弹窗开关数据。
- 账户页和分类页的月度统计范围已用 `monthlyRange` 直接表达，不再保留迁移期解释性注释；行为仍是按当前月加载统计。
- 饼图页的选中分类状态已从完整 `Category` 收为分类 ID；图表和列表仍通过 `CategoryAmount` 渲染分类名称、图标和颜色，选中高亮只比较 ID。
- 导入恢复 flow 子组件不再直接调用 `navigation()` 或引用手动 CSV route；`ImportCSVScreen` 和 `CSVScreen` 入口负责把返回、完成和进入手动 CSV 导入翻译为导航行为。
- 报表页和报表筛选浮层改用报表模块私有复选行；`shared:ui:legacy` 的 `IvyCheckboxWithText` 收窄为旧账户弹窗内部实现，借贷弹窗迁回 feature 后使用借贷模块私有复选行。
- 借贷列表和借贷详情页改用借贷模块私有进度条；`shared:ui:legacy` 删除不再被复用的 `ProgressBar`。
- 计划付款底栏和饼图统计底栏改用各自模块私有操作行；`shared:ui:legacy` 的 `ActionsRow` 收窄为旧弹窗/旧编辑底栏内部构件。
- 分类、预算和汇率页底部返回栏改用各自模块私有实现；`shared:ui:legacy` 删除不再被复用的 `BackBottomBar`。
- data-core 的 UTC 毫秒和 `LocalDateTime` 转换扩展收窄为模块内部实现；外部继续通过 Room converter、serializer 和 Store API 间接使用对应数据格式。
- 交易筛选页和借贷详情页顶部统计工具栏改用各自模块私有实现；`shared:ui:legacy` 删除不再被复用的 `ItemStatisticToolbar`。
- 计划付款卡片和借贷记录改用各自模块私有金额行；`shared:ui:legacy` 的 `TypeAmountCurrency` 收窄为旧交易卡片内部实现，保留到期/逾期交易样式逻辑。
- 借贷弹窗、借贷记录弹窗和对应 modal data 已迁回 `feature:loans` 私有边界；借贷 feature 继续复用 legacy 的公开基础弹窗/按钮/金额输入能力，但不再通过 `shared:ui:legacy` 导出借贷业务弹窗。
- 编辑交易和计划付款编辑页的交易类型切换弹窗已改为各自 feature 私有实现；`shared:ui:legacy` 不再导出只服务编辑流程的 `ChangeTransactionTypeModal`。
- 编辑交易和计划付款编辑页的描述编辑弹窗已改为各自 feature 私有实现；`shared:ui:legacy` 不再导出只服务编辑流程的 `DescriptionModal` 或描述输入框内部细节。
- 编辑交易和计划付款编辑页的描述展示卡片和添加描述按钮已改为各自 feature 私有实现；`shared:ui:legacy` 删除只服务该展示入口的 `Description`、`PrimaryAttributeColumn` 和 `AddPrimaryAttributeButton`。
- 编辑交易和计划付款编辑页的分类按钮和顶部工具栏已改为各自 feature 私有实现；`shared:ui:legacy` 删除只服务这两个编辑页的 `edit.core.Category` 和 `edit.core.Toolbar`。
- 编辑交易和计划付款编辑页的标题输入和标题建议列表已改为各自 feature 私有实现；`shared:ui:legacy` 删除只服务这两个编辑页的 `edit.core.Title`，旧 `IvyTitleTextField` 暂留给 legacy 标签弹窗内部使用。
- 编辑交易和计划付款编辑页的底部金额/账户面板已改为各自 feature 私有实现；`shared:ui:legacy` 删除最后一个 `edit.core.EditBottomSheet`，`legacy.ui.edit.core` 包不再承载编辑页业务 UI。
- app 仍保留文件选择、文件分享、Material 日期选择器、BuildInfo、Locale 设置、生物识别和窗口安全等真正依赖 Activity 或 Android app 壳层的装配。

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
   - 第三方导入 logo、widget 预览图、推广/分享文案已经删除。
   - 后续只处理明确无引用或明显过期的文案；多语言资源暂时保留，避免无意义地降低现有 UI 覆盖。
2. **构建约定插件整理**
   - `shared:base` 已删除；`shared:data:model`、`shared:data:api`、`shared:domain` 和 `shared:test-support` 已是 JVM 模块。
   - 后续只在实际改到某个模块时继续删多余依赖，不为追求形式统一批量改 Gradle。
3. **测试 helper 归位**
   - `shared:test-support` 已建立，跨模块复用的测试生成器和断言已迁入。
   - fake DAO 继续留在 data-core 测试源集；后续只在出现新的跨模块复用需求时再移动。
4. **收尾旧设计兼容层**
   - `:temp:old-design` 已删除。
   - 后续替换或收窄 `LegacyTheme`、旧颜色常量和旧组件。
5. **收窄 `shared:ui:legacy`**
   - 优先继续缩小旧交易列表、旧弹窗和旧主题对 feature 暴露的模型。
   - 只在页面调用点已经只需要 ID、枚举或小展示模型时，再把完整旧模型收回组件内部。
6. **偏好设置重构**
   - 继续保持 feature 不直接访问 `SharedPrefs`。
   - 后续只在确实需要迁移数据格式时再评估 `SettingsEntity`、SharedPrefs 和 DataStore 的归并。
7. **平台能力拆分**
   - `RootActivity` 和 `RootScreen` 大接口已基本拆完。
   - 后续只处理仍能明确降低 Activity/app 边界负担的小平台适配器。
8. **数据库遗留迁移**
   - 用户表、同步字段、旧设置表单独处理。
9. **feature 模块合并**
   - 在 `shared:ui:legacy` 边界继续收窄后再做，避免只是把旧耦合搬进更大的 feature 模块。

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

1. 继续审计 `LegacyTransaction` / `LegacyAccount` 在 UI/统计路径中的真实必要性；优先从只做展示、筛选或参数传递的页面状态开始，评估是否能接收正式模型 ID 或更小的展示模型。
2. 继续检查 shared/feature 模块依赖，优先处理 feature 仍直接引用 domain 内部算法模型、legacy 兼容模型或过宽 use case 的位置。
3. 偏好设置代码边界已基本收窄，短期不再为清理而迁移存储格式；若后续要处理 `SettingsEntity`、SharedPrefs 或 DataStore 归并，必须单独规划 schema/备份兼容迁移。
4. 继续数据库只读审计：`isDeleted` 目前先保留为本地软删除语义；不再把业务表里的 `isDeleted` 当作纯云同步字段批量删除。
5. feature 模块合并属于较大结构调整，短期只在实际修改某个功能时收敛依赖；真正合并模块前需要先确认导航、资源和 Hilt 边界。
