package com.gunmu.northeast_china_delight.worldgen;

import com.gunmu.northeast_china_delight.NortheastChinaConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public final class VillageFarmCropRuleTest extends RuleTest {
    //? if <1.20.5 {
    public static final Codec<VillageFarmCropRuleTest> CODEC = RecordCodecBuilder.create(

    //?} else {
    /*public static final MapCodec<VillageFarmCropRuleTest> CODEC = RecordCodecBuilder.mapCodec(
    *///?}
            instance -> instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(rule -> rule.block),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(rule -> rule.probability),
                    ResourceLocation.CODEC.fieldOf("crop_item").forGetter(rule -> rule.cropItem)
            ).apply(instance, VillageFarmCropRuleTest::new));

    private final Block block;
    private final float probability;
    private final ResourceLocation cropItem;
    private final RandomBlockMatchTest blockMatch;

    public VillageFarmCropRuleTest(Block block, float probability, ResourceLocation cropItem) {
        this.block = block;
        this.probability = probability;
        this.cropItem = cropItem;
        this.blockMatch = new RandomBlockMatchTest(block, probability);
    }

    @Override
    public boolean test(BlockState state, RandomSource random) {
        return NortheastChinaConfig.isVillageFarmCropAllowed(cropItem) && blockMatch.test(state, random);
    }

    @Override
    protected RuleTestType<?> getType() {
        return ModWorldGen.VILLAGE_FARM_CROP_TEST.get();
    }
}
