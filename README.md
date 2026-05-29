# 俄罗斯方块 (Tetris)

经典俄罗斯方块 Android 版，金砖质感风格，带消行碎裂特效。

## 下载安装

从 [Releases](../../releases) 下载 `俄罗斯方块.apk`，或从源码构建：

```
app/build/outputs/apk/release/俄罗斯方块.apk
```

### 安装前注意

| 品牌 | 操作 |
|------|------|
| 小米 MIUI | 设置 → 开发者选项 → 关闭 MIUI 优化 |
| 华为 EMUI | 设置 → 系统和更新 → 关闭纯净模式 |
| OPPO ColorOS | 设置 → 安全 → 关闭外部来源应用检查 |
| 其他 | 设置 → 安全 → 允许未知来源安装 |

## 操作

| 按钮 | 功能 |
|------|------|
| ← → | 左右移动 |
| ↻ | 旋转 |
| ↓ 点击 | 下移一格 |
| ↓ 长按 | 直接落底 |
| 暂停 | 暂停 / 继续 |

## 从源码构建

```bash
git clone https://github.com/你的用户名/Tetris.git
cd Tetris

# 配置 SDK 路径
echo "sdk.dir=/你的AndroidSdk路径" > local.properties

# 构建
./gradlew assembleRelease
```

## 技术

- Kotlin + AndroidX + Canvas 自定义绘制
- minSdk 24 / targetSdk 33
- 测试通过：小米 Mi 11 Lite (MIUI 13)

## License

MIT
