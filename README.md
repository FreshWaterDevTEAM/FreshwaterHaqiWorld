# FreshwaterHaqiWorld 2.0 — Paper 插件 + Forge 客户端

哈气世界生存：用麦克风哈气发射坚守者风格音波炮。

## 架构（v2）

| 组件 | 安装位置 | 作用 |
|------|----------|------|
| **fhw-plugin** | Paper `plugins/` | 全部玩法（语音、音波炮、物品、排行榜…） |
| **fhw-client** | Forge `mods/` | 内置贴图/音效 + 调试 H 键 |
| **fhw-resourcepack** | 可选手装 / `resource-pack=` | 与上面同源的独立资源包 |
| **Simple Voice Chat** | 服务端 Paper 插件 + 客户端模组 | 麦克风 |

> 旧版「纯 Forge 双端模组」已废弃，请用本架构。

## 服务端（Paper 1.21.11）

1. 安装 [Simple Voice Chat（Bukkit/Paper）](https://modrinth.com/plugin/simple-voice-chat)
2. 放入 `fhw-plugin-2.0.7.jar`
3. 启动后可在 `plugins/FreshwaterHaqiWorld/config.yml` 配置：
   - `resource-pack.host`：公网 IP/域名（内置 HTTP 推送资源包时用）
   - 或 `resource-pack.url`：直接填 Release 上的 `fhw-resourcepack` 下载链接（推荐生产环境）

## 客户端（Forge 1.21.11）

1. 安装 Forge 1.21.11 + Simple Voice Chat
2. 放入 `fhw-client-2.0.7.jar`
3. （可选）另装资源包；一般客户端模组已内置，服务端也会推送

## 玩法

- 对着麦克风哈气 → 音波炮；音量影响射程与伤害
- 需手持哈气物品；蹲下右键哈气物品可永久解锁该阶
- 合成：铁锭围基础哈气 → 升级；钻石围升级 → 强化；坚守者回响围强化 → 坚守者哈气
- 击杀坚守者掉落回响
- 铁傀儡 / 凋零骷髅 / 蛮兵 / 末影龙会音波炮
- `/haqi top` 排行榜；`/haqi unlock <tier>`（OP）

调试：按住 **H**（需客户端模组）模拟满响度哈气。

## 构建

```bash
./gradlew build
```

产物在 `build/release/`：

- `fhw-plugin-2.0.7.jar`
- `fhw-client-2.0.7.jar`
- `fhw-resourcepack-2.0.7.zip`
