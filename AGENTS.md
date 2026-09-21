# 饮品日记 — 项目规范

## 一、设计规范（硬性，做 UI 必须遵守）

### 1. 材质要真，不要假

- **玻璃必须真模糊**：用 `dev.chrisbanes.haze` 采样背后内容，禁止用「半透明纯色块」冒充玻璃。
  - `Modifier.haze(state)` 标记源，`Modifier.hazeChild(state) { blurRadius/noiseFactor/tints/backgroundColor }` 消费。
  - 注意 API 是 `tints: List<HazeTint>`，**没有 `shape` 参数**，形状靠外层 `clip`。
  - ⚠️ 玻璃生效的前提：被模糊的内容要能滚到玻璃后面。列表 `contentPadding` 底部必须留够，否则内容滚不到底栏后面 → 模糊无对象 → 看起来像假的。
  - haze 版本锁 **1.5.3**（2.0.0-rc 要 compileSdk 37，本机只有 36）。
- **卡片必须有阴影层次**：用 `SoftCard`（12dp 阴影 + 高光边框）。**禁止纯扁平卡片**。
- **圆角必须是 iOS 连续曲率**：用 `SquircleShape`（在 `ui/theme/Shape.kt`），**禁止裸用 `RoundedCornerShape` 当主要容器圆角**。
- **要有物理动态**：底栏玻璃要有「光在玻璃里流动」（手指滑动时光带跟随 + 弹簧阻尼），不要静态装饰。

### 2. 交互必须有物理反馈

- **震动**：用 `rememberHaptics()`（`util/Haptics.kt`）。
  - 切 tab / 滑动翻页 / 选品牌 / 切品类 → `tick()`（轻）
  - 保存记录 → `confirm()`
  - 删除 / 错误 → `reject()`
  - 新增可点击交互时，**默认要配震动**。
- **弹簧动画**：跟手元素用 `spring(dampingRatio = 0.5~0.6)`，不要线性 `tween` 走完全程。
- **页面必须有弹性滚动空间**：任何页面内容都不能被底栏「卡死」。底栏遮挡的部分必须能滑出来。

### 3. 空间节奏要抠到 dp

- 页面顶部：必须 `statusBarsPadding()`，不能被状态栏压住。
- 页面底部：`contentPadding` 底部要 ≥ 底栏高度 + 安全区（当前 132dp）。
- 底栏本身：高度 62dp、圆角 42dp、图标 25dp、blur 48dp（这套是调好的基准，别随手改小）。

### 4. 选项要精选，不要冗余

- 品类只有 `Tea` / `Coffee` 两类（砍掉过 Alcohol/Other）。
- 咖啡品牌只留门店数前 10 + 「手冲 / 自制」兜底。
- **宁可少而准，不要多而杂**。加选项前先想清楚是不是必要。

## 二、工程规范

### 版本意识（每次改动都要走完）

1. 改 `app/build.gradle.kts` 的 `versionCode`（+1）和 `versionName`（语义化）。
2. 构建 release：产物复制到 `dist/饮品日记-v{versionName}.apk`（带版本号，不覆盖）。
3. `git commit` + `git tag -a v{versionName}`。
4. **不允许**反复覆盖同一个 `app-release.apk` 就交付。

### 构建命令

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
~/opt/gradle/current/bin/gradle -p /Users/xin/Desktop/闲聊/naicha-diary assembleRelease
```

### 别做无用功

- **不要下载模拟器 / Android Studio**（用户对 1.5GB 下载强烈反感）。验证靠「编译通过 + 静态审查」。
- **不要死卡体积指标**。release 约 1.8MB 已达标，可为视觉效果牺牲几百 KB。
- 不要为了「保险」重复确认已经确认过的环境信息。

## 三、协作方式

- **拿不准就先问**：一句话确认「你是指 A 还是 B」，好过猜一个方向猛做一大轮。
- 用户会直接指出理解偏差，**被指正后不要辩解，立刻切到正确方向**。
- 用户偏好结果，不介意语气直接。

## 四、技术栈速查

| 项 | 值 |
|---|---|
| 语言 / UI | Kotlin 2.2.0 + Jetpack Compose（BOM 2025.06.01） |
| 构建 | AGP 8.11.1，Gradle 8.14.3，JDK 21 |
| SDK | minSdk 26 / targetSdk 36 / compileSdk 36 |
| 包名 | `com.naicha.diary` |
| 玻璃 | `dev.chrisbanes.haze:haze:1.5.3` |
| 存储 | `DrinkRepository`（本机 JSON 文件），API Key 在 SharedPreferences |
| AI | `DeepSeekClient`，模型 `deepseek-flash`，OpenAI 兼容 `content` 数组 + `image_url` data URL |
| 图片 | MediaStore（API 29+）绕开 FileProvider |
| 品牌 logo | `res/drawable-nodpi/logo_*.webp` 61 个；`Catalog.isSquircle()` 决定圆形 / 圆角方形 |

### 关键文件

- `ui/DrinkRoot.kt` — 主框架：HorizontalPager（4 页）+ 5 槽玻璃底栏 + RecordButton
- `ui/screens/RecordSheet.kt` — 记录表单，品类分段控件 + 品牌网格 + 品牌色联动
- `ui/screens/SettingsSheet.kt` — `SettingsSheet`（弹窗薄壳）+ `SettingsContent`（可复用页面）
- `ui/components/Common.kt` — `SoftCard` / `BrandBadge` / `BrandLogo` / `TagChip`
- `ui/theme/Shape.kt` — `DrinkShapes` + `SquircleShape`
- `util/Haptics.kt` — 统一触感反馈
- `net/DeepSeekClient.kt` — 识图 / 洞察 / 连通性测试（HttpURLConnection 零依赖）
- `data/Models.kt` — `Drink` / `DrinkCategory` / `Brand` / `Catalog`
