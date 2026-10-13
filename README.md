# 东北乐事 (Northeast China Delight)

本分支为 Minecraft 1.20.1（Forge）版本线；1.21.1（NeoForge）开发主干在 main 分支。

## 当前最新版本

**3.0.0**

- Minecraft 1.20.1：Forge 47.1.33 及以上

需要[农夫乐事](https://github.com/vectorwing/FarmersDelight)与[机械动力](https://github.com/Creators-of-Create/Create)作为前置。

## 更新记录

**3.0.0**

- 烤架配方改为带稳定 ID 的数据包配方，支持新增、覆盖或禁用单条配方
- 烤架与 JEI 共用数据包规则，支持配方重载并兼容旧烤架存档
- 批量风干配方开放数据包编辑，制作干豆腐时按原料数量产出并返还空碗
- 新增海洋取水三档配置：海水桶、带海水标记的普通水桶、普通取水
- 标记水桶提示简化为“可用于制盐”，修复制盐烧炼后不返还空桶的问题
- 新增村庄农田与箱子战利品的独立生成开关和物品允许列表

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

修改现有配方时复制模组自带的 JSON 再编辑 `config`；本模组默认有 14 条大缸配方。1.20.1 数据包中的通用标签使用 `forge:`，1.21.1 使用 `c:`。

## 烤架配方数据包

烤架使用 `northeast_china_delight:grill` 配方类型，默认四条配方 ID 为 `northeast_china_delight:grill/grilled_chicken_frame`、`grill/grilled_corn`、`grill/grilled_oil_edge`、`grill/grilled_cold_noodles`（后三条同样使用本模组命名空间）。覆盖烤冷面时，1.21.1 文件路径为 `data/northeast_china_delight/recipe/grill/grilled_cold_noodles.json`，1.20.1 为 `data/northeast_china_delight/recipes/grill/grilled_cold_noodles.json`。禁用示例：

```json
{"type":"northeast_china_delight:grill","config":{"disabled":true}}
```

新增配方使用新的文件名或命名空间，例如 1.21.1 的 `data/example/recipe/grill/sweet_carrot.json`：

```json
{
  "type": "northeast_china_delight:grill",
  "config": {
    "priority": 0,
    "input": {"item": "minecraft:carrot"},
    "seasonings": [{"item": "minecraft:sugar"}],
    "result": {"item": "minecraft:golden_carrot", "count": 1},
    "seconds": 20
  }
}
```

`input` 和每个调料条目均支持物品、标签及任选数组；`seasonings` 可省略，最多四份，重复条目代表需要多份。瓶装调料仍需用刷子，每份消耗 10 mB。相同食材按 `priority` 从高到低、再按配方 ID 排序。配方属于服务端数据，应作为世界数据包加载，而不是客户端资源包；执行 `/reload` 后实际烤制和 JEI 都使用更新后的配方，标签候选仍在同一格滚动展示。

每份架上食材都会保存所绑定的配方 ID；规则重载后重新计时并保留仍适用的调料。已绑定配方被禁用、删除或改成不匹配的食材时停止加工，不会自动切换到另一条配方，原料仍可空手取回。无 ID 的旧烤架存档首次读取时按食材匹配并绑定。

## 批量风干配方数据包

批量风干使用 `northeast_china_delight:drying` 类型，干豆腐配方 ID 为 `northeast_china_delight:drying/dried_tofu`。1.21.1 的路径是 `data/northeast_china_delight/recipe/drying/dried_tofu.json`，1.20.1 使用 `recipes/drying/`。默认规则如下，1.20.1 将 `c:` 标签改为 `forge:`：

```json
{
  "type": "northeast_china_delight:drying",
  "config": {
    "priority": 0,
    "input": {"tag": "c:foods/soy_milk"},
    "results": [
      {"item": "northeast_china_delight:dried_tofu", "count": 1},
      {"item": "minecraft:bowl", "count": 1}
    ]
  }
}
```

支持新 ID 新增、同 ID 覆盖及 `{"type":"northeast_china_delight:drying","config":{"disabled":true}}` 禁用。原料支持物品、标签和任选数组；产物为 1–2 项，按每份原料扩展数量并自动分堆。加工时长仍由机械动力的鼓风机配置决定，不增加独立的配方时长。执行 `/reload` 后新规则用于实际风干，并刷新 1.21.1 的 JEI 页面；1.20.1 保持原有风干分类不注册的状态。

## 海洋取水配置

通用配置文件为 `config/northeast_china_delight-common.toml`，修改后重启游戏或服务端：

```toml
[sea_water]
bucket_mode = 1
```

- `1`（默认）：保留原行为，在干净的海洋水柱中取水得到海水桶。
- `2`：在相同环境取水得到带海水标记的原版水桶，可正常倒水，也能用于厨锅及烟熏炉制盐。
- `3`：关闭本模组的特殊取水，不拦截原版或其他模组的取水流程；普通水源给出未标记的水桶。

未标记的普通水桶不能制盐。标记只附在物品堆叠上，不把全部水桶加入海水标签，不改变水流方块；模式 2 的水倒到陆地后再舀起，不会自动保留海水标记。切换模式不会删除已有海水桶或标记水桶。

## 自然生成配置

在 `config/northeast_china_delight-common.toml` 中，农田与箱子分别有一个开关和一个允许列表：

| 配置项 | 默认值 / 含义 |
| --- | --- |
| `worldgen.village_farm_crops_enabled` | `true`，允许本模组作物在村庄农田生成 |
| `worldgen.village_farm_allowed_crops` | 全部 14 种农田作物的播种物品 ID |
| `chest_loot.enabled` | `true`，允许本模组物品在箱子战利品生成 |
| `chest_loot.allowed_items` | 全部 63 种现有箱子候选的物品 ID |

列表由游戏自动写入完整默认值，可直接删除不希望生成的条目；填写完整注册 ID，例如 `northeast_china_delight:soybean`、`northeast_china_delight:corn_seeds`，不要填写中文名。设为 `[]` 表示不允许生成任何本模组候选，开关设为 `false` 则直接关闭对应生成。

两项配置彼此独立；默认保持原有气候分配及概率。农田只影响新生成的村庄，不影响玩家种植和野外植物；箱子包含村庄、其他结构及东北小院，只限制本模组物品，不影响原版/其他模组物品、钓鱼和实体/方块掉落。修改后重启游戏或服务端；已有作物、已经生成的战利品和玩家库存不会被删除。

允许列表只筛选已有候选，不会自动增加新的作物分配或战利品条目；需要新增候选时仍应编辑数据包。多人游戏以服务端配置为准。

## 许可证

[MIT](LICENSE)
