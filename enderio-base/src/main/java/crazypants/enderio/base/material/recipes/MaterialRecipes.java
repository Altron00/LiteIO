package crazypants.enderio.base.material.recipes;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.NNList;

import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.config.recipes.xml.Crafting;
import crazypants.enderio.base.material.alloy.Alloy;
import crazypants.enderio.base.material.alloy.IAlloy;
import crazypants.enderio.base.material.alloy.endergy.AlloyEndergy;
import crazypants.enderio.base.material.glass.FusedQuartzType;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = EnderIO.MODID)
public class MaterialRecipes {

  @SubscribeEvent
  public static void register(@Nonnull RegistryEvent.Register<IRecipe> event) {
    final IForgeRegistry<IRecipe> registry = event.getRegistry();

    for (IAlloy alloy : new NNList<IAlloy>(Alloy.values()).addAll(AlloyEndergy.values())) {
      registry.register(new ShapedOreRecipe(null, alloy.getStackBlock(), "iii", "iii", "iii", 'i', alloy.getOreIngot())
          .setRegistryName(Crafting.mkRL("Auto: %s 1 block to 9 ingots", alloy.getBaseName())));
      registry.register(new ShapelessOreRecipe(null, alloy.getStackIngot(9), alloy.getOreBlock())
          .setRegistryName(Crafting.mkRL("Auto: %s 9 ingots to 1 block", alloy.getBaseName())));

      registry.register(new ShapedOreRecipe(null, alloy.getStackIngot(), "nnn", "nnn", "nnn", 'n', alloy.getOreNugget())
          .setRegistryName(Crafting.mkRL("Auto: %s 9 nuggets to 1 ingot", alloy.getBaseName())));
      registry.register(new ShapelessOreRecipe(null, alloy.getStackNugget(9), alloy.getStackIngot())
          .setRegistryName(Crafting.mkRL("Auto: %s 1 ingot to 9 nuggets", alloy.getBaseName())));
    }

    for (EnumDyeColor color : EnumDyeColor.values()) {
      for (FusedQuartzType type : FusedQuartzType.values()) {
        registry.register(new ShapedOreRecipe(null, new ItemStack(type.getBlock(), 8, color.getMetadata()), "GGG", "CGG", "GGG", 'G', type.getOreDictName(),
            'C', "dye" + MaterialOredicts.dyes[color.getDyeDamage()])
                .setRegistryName(Crafting.mkRL("Auto: Coloring %s with %s", type.getName(), color.getUnlocalizedName())));
        if (color != EnumDyeColor.WHITE) {
          registry.register(new ShapedOreRecipe(null, new ItemStack(type.getBlock(), 8, color.getMetadata()), "GGG", "CGG", "GGG", 'G',
              new ItemStack(type.getBlock(), 1, EnumDyeColor.WHITE.getMetadata()), 'C', "dye" + MaterialOredicts.dyes[color.getDyeDamage()])
                  .setRegistryName(Crafting.mkRL("Auto: Easy Lookup for coloring %s with %s", type.getName(), color.getUnlocalizedName())));
        }
      }
    }

  }

}
