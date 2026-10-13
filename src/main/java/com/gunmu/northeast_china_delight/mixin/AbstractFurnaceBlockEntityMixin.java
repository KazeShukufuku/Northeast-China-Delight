package com.gunmu.northeast_china_delight.mixin;

import com.gunmu.northeast_china_delight.item.ModItems;
import com.gunmu.northeast_china_delight.item.SeaWaterBuckets;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {

    @WrapOperation(method = "burn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    //? if <1.20.5 {
    /*private void northeast$returnSeaWaterBucket(ItemStack input, int amount,
    *///?} else {
    private static void northeast$returnSeaWaterBucket(ItemStack input, int amount,
    //?}
            Operation<Void> original, @Local(argsOnly = true) NonNullList<ItemStack> inventory) {
        boolean returnsBucket = input.is(ModItems.SEA_WATER_BUCKET.get())
                || SeaWaterBuckets.isMarkedWaterBucket(input);
        original.call(input, amount);
        if (returnsBucket && input.isEmpty() && inventory.get(0).isEmpty()) {
            inventory.set(0, new ItemStack(Items.BUCKET));
        }
    }
}
