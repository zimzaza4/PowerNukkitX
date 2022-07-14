package cn.nukkit.network.protocol;

import cn.nukkit.Nukkit;
import cn.nukkit.Server;
import cn.nukkit.entity.custom.CustomEntityDefinition;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import lombok.ToString;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.ByteOrder;

@ToString(exclude = {"tag"})
public class AvailableEntityIdentifiersPacket extends DataPacket {
    public static final byte NETWORK_ID = ProtocolInfo.AVAILABLE_ENTITY_IDENTIFIERS_PACKET;

    private static final byte[] TAG;

    static {
        try {
            InputStream inputStream = Nukkit.class.getClassLoader().getResourceAsStream("entity_identifiers.dat");


            if (inputStream == null) {
                throw new AssertionError("Could not find entity_identifiers.dat");
            }
            //noinspection UnstableApiUsage

            BufferedInputStream bis = new BufferedInputStream(inputStream);
            CompoundTag nbt = NBTIO.read(bis, ByteOrder.BIG_ENDIAN, false);
            ListTag<CompoundTag> list = nbt.getList("idlist", CompoundTag.class);
            for (CustomEntityDefinition definition : Server.getInstance().getCustomEntityManager().getCustomEntitiesDefinition().values()) {
                list.add(definition.nbt());
            }
            nbt.putList(list);
            System.out.println(nbt);
            TAG = NBTIO.write(nbt, ByteOrder.BIG_ENDIAN, false);
        } catch (Exception e) {
            throw new AssertionError("Error whilst loading entity_identifiers.dat", e);
        }
    }

    public byte[] tag = TAG;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.tag = this.get();
    }

    @Override
    public void encode() {
        this.reset();
        this.put(this.tag);
    }
}
