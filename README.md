# Block-Client — Part 1: UI Framework

Minecraft **Java 1.21.11** Fabric 客户端外挂框架。纯客户端侧，无服务端代码。

## 构建

```bash
cd block-client
./gradlew build           # 需要先有 JDK 21 + Gradle wrapper
```

如果还没有 Gradle wrapper，在项目目录执行：

```bash
gradle wrapper --gradle-version 8.10
```

构建产物在 `build/libs/block-client-1.0.0.jar`，丢到 `.minecraft/mods/` 即可。

## 依赖

| 依赖 | 版本 |
|------|------|
| Minecraft | 1.21.11 |
| Fabric Loader | ≥0.16.9 |
| Fabric API | 最新 1.21.11 兼容版 |
| imgui-java | 1.86.11 |

**构建时需要联网下载所有依赖。**

## Part 1 实现内容

- ✅ Fabric API `HudRenderCallback` 注入 ImGui 渲染管线
- ✅ 7 面板布局：Combat / Misc / Render / Movement / Player / Exploit / Client
- ✅ 右 Shift 切换 UI 开/关，UI 打开时解锁鼠标、不暂停游戏
- ✅ 纯客户端侧，零服务端数据包
- ✅ 暗紫色半透明主题 (#2A004E @85%)
- ✅ 1px #9D4EDD 边框 + #B388FF 文字描边
- ✅ 分辨率自适应缩放
- ✅ "Block-Client" 品牌标识左上角固定渲染
- ✅ 不使用 Minecraft Screen 类

## 文件结构

```
src/main/java/com/blockclient/
├── BlockClientMod.java          # Mod 入口，注册 HUD 回调
├── ui/
│   ├── ImGuiManager.java        # ImGui 生命周期管理 (GL3/GLFW)
│   └── PanelRenderer.java       # 7 面板布局渲染
└── input/
    └── InputManager.java        # GLFW 输入拦截 + 右 Shift 切换
```

## 注意

- `gradle.properties` 中的 `yarn_mappings` / `fabric_version` 可能需要根据 Fabric 官方仓库的实际版本号调整
- 首次启动时如果 ImGui native 加载失败，检查 `imgui-java-natives-*` 依赖是否完整
- 本 mod 声明 `"environment": "client"`，服务端加载会被自动忽略
