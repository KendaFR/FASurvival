package fr.kenda.fasurvie.util;

import fr.kenda.fasurvie.FASurvival;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TitleActionUtil {
    public static void sendActionBar(final Player player, final String msg, final int seconds) {
        new BukkitRunnable() {
            public void run() {
                if (this.time >= seconds || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                TitleActionUtil.sendActionBarOnce(player, msg);
                ++this.time;
            }

            int time = 0;
        }.runTaskTimer(FASurvival.getInstance(), 0L, 20L);
    }

    public static void sendActionBarOnce(Player player, String msg) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
    }
}

