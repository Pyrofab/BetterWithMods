package betterwithmods.module.hardcore;

import betterwithmods.BWMod;
import betterwithmods.client.gui.GuiHunger;
import betterwithmods.common.BWMItems;
import betterwithmods.common.blocks.BlockRawPastry;
import betterwithmods.module.Feature;
import betterwithmods.module.gameplay.CauldronRecipes;
import betterwithmods.module.gameplay.KilnRecipes;
import betterwithmods.util.BWMFoodStats;
import betterwithmods.util.RecipeUtils;
import betterwithmods.util.player.EntityPlayerExt;
import betterwithmods.util.player.FatPenalty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.FoodStats;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by tyler on 4/20/17.
 */
public class HCHunger extends Feature {

    /**
     * Walking speed changed according to health/exhaustion/fat
     */

    protected final static UUID penaltySpeedUUID = UUID.fromString("c5595a67-9410-4fb2-826a-bcaf432c6a6f");
    private static GuiHunger guiHunger = null;
    private static Field foodField = ReflectionHelper.findField(ItemFood.class, "healAmount", "field_77853_b");
    private static Field satField = ReflectionHelper.findField(ItemFood.class, "saturationModifier", "field_77854_c");

    static {
        foodField.setAccessible(true);
        satField.setAccessible(true);
    }

    private double jumpExhaustion;
    private boolean foodStackSize;

    public static void setDessert(ItemFood food) {
        food.setAlwaysEdible();
    }

    public static void modifySaturation(ItemFood item, float saturation) {
        try {
            satField.set(item, saturation);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void modifyFood(ItemFood item, int food) {
        try {
            foodField.set(item, food);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void modifyFoodValue(ItemFood item, int food, float saturation) {
        modifyFood(item, food);
        modifySaturation(item, saturation);
    }

    @Override
    public void setupConfig() {
        jumpExhaustion = loadPropDouble("Jump Exhaustion", "Exhaustion penalty from jumping", 0.09);
        foodStackSize = loadPropBool("Change Food Stacksize", "All Foods all stack up to 16", false);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        //MOD FOODS: makes it so when HCHunger is disabled our foods are balanced.

        modifyFood((ItemFood) BWMItems.BEEF_DINNER, 24);
        modifyFood((ItemFood) BWMItems.BEEF_POTATOES, 18);
        modifyFood((ItemFood) BWMItems.RAW_KEBAB, 19);
        modifyFood((ItemFood) BWMItems.COOKED_KEBAB, 24);
        modifyFood((ItemFood) BWMItems.CHICKEN_SOUP, 24);
        modifyFood((ItemFood) BWMItems.CHOWDER, 15);
        modifyFood((ItemFood) BWMItems.HEARTY_STEW, 30);
        modifyFood((ItemFood) Items.RABBIT_STEW, 30);
        modifyFood((ItemFood) BWMItems.PORK_DINNER, 24);
        modifyFood((ItemFood) BWMItems.RAW_EGG, 6);
        modifyFood((ItemFood) BWMItems.COOKED_EGG, 9);
        modifyFood((ItemFood) BWMItems.RAW_SCRAMBLED_EGG, 12);
        modifyFood((ItemFood) BWMItems.COOKED_SCRAMBLED_EGG, 15);
        modifyFood((ItemFood) BWMItems.RAW_OMELET, 9);
        modifyFood((ItemFood) BWMItems.COOKED_OMELET, 12);
        modifyFood((ItemFood) BWMItems.HAM_AND_EGGS, 18);
        modifyFood((ItemFood) BWMItems.TASTY_SANDWICH, 18);
        modifyFood((ItemFood) BWMItems.CREEPER_OYSTER, 6);
        modifyFood((ItemFood) BWMItems.WOLF_CHOP, 3);
        modifyFoodValue((ItemFood) BWMItems.APPLE_PIE, 15, 15);
        modifyFoodValue((ItemFood) BWMItems.CHOCOLATE, 6, 3);
        modifyFoodValue((ItemFood) BWMItems.DONUT, 3, 1.5f);
        modifyFoodValue((ItemFood) BWMItems.KIBBLE, 9, 0);
        //MEATS
        modifyFoodValue((ItemFood) Items.SPIDER_EYE, 6, 0);

        modifyFoodValue((ItemFood) Items.ROTTEN_FLESH, 9, 0);
        modifyFoodValue((ItemFood) Items.CHICKEN, 9, 0);
        modifyFoodValue((ItemFood) Items.MUTTON, 9, 0);

        modifyFoodValue((ItemFood) Items.COOKED_CHICKEN, 12, 0);
        modifyFoodValue((ItemFood) Items.COOKED_MUTTON, 12, 0);


        modifyFoodValue((ItemFood) Items.BEEF, 12, 0);
        modifyFoodValue((ItemFood) Items.PORKCHOP, 12, 0);
        modifyFoodValue((ItemFood) Items.RABBIT, 12, 0);
        modifyFoodValue((ItemFood) BWMItems.WOLF_CHOP, 12, 0);

        modifyFoodValue((ItemFood) Items.COOKED_BEEF, 15, 0);
        modifyFoodValue((ItemFood) Items.COOKED_PORKCHOP, 15, 0);
        modifyFoodValue((ItemFood) Items.COOKED_RABBIT, 15, 0);
        modifyFoodValue((ItemFood) BWMItems.COOKED_WOLF_CHOP, 15, 0);

        modifyFoodValue((ItemFood) Items.FISH, 9, 0);
        modifyFoodValue((ItemFood) Items.COOKED_FISH, 12, 0);
        //OTHER
        modifyFoodValue((ItemFood) Items.MELON, 2, 0);
        modifyFoodValue((ItemFood) Items.MUSHROOM_STEW, 15, 0);
        modifyFoodValue((ItemFood) Items.BEETROOT, 15, 0);
        modifyFoodValue((ItemFood) Items.BREAD, 12, 0);
        modifyFoodValue((ItemFood) Items.COOKIE, 3, 3);
        modifyFoodValue((ItemFood) Items.PUMPKIN_PIE, 15, 15);
        //TODO CAKE????
        modifyFoodValue((ItemFood) Items.POTATO, 3, 0);
        modifyFoodValue((ItemFood) Items.BAKED_POTATO, 6, 0);
        modifyFoodValue((ItemFood) Items.CARROT, 3, 0);
        modifyFoodValue((ItemFood) Items.BEETROOT, 3, 0);
        modifyFoodValue((ItemFood) Items.APPLE, 3, 0);
        modifyFoodValue((ItemFood) Items.GOLDEN_APPLE, 3, 0);
        modifyFoodValue((ItemFood) Items.GOLDEN_CARROT, 3, 0);

        setDessert((ItemFood) Items.COOKIE);
        setDessert((ItemFood) Items.PUMPKIN_PIE);
        setDessert((ItemFood) BWMItems.CHOCOLATE);

        RecipeUtils.removeRecipes(Items.BREAD, 0);
        RecipeUtils.removeRecipes(Items.MUSHROOM_STEW, 0);
        RecipeUtils.removeRecipes(Items.CAKE, 0);
        RecipeUtils.removeRecipes(Items.COOKIE, 0);
        RecipeUtils.removeRecipes(Items.PUMPKIN_PIE, 0);
        RecipeUtils.removeRecipes(Items.RABBIT_STEW, 0);
        RecipeUtils.removeRecipes(Items.BEETROOT_SOUP, 0);

        GameRegistry.addSmelting(BlockRawPastry.getStack(BlockRawPastry.EnumType.COOKIE), new ItemStack(Items.COOKIE, 8), 0.1F);
        GameRegistry.addSmelting(BlockRawPastry.getStack(BlockRawPastry.EnumType.PUMPKIN), new ItemStack(Items.PUMPKIN_PIE, 1), 0.1F);
        GameRegistry.addSmelting(BlockRawPastry.getStack(BlockRawPastry.EnumType.APPLE), new ItemStack(BWMItems.APPLE_PIE, 1), 0.1F);

        KilnRecipes.addKilnRecipe(BlockRawPastry.getStack(BlockRawPastry.EnumType.COOKIE), new ItemStack(Items.COOKIE, 8));
        KilnRecipes.addKilnRecipe(BlockRawPastry.getStack(BlockRawPastry.EnumType.PUMPKIN), new ItemStack(Items.PUMPKIN_PIE, 1));
        KilnRecipes.addKilnRecipe(BlockRawPastry.getStack(BlockRawPastry.EnumType.APPLE), new ItemStack(BWMItems.APPLE_PIE, 1));

        CauldronRecipes.addCauldronRecipe(new ItemStack(Items.MUSHROOM_STEW), new ItemStack(Items.BUCKET), new Object[]{new ItemStack(Blocks.BROWN_MUSHROOM, 3), new ItemStack(Items.MILK_BUCKET), new ItemStack(Items.BOWL)});
        CauldronRecipes.addCauldronRecipe(new ItemStack(Items.BEETROOT_SOUP), new Object[]{new ItemStack(Items.BEETROOT, 6), new ItemStack(Items.BOWL)});
        CauldronRecipes.addCauldronRecipe(new ItemStack(Items.RABBIT_STEW, 5), new Object[]{Items.COOKED_RABBIT, Items.CARROT, Items.BAKED_POTATO, new ItemStack(Items.BOWL, 5), new ItemStack(Blocks.RED_MUSHROOM, 3), "foodFlour"});

        if (foodStackSize) {
            Item.REGISTRY.forEach(item -> {
                if (item instanceof ItemFood)
                    item.setMaxStackSize(16);
            });
        }
    }

    @Override
    public void disabledInit(FMLInitializationEvent event) {
        GameRegistry.addSmelting(BlockRawPastry.getStack(BlockRawPastry.EnumType.COOKIE), new ItemStack(Items.COOKIE, 16), 0.1F);
        GameRegistry.addSmelting(BlockRawPastry.getStack(BlockRawPastry.EnumType.PUMPKIN), new ItemStack(Items.PUMPKIN_PIE, 2), 0.1F);
        KilnRecipes.addKilnRecipe(BlockRawPastry.getStack(BlockRawPastry.EnumType.COOKIE), new ItemStack(Items.COOKIE, 16));
        KilnRecipes.addKilnRecipe(BlockRawPastry.getStack(BlockRawPastry.EnumType.PUMPKIN), new ItemStack(Items.PUMPKIN_PIE, 2));
    }

    @Override
    public String getFeatureDescription() {
        return "Completely revamps the hunger system of Minecraft. \n" +
                "The Saturation value is replaced with Fat. \n" +
                "Fat will accumulate if too much food is consumed then need to fill the bar.\n" +
                "Fat will only be burned once the entire hunger bar is emptied \n" +
                "The more fat the slower you will walk.\n" +
                "Food Items values are also changed, while a ton of new foods are add.";
    }

    @Override
    public String[] getIncompatibleMods() {
        return new String[]{"applecore"};
    }

    @Override
    public boolean requiresMinecraftRestartToEnable() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void replaceHungerGui(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.FOOD) {
            if (!(Minecraft.getMinecraft().player.getFoodStats() instanceof BWMFoodStats))
                return;// Can happen for a moment when changing config
            event.setCanceled(true);
            if (guiHunger == null)
                guiHunger = new GuiHunger();
            guiHunger.draw();
        }
    }

    @SubscribeEvent
    public void replaceFoodSystem(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            applyFoodSystem(player);
        }
    }

    private void setFoodStats(EntityPlayer player, FoodStats foodStats) {
        ReflectionHelper.setPrivateValue(EntityPlayer.class, player, foodStats, "field_71100_bB", "foodStats");
    }

    private void applyFoodSystem(EntityPlayer player) {
        if (player.getFoodStats() instanceof BWMFoodStats)
            return;
        BWMFoodStats newFS = new BWMFoodStats(player);
        NBTTagCompound compound = player.getEntityData();
        newFS.readNBT(compound);
        setFoodStats(player, newFS);
        BWMod.logger.debug("Custom food system " + newFS + " applied on " + player.getName() + ".");
    }

    /**
     * The FoodStats must be manually saved with event. Why is not known.
     */
    @SubscribeEvent
    public void saveFoodSystem(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.player.getFoodStats() instanceof BWMFoodStats))
            return;
        event.player.getFoodStats().writeNBT(event.player.getEntityData());
    }

    public Optional<EntityPlayer> isFoodSystemValid(EntityLivingBase entity) {
        return Optional.ofNullable(entity != null && entity instanceof EntityPlayer && EntityPlayerExt.isSurvival((EntityPlayer) entity) && ((EntityPlayer) entity).getFoodStats() instanceof BWMFoodStats ? (EntityPlayer) entity : null);
    }

    /**
     * Eating is not allowed when food poisoned.
     */
    @SubscribeEvent
    public void onFood(LivingEntityUseItemEvent.Start event) {
        if (!(event.getItem().getItem() instanceof ItemFood))
            return;
        isFoodSystemValid(event.getEntityLiving()).ifPresent(player -> {
            if (player.isPotionActive(MobEffects.HUNGER)) {
                event.setCanceled(true);
            }
        });
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void walkingShaked(LivingEvent.LivingUpdateEvent event) {
        isFoodSystemValid(event.getEntityLiving()).ifPresent(player -> {
            if (player.world.isRemote && player.isSprinting())
                if (guiHunger != null)
                    guiHunger.triggerShake();
        });
    }

    @SubscribeEvent
    public void walkingPenalty(LivingEvent.LivingUpdateEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote)
            return;
        EntityPlayer player = isFoodSystemValid(event.getEntityLiving()).orElse(null);
        if (player != null) {
            EntityPlayerExt.changeSpeed(player, penaltySpeedUUID, "Health speed penalty", EntityPlayerExt.getHealthAndExhaustionModifier(player));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void jumpingShaked(LivingEvent.LivingJumpEvent event) {
        isFoodSystemValid(event.getEntityLiving()).ifPresent(player -> {
            if (player.world.isRemote)
                guiHunger.triggerShake();
        });
    }

    @SubscribeEvent
    public void jumpingPenalty(LivingEvent.LivingJumpEvent event) {
        isFoodSystemValid(event.getEntityLiving()).ifPresent(player -> {
            if (!EntityPlayerExt.canJump(player)) {
                event.getEntityLiving().motionX = 0;
                event.getEntityLiving().motionY = 0;
                event.getEntityLiving().motionZ = 0;
            }
            player.addExhaustion((float) jumpExhaustion);
        });
    }

    /**
     * Cancel the FOV decrease caused by the decreasing speed due to player
     * penalties. Original FOV value given by the event is never used, we start
     * from scratch 1.0F value. Edited from
     * AbstractClientPlayer.getFovModifier()
     */
    @SubscribeEvent
    public void onFOVUpdate(FOVUpdateEvent event) {
        EntityPlayer player = event.getEntity();
        if (!(player.getFoodStats() instanceof BWMFoodStats))
            return;
        float modifier = EntityPlayerExt.getHealthAndExhaustionModifier(player);
        float f = 1.0F;

        if (player.capabilities.isFlying) {
            f *= 1.1F;
        }

        IAttributeInstance iattributeinstance = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        double oldAttributeValue = iattributeinstance.getAttributeValue() / modifier;
        f = (float) ((double) f * ((oldAttributeValue / (double) player.capabilities.getWalkSpeed() + 1.0D) / 2.0D));

        if (player.capabilities.getWalkSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)) {
            f = 1.0F;
        }

        if (player.isHandActive() && !player.getActiveItemStack().isEmpty()
                && player.getActiveItemStack().getItem() == Items.BOW) {
            int i = player.getItemInUseMaxCount();
            float f1 = (float) i / 20.0F;

            if (f1 > 1.0F) {
                f1 = 1.0F;
            } else {
                f1 = f1 * f1;
            }

            f *= 1.0F - f1 * 0.15F;
        }

        event.setNewfov(f);
    }

    @SubscribeEvent
    public void saveSoup(LivingEntityUseItemEvent.Finish event) {
        if (!event.getItem().isEmpty()) {
            if (event.getItem().getItem() instanceof ItemSoup) {
                if (event.getItem().getCount() > 0) {
                    ItemStack result = event.getResultStack();
                    event.setResultStack(event.getItem());
                    if (event.getEntityLiving() instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
                        if (!player.inventory.addItemStackToInventory(result)) {
                            player.dropItem(result, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean hasSubscriptions() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void renderPlayer(RenderPlayerEvent.Pre e) {
        EntityPlayer player = e.getEntityPlayer();
        if (!(player.getFoodStats() instanceof BWMFoodStats))
            return;
        FatPenalty fat = EntityPlayerExt.getFatPenalty(player);
        RenderPlayer render = e.getRenderer();
        float scale = fat != FatPenalty.NO_PENALTY ? Math.max(0, fat.ordinal() / 4f) : 0.0f;
        render.getMainModel().bipedBody = new ModelRenderer(render.getMainModel(), 16, 16);
        render.getMainModel().bipedBody.addBox(-4.0F, 0, -2.0F, 8, 12, 4, scale);
        render.getMainModel().bipedBodyWear = new ModelRenderer(render.getMainModel(), 16, 32);
        render.getMainModel().bipedBodyWear.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.25F + scale);

    }

}


