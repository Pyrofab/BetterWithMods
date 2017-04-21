package betterwithmods.module.tweaks;

import betterwithmods.common.entity.ai.EntityAISearchFood;
import betterwithmods.module.Feature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by tyler on 4/20/17.
 */
public class EasyBreeding extends Feature {
    @Override
    public String getFeatureDescription() {
        return "Animals will pick up breeding items off of the ground as necessary";
    }

    @SubscribeEvent
    public void addEntityAI(EntityJoinWorldEvent evt) {
        if (evt.getEntity() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) evt.getEntity();
            if (entity instanceof EntityAnimal) {
                ((EntityAnimal) entity).tasks.addTask(3, new EntityAISearchFood((EntityAnimal) entity));
            }
        }

    }

    @Override
    public boolean hasSubscriptions() {
        return true;
    }

    @Override
    public String[] getIncompatibleMods() {
        return new String[]{"easyBreeding"};
    }
}