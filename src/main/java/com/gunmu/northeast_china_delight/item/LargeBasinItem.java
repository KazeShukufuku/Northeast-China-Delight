package com.gunmu.northeast_china_delight.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
//? if <1.20.5 {
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
//?} else {
/*import com.gunmu.northeast_china_delight.NortheastChinaDelight;
import com.gunmu.northeast_china_delight.util.DdIds;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
*///?}

/**
 * 大脸盆（空盆）。跟普通方块一样能放地上，也能当帽子戴：右键空气扣头上，走原版头盔槽。
 *
 * <p>槽位判定两个版本同一套：本类实现 {@link Equipable} 后，1.20.1 的
 * {@code LivingEntity.getEquipmentSlotForItem}（Forge 补丁会读 Equipable）和 1.21.1 的
 * {@code IItemExtension.canEquip} 都会给出 HEAD，所以物品栏头盔格子能直接放进去。</p>
 *
 * <p>外观分流见 {@code client/LargeBasinItemRenderer}：物品栏是 2D 贴图，手 / 地面 / 头上是方块模型。</p>
 */
public class LargeBasinItem extends ItemNameBlockItem implements Equipable
{
    public LargeBasinItem(Block block, Item.Properties properties)
    {
        super(block, properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot()
    {
        return EquipmentSlot.HEAD;
    }

    /**
     * 原版 {@code Item#use} 自己不认 Equipable（只有盔甲、鞘翅在里面调了 swap），
     * 所以这里补上「右键空气 = 扣头上」；对着方块右键还是方块放置。
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    /**
     * 戴在头盔槽时给 1 点护甲值。
     *
     * <p>刻意不继承 {@code ArmorItem}：那会把耐久条、附魔表、盔甲贴图一起带进来，而大脸盆的定位是
     * 「方块物品 + 能扣头上」。这里只补一个属性修改器，所以没耐久、也不进附魔表
     * （{@code Item#isEnchantable} 默认要求「不可堆叠 + 有耐久组件」，本物品两条都不满足）。</p>
     */
    //? if <1.20.5 {
    private static final java.util.UUID ARMOR_MODIFIER_ID = java.util.UUID.fromString("6f2f5d1e-6e2a-4a3d-9f0e-1b7c4d5a8e11");

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot)
    {
        if (slot != EquipmentSlot.HEAD)
        {
            return super.getDefaultAttributeModifiers(slot);
        }
        return ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .putAll(super.getDefaultAttributeModifiers(slot))
                .put(Attributes.ARMOR,
                        new AttributeModifier(ARMOR_MODIFIER_ID, "large_basin_armor", 1.0D, AttributeModifier.Operation.ADDITION))
                .build();
    }
    //?} else {
    /*private static final ItemAttributeModifiers HEAD_ARMOR_MODIFIERS = ItemAttributeModifiers.builder()
            .add(Attributes.ARMOR,
                    new AttributeModifier(DdIds.of(NortheastChinaDelight.MODID, "large_basin_armor"), 1.0D, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.HEAD)
            .build();

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers()
    {
        return HEAD_ARMOR_MODIFIERS;
    }
    *///?}

    /*
     * 1.20.1 没有 RegisterClientExtensionsEvent，客户端渲染器只能从物品这里挂；
     * 1.21.1 那份在 NortheastChinaDelight.ClientModEvents#onRegisterClientExtensions。
     */
    //? if <1.20.2 {
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer)
    {
        consumer.accept(com.gunmu.northeast_china_delight.client.LargeBasinItemRenderer.CLIENT_EXTENSION);
    }
    //?}
}
