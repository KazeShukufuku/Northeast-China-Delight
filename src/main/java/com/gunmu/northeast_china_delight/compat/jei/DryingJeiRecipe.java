package com.gunmu.northeast_china_delight.compat.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record DryingJeiRecipe(ResourceLocation id, List<ItemStack> ingredient, List<ItemStack> results) {
}
