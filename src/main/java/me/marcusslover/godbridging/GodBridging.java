package me.marcusslover.godbridging;

import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GodBridging extends JavaPlugin implements CommandExecutor, Listener {
    private final Map<UUID, BridgeRunnable> active;

    public GodBridging() {
        this.active = new HashMap<>();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getCommand("gb").setExecutor(this);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("gb.toggle")) {

                if (this.active.containsKey(player.getUniqueId())) {
                    this.active.get(player.getUniqueId()).stop();
                    this.active.remove(player.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "God speed bridging off!");
                } else {
                    ItemStack itemInMainHand = player.getInventory().getItemInHand();
                    if (this.isBlock(itemInMainHand)) {
                        this.active.put(player.getUniqueId(), new BridgeRunnable(this, player));
                        player.sendMessage(ChatColor.YELLOW + "God speed bridging on!");
                        return true;
                    }
                    player.sendMessage(ChatColor.RED + "You need to be holding a block in your hand!");

                }
            } else {
                player.sendMessage(ChatColor.RED + "No permission!");
            }
        }
        return true;
    }

    private boolean isBlock(ItemStack itemInMainHand) {
        if (itemInMainHand != null && itemInMainHand.getType() != Material.AIR) {
            return itemInMainHand.getType().isBlock();
        }
        return false;
    }

    class BridgeRunnable extends BukkitRunnable {

        private final Player player;

        public BridgeRunnable(Plugin plugin, Player player) {
            this.player = player;
            this.runTaskTimer(plugin, 1L, 1L);
        }


        @Override
        public void run() {
            boolean valid = this.isValid();
            if (valid) {
                this.sendPacket();
            } else {
                this.stop();
            }
        }

        private void sendPacket() {
            Block relative = player.getLocation().getBlock().getRelative(0, -1, 0);
            relative.setType(player.getInventory().getItemInHand().getType());

            PacketPlayOutAnimation packet = new PacketPlayOutAnimation(this.cp(player).getHandle(), 0);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getLocation().distance(player.getLocation()) <= 30) {
                    cp(onlinePlayer).getHandle().playerConnection.sendPacket(packet);
                }
            }
        }

        private CraftPlayer cp(Player player) {
            return (CraftPlayer) player;
        }

        private boolean isValid() {
            if (!this.player.isOnline()) {
                return false;
            }
            return isBlock(player.getInventory().getItemInHand());
        }

        public void stop() {
            this.cancel();
        }
    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
