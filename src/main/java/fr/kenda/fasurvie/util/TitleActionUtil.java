package fr.kenda.fasurvie.util;

import fr.kenda.fasurvie.FASurvival;
import org.bukkit.entity.Player;

public class TitleActionUtil {

    public static void sendActionBar(Player player, String msg, int seconds) {
        new org.bukkit.scheduler.BukkitRunnable() {
            int time = 0;
            @Override
            public void run() {
                if (time >= seconds || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                sendActionBarOnce(player, msg);
                time++;
            }
        }.runTaskTimer(FASurvival.getInstance(), 0L, 20L);
    }

    // Cette méthode envoie l'action bar UNE FOIS (ta version actuelle renommée)
    public static void sendActionBarOnce(Player player, String msg) {
        try {
            String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);

            Class<?> chatComponentTextClass = Class.forName("net.minecraft.server." + version + ".ChatComponentText");
            Object chatComponentText = chatComponentTextClass.getConstructor(String.class).newInstance(msg);

            Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat");
            Object packet = packetPlayOutChatClass.getConstructor(
                    Class.forName("net.minecraft.server." + version + ".IChatBaseComponent"),
                    byte.class
            ).newInstance(chatComponentText, (byte)2);

            Object playerConnection = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            playerConnection = playerConnection.getClass().getField("playerConnection").get(playerConnection);
            playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet")).invoke(playerConnection, packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
