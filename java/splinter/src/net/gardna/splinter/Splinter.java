package net.gardna.splinter;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.gardna.splinter.listeners.ListenerManager;
import net.gardna.splinter.util.Vector2;
import net.gardna.splinter.zoner.MassiveRegion;
import net.gardna.splinter.zoner.Region;
import net.gardna.splinter.zoner.Zoner;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;


public class Splinter extends JavaPlugin implements PluginMessageListener {
    private static Splinter instance;

    public ListenerManager listeners;
    public MoveDetector moveDetector;
    public NetHandler netHandler;
    public Zoner zoner;
    public String serverName;
    public World mainWorld;

    @Override
    public void onEnable() {
        instance = this;

        zoner = new Zoner(new Region[]{
                new Region("server1", new Vector2(-100, -100), new Vector2(100, 100)),
                new MassiveRegion("server2")
        });

        moveDetector = new MoveDetector();
        moveDetector.runTaskTimer(this, 0, 10);

        netHandler = new NetHandler();
        netHandler.runTaskAsynchronously(this);

        mainWorld = getServer().getWorld("world");
        mainWorld.setAutoSave(false);

        listeners = new ListenerManager();
        listeners.registerAll(this);
        
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
    }

    public static Splinter getInstance() {
        return instance;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        String data = in.readUTF();

        if (subChannel.equals("GetServer")) {
            serverName = data;
            getLogger().info("Got server name: " + serverName);
        }
    }
}
