package com.gunmu.northeast_china_delight.item;

import com.gunmu.northeast_china_delight.NortheastChinaDelight;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
//? if <1.20.2 {
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
*///?}
//? if >=1.20.5 {
/*import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;
*///?}

public final class SeaWaterBuckets {

    public static final String MARKER_KEY = NortheastChinaDelight.MODID + ":sea_water";
    public static final int SEPARATE_BUCKET = 1;
    public static final int MARKED_WATER_BUCKET = 2;
    public static final int VANILLA_WATER_BUCKET = 3;

    //? if >=1.20.5 {
    /*private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NortheastChinaDelight.MODID);
    public static final Supplier<DataComponentType<Boolean>> SEA_WATER =
            COMPONENTS.register("sea_water", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    *///?}

    private SeaWaterBuckets() {
    }

    public static void register(IEventBus eventBus) {
        //? if >=1.20.5 {
        /*COMPONENTS.register(eventBus);
        *///?}
    }

    public static ItemStack fromOceanPickup(ItemStack picked, int mode) {
        if (picked.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (mode == SEPARATE_BUCKET) {
            return new ItemStack(ModItems.SEA_WATER_BUCKET.get());
        }
        if (mode != MARKED_WATER_BUCKET && mode != VANILLA_WATER_BUCKET) {
            throw new IllegalArgumentException("未知的海洋取水模式：" + mode);
        }
        ItemStack result = picked.copy();
        if (mode == MARKED_WATER_BUCKET && result.is(Items.WATER_BUCKET)) {
            //? if <1.20.5 {
            result.getOrCreateTag().putBoolean(MARKER_KEY, true);
            //?} else {
            /*result.set(SEA_WATER.get(), true);
            *///?}
        }
        return result;
    }

    public static boolean isMarkedWaterBucket(ItemStack stack) {
        if (!stack.is(Items.WATER_BUCKET)) {
            return false;
        }
        //? if <1.20.5 {
        return stack.hasTag() && stack.getTag().getBoolean(MARKER_KEY);
        //?} else {
        /*return Boolean.TRUE.equals(stack.get(SEA_WATER.get()));
        *///?}
    }
}
