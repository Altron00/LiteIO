package crazypants.enderio.machine.invpanel;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.enderio.core.common.network.MessageTileEntity;
import com.enderio.core.common.network.NetworkUtil;

import crazypants.enderio.EnderIO;
import crazypants.enderio.machine.invpanel.client.InventoryDatabaseClient;
import crazypants.enderio.network.PacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketItemList extends MessageTileEntity<TileInventoryPanel> implements IMessageHandler<PacketItemList, IMessage> {

  private int generation;
  private byte[] compressed;

  public PacketItemList() {
  }

  public PacketItemList(TileInventoryPanel tile, int generation, byte[] compressed) {
    super(tile);
    this.generation = generation;
    this.compressed = compressed;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    super.fromBytes(buf);
    generation = buf.readInt();
    compressed = NetworkUtil.readByteArray(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    super.toBytes(buf);
    buf.writeInt(generation);
    NetworkUtil.writeByteArray(buf, compressed);
  }

  @Override
  public IMessage onMessage(PacketItemList message, MessageContext ctx) {
    EntityPlayer player = EnderIO.proxy.getClientPlayer();
    TileEntity te = player.worldObj.getTileEntity(new BlockPos(message.x, message.y, message.z));
    if(te instanceof TileInventoryPanel) {
      TileInventoryPanel teInvPanel = (TileInventoryPanel) te;
      InventoryDatabaseClient db = teInvPanel.getDatabaseClient(message.generation);
      try {
        List<Integer> missingItems = db.readCompressedItemList(message.compressed);
        if(missingItems != null) {
          PacketHandler.INSTANCE.sendToServer(new PacketRequestMissingItems(teInvPanel, db.getGeneration(), missingItems));
        }
      } catch (IOException ex) {
        Logger.getLogger(PacketItemInfo.class.getName()).log(Level.SEVERE, "Exception while reading item list", ex);
      }
    }
    return null;
  }

}
