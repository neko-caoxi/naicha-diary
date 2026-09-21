# 饮品日记

记录每天喝的奶茶和咖啡。给每一杯留个念想，顺手看看这个月到底喝了多少。

Jetpack Compose 写的，液态玻璃底栏，小票拍一张就能自动录入。

**[⬇ 下载最新版 APK](https://github.com/neko-caoxi/naicha-diary/releases/latest)**　·　1.8 MB　·　Android 8.0+

## 功能

- **快速记录** — 品牌、品名、规格、价格、甜度冰量、评分、照片
- **AI 识图录入** — 拍一张小票，自动识别品牌 / 品名 / 规格 / 价格（DeepSeek 视觉模型）
- **饮品墙** — 瀑布流浏览所有记录，可按品牌排行 / 时间轴查看，支持生成分享海报
- **统计** — 品类环形图、日历热力图、成就徽章、AI 生成的饮品洞察
- **液态玻璃底栏** — 真背景模糊，手指划过时光带跟随流动
- **灵动胶囊** — 适配 Android 16 `Notification.ProgressStyle`

## 截图

> 待补充

## 技术栈

| 项 | 值 |
|---|---|
| 语言 / UI | Kotlin 2.2.0 + Jetpack Compose（BOM 2025.06.01） |
| 构建 | AGP 8.11.1 / Gradle 8.14.3 / JDK 21 |
| SDK | minSdk 26，targetSdk 36，compileSdk 36 |
| 玻璃材质 | [chrisbanes/haze](https://github.com/chrisbanes/haze) 1.5.3 |
| AI | DeepSeek `deepseek-flash`（OpenAI 兼容接口） |
| 存储 | 本机 JSON 文件，无后端、无账号、不联网上传 |
| APK 体积 | release 约 1.8 MB（R8 + 资源压缩） |

## 构建

```bash
export JAVA_HOME=/path/to/jdk21
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleRelease
```

签名信息从 `local.properties` 读取（不入库）：

```properties
RELEASE_STORE_FILE=your.jks
RELEASE_STORE_PASSWORD=***
RELEASE_KEY_ALIAS=***
RELEASE_KEY_PASSWORD=***
```

AI 识图需要在 App 内「我的 → AI 识图录入」填入自己的 DeepSeek API Key，只存本机。

## 设计约定

- 圆角统一用 iOS 连续曲率（`SquircleShape`）
- 卡片保留阴影层次，不做纯扁平
- 交互都配触感反馈（切页 / 选择 / 保存 / 删除）
- 版本产物归档在 `dist/`，每次发版打 tag

## 许可

MIT
