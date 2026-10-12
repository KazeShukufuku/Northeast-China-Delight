package com.gunmu.northeast_china_delight.gametest;

import com.gunmu.northeast_china_delight.block.ModBlocks;
import com.gunmu.northeast_china_delight.block.VatBlockEntity;
import com.gunmu.northeast_china_delight.compat.jei.VatJeiRecipe;
import com.gunmu.northeast_china_delight.compat.jei.VatJeiRecipes;
import com.gunmu.northeast_china_delight.crafting.VatBrewing;
import com.gunmu.northeast_china_delight.crafting.ModVatRecipes;
import com.gunmu.northeast_china_delight.crafting.VatDataRecipe;
import com.gunmu.northeast_china_delight.crafting.VatRecipe;
import com.gunmu.northeast_china_delight.crafting.VatRecipes;
import com.gunmu.northeast_china_delight.crafting.VatRecipeValidator;
import com.gunmu.northeast_china_delight.fluid.ModFluids;
import com.gunmu.northeast_china_delight.item.ModItems;
import com.gunmu.northeast_china_delight.NortheastChinaDelight;
import com.gunmu.northeast_china_delight.util.DdIds;
import com.gunmu.northeast_china_delight.block.Vat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//? if <1.20.2 {
import net.minecraftforge.gametest.GameTestHolder;
//?} else {
/*import net.neoforged.neoforge.gametest.GameTestHolder;
*///?}
//? if <1.20.2 {
import net.minecraftforge.gametest.PrefixGameTestTemplate;
//?} else {
/*import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
*///?}

import java.util.List;
import java.util.ArrayList;

/**
 * 大缸的自动化回归（跑法：{@code gradlew runGameTestServer}）。
 */
@GameTestHolder(NortheastChinaDelight.MODID)
@PrefixGameTestTemplate(false)
public final class VatGameTests {

    private VatGameTests() {
    }

    private static Player mockPlayer(GameTestHelper helper, GameType gameType) {
        //? if <1.20.5 {
        return gameType == GameType.CREATIVE ? helper.makeMockPlayer() : helper.makeMockSurvivalPlayer();
        //?} else {
        /*return helper.makeMockPlayer(gameType);
        *///?}
    }

    private static final BlockPos AT = new BlockPos(1, 1, 1);

    @GameTest(template = "empty")
    public static void vatPicklesProduceThreeBucketsOfSourWater(GameTestHelper helper) {
        for (VatRecipes.Kind kind : List.of(VatRecipes.Kind.PICKLE, VatRecipes.Kind.SPICY_PICKLE)) {
            VatRecipe recipe = VatRecipes.recipeOf(kind, helper.getLevel());
            if (recipe == null || recipe.liquid().product() != VatRecipe.Fluid.SOUR_WATER
                    || recipe.productMbFor(3) != 3 * VatRecipes.WATER_MB_PER_LEVEL) {
                helper.fail("Missing or invalid sour water recipe: " + kind);
            }
            VatBlockEntity vat = placeVat(helper);
            vat.setRecipe(recipe);
            if (vat.getTankCapacity(0) != VatRecipes.SOUR_WATER_MB) {
                helper.fail("Sour water tank is smaller than its recipe output: " + kind);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vatDisabledRecipeReturnsItsIngredients(GameTestHelper helper) {
        VatBlockEntity vat = placeVat(helper);
        vat.setRecipe(VatRecipe.of(VatRecipes.Kind.SPICY_PICKLE,
                "northeast_china_delight:vat/disabled_for_test").build());
        vat.addContent(new ItemStack(ModItems.CHILI_SAUCE.get()));
        List<ItemStack> refunds = new ArrayList<>();
        ItemStack taken = VatBrewing.takeOne(vat, refunds);
        if (!taken.is(ModItems.CHILI_SAUCE.get()) || !refunds.isEmpty()) {
            helper.fail("Disabled vat recipe should return raw inputs rather than absorb them");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vatDisabledRecipeCanBeEmptiedWithoutBreaking(GameTestHelper helper) {
        Player player = mockPlayer(helper, GameType.CREATIVE);
        VatBlockEntity vat = placeVat(helper);
        vat.setRecipe(VatRecipe.of(VatRecipes.Kind.SPICY_PICKLE,
                "northeast_china_delight:vat/disabled_for_test").build());
        vat.addContent(new ItemStack(ModItems.CHILI_SAUCE.get()));
        helper.getLevel().setBlock(vat.getBlockPos(), vat.getBlockState().setValue(Vat.PROGRESS, 1), 3);
        click(helper, player, ItemStack.EMPTY);
        if (vat.countOf(ModItems.CHILI_SAUCE.get()) != 0 || vat.recipeId() != null) {
            helper.fail("An unfinished disabled vat recipe must return its inputs on an empty-hand click");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vatDisabledRecipeStopsBeforeFirstTick(GameTestHelper helper) {
        VatBlockEntity vat = placeVat(helper);
        vat.addWater(3 * VatRecipes.WATER_MB_PER_LEVEL);
        for (int index = 0; index < VatRecipes.PASTE_CHUNKS; index++) {
            vat.addContent(new ItemStack(ModItems.SOY_PASTE_CHUNK.get()));
        }
        for (int index = 0; index < VatRecipes.PASTE_SALT; index++) {
            vat.addContent(new ItemStack(ModItems.SALT.get()));
        }
        vat.setCover(new ItemStack(Items.WHITE_CARPET));
        vat.setRecipe(VatRecipe.of(VatRecipes.Kind.PASTE,
                "northeast_china_delight:vat/disabled_for_test").build());
        if (vat.getBlockState().getValue(Vat.PROGRESS) != 0 || VatBrewing.ready(vat) != null
                || VatBrewing.current(vat) != null
                || VatBrewing.forInsert(vat, new ItemStack(Items.WHEAT)) != null
                || VatBrewing.sealRecipe(vat, new ItemStack(Items.WHITE_CARPET)) != null) {
            helper.fail("Disabled vat recipe must not switch to another recipe before the first tick");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vatDisabledRecipeRejectsNewLiquids(GameTestHelper helper) {
        VatBlockEntity vat = placeVat(helper);
        vat.setRecipe(VatRecipe.of(VatRecipes.Kind.PICKLE,
                "northeast_china_delight:vat/disabled_for_test").build());
        if (!vat.hasMissingUnfinishedRecipe()) {
            helper.fail("Disabled vat recipe should not accept water or sour water");
        }
        vat.setKind(VatRecipes.Kind.PICKLE);
        if (vat.hasMissingUnfinishedRecipe()) {
            helper.fail("Unbound vat should still accept water or sour water");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vatDisabledRecipeKeepsItsFinishedFluid(GameTestHelper helper) {
        VatBlockEntity vat = placeVat(helper);
        helper.getLevel().setBlock(vat.getBlockPos(), vat.getBlockState().setValue(Vat.FERMENTED, true), 3);
        vat.setRecipe(VatRecipe.of(VatRecipes.Kind.VINEGAR,
                "northeast_china_delight:vat/disabled_for_test")
                .product(VatRecipe.Fluid.WHITE_VINEGAR, VatRecipes.SERVING_MB, false).build());
        vat.setProductMb(VatRecipe.Fluid.WHITE_VINEGAR, VatRecipes.SERVING_MB);
        if (vat.getFluidInTank(0).getFluid() != ModFluids.whiteVinegarSource()) {
            helper.fail("Disabled vat recipe should keep its brewed fluid accessible");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vatRecipesPassSelfCheck(GameTestHelper helper) {
        List<String> problems = VatRecipeValidator.check(VatRecipes.all(helper.getLevel()), true);
        if (!problems.isEmpty()) {
            helper.fail("配方自检没过：" + problems);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vatDataPackIdsOverrideAndDisable(GameTestHelper helper) {
        ResourceLocation id = DdIds.of(NortheastChinaDelight.MODID, "vat/bean_sprouts");
        //? if <1.20.5 {
        VatDataRecipe source = (VatDataRecipe) helper.getLevel().getRecipeManager().byKey(id)
                .orElseThrow();
        //?} else {
        /*VatDataRecipe source = (VatDataRecipe) helper.getLevel().getRecipeManager().byKey(id)
                .orElseThrow().value();
        *///?}
        JsonObject override = JsonParser.parseString("""
                {"type":"northeast_china_delight:vat", "config":{
                  "kind":"BEAN_SPROUTS", "priority":20, "seal":"CLOTH", "seconds":7,
                  "liquid":{"water_min":1,"water_max":1,"water_after":"CLEAR"},
                  "slots":[{"ingredient":{"item":"northeast_china_delight:soybean"},
                    "bounds":{"type":"exact","count":1},"fate":"CONVERT",
                    "result":"northeast_china_delight:bean_sprouts"}]
                }}
                """).getAsJsonObject();
        //? if <1.20.5 {
        VatDataRecipe changed = ModVatRecipes.SERIALIZER.get().fromJson(id, override);
        //?} else {
        /*VatDataRecipe changed = ModVatRecipes.SERIALIZER.get().codec()
                .codec().parse(com.mojang.serialization.JsonOps.INSTANCE, override).getOrThrow();
        *///?}
        if (source.rule(id) == null || source.rule(id).seconds() != VatRecipes.BEAN_SPROUTS_SECONDS
                || !source.rule(id).id().equals(id.toString())
                || changed.rule(id) == null || changed.rule(id).seconds() != 7) {
            helper.fail("数据包大缸配方的路径应成为稳定 ID");
        }
        JsonObject disabled = override.deepCopy();
        disabled.getAsJsonObject("config").addProperty("disabled", true);
        //? if <1.20.5 {
        VatDataRecipe removed = ModVatRecipes.SERIALIZER.get().fromJson(id, disabled);
        //?} else {
        /*VatDataRecipe removed = ModVatRecipes.SERIALIZER.get().codec()
                .codec().parse(com.mojang.serialization.JsonOps.INSTANCE, disabled).getOrThrow();
        *///?}
        if (removed.rule(id) != null) {
            helper.fail("被数据包禁用的大缸配方不应继续提供规则");
        }
        helper.succeed();
    }

    /** 展示的份数必须满足配方本身：咸腊肉/咸鱼是"盐数 = 肉数"，不是 1 份盐 */
    @GameTest(template = "empty")
    public static void vatJeiCountsMatchRecipe(GameTestHelper helper) {
        checkSaltFollowsMainIngredient(helper, VatRecipes.Kind.MEAT, "咸腊肉");
        checkSaltFollowsMainIngredient(helper, VatRecipes.Kind.SALTED_FISH, "咸鱼");
        helper.succeed();
    }

    private static void checkSaltFollowsMainIngredient(GameTestHelper helper, VatRecipes.Kind kind, String name) {
        VatRecipe recipe = VatRecipes.recipeOf(kind, helper.getLevel());
        int[] counts = recipe.displayedCounts(0);
        if (counts.length != 2 || counts[0] != counts[1]) {
            helper.fail(name + "展示的份数不对（主料 " + counts[0] + " / 盐 " + counts[1] + "）");
        }
    }

    @GameTest(template = "empty")
    public static void vatJeiPagesAreGenerated(GameTestHelper helper) {
        for (VatRecipe recipe : VatRecipes.all(helper.getLevel())) {
            List<VatJeiRecipe> pages = VatJeiRecipes.pagesFor(recipe);
            if (pages.size() != 1) {
                helper.fail("配方 " + recipe.id() + " 在 JEI 里应该正好一页，实际 " + pages.size() + " 页");
            }
            for (VatJeiRecipe page : pages) {
                for (VatJeiRecipe.State state : page.states()) {
                    if (state.results().isEmpty() && !state.hasResultFluid()) {
                        helper.fail("配方 " + recipe.id() + " 有一页什么都不产（投料画了、产物没画）");
                    }
                }
            }
            checkPageFollowsIngredient(helper, recipe, pages, VatRecipes.Kind.MEAT);
            checkPageFollowsIngredient(helper, recipe, pages, VatRecipes.Kind.SALTED_FISH);
        }
        helper.succeed();
    }

    /** 抽查"盐数 = 主料数"：JEI 页面上主料格与盐格的份数必须相等 */
    private static void checkPageFollowsIngredient(GameTestHelper helper, VatRecipe recipe,
                                                   List<VatJeiRecipe> pages, VatRecipes.Kind kind) {
        if (recipe.kind() != kind) {
            return;
        }
        for (VatJeiRecipe page : pages) {
            for (VatJeiRecipe.State state : page.states()) {
                int main = shownCount(state.primary());
                int salt = shownCount(state.seasoning());
                if (main < 0 || salt != main) {
                    helper.fail("配方 " + recipe.id() + " 的 JEI 页面盐数(" + salt + ") 和主料(" + main + ") 不一样");
                }
            }
        }
    }

    private static int shownCount(List<List<ItemStack>> layer) {
        if (layer.isEmpty() || layer.get(0).isEmpty()) {
            return -1;
        }
        return layer.get(0).get(0).getCount();
    }

    @GameTest(template = "empty")
    public static void finishedPasteCanBrewVinegar(GameTestHelper helper) {
        Player player = mockPlayer(helper, GameType.CREATIVE);
        VatBlockEntity vat = placeVat(helper);
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(AT);

        vat.addWater(3 * VatRecipes.WATER_MB_PER_LEVEL);
        for (int i = 0; i < VatRecipes.PASTE_CHUNKS; i++) {
            vat.addContent(new ItemStack(ModItems.SOY_PASTE_CHUNK.get()));
        }
        for (int i = 0; i < VatRecipes.PASTE_SALT; i++) {
            vat.addContent(new ItemStack(ModItems.SALT.get()));
        }
        vat.setKind(VatRecipes.Kind.PASTE);
        vat.setCover(new ItemStack(Items.WHITE_CARPET));
        VatBrewing.complete(level, pos, vat, VatRecipes.recipeOf(VatRecipes.Kind.PASTE, level));
        if (vat.residueCount() != VatRecipes.RESIDUE_COUNT) {
            helper.fail("大酱酿完缸里应该有 " + VatRecipes.RESIDUE_COUNT + " 份酱渣，实际 " + vat.residueCount());
        }
        vat.setProductMb(VatRecipe.Fluid.PASTE, 0);
        vat.takeCover();

        if (VatBrewing.forInsert(vat, new ItemStack(ModItems.CORN_SEEDS.get())) == null) {
            helper.fail("酿完大酱的缸加不进谷物：二次发酵被 forInsert 挡掉了");
        }

        // 醋要一缸水：3 层，一桶一层
        for (int i = 0; i < 3; i++) {
            click(helper, player, new ItemStack(Items.WATER_BUCKET));
        }
        for (int i = 0; i < VatRecipes.VINEGAR_GRAIN_COUNT; i++) {
            click(helper, player, new ItemStack(ModItems.CORN_SEEDS.get()));
        }
        Item cloth = clothRug();
        if (cloth == null) {
            helper.succeed();
            return;
        }
        click(helper, player, new ItemStack(cloth));

        if (!vat.isCovered() || !vat.cover().is(cloth)) {
            helper.fail("酱渣缸没有接收粗布毯");
        }
        VatRecipe vinegarRecipe = VatBrewing.ready(vat);
        if (vinegarRecipe == null || vinegarRecipe.kind() != VatRecipes.Kind.VINEGAR
                || vat.kind() != VatRecipes.Kind.VINEGAR) {
            helper.fail("酱渣 + 谷物 + 水 + 粗布毯没有开工（配方也没认出来）");
        }
        assertScheduled(helper, level, pos);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void soySauceResidueRequiresGrainBeforeCloth(GameTestHelper helper) {
        Player player = mockPlayer(helper, GameType.CREATIVE);
        VatBlockEntity vat = placeVat(helper);
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(AT);

        vat.addWater(3 * VatRecipes.WATER_MB_PER_LEVEL);
        for (int i = 0; i < VatRecipes.PASTE_CHUNKS; i++) {
            vat.addContent(new ItemStack(ModItems.SOY_PASTE_CHUNK.get()));
        }
        for (int i = 0; i < VatRecipes.PASTE_SALT; i++) {
            vat.addContent(new ItemStack(ModItems.SALT.get()));
        }
        vat.addContent(new ItemStack(Items.WHEAT));
        vat.setKind(VatRecipes.Kind.SOY_SAUCE);
        vat.setCover(new ItemStack(Items.WHITE_CARPET));
        VatBrewing.complete(level, pos, vat, VatRecipes.recipeOf(VatRecipes.Kind.SOY_SAUCE, level));
        if (vat.residueCount() != VatRecipes.RESIDUE_COUNT) {
            helper.fail("酱油酿完酱渣数量不对");
        }
        vat.setProductMb(VatRecipe.Fluid.SOY_SAUCE, 0);
        vat.takeCover();
        for (int i = 0; i < 3; i++) {
            click(helper, player, new ItemStack(Items.WATER_BUCKET));
        }
        if (vat.waterLayers() != 3) {
            helper.fail("酱油酱渣缸未能续满三层水");
        }
        for (int i = 0; i < VatRecipes.VINEGAR_GRAIN_COUNT - 1; i++) {
            click(helper, player, new ItemStack(ModItems.CORN_SEEDS.get()));
        }
        Item cloth = clothRug();
        if (cloth == null) {
            helper.succeed();
            return;
        }
        click(helper, player, new ItemStack(cloth));
        if (vat.isCovered()) {
            helper.fail("谷物未齐时，不应盖上醋的粗布毯");
        }

        click(helper, player, new ItemStack(ModItems.CORN_SEEDS.get()));
        click(helper, player, new ItemStack(cloth));
        VatRecipe vinegarRecipe = VatBrewing.ready(vat);
        if (!vat.isCovered() || !vat.cover().is(cloth) || vat.kind() != VatRecipes.Kind.VINEGAR
                || vinegarRecipe == null || vinegarRecipe.kind() != VatRecipes.Kind.VINEGAR) {
            helper.fail("酱油酱渣补齐谷物后应盖粗布毯并开始酿醋");
        }
        assertScheduled(helper, level, pos);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pickleSourWaterCanBrewWhiteVinegar(GameTestHelper helper) {
        Player player = mockPlayer(helper, GameType.CREATIVE);
        VatBlockEntity vat = placeVat(helper);
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(AT);

        vat.addWater(3 * VatRecipes.WATER_MB_PER_LEVEL);
        for (int i = 0; i < 6; i++) {
            vat.addContent(new ItemStack(ModItems.NAPA_CABBAGE.get()));
        }
        for (int i = 0; i < 3; i++) {
            vat.addContent(new ItemStack(ModItems.SALT.get()));
        }
        vat.setKind(VatRecipes.Kind.PICKLE);
        VatBrewing.complete(level, pos, vat, VatRecipes.recipeOf(VatRecipes.Kind.PICKLE, level));
        if (vat.sourWaterMb() < VatRecipes.SOUR_WATER_MB) {
            helper.fail("泡菜腌完应该有酸引水，实际 " + vat.sourWaterMb() + " mB");
        }
        for (int i = 0; i < 6; i++) {
            VatBrewing.takeOne(vat, new java.util.ArrayList<>());
        }
        if (!vat.contents().isEmpty()) {
            helper.fail("菜没取干净，还剩 " + vat.contents().size() + " 份");
        }

        if (VatBrewing.forInsert(vat, new ItemStack(ModItems.CORN_SEEDS.get())) == null) {
            helper.fail("泡菜缸里的酸引水加不进谷物：二次发酵被挡掉了");
        }

        for (int i = 0; i < VatRecipes.VINEGAR_GRAIN_COUNT; i++) {
            click(helper, player, new ItemStack(ModItems.CORN_SEEDS.get()));
        }
        Item cloth = clothRug();
        if (cloth == null) {
            helper.succeed();
            return;
        }
        click(helper, player, new ItemStack(cloth));

        if (VatBrewing.ready(vat) == null) {
            helper.fail("酸引水 + 谷物 + 粗布毯没有开工（配方也没认出来）");
        }
        assertScheduled(helper, level, pos);
        helper.succeed();
    }

    /** 先盖地毯、后倒水：倒完水就该开工（以前加水分支不会触发开工） */
    @GameTest(template = "empty")
    public static void waterAfterSealingStartsBrew(GameTestHelper helper) {
        Player player = mockPlayer(helper, GameType.CREATIVE);
        VatBlockEntity vat = placeVat(helper);
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(AT);

        for (int i = 0; i < VatRecipes.PASTE_CHUNKS; i++) {
            vat.addContent(new ItemStack(ModItems.SOY_PASTE_CHUNK.get()));
        }
        for (int i = 0; i < VatRecipes.PASTE_SALT; i++) {
            vat.addContent(new ItemStack(ModItems.SALT.get()));
        }
        vat.setKind(VatRecipes.Kind.PASTE);
        click(helper, player, new ItemStack(Items.WHITE_CARPET));
        if (!vat.isCovered()) {
            helper.fail("羊毛地毯没能盖到缸上");
        }
        for (int i = 0; i < 3; i++) {
            click(helper, player, new ItemStack(Items.WATER_BUCKET));
        }
        if (vat.waterLayers() != 3) {
            helper.fail("三桶水应该正好三层，实际 " + vat.waterLayers() + " 层");
        }
        assertScheduled(helper, level, pos);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void coverComesBackBeforeFermented(GameTestHelper helper) {
        Player player = mockPlayer(helper, GameType.CREATIVE);
        VatBlockEntity vat = placeVat(helper);
        vat.addContent(new ItemStack(ModItems.SOY_PASTE_CHUNK.get()));
        vat.setKind(VatRecipes.Kind.PASTE);
        vat.setCover(new ItemStack(Items.WHITE_CARPET));

        click(helper, player, ItemStack.EMPTY);
        if (vat.isCovered()) {
            helper.fail("没发酵的缸取不回盖布");
        }
        helper.succeed();
    }

    private static VatBlockEntity placeVat(GameTestHelper helper) {
        helper.setBlock(AT, ModBlocks.VAT.get());
        return (VatBlockEntity) helper.getBlockEntity(AT);
    }

    private static void click(GameTestHelper helper, Player player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.useBlock(AT, player);
    }

    private static void assertScheduled(GameTestHelper helper, ServerLevel level, BlockPos pos) {
        if (!level.getBlockTicks().hasScheduledTick(pos, ModBlocks.VAT.get())) {
            helper.fail("条件齐了但没排上工序：这一缸不会自己往下走");
        }
    }

    private static Item clothRug() {
        Item rug = BuiltInRegistries.ITEM.get(
                DdIds.of("farmersdelight", "canvas_rug"));
        return rug == Items.AIR ? null : rug;
    }
}
