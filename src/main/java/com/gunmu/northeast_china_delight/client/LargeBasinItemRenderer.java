package com.gunmu.northeast_china_delight.client;

import com.gunmu.northeast_china_delight.NortheastChinaDelight;
import com.gunmu.northeast_china_delight.block.ModBlocks;
import com.gunmu.northeast_china_delight.util.DdIds;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
//? if <1.20.2 {
/*import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
*///?} else {
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
//?}

/**
 * 大脸盆（空盆）的分语境渲染：物品栏画 2D 贴图，其余场合（手、地面掉落物、展示框、头盔槽）画方块模型。
 *
 * <p>为什么必须自定义渲染器：一个物品模型只有一套 quad，`isGui3d` 与 display 变不出两种几何，
 * 想「图标是 2D、拿在手里是方块」只能在拿到 {@link ItemDisplayContext} 的这里分流。
 * {@code models/item/large_basin.json} 的 parent 是 {@code builtin/entity}（isCustomRenderer），
 * 原版 {@code ItemRenderer.render} 会先套用 display 里对应视角的位移/缩放，再进到本类。</p>
 *
 * <ul>
 *   <li>GUI：画 {@code models/item/large_basin_icon.json} 的普通 2D 贴图；</li>
 *   <li>其他：{@code renderSingleBlock} 画空盆方块，各视角的位移/缩放写在物品模型的 display 里
 *       （跟原版方块物品同一套数值）；头盔槽那 180 度倒扣也是 display.head 给的；</li>
 *   <li>粒子贴图取物品模型的 textures.particle。</li>
 * </ul>
 */
public class LargeBasinItemRenderer extends BlockEntityWithoutLevelRenderer
{
    /**
     * 客户端渲染器挂载点。1.20.1 由 {@code LargeBasinItem#initializeClient} 取用，
     * 1.21.1 由 {@code ClientModEvents#onRegisterClientExtensions} 注册。
     */
    public static final IClientItemExtensions CLIENT_EXTENSION = new IClientItemExtensions()
    {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer()
        {
            return LargeBasinItemRenderer.get();
        }
    };

    /**
     * 物品栏图标模型。它不在方块/物品的默认取模型路径上，得先由
     * {@code ClientModEvents#onRegisterAdditionalModels} 登记才会被烘焙。
     */
    //? if <1.20.2 {
    /*private static final ResourceLocation ICON_MODEL = DdIds.of(NortheastChinaDelight.MODID, "item/large_basin_icon");
    *///?} else {
    private static final ModelResourceLocation ICON_MODEL =
            ModelResourceLocation.standalone(DdIds.of(NortheastChinaDelight.MODID, "item/large_basin_icon"));
    //?}

    private static LargeBasinItemRenderer instance;

    private LargeBasinItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet)
    {
        super(dispatcher, modelSet);
    }

    /** 懒建：这个渲染器要等客户端起来才能造，不能放静态初始化。 */
    public static LargeBasinItemRenderer get()
    {
        if (instance == null)
        {
            instance = new LargeBasinItemRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels());
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay)
    {
        if (displayContext == ItemDisplayContext.GUI)
        {
            // 物品栏 / 配方书 / JEI：普通 2D 贴图，跟没有自定义渲染器的物品一模一样（GUI 里原版给的就是满亮）
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                    poseStack.last(),
                    buffer.getBuffer(Sheets.translucentItemSheet()),
                    null,
                    Minecraft.getInstance().getModelManager().getModel(ICON_MODEL),
                    1.0F, 1.0F, 1.0F,
                    15728880,
                    packedOverlay);
        }
        else
        {
            // 手 / 地面 / 展示框 / 头顶：画空盆方块
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                    ModBlocks.LARGE_BASIN.get().defaultBlockState(),
                    poseStack, buffer, packedLight, packedOverlay);
        }
    }
}
