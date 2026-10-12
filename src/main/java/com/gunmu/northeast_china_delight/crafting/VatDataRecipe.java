package com.gunmu.northeast_china_delight.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
//? if <1.20.5 {
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeInput;
*///?}

public final class VatDataRecipe implements Recipe<
        //? if <1.20.5 {
        Container
        //?} else {
        /*RecipeInput
        *///?}
        > {

    private final JsonObject config;
    @Nullable
    private final VatRecipe rule;
    @Nullable
    private final ResourceLocation id;
    @Nullable
    private VatRecipe boundRule;

    private VatDataRecipe(JsonObject config, @Nullable ResourceLocation id) {
        this.config = config.deepCopy();
        this.id = id;
        this.rule = VatRecipeJson.decode(id, config);
    }

    @Nullable
    public VatRecipe rule(ResourceLocation id) {
        if (this.rule == null) {
            return null;
        }
        if (this.boundRule == null || !this.boundRule.id().equals(id.toString())) {
            this.boundRule = this.rule.withId(id.toString());
        }
        return this.boundRule;
    }

    @Override
    public boolean matches(
            //? if <1.20.5 {
            Container input,
            //?} else {
            /*RecipeInput input,
            *///?}
            Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(
            //? if <1.20.5 {
            Container input, RegistryAccess registries
            //?} else {
            /*RecipeInput input, HolderLookup.Provider registries
            *///?}
    ) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(
            //? if <1.20.5 {
            RegistryAccess registries
            //?} else {
            /*HolderLookup.Provider registries
            *///?}
    ) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    //? if <1.20.5 {
    @Override
    public ResourceLocation getId() {
        return this.id;
    }
    //?}

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModVatRecipes.SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModVatRecipes.TYPE.get();
    }

    public static final class Serializer implements RecipeSerializer<VatDataRecipe> {
        //? if <1.20.5 {
        @Override
        public VatDataRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new VatDataRecipe(json.getAsJsonObject("config"), id);
        }

        @Override
        public VatDataRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new VatDataRecipe(JsonParser.parseString(buffer.readUtf()).getAsJsonObject(), id);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, VatDataRecipe recipe) {
            buffer.writeUtf(recipe.config.toString());
        }
        //?} else {
        /*private static final MapCodec<VatDataRecipe> CODEC = Codec.PASSTHROUGH.fieldOf("config")
                .xmap(dynamic -> new VatDataRecipe(
                                (JsonObject) dynamic.convert(JsonOps.INSTANCE).getValue(), null),
                        recipe -> new Dynamic<>(JsonOps.INSTANCE, recipe.config));

        private static final StreamCodec<RegistryFriendlyByteBuf, VatDataRecipe> STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> buffer.writeUtf(recipe.config.toString()),
                buffer -> new VatDataRecipe(JsonParser.parseString(buffer.readUtf()).getAsJsonObject(), null));

        @Override
        public MapCodec<VatDataRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, VatDataRecipe> streamCodec() {
            return STREAM_CODEC;
        }
        *///?}
    }
}
