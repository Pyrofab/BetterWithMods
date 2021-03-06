package betterwithmods.module.compat;

import betterwithmods.common.BWMBlocks;
import betterwithmods.common.blocks.mini.*;
import betterwithmods.common.items.ItemBark;
import betterwithmods.common.items.ItemMaterial;
import betterwithmods.module.CompatFeature;
import betterwithmods.module.ModuleLoader;
import betterwithmods.module.gameplay.AnvilRecipes;
import betterwithmods.module.gameplay.SawRecipes;
import betterwithmods.module.hardcore.HCSaw;
import betterwithmods.module.tweaks.HighEfficiencyRecipes;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import static betterwithmods.common.BWMBlocks.registerBlock;
import static betterwithmods.common.BWMBlocks.setInventoryModel;
import static betterwithmods.common.BWOreDictionary.registerOre;

/**
 * Created by tyler on 5/27/17.
 */
@SuppressWarnings("unused")
public class Rustic extends CompatFeature {
    public static final Block SIDING = new BlockSiding(Material.ROCK) {
        @Override
        public int getUsedTypes() {
            return 3;
        }

        @Override
        public Material getMaterial(IBlockState state) {
            int type = state.getValue(BlockMini.TYPE);
            return type < 2 ? BlockMini.MINI : Material.ROCK;
        }
    }.setRegistryName("rustic_compat_siding");
    public static final Block MOULDING = new BlockMoulding(Material.ROCK) {
        @Override
        public int getUsedTypes() {
            return 3;
        }

        @Override
        public Material getMaterial(IBlockState state) {
            int type = state.getValue(BlockMini.TYPE);
            return type < 2 ? BlockMini.MINI : Material.ROCK;
        }
    }.setRegistryName("rustic_compat_moulding");
    public static final Block CORNER = new BlockCorner(Material.ROCK) {
        @Override
        public int getUsedTypes() {
            return 3;
        }

        @Override
        public Material getMaterial(IBlockState state) {
            int type = state.getValue(BlockMini.TYPE);
            return type < 2 ? BlockMini.MINI : Material.ROCK;
        }
    }.setRegistryName("rustic_compat_corner");
    public String[] woods = {"oak", "spruce", "birch", "jungle", "acacia", "big_oak", "olive", "ironwood"};

    public Rustic() {
        super("rustic");
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        registerBlock(SIDING, new ItemBlockMini(SIDING));
        registerBlock(MOULDING, new ItemBlockMini(MOULDING));
        registerBlock(CORNER, new ItemBlockMini(CORNER));
        ItemBark.barks.add("olive");
        ItemBark.barks.add("ironwood");
    }

    @Override
    public void preInitClient(FMLPreInitializationEvent event) {
        setInventoryModel(SIDING);
        setInventoryModel(MOULDING);
        setInventoryModel(CORNER);

    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        ItemStack rope = new ItemStack(getBlock(new ResourceLocation("rustic", "rope")));
        //TODO
//        BWMRecipes.removeRecipes(rope);
        addHardcoreRecipe(new ShapedOreRecipe(null, rope, "F", "F", "F", 'F', "fiberHemp").setRegistryName(new ResourceLocation("betterwithmods", "rustic_rope")));
        addHardcoreRecipe(new ShapedOreRecipe(null, new ItemStack(getBlock(new ResourceLocation("rustic", "candle")), 6), "S", "T", "I", 'S', "string", 'T', "tallow", 'I', "ingotIron").setRegistryName(new ResourceLocation("betterwithmods", "rustic_candle")));
        Block plank = getBlock("rustic:planks");
        Block log = getBlock("rustic:log");
        for (int i = 0; i < 2; i++) {
            SawRecipes.addSawRecipe(log, i, new ItemStack[]{new ItemStack(plank, 4, i), ItemMaterial.getMaterial(ItemMaterial.EnumMaterial.SAWDUST, 2), ItemBark.getStack(woods[i + 6], 1)});
            SawRecipes.addSawRecipe(plank, i, new ItemStack(SIDING, 2, i));
            SawRecipes.addSawRecipe(SIDING, i, new ItemStack(MOULDING, 2, i));
            SawRecipes.addSawRecipe(MOULDING, i, new ItemStack(CORNER, 2, i));
            SawRecipes.addSawRecipe(CORNER, i, ItemMaterial.getMaterial(ItemMaterial.EnumMaterial.GEAR, 2));
            addHardcoreRecipe(new ShapelessOreRecipe(null, new ItemStack(plank, 1, i), new ItemStack(SIDING, 1, i), new ItemStack(SIDING, 1, i)).setRegistryName(new ResourceLocation("betterwithmods", "rustic_" + woods[i + 6] + "_plank_recover")));
            addHardcoreRecipe(new ShapelessOreRecipe(null, new ItemStack(SIDING, 1, i), new ItemStack(MOULDING, 1, i), new ItemStack(MOULDING, 1, i)).setRegistryName(new ResourceLocation("betterwithmods", "rustic_" + woods[i + 6] + "_siding_recover")));
            addHardcoreRecipe(new ShapelessOreRecipe(null, new ItemStack(MOULDING, 1, i), new ItemStack(CORNER, 1, i), new ItemStack(CORNER, 1, i)).setRegistryName(new ResourceLocation("betterwithmods", "rustic_" + woods[i + 6] + "_moulding_recover")));
        }
        boolean isHCSawEnabled = ModuleLoader.isFeatureEnabled(HCSaw.class);
        Block wooden_stake = getBlock("rustic:crop_stake");
        if (isHCSawEnabled) {
            //TODO
//            BWMRecipes.removeRecipes(wooden_stake);
        }//TODO
        addHardcoreRecipe(new ShapedOreRecipe(null, new ItemStack(wooden_stake, 3), "M", "M", "M", 'M', "mouldingWood").setRegistryName(new ResourceLocation("betterwithmods", "rustic_stake")));
        if (ModuleLoader.isFeatureEnabled(HighEfficiencyRecipes.class)) {
            for (int i = 0; i < woods.length; i++) {
                ItemStack moulding = i >= 6 ? new ItemStack(MOULDING, 1, i - 6) : new ItemStack(BWMBlocks.WOOD_MOULDING, 1, i);
                ItemStack siding = i >= 6 ? new ItemStack(SIDING, 1, i - 6) : new ItemStack(BWMBlocks.WOOD_SIDING, 1, i);
                ItemStack chair = new ItemStack(getBlock("rustic:chair_" + woods[i]), 4);
                ItemStack table = new ItemStack(getBlock("rustic:table_" + woods[i]), 2);
                if (isHCSawEnabled) {
                    //TODO
//                    BWMRecipes.removeRecipes(chair);
//                    BWMRecipes.removeRecipes(table);
                }
                addHardcoreRecipe(new ShapedOreRecipe(null, chair, "S  ", "SSS", "M M", 'S', siding, 'M', moulding).setRegistryName(new ResourceLocation("betterwithmods", "rustic_" + woods[i] + "_chair")));
                addHardcoreRecipe(new ShapedOreRecipe(null, table, "SSS", "M M", 'S', siding, 'M', moulding).setRegistryName(new ResourceLocation("betterwithmods", "rustic_" + woods[i] + "_table")));
                if (i >= 6) {
                    ItemStack fencegate = new ItemStack(getBlock("rustic:fence_gate_" + woods[i]));
                    ItemStack fence = new ItemStack(getBlock("rustic:fence_" + woods[i]), 3);
                    if (isHCSawEnabled) {
                        //TODO
//                        BWMRecipes.removeRecipes(fencegate);
//                        BWMRecipes.removeRecipes(fence);
                    }
                    addHardcoreRecipe(new ShapedOreRecipe(null, fencegate, "MSM", 'S', siding, 'M', moulding).setRegistryName(new ResourceLocation("betterwithmods", "rustic_" + woods[i] + "_fence_gate")));
                    addHardcoreRecipe(new ShapedOreRecipe(null, fence, "MMM", 'M', moulding).setRegistryName(new ResourceLocation("betterwithmods", "rustic_" + woods[i] + "_fence")));
                }
            }
        }
        AnvilRecipes.addSteelShapedRecipe(new ResourceLocation("rustic_siding"), new ItemStack(SIDING, 8, 2), "SSSS", 'S', getBlock("rustic:slate"));
        AnvilRecipes.addSteelShapedRecipe(new ResourceLocation("rustic_moulding"), new ItemStack(MOULDING, 8, 2), "SSSS", 'S', new ItemStack(SIDING, 1, 2));
        AnvilRecipes.addSteelShapedRecipe(new ResourceLocation("rustic_corner"), new ItemStack(CORNER, 8, 2), "SSSS", 'S', new ItemStack(MOULDING, 1, 2));

        registerOre("sidingWood", new ItemStack(SIDING, 1, 0), new ItemStack(SIDING, 1, 1));
        registerOre("mouldingWood", new ItemStack(MOULDING, 1, 0), new ItemStack(MOULDING, 1, 1));
        registerOre("cornerWood", new ItemStack(CORNER, 1, 0), new ItemStack(CORNER, 1, 1));
    }
}

