# 东北乐事 (Northeast China Delight)

本分支为 Minecraft 1.20.1（Forge）版本线；1.21.1（NeoForge）开发主干在 main 分支。

## 当前最新版本

**2.9.3**

- Minecraft 1.20.1：Forge 47.1.33 及以上

需要[农夫乐事](https://github.com/vectorwing/FarmersDelight)与[机械动力](https://github.com/Creators-of-Create/Create)作为前置。

## 更新记录

**2.9.3 后续修复**

- 大缸配方改为带稳定 ID 的数据包配方，支持覆盖或禁用单条配方
- 修复酱渣复酿时的封缸问题，统一醋和白醋的原料液体用量
- 合并 JEI 中重复的原料选项，并修复泡菜产酸引水的容量校验

**2.9.3**

- 现在大脸盆拥有模型，可以被放置为方块形式，可以用作头盔，提供1点护甲值
- 修复调料瓶可以被附魔的问题
- 手持和丢出盆装菜时，显示方块模型
- 优化部分配方所用食材及状态效果时长
- 修复烧烤架配方刷过一种调料后其他调料加不上的问题

**2.9.2**

- 修复“批量风干”导致其他鼓风机配方不可用的问题
- 优化战利品与副食商交易，使所有食材的获取不均依赖于机械动力模组

**2.9.1**

- 修复部分大缸配方的冲突问题
- 优化部分厨锅配方
- 优化音效和贴图

**2.9.0**

- 增加了1种东北小院的结构
- 优化了与其他乐事类模组的适配
- 调整了食用部分食物所得状态效果

## 大缸配方数据包

大缸配方 ID 取资源路径，例如 `northeast_china_delight:vat/bean_sprouts`。把文件放在数据包内同一路径即可覆盖：1.21.1 为 `data/northeast_china_delight/recipe/vat/bean_sprouts.json`，1.20.1 为 `data/northeast_china_delight/recipes/vat/bean_sprouts.json`。若要禁用，只需覆盖为：

```json
{"type":"northeast_china_delight:vat","config":{"disabled":true}}
```

修改现有配方时复制模组自带的 JSON 再编辑 `config`；本模组默认有 14 条大缸配方。1.20.1 数据包中的通用标签使用 `forge:`，1.21.1 使用 `c:`。烧烤配方尚未开放此数据包功能。

## 许可证

[MIT](LICENSE)
