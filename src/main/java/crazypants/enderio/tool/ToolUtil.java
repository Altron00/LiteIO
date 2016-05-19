package crazypants.enderio.tool;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import crazypants.enderio.EnderIO;
import crazypants.enderio.Log;
import crazypants.enderio.api.tool.ITool;
import crazypants.enderio.item.ItemYetaWrench;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ToolUtil {

  public static boolean isToolEquipped(EntityPlayer player) {
    return getInstance().isToolEquippedImpl(player);
  }

  public static ITool getEquippedTool(EntityPlayer player) {
    return getInstance().getEquippedToolImpl(player);
  }

  public static ItemYetaWrench addInterfaces(ItemYetaWrench item) {
    return getInstance().addInterfacesImpl(item);
  }

  public static boolean breakBlockWithTool(Block block, World world, int x, int y, int z, EntityPlayer entityPlayer) {
    return breakBlockWithTool(block, world, new BlockPos(x,y,z), entityPlayer);
  }
  
  public static boolean breakBlockWithTool(Block block, World world, BlockPos pos, EntityPlayer entityPlayer) {
    ITool tool = ToolUtil.getEquippedTool(entityPlayer);
    if(tool != null && entityPlayer.isSneaking() && tool.canUse(entityPlayer.getHeldItemMainhand(), entityPlayer, pos)) {
      IBlockState bs = world.getBlockState(pos);;
      if(block.removedByPlayer(bs, world, pos, entityPlayer, true)) {
        block.harvestBlock(world, entityPlayer, pos, world.getBlockState(pos), world.getTileEntity(pos), entityPlayer.getHeldItemMainhand());
      }
      tool.used(entityPlayer.getHeldItemMainhand(), entityPlayer, pos);
      return true;
    }
    return false;
  }

  private static ToolUtil instance;

  private static ToolUtil getInstance() {
    if(instance == null) {
      instance = new ToolUtil();
    }
    return instance;
  }

  private final List<IToolProvider> toolProviders = new ArrayList<IToolProvider>();
  private final List<IToolImpl> toolImpls = new ArrayList<IToolImpl>();

  private ToolUtil() {

    try {
      Object obj = Class.forName("crazypants.enderio.tool.BuildCraftToolProvider").newInstance();
      toolProviders.add((IToolProvider) obj);
      toolImpls.add((IToolImpl) obj);
    } catch (Exception e) {
      Log.warn("Could not find Build Craft Wrench definition. Wrench integration with other mods may fail");
    }
//    try {
//      Object obj = Class.forName("crazypants.enderio.tool.AEToolProvider").newInstance();
//      toolProviders.add((IToolProvider) obj);
//      toolImpls.add((IToolImpl) obj);
//    } catch (Exception e) {
//      Log.debug("Could not find AE Wrench definition. Wrench integration with AE may fail");
//    }

//    toolProviders.add(new TEToolProvider());
//    toolImpls.add(new TEToolProvider());

  }

  public void registerToolProvider(IToolProvider toolProvider) {
    toolProviders.add(toolProvider);
  }

  private boolean isToolEquippedImpl(EntityPlayer player) {
    return getEquippedToolImpl(player) != null;
  }

  private ITool getEquippedToolImpl(EntityPlayer player) {
    player = player == null ? EnderIO.proxy.getClientPlayer() : player;
    if(player == null) {
      return null;
    }
    ItemStack equipped = player.getHeldItemMainhand();
    if(equipped == null) {
      return null;
    }
    if(equipped.getItem() instanceof ITool) {
      return (ITool) equipped.getItem();
    }
    return getToolImpl(equipped);

  }

  private ITool getToolImpl(ItemStack equipped) {
    for (IToolProvider provider : toolProviders) {
      ITool result = provider.getTool(equipped);
      if(result != null) {
        return result;
      }
    }
    return null;
  }

  private ItemYetaWrench addInterfacesImpl(ItemYetaWrench item) {
    Enhancer e = new Enhancer();
    YetaWrenchProxy proxy = new YetaWrenchProxy(item, toolImpls);
    e.setCallback(proxy);
    e.setSuperclass(item.getClass());
    e.setInterceptDuringConstruction(false);
    
    List<Class<?>> interfaces = new ArrayList<Class<?>>();
    for (IToolImpl tool : toolImpls) {
      Class<?> inf = tool.getInterface();
      if(inf != null) {
        interfaces.add(inf);
      }      
    }
    if(!interfaces.isEmpty()) {
      e.setInterfaces(interfaces.toArray(new Class<?>[interfaces.size()]));
    }
    ItemYetaWrench proxifiedObj = (ItemYetaWrench) e.create();
    return proxifiedObj;
  }

  public static class YetaWrenchProxy implements MethodInterceptor {

    private ItemYetaWrench item;
    private Map<Class<?>, IToolImpl> impls;

    private YetaWrenchProxy(ItemYetaWrench item, List<IToolImpl> toolImpls) {
      this.item = item;
      impls = new HashMap<Class<?>, IToolImpl>();
      for (IToolImpl tool : toolImpls) {
        impls.put(tool.getInterface(), tool);
      }
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
      try {
        IToolImpl impl = impls.get(method.getDeclaringClass());
        if(impl != null) {
          return impl.handleMethod(item, method, args);
        }
        method.setAccessible(true);
        return method.invoke(item, args);
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }
  }
}
