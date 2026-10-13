package com.gunmu.northeast_china_delight.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.gunmu.northeast_china_delight.NortheastChinaDelight;
import com.gunmu.northeast_china_delight.util.DdIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
//? if <1.20.5 {
/*import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
*///?} else {
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeInput;
//?}

import java.util.ArrayList;
import java.util.List;

public final class DryingDataRecipe implements Recipe<
        //? if <1.20.5 {
        /*Container
        *///?} else {
        RecipeInput
        //?}
        > {

    private final JsonObject config;
    @Nullable
    private final ResourceLocation id;
    @Nullable
    private final DryingRecipes.Recipe rule;
    @Nullable
    private DryingRecipes.Recipe boundRule;

    private DryingDataRecipe(JsonObject config, @Nullable ResourceLocation id) {
        this.config = config.deepCopy();
        if (this.config.toString().length() > 32767) {
            throw new JsonParseException("风干配方 config 超过网络同步长度上限，请使用标签精简食材列表");
        }
        this.id = id;
        this.rule = decode(config, id);
    }

    @Nullable
    private static DryingRecipes.Recipe decode(JsonObject config, @Nullable ResourceLocation id) {
        if (GsonHelper.getAsBoolean(config, "disabled", false)) {
            return null;
        }
        JsonElement inputJson = config.get("input");
        if (inputJson == null) {
            throw new JsonParseException("风干配方缺少 input");
        }
        //? if <1.20.5 {
        /*Ingredient input = Ingredient.fromJson(inputJson);
        *///?} else {
        Ingredient input = Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, inputJson).getOrThrow();
        //?}
        if (input.isEmpty()) {
            throw new JsonParseException("风干配方的食材不能为空");
        }
        JsonArray resultJson = GsonHelper.getAsJsonArray(config, "results");
        if (resultJson.isEmpty() || resultJson.size() > DryingRecipes.MAX_RESULTS) {
            throw new JsonParseException("风干配方必须有 1 到 2 个产物");
        }
        List<ItemStack> results = new ArrayList<>();
        for (JsonElement entry : resultJson) {
            if (!entry.isJsonObject()) {
                throw new JsonParseException("风干配方产物必须是对象");
            }
            JsonObject output = entry.getAsJsonObject();
            ResourceLocation outputId = ResourceLocation.tryParse(GsonHelper.getAsString(output, "item"));
            if (outputId == null || !BuiltInRegistries.ITEM.containsKey(outputId)) {
                throw new JsonParseException("风干配方产物不存在：" + outputId);
            }
            int count = integer(output, "count", 1);
            ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(outputId), count);
            if (result.isEmpty() || count <= 0 || count > result.getMaxStackSize()) {
                throw new JsonParseException("风干配方产物数量必须为 1 到该物品堆叠上限");
            }
            results.add(result);
        }
        return new DryingRecipes.Recipe(id == null ? DdIds.of(NortheastChinaDelight.MODID, "drying/unbound") : id,
                integer(config, "priority", 0), input, results);
    }

    private static int integer(JsonObject config, String field, int fallback) {
        if (!config.has(field)) {
            return fallback;
        }
        JsonElement value = config.get(field);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException(field + " 必须是整数");
        }
        try {
            return value.getAsBigDecimal().intValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new JsonParseException(field + " 必须是有效的 32 位整数", exception);
        }
    }

    @Nullable
    public DryingRecipes.Recipe rule(ResourceLocation id) {
        if (this.rule == null) {
            return null;
        }
        if (this.boundRule == null || !this.boundRule.id().equals(id)) {
            this.boundRule = this.rule.withId(id);
        }
        return this.boundRule;
    }

    @Override
    public boolean matches(
            //? if <1.20.5 {
            /*Container input,
            *///?} else {
            RecipeInput input,
            //?}
            Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(
            //? if <1.20.5 {
            /*Container input, RegistryAccess registries
            *///?} else {
            RecipeInput input, HolderLookup.Provider registries
            //?}
    ) {
        return getResultItem(registries);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(
            //? if <1.20.5 {
            /*RegistryAccess registries
            *///?} else {
            HolderLookup.Provider registries
            //?}
    ) {
        return this.rule == null ? ItemStack.EMPTY : this.rule.results().get(0).copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        if (this.rule != null) {
            ingredients.add(this.rule.input());
        }
        return ingredients;
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
    /*@Override
    public ResourceLocation getId() {
        return this.id;
    }
    *///?}

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModDryingRecipes.SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModDryingRecipes.TYPE.get();
    }

    public static final class Serializer implements RecipeSerializer<DryingDataRecipe> {
        //? if <1.20.5 {
        /*@Override
        public DryingDataRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new DryingDataRecipe(GsonHelper.getAsJsonObject(json, "config"), id);
        }

        @Override
        public DryingDataRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new DryingDataRecipe(JsonParser.parseString(buffer.readUtf()).getAsJsonObject(), id);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DryingDataRecipe recipe) {
            buffer.writeUtf(recipe.config.toString());
        }
        *///?} else {
        private static final MapCodec<DryingDataRecipe> CODEC = Codec.PASSTHROUGH.fieldOf("config")
                .flatXmap(dynamic -> {
                    try {
                        JsonElement config = dynamic.convert(JsonOps.INSTANCE).getValue();
                        if (!config.isJsonObject()) {
                            return DataResult.error(() -> "风干配方 config 必须是对象");
                        }
                        return DataResult.success(new DryingDataRecipe(config.getAsJsonObject(), null));
                    } catch (RuntimeException exception) {
                        return DataResult.error(() -> "无效的风干配方：" + exception.getMessage());
                    }
                }, recipe -> DataResult.success(new Dynamic<>(JsonOps.INSTANCE, recipe.config)));

        private static final StreamCodec<RegistryFriendlyByteBuf, DryingDataRecipe> STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> buffer.writeUtf(recipe.config.toString()),
                buffer -> new DryingDataRecipe(JsonParser.parseString(buffer.readUtf()).getAsJsonObject(), null));

        @Override
        public MapCodec<DryingDataRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DryingDataRecipe> streamCodec() {
            return STREAM_CODEC;
        }
        //?}
    }
}
