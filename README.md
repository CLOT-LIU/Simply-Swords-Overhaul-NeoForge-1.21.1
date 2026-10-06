# 简易刀剑：全面改造（Simply Swords Overhaul）

对 [Simply Swords](https://www.curseforge.com/minecraft/mc-mods/simply-swords) 本体中 9 把独特武器的全面改造模组。

- **平台**：NeoForge 1.21.1
- **本体依赖**：Simply Swords `>= 1.70.2`（Minecraft 1.21.1）
- **运行依赖**：Architectury API（Simply Swords 的前置）
- **Java**：21

> 本模组基于 Forge 1.20.1 版 `simply_swords_overhaul-1.0.4` 迁移并适配 NeoForge 1.21.1 的 Simply Swords 新架构（主动技能接口）。

## 维护说明

本项目的版本升级与适配工作由 **AI 辅助完成**。若有人以更好的方式重写了本模组，本项目将停止更新。

## 改造内容

| 武器 | 主要改动 |
| --- | --- |
| 灰烬鞭 Emberlash | 命中叠加阴燃并按层数造成额外伤害；右键冲刺并回血，双持获得额外充能 |
| 熔火之刃 Molten Edge | 命中点燃；残血时获得力量/急迫/速度；右键自损换回血 |
| 影刺 Shadowsting | 物理伤害转为魔法伤害，目标护甲越高伤害越高；右键致盲并传送 |
| 灵魂束缚 Soul Pyre | 命中施加凋零；右键将视线内目标拉到身前 |
| 噬魂剑 Soulrender | 命中依次叠加缓慢/虚弱/厄运；右键消耗层数造成伤害并回血 |
| 星辰边缘 Stars Edge | 命中附加魔法伤害；右键记录位置并冲刺，到时或再次使用返回 |
| 风暴边缘 Storm's Edge | 命中缩短技能冷却；右键冲刺撞怪并获得急迫/速度 |
| 观察者 Watcher | 每击回血，击杀额外回复目标最大生命百分比 |
| 低语轻风 Whisperwind | 右键冲刺造成暴击，击杀重置冷却 |

所有行为数值、冷却、距离、效果等级均可在配置文件中调整，并可按武器单独开关改造。

## 安装

1. 安装 [NeoForge 1.21.1](https://neoforged.net/)。
2. 安装 [Simply Swords](https://www.curseforge.com/minecraft/mc-mods/simply-swords)（1.70.2 或更高）及其前置 Architectury API。
3. 将本模组 jar 放入 `mods/` 目录。

## 构建

```powershell
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

产物位于 `build/libs/simply_swords_overhaul-<version>.jar`。

编译时通过 CurseMaven 拉取 SimplySwords 1.70.2 的 NeoForge 生产 jar 作为编译依赖（不打包），Architectury API 仅作 `compileOnly`。

## 配置

配置文件：`config/simply_swords_overhaul-common.toml`

- `enable<Weapon>Changes`：按武器开关改造（默认全部开启）。
- 其余数值项：各武器的伤害倍率、冲刺距离、冷却（tick）、效果等级与时长等。

修改后需重启游戏生效。

## 许可证

本项目代码使用 MIT License，详见 [`LICENSE`](LICENSE)。