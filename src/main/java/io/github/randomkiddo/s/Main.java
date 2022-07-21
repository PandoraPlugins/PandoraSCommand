package io.github.randomkiddo.s;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Main extends JavaPlugin implements CommandExecutor, Listener {
    private HashMap<String, ItemStack[]> sList;
    private HashMap<String, ItemStack[]> armorList;
    @Override public void onEnable() {
        this.sList = new HashMap<>();
        this.armorList = new HashMap<>();
        this.getCommand("s").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //todo handle /thru
        if (!(sender instanceof Player) || !sender.isOp()) { return false; }
        Player player = (Player)sender;
        if (this.isActive(player)) {
            player.setGameMode(GameMode.SURVIVAL);
            ItemStack[] saved = this.sList.get(player.getName());
            player.getInventory().setContents(saved);
            ItemStack[] armor = this.armorList.get(player.getName());
            player.getInventory().setArmorContents(armor);
            player.showPlayer(player);
            this.sList.remove(player.getName());
            this.armorList.remove(player.getName());
            PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,
                    200, 255);
            player.addPotionEffect(resistance);
        } else {
            this.sList.put(player.getName(), player.getInventory().getContents());
            player.getInventory().clear();
            player.setGameMode(GameMode.CREATIVE);
            ItemStack[] contents = { new ItemStack(Material.DIAMOND_BLOCK), new ItemStack(Material.GLASS) };
            ItemMeta meta = contents[0].getItemMeta();
            meta.setDisplayName("Change Gamemode");
            contents[0].setItemMeta(meta);
            meta = contents[1].getItemMeta();
            meta.setDisplayName("Change Vanish");
            contents[1].setItemMeta(meta);
            player.getInventory().setContents(contents);
            player.hidePlayer(player);
            this.armorList.put(player.getName(), player.getInventory().getArmorContents());
            player.getInventory().setArmorContents(null);
        }
        return true;
    }
    private boolean isActive(Player player) {
        for (String name : this.sList.keySet()) {
            if (player.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    @EventHandler public void onRightClick(PlayerInteractEvent interact) {
        try {
            if (interact.hasItem()) {
                ItemStack item = interact.getItem();
                Player player = interact.getPlayer();
                String name = item.getItemMeta().getDisplayName();
                if (name.equals("Change Gamemode")) {
                    player.setGameMode(this.rotateGameMode(player.getGameMode()));
                } else if (name.equals("Change Vanish")) {
                    if (player.canSee(player)) {
                        player.hidePlayer(player);
                    } else {
                        player.showPlayer(player);
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        } catch (NullPointerException e) {
            /*
            NullPointerException occurs on Right Click Interacts not related
            to S command. Instead of hard-coding a fix, it is easier to just
            ignore the Exception and return the function immediately
             */
            return;
        }
    }
    private GameMode rotateGameMode(GameMode current) {
        GameMode[] rotation = { GameMode.CREATIVE, GameMode.SPECTATOR, GameMode.SURVIVAL };
        for (int i = 0; i < rotation.length; ++i) {
            if (rotation[i].equals(current)) {
                return rotation[(i+1)%rotation.length];
            }
        }
        return null;
    }
    @EventHandler public void onPlayerClicked(PlayerInteractEntityEvent interact) {
        if (!(interact.getRightClicked() instanceof Player)) { return; }
        Player target = (Player)interact.getRightClicked();
        interact.getPlayer().openInventory(target.getInventory());
    }
    @EventHandler public void onMove(PlayerMoveEvent move) {
        if (move.getPlayer().getActivePotionEffects().size() == 0) { return; };
        for (PotionEffect effect : move.getPlayer().getActivePotionEffects()) {
            if (this.isSResistance(effect)) {
                move.getPlayer().removePotionEffect(effect.getType());
                return;
            }
        }
    }
    private boolean isSResistance(PotionEffect effect) {
        return (effect.getType().toString().contains("DAMAGE_RESISTANCE") && effect.getAmplifier() == 255);
    }
}