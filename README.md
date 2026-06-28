# Ivy Wallet（个人维护分支）

Ivy Wallet 是一款开源记账应用，用 Kotlin 和 Jetpack Compose 编写，界面清爽，适合手动记录和打理个人收支。原项目 [Ivy-Apps/ivy-wallet](https://github.com/Ivy-Apps/ivy-wallet) 自 2024 年 11 月起不再维护，本仓库是在其最后一个版本基础上 fork 出来的个人维护分支，按 GPL-3.0 继续开发，主要供自己编译和使用。

应用的记账功能和使用体验与原版保持一致。这个分支做的事情更偏工程层面：去掉发布、协作、推广、云服务这些和个人使用无关的部分，把模块和依赖关系收拾得更清楚，方便一个人长期维护。

## 功能

- 账户、分类、预算、借贷、计划付款
- 收支记录、搜索、报表、饼图、余额走势
- 本地 zip 备份与恢复、CSV 导入导出
- 多币种与实时汇率、定时通知、应用锁（支持生物识别）

## 与原仓库的区别

功能本身基本没动，差异集中在工程和依赖上：

- 去掉了面向应用商店和社区协作的内容：CI 工作流、Issue/PR 模板、贡献文档、Fastlane、发布与推广入口、贡献者和致谢页等。
- 去掉了云服务和遥测：Firebase、Crashlytics、Firestore、Google Services、Play 应用内评价、云端备份迁移等。
- 模块大幅合并：原来的 16 个 feature 模块合并为 3 个（wallet / analytics / settings），删除了 temp、legacy UI 等历史兼容模块，并收窄了各模块对外暴露的 API。
- 偏好存储从 SharedPreferences、DataStore、Room 三套收敛为 DataStore 加 Room 两套，旧数据在首次启动时自动迁移。
- 构建不再依赖 Android Studio 和外部签名：本地 JDK + Android SDK + Gradle 即可，release 包也用仓库内的 debug 证书签名。
- 应用图标换回与官方一致的彩色版本（开源源码里自带的是中性灰图标）。

由于用的是 debug 证书，签名和 Google Play 上的官方版不同，无法直接覆盖安装官方版。需要迁移数据时，在官方版里导出备份，再到这个版本导入即可。

## 构建与运行

环境要求：

- JDK 17
- Android SDK（platform 34、build-tools、platform-tools）
- 在 `local.properties` 里把 `sdk.dir` 指向本机 SDK 路径

常用命令：

```bash
./gradlew :app:assembleDebug     # 调试包
./gradlew :app:assembleRelease   # 发布包（本地 debug 签名）
./gradlew :app:installDebug      # 安装到已连接的设备
```

最低支持 Android 9（API 28），编译使用 Android 14（API 34）。

## 技术栈

Kotlin、Jetpack Compose、Hilt、Room、DataStore、Coroutines/Flow、WorkManager，多模块构建由 `buildSrc` 里的 Gradle convention plugin 统一管理。

## 模块结构

- `app` — 应用壳层：Activity、Hilt 装配、导航图、平台能力适配、通知与启动
- `feature/wallet` — 核心记账与编辑：首页、账户、分类、预算、交易、借贷、计划付款、搜索、汇率
- `feature/analytics` — 报表、饼图、余额走势
- `feature/settings` — 设置与 CSV 数据导入
- `shared/data` — 数据层：`api` 端口、`core` 实现（Room、DataStore、备份、汇率）、`model` 业务模型
- `shared/domain` — 用例层，衔接 feature 与 data 之间的业务逻辑
- `shared/ui` — 跨页面复用的 `core` 组件与 `navigation` 导航
- `buildSrc`、`gradle` — 构建约定插件与版本目录

## 许可

本项目 fork 自 [Ivy-Apps/ivy-wallet](https://github.com/Ivy-Apps/ivy-wallet)，原作者 Iliyan Germanov 及社区贡献者，遵循 [GPL-3.0](LICENSE) 继续开源。
