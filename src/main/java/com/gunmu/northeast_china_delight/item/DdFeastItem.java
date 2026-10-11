package com.gunmu.northeast_china_delight.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 大盆菜（8 道用大脸盆盛装的料理）的方块物品：放下就是一大盆（见 {@code block/DdFeastBlock}），
 * 右键取一份得「碗装XX」。
 *
 * <p>单独立这个类只为挂客户端渲染器：物品栏是 2D 贴图、手 / 地面是方块模型，
 * 分流见 {@code client/DdFeastItemRenderer} —— 渲染器得注册在具体的物品类上，
 * 所以不能继续用裸 {@code BlockItem}。</p>
 */
public class DdFeastItem extends BlockItem
{
    public DdFeastItem(Block block, Item.Properties properties)
    {
        super(block, properties);
    }

    /*
     * 1.20.1 没有 RegisterClientExtensionsEvent，客户端渲染器只能从物品这里挂；
     * 1.21.1 那份在 NortheastChinaDelight.ClientModEvents#onRegisterClientExtensions。
     */
    //? if <1.20.2 {
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer)
    {
        consumer.accept(com.gunmu.northeast_china_delight.client.DdFeastItemRenderer.CLIENT_EXTENSION);
    }
    //?}
}
