package com.xioyim.jeicommands;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

/**
 * Client-only class that calls the JEI API to open recipe/usage/effect views.
 * Called from {@link PacketOpenJei#handle} on the client game thread.
 *
 * Must never be class-loaded on a dedicated server — guarded by
 * {@link OnlyIn} and {@link net.minecraftforge.fml.DistExecutor} in the
 * packet handler.
 */
@OnlyIn(Dist.CLIENT)
public class ClientCommandHelper {

    public static void execute(PacketOpenJei.Action action, ResourceLocation id) {
        switch (action) {
            case RECIPE -> openItemRecipe(id);
            case USAGE  -> openItemUsage(id);
            case EFFECT -> openEffect(id);
        }
    }

    // -------------------------------------------------------------------------
    // /jeiitemr — OUTPUT focus: show all ways to obtain the item (R key)
    // -------------------------------------------------------------------------
    private static void openItemRecipe(ResourceLocation itemId) {
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null || item == Items.AIR) return;

        IJeiRuntime runtime = JeiCommandsPlugin.runtime;
        if (runtime == null) return;

        IFocus<ItemStack> focus = runtime.getJeiHelpers().getFocusFactory()
                .createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, new ItemStack(item));
        runtime.getRecipesGui().show(focus);
    }

    // -------------------------------------------------------------------------
    // /jeiitemu — INPUT + CATALYST focus: show where the item is used (U key)
    //   INPUT first  → crafting recipes that consume this item as ingredient
    //                   e.g. furnace → shows "furnace used to craft blast furnace"
    //   CATALYST second → processing recipes where item acts as the workstation
    //                   e.g. stonecutter → shows stonecutter cutting recipes
    // -------------------------------------------------------------------------
    private static void openItemUsage(ResourceLocation itemId) {
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null || item == Items.AIR) return;

        IJeiRuntime runtime = JeiCommandsPlugin.runtime;
        if (runtime == null) return;

        ItemStack stack = new ItemStack(item);
        IFocusFactory focusFactory = runtime.getJeiHelpers().getFocusFactory();
        runtime.getRecipesGui().show(List.of(
                focusFactory.createFocus(RecipeIngredientRole.INPUT,    VanillaTypes.ITEM_STACK, stack),
                focusFactory.createFocus(RecipeIngredientRole.CATALYST, VanillaTypes.ITEM_STACK, stack)
        ));
    }

    // -------------------------------------------------------------------------
    // /jeiiteme — JEED effect description page
    //   JEED uses MobEffectInstance (not MobEffect) as its ingredient type.
    //   JEED's own onClickedEffect uses RecipeIngredientRole.INPUT.
    // -------------------------------------------------------------------------
    private static void openEffect(ResourceLocation effectId) {
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
        if (effect == null) return;

        IJeiRuntime runtime = JeiCommandsPlugin.runtime;
        if (runtime == null) return;

        try {
            Optional<IIngredientType<MobEffectInstance>> typeOpt =
                    runtime.getIngredientManager().getIngredientTypeChecked(MobEffectInstance.class);
            if (typeOpt.isPresent()) {
                IFocus<MobEffectInstance> focus = runtime.getJeiHelpers().getFocusFactory()
                        .createFocus(RecipeIngredientRole.INPUT, typeOpt.get(), new MobEffectInstance(effect));
                runtime.getRecipesGui().show(focus);
            }
        } catch (Exception e) {
            JeiCommandsMod.LOGGER.debug("Error querying JEED MobEffectInstance ingredient type", e);
        }
    }
}
