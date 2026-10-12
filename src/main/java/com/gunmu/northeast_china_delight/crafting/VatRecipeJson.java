package com.gunmu.northeast_china_delight.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.gunmu.northeast_china_delight.util.DdIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Predicate;

public final class VatRecipeJson {

    private VatRecipeJson() {
    }

    @Nullable
    public static VatRecipe decode(@Nullable ResourceLocation id, JsonObject config) {
        if (config.has("disabled") && config.get("disabled").getAsBoolean()) {
            return null;
        }
        VatRecipes.Kind kind = enumValue(VatRecipes.Kind.class, GsonHelper.getAsString(config, "kind"));
        if (kind == VatRecipes.Kind.NONE) {
            throw new JsonParseException("大缸配方不能使用 Kind.NONE");
        }
        VatRecipe.Builder builder = VatRecipe.of(kind, id == null ? "unbound" : id.toString())
                .priority(GsonHelper.getAsInt(config, "priority", 0))
                .seal(enumValue(VatRecipe.Seal.class, GsonHelper.getAsString(config, "seal")))
                .seconds(GsonHelper.getAsInt(config, "seconds"));
        if (GsonHelper.getAsBoolean(config, "seconds_per_layer", false)) {
            builder.perLayerSeconds();
        }
        if (GsonHelper.getAsBoolean(config, "requires_snowy_biome", false)) {
            builder.requiresSnowyBiome();
        }
        if (config.has("required_seal_item")) {
            builder.requiredSealItem(ingredient(config.get("required_seal_item")));
        }
        if (config.has("biome_seconds")) {
            JsonObject multipliers = GsonHelper.getAsJsonObject(config, "biome_seconds");
            builder.biomeSeconds(map -> {
                for (String key : multipliers.keySet()) {
                    float multiplier = GsonHelper.getAsFloat(multipliers, key);
                    if (!Float.isFinite(multiplier) || multiplier <= 0) {
                        throw new JsonParseException("无效的群系时长系数：" + multiplier);
                    }
                    map.put(enumValue(VatRecipe.BiomeBand.class, key), multiplier);
                }
            });
        }

        JsonObject liquid = GsonHelper.getAsJsonObject(config, "liquid");
        int waterMin = GsonHelper.getAsInt(liquid, "water_min", 0);
        int waterMax = GsonHelper.getAsInt(liquid, "water_max", 0);
        if (waterMin < 0 || waterMax > 3 || waterMin > waterMax) {
            throw new JsonParseException("大缸水位必须在 0 到 3 层之间");
        }
        builder.water(waterMin, waterMax,
                enumValue(VatRecipe.LiquidAfter.class, GsonHelper.getAsString(liquid, "water_after", "KEEP")));
        if (liquid.has("sour_water_mb")) {
            int sourWaterMb = GsonHelper.getAsInt(liquid, "sour_water_mb");
            if (waterMax != 0 || sourWaterMb < 0 || sourWaterMb > 3000) {
                throw new JsonParseException("酸引水配方不能同时要求清水，且用量必须在 0 到 3000 mB 之间");
            }
            builder.sourWater(sourWaterMb,
                    enumValue(VatRecipe.LiquidAfter.class, GsonHelper.getAsString(liquid, "sour_water_after", "KEEP")));
        }
        if (liquid.has("product")) {
            VatRecipe.Fluid product = enumValue(VatRecipe.Fluid.class, GsonHelper.getAsString(liquid, "product"));
            int productMb = GsonHelper.getAsInt(liquid, "product_mb");
            int capacity = product == VatRecipe.Fluid.SOUR_WATER
                    ? VatRecipes.SOUR_WATER_MB : VatRecipes.PRODUCT_CAPACITY_MB;
            if (product == VatRecipe.Fluid.WATER || productMb <= 0
                    || (long) productMb * (GsonHelper.getAsBoolean(liquid, "product_per_layer", false)
                    ? Math.max(1, waterMax) : 1) > capacity) {
                throw new JsonParseException("大缸产物需是可存放的液体，且总量不得超过缸容量");
            }
            builder.product(product, productMb,
                    GsonHelper.getAsBoolean(liquid, "product_per_layer", false));
        }
        JsonArray slots = GsonHelper.getAsJsonArray(config, "slots");
        if (slots.isEmpty() || slots.size() > 12) {
            throw new JsonParseException("大缸配方必须有 1 到 12 个投料格");
        }
        for (int index = 0; index < slots.size(); index++) {
            builder.slot(slot(slots.get(index).getAsJsonObject(), index));
        }
        return builder.build();
    }

    private static VatRecipe.Slot slot(JsonObject json, int index) {
        Predicate<ItemStack> matcher = ingredient(json.get("ingredient"));
        VatRecipe.BoundsRule bounds = bounds(GsonHelper.getAsJsonObject(json, "bounds"), index);
        VatRecipe.Fate fate = enumValue(VatRecipe.Fate.class, GsonHelper.getAsString(json, "fate"));
        VatRecipe.Slot slot = switch (fate) {
            case KEEP -> VatRecipe.Slot.keep(matcher, bounds, converter(json));
            case CONVERT -> VatRecipe.Slot.convert(matcher, bounds, item(GsonHelper.getAsString(json, "result")));
            case CONSUME -> VatRecipe.Slot.consume(matcher, bounds);
            case ABSORB -> VatRecipe.Slot.absorb(matcher, bounds);
        };
        if (json.has("refund")) {
            slot = slot.refund(item(GsonHelper.getAsString(json, "refund")));
        }
        if (GsonHelper.getAsBoolean(json, "per_dose", false)) {
            slot = slot.dosed();
        }
        if (enumValue(VatRecipe.Layer.class, GsonHelper.getAsString(json, "layer", "INGREDIENT"))
                == VatRecipe.Layer.SEASONING) {
            slot = slot.seasoning();
        }
        return slot;
    }

    private static VatRecipe.Converter converter(JsonObject json) {
        if (json.has("converter")) {
            return switch (GsonHelper.getAsString(json, "converter")) {
                case "pickle" -> VatRecipes::convertPickle;
                case "spicy_pickle" -> VatRecipes::convertSpicyPickle;
                default -> throw new JsonParseException("未知的大缸换算：" + json.get("converter"));
            };
        }
        return json.has("result") ? VatRecipe.Converter.to(item(GsonHelper.getAsString(json, "result")))
                : VatRecipe.Converter.same();
    }

    private static VatRecipe.BoundsRule bounds(JsonObject json, int index) {
        String type = GsonHelper.getAsString(json, "type");
        if ((type.equals("same_as") || type.equals("half_of"))
                && (GsonHelper.getAsInt(json, "slot") < 0 || GsonHelper.getAsInt(json, "slot") >= index)) {
            throw new JsonParseException("数量规则只能引用前面的格子");
        }
        return switch (type) {
            case "exact" -> VatRecipe.BoundsRule.exact(GsonHelper.getAsInt(json, "count"));
            case "between" -> VatRecipe.BoundsRule.between(GsonHelper.getAsInt(json, "min"),
                    GsonHelper.getAsInt(json, "max"));
            case "per_layer" -> VatRecipe.BoundsRule.perLayer(GsonHelper.getAsInt(json, "min"),
                    GsonHelper.getAsInt(json, "max"));
            case "per_layer_exact" -> VatRecipe.BoundsRule.perLayerExact(GsonHelper.getAsInt(json, "count"));
            case "min_with_per_layer_max" -> VatRecipe.BoundsRule.minWithPerLayerMax(
                    GsonHelper.getAsInt(json, "min"), GsonHelper.getAsInt(json, "max_per_layer"));
            case "min_by_water" -> {
                JsonArray levels = GsonHelper.getAsJsonArray(json, "min_by_water");
                if (levels.size() != 4) {
                    throw new JsonParseException("min_by_water 必须列出 0、1、2、3 层水的下限");
                }
                int[] minimums = new int[4];
                for (int waterLevel = 0; waterLevel < minimums.length; waterLevel++) {
                    minimums[waterLevel] = levels.get(waterLevel).getAsInt();
                }
                yield VatRecipe.BoundsRule.minByWater(minimums, GsonHelper.getAsInt(json, "max"));
            }
            case "same_as" -> VatRecipe.BoundsRule.sameAs(GsonHelper.getAsInt(json, "slot"),
                    GsonHelper.getAsInt(json, "ratio"));
            case "half_of" -> VatRecipe.BoundsRule.halfOf(GsonHelper.getAsInt(json, "slot"),
                    GsonHelper.getAsInt(json, "max"));
            default -> throw new JsonParseException("未知的大缸数量规则：" + type);
        };
    }

    private static Predicate<ItemStack> ingredient(JsonElement json) {
        if (json == null) {
            throw new JsonParseException("大缸原料不能为空");
        }
        if (json.isJsonArray()) {
            JsonArray choices = json.getAsJsonArray();
            if (choices.isEmpty()) {
                throw new JsonParseException("大缸原料选项不能为空");
            }
            Predicate<ItemStack> match = stack -> false;
            for (JsonElement choice : choices) {
                match = match.or(ingredient(choice));
            }
            return match;
        }
        JsonObject object = json.getAsJsonObject();
        if (object.has("item")) {
            Item item = item(GsonHelper.getAsString(object, "item"));
            return stack -> stack.is(item);
        }
        if (object.has("tag")) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, DdIds.parse(GsonHelper.getAsString(object, "tag")));
            return stack -> stack.is(tag);
        }
        if (object.has("matcher")) {
            return switch (GsonHelper.getAsString(object, "matcher")) {
                case "pickle_ingredient" -> VatRecipes::isPickleIngredient;
                case "raw_pork" -> VatRecipes::isMeatInput;
                case "raw_fish" -> VatRecipes::isRawFish;
                case "soybean" -> VatRecipes::isSoybean;
                case "wheat" -> VatRecipes::isWheat;
                case "shrimp" -> VatRecipes::isShrimp;
                case "vinegar_grain" -> VatRecipes::isVinegarGrain;
                case "corn_kernels" -> VatRecipes::isCornKernels;
                case "bread" -> VatRecipes::isBread;
                case "pear" -> VatRecipes::isPear;
                default -> throw new JsonParseException("未知的大缸原料匹配器：" + object.get("matcher"));
            };
        }
        throw new JsonParseException("大缸原料需提供 item、tag 或 matcher");
    }

    private static Item item(String id) {
        ResourceLocation key = DdIds.parse(id);
        return BuiltInRegistries.ITEM.getOptional(key)
                .orElseThrow(() -> new JsonParseException("不存在的物品：" + key));
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value) {
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException("未知的 " + type.getSimpleName() + "：" + value, exception);
        }
    }
}
