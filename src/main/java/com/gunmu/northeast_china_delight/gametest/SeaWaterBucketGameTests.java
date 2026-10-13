package com.gunmu.northeast_china_delight.gametest;

import com.mojang.authlib.GameProfile;
import com.gunmu.northeast_china_delight.NortheastChinaConfig;
import com.gunmu.northeast_china_delight.NortheastChinaDelight;
import com.gunmu.northeast_china_delight.event.ModGameplayEvents;
import com.gunmu.northeast_china_delight.item.ModItems;
import com.gunmu.northeast_china_delight.item.SeaWaterBucketItem;
import com.gunmu.northeast_china_delight.item.SeaWaterBuckets;
import com.gunmu.northeast_china_delight.util.DdIds;
import com.gunmu.northeast_china_delight.util.DdNbt;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.material.Fluids;
//? if <1.20.5 {
import net.minecraft.network.FriendlyByteBuf;
//?} else {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
*///?}
//? if <1.20.2 {
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
//?} else {
/*import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
*///?}

import java.util.List;
import java.util.UUID;

@GameTestHolder(NortheastChinaDelight.MODID)
@PrefixGameTestTemplate(false)
public final class SeaWaterBucketGameTests {

    private SeaWaterBucketGameTests() {
    }

    private static Player mockPlayer(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "sea-water-test"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        return player;
    }

    private static void name(ItemStack stack, String value) {
        //? if <1.20.5 {
        stack.setHoverName(Component.literal(value));
        //?} else {
        /*stack.set(DataComponents.CUSTOM_NAME, Component.literal(value));
        *///?}
    }

    private static void biome(GameTestHelper helper, BlockPos pos, String biome) {
        String minimum = (pos.getX() - 8) + " "
                + Math.max(pos.getY() - 8, helper.getLevel().getMinBuildHeight()) + " " + (pos.getZ() - 8);
        String maximum = (pos.getX() + 8) + " "
                + Math.min(pos.getY() + 8, helper.getLevel().getMaxBuildHeight() - 1) + " " + (pos.getZ() + 8);
        helper.getLevel().getServer().getCommands().performPrefixedCommand(
                helper.getLevel().getServer().createCommandSourceStack().withLevel(helper.getLevel())
                        .withSuppressedOutput(), "fillbiome " + minimum + " " + maximum + " " + biome);
    }

    private static ItemStack collectOceanWater(GameTestHelper helper, int mode) {
        BlockPos localPos = new BlockPos(1, 2, 1);
        BlockPos pos = helper.absolutePos(localPos);
        helper.setBlock(localPos, Blocks.WATER);
        biome(helper, pos, "minecraft:ocean");
        for (int height = pos.getY() + 1; height <= helper.getLevel().getSeaLevel(); height++) {
            helper.getLevel().setBlockAndUpdate(new BlockPos(pos.getX(), height, pos.getZ()),
                    Blocks.AIR.defaultBlockState());
        }
        helper.assertTrue(SeaWaterBucketItem.isValidOceanColumn(helper.getLevel(), pos),
                "测试必须实际使用干净的海洋水柱");
        Player player = mockPlayer(helper);
        player.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        player.setXRot(90);
        player.setYRot(0);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        int previous = NortheastChinaConfig.SEA_WATER_BUCKET_MODE.get();
        NortheastChinaConfig.SEA_WATER_BUCKET_MODE.set(mode);
        try {
            //? if <1.20.2 {
            FillBucketEvent event = new FillBucketEvent(player, player.getMainHandItem(), helper.getLevel(),
                    SeaWaterBucketItem.raycastSourceFluid(helper.getLevel(), player));
            //?} else {
            /*PlayerInteractEvent.RightClickItem event =
                    new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND);
            *///?}
            ModGameplayEvents.onWaterBucketPickup(event);
            if (mode == SeaWaterBuckets.VANILLA_WATER_BUCKET) {
                helper.assertTrue(!event.isCanceled() && helper.getLevel().getFluidState(pos).getType() == Fluids.WATER,
                        "模式 3 不能取消事件或先移除水源");
                //? if <1.20.2 {
                helper.assertTrue(event.getResult() == Event.Result.DEFAULT,
                        "模式 3 不能覆盖其他模组或原版的取水结果");
                //?}
                return Items.BUCKET.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getObject();
            }
            //? if <1.20.2 {
            helper.assertTrue(event.getResult() == Event.Result.ALLOW, "应使用 Forge 的取水事件结果");
            return event.getFilledBucket().copy();
            //?} else {
            /*helper.assertTrue(event.isCanceled(), "模式 1 和 2 应由专用海洋取水逻辑处理");
            return player.getMainHandItem().copy();
            *///?}
        } finally {
            NortheastChinaConfig.SEA_WATER_BUCKET_MODE.set(previous);
        }
    }

    @GameTest(template = "empty", batch = "sea_water_pickup")
    public static void seaWaterModeOneCollectsExistingBucket(GameTestHelper helper) {
        helper.assertTrue(collectOceanWater(helper, SeaWaterBuckets.SEPARATE_BUCKET)
                .is(ModItems.SEA_WATER_BUCKET.get()), "模式 1 应保持原来的海水桶物品");
        helper.succeed();
    }

    private static AbstractFurnaceBlockEntity smoke(GameTestHelper helper, ItemStack input,
                                                   ItemStack fuel, int ticks) {
        return smoke(helper, input, fuel, ItemStack.EMPTY, ticks);
    }

    private static AbstractFurnaceBlockEntity smoke(GameTestHelper helper, ItemStack input,
                                                   ItemStack fuel, ItemStack output, int ticks) {
        BlockPos localPos = new BlockPos(1, 2, 1);
        BlockPos absolutePos = helper.absolutePos(localPos);
        helper.setBlock(localPos, Blocks.AIR);
        helper.setBlock(localPos, Blocks.SMOKER);
        AbstractFurnaceBlockEntity smoker = (AbstractFurnaceBlockEntity) helper.getBlockEntity(localPos);
        smoker.setItem(0, input);
        smoker.setItem(1, fuel);
        smoker.setItem(2, output);
        for (int tickIndex = 0; tickIndex < ticks; tickIndex++) {
            AbstractFurnaceBlockEntity.serverTick(helper.getLevel(), absolutePos,
                    helper.getLevel().getBlockState(absolutePos), smoker);
        }
        return smoker;
    }

    private static void assertSmokedSaltAndBucket(GameTestHelper helper, ItemStack input) {
        AbstractFurnaceBlockEntity smoker = smoke(helper, input, new ItemStack(Items.COAL), 120);
        helper.assertTrue(smoker.getItem(2).is(ModItems.SALT.get()) && smoker.getItem(2).getCount() == 1,
                "烟熏炉必须先实际完成一份制盐配方");
        helper.assertTrue(smoker.getItem(0).is(Items.BUCKET) && smoker.getItem(0).getCount() == 1,
                "烟熏炉完成制盐后必须在原料格返还一个空桶");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seaWaterSmokingReturnsSeparateBucket(GameTestHelper helper) {
        assertSmokedSaltAndBucket(helper, new ItemStack(ModItems.SEA_WATER_BUCKET.get()));
    }

    @GameTest(template = "empty")
    public static void seaWaterSmokingReturnsMarkedBucket(GameTestHelper helper) {
        assertSmokedSaltAndBucket(helper, SeaWaterBuckets.fromOceanPickup(new ItemStack(Items.WATER_BUCKET),
                SeaWaterBuckets.MARKED_WATER_BUCKET));
    }

    @GameTest(template = "empty")
    public static void seaWaterContainersDeclareEmptyBucket(GameTestHelper helper) {
        helper.assertTrue(new ItemStack(ModItems.SEA_WATER_BUCKET.get()).getCraftingRemainingItem().is(Items.BUCKET)
                && new ItemStack(Items.WATER_BUCKET).getCraftingRemainingItem().is(Items.BUCKET),
                "海水桶和原版水桶都必须已声明空桶容器余物");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seaWaterSmokingWithoutFuelKeepsFilledBucket(GameTestHelper helper) {
        AbstractFurnaceBlockEntity smoker = smoke(helper,
                SeaWaterBuckets.fromOceanPickup(new ItemStack(Items.WATER_BUCKET),
                        SeaWaterBuckets.MARKED_WATER_BUCKET), ItemStack.EMPTY, 120);
        helper.assertTrue(SeaWaterBuckets.isMarkedWaterBucket(smoker.getItem(0))
                && smoker.getItem(0).getCount() == 1 && smoker.getItem(2).isEmpty(),
                "没有燃料时不能消耗海水、产盐或提前返还空桶");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seaWaterSmokingFullOutputKeepsFilledBucket(GameTestHelper helper) {
        AbstractFurnaceBlockEntity smoker = smoke(helper, new ItemStack(ModItems.SEA_WATER_BUCKET.get()),
                new ItemStack(Items.COAL), new ItemStack(ModItems.SALT.get(), 64), 120);
        helper.assertTrue(smoker.getItem(0).is(ModItems.SEA_WATER_BUCKET.get())
                && smoker.getItem(0).getCount() == 1 && smoker.getItem(2).getCount() == 64,
                "产物格满时不能消耗海水或提前返还空桶");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seaWaterSmokingLavaFuelReturnsBothBuckets(GameTestHelper helper) {
        AbstractFurnaceBlockEntity smoker = smoke(helper, new ItemStack(ModItems.SEA_WATER_BUCKET.get()),
                new ItemStack(Items.LAVA_BUCKET), 120);
        helper.assertTrue(smoker.getItem(2).is(ModItems.SALT.get()) && smoker.getItem(2).getCount() == 1
                && smoker.getItem(0).is(Items.BUCKET) && smoker.getItem(0).getCount() == 1
                && smoker.getItem(1).is(Items.BUCKET) && smoker.getItem(1).getCount() == 1,
                "海水原料桶与熔岩燃料桶都要返还，不能互相覆盖");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seaWaterSmokingDoesNotDuplicateBucket(GameTestHelper helper) {
        AbstractFurnaceBlockEntity smoker = smoke(helper,
                SeaWaterBuckets.fromOceanPickup(new ItemStack(Items.WATER_BUCKET),
                        SeaWaterBuckets.MARKED_WATER_BUCKET), new ItemStack(Items.COAL), 420);
        helper.assertTrue(smoker.getItem(0).is(Items.BUCKET) && smoker.getItem(0).getCount() == 1
                && smoker.getItem(2).is(ModItems.SALT.get()) && smoker.getItem(2).getCount() == 1,
                "继续燃烧时不能重复生成空桶或额外的盐");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seaWaterSmokingLeavesOrdinaryRecipesUntouched(GameTestHelper helper) {
        AbstractFurnaceBlockEntity smoker = smoke(helper, new ItemStack(Items.BEEF),
                new ItemStack(Items.COAL), 120);
        helper.assertTrue(smoker.getItem(2).is(Items.COOKED_BEEF) && smoker.getItem(2).getCount() == 1
                && smoker.getItem(0).isEmpty(), "普通烧炼配方不能凭空生成空桶");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seaWaterSmokingRejectsUnmarkedWater(GameTestHelper helper) {
        AbstractFurnaceBlockEntity smoker = smoke(helper, new ItemStack(Items.WATER_BUCKET),
                new ItemStack(Items.COAL), 120);
        helper.assertTrue(smoker.getItem(0).is(Items.WATER_BUCKET) && smoker.getItem(0).getCount() == 1
                && smoker.getItem(2).isEmpty(), "普通淡水不能产盐，也不能被替换成空桶");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "sea_water_pickup")
    public static void seaWaterModeTwoCollectsMarkedVanillaWater(GameTestHelper helper) {
        ItemStack bucket = collectOceanWater(helper, SeaWaterBuckets.MARKED_WATER_BUCKET);
        helper.assertTrue(bucket.is(Items.WATER_BUCKET) && SeaWaterBuckets.isMarkedWaterBucket(bucket),
                "模式 2 必须获得带海水标记的原版水桶，而不是新的桶物品");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "sea_water_pickup")
    public static void seaWaterModeThreeLeavesVanillaCollectionUntouched(GameTestHelper helper) {
        ItemStack bucket = collectOceanWater(helper, SeaWaterBuckets.VANILLA_WATER_BUCKET);
        helper.assertTrue(bucket.is(Items.WATER_BUCKET) && !SeaWaterBuckets.isMarkedWaterBucket(bucket),
                "模式 3 应让海洋取水也得到未标记的普通水桶");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seaWaterMarkPreservesOtherItemData(GameTestHelper helper) {
        ItemStack input = new ItemStack(Items.WATER_BUCKET);
        name(input, "another_mod_water");
        ItemStack marked = SeaWaterBuckets.fromOceanPickup(input, SeaWaterBuckets.MARKED_WATER_BUCKET);
        helper.assertTrue(SeaWaterBuckets.isMarkedWaterBucket(marked) && !SeaWaterBuckets.isMarkedWaterBucket(input)
                && marked.getHoverName().getString().equals("another_mod_water")
                && Ingredient.of(Items.WATER_BUCKET).test(marked),
                "海水标记不能擦掉其他物品数据、修改原堆叠或妨碍普通水桶原料匹配");
        helper.assertTrue(SeaWaterBuckets.fromOceanPickup(new ItemStack(Items.MILK_BUCKET),
                SeaWaterBuckets.MARKED_WATER_BUCKET).is(Items.MILK_BUCKET),
                "不能把其他模组的非普通水桶容器强行改成水桶");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seaWaterMarkedBucketPlacesNormalWaterOnLand(GameTestHelper helper) {
        BlockPos localPos = new BlockPos(1, 2, 1);
        BlockPos pos = helper.absolutePos(localPos);
        helper.setBlock(localPos, Blocks.AIR);
        biome(helper, pos, "minecraft:plains");
        ItemStack marked = SeaWaterBuckets.fromOceanPickup(new ItemStack(Items.WATER_BUCKET),
                SeaWaterBuckets.MARKED_WATER_BUCKET);
        helper.assertTrue(!SeaWaterBucketItem.isValidOceanColumn(helper.getLevel(), pos)
                && ((BucketItem) marked.getItem()).emptyContents(null, helper.getLevel(), pos, null, marked)
                && helper.getLevel().getFluidState(pos).getType() == Fluids.WATER,
                "带海水标记的原版水桶必须能像普通水桶一样在陆地放水");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seaWaterSaltRecipesAcceptOnlyMarkedWater(GameTestHelper helper) {
        ItemStack marked = SeaWaterBuckets.fromOceanPickup(new ItemStack(Items.WATER_BUCKET),
                SeaWaterBuckets.MARKED_WATER_BUCKET);
        name(marked, "other_mod_name");
        for (String path : List.of("cooking/salt_from_sea_water_bucket", "smoking/salt_from_sea_water_bucket")) {
            var entry = helper.getLevel().getRecipeManager().byKey(DdIds.of(NortheastChinaDelight.MODID, path))
                    .orElseThrow();
            //? if <1.20.5 {
            Ingredient ingredient = entry.getIngredients().get(0);
            //?} else {
            /*Ingredient ingredient = entry.value().getIngredients().get(0);
            *///?}
            helper.assertTrue(ingredient.test(marked)
                    && ingredient.test(new ItemStack(ModItems.SEA_WATER_BUCKET.get()))
                    && !ingredient.test(new ItemStack(Items.WATER_BUCKET)),
                    "制盐配方必须接受海水桶或带海水标记的水桶，但不能接受普通淡水：" + path);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seaWaterFalseMarkerCannotMakeSalt(GameTestHelper helper) {
        ItemStack bucket = new ItemStack(Items.WATER_BUCKET);
        //? if <1.20.5 {
        bucket.getOrCreateTag().putBoolean(SeaWaterBuckets.MARKER_KEY, false);
        //?} else {
        /*bucket.set(SeaWaterBuckets.SEA_WATER.get(), false);
        *///?}
        helper.assertTrue(!SeaWaterBuckets.isMarkedWaterBucket(bucket), "只有 true 标记才是海水");
        var entry = helper.getLevel().getRecipeManager().byKey(
                DdIds.of(NortheastChinaDelight.MODID, "smoking/salt_from_sea_water_bucket")).orElseThrow();
        //? if <1.20.5 {
        helper.assertTrue(!entry.getIngredients().get(0).test(bucket), "false 标记不能用于制盐");
        //?} else {
        /*helper.assertTrue(!entry.value().getIngredients().get(0).test(bucket), "false 标记不能用于制盐");
        *///?}
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seaWaterMarkerSurvivesSavingAndNetworkSync(GameTestHelper helper) {
        ItemStack source = SeaWaterBuckets.fromOceanPickup(new ItemStack(Items.WATER_BUCKET),
                SeaWaterBuckets.MARKED_WATER_BUCKET);
        ItemStack saved = DdNbt.parseItem((CompoundTag) DdNbt.saveItem(source,
                helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
        helper.assertTrue(SeaWaterBuckets.isMarkedWaterBucket(saved), "存档读写必须保留海水标记");
        //? if <1.20.5 {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        //?} else {
        /*RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(),
                helper.getLevel().registryAccess());
        *///?}
        try {
            //? if <1.20.5 {
            buffer.writeItem(source);
            ItemStack synced = buffer.readItem();
            //?} else {
            /*ItemStack.STREAM_CODEC.encode(buffer, source);
            ItemStack synced = ItemStack.STREAM_CODEC.decode(buffer);
            *///?}
            helper.assertTrue(SeaWaterBuckets.isMarkedWaterBucket(synced), "网络同步必须保留海水标记");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }
}
