package org.tokishu.multisignbook.multisignbook;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiSignBook extends JavaPlugin implements Listener {

    private Set<String> lockedBooks = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("sign") && sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getType() == Material.WRITTEN_BOOK) {
                BookMeta bookMeta = (BookMeta) item.getItemMeta();

                if (args.length == 1 && args[0].equalsIgnoreCase("lock")) {
                    if (!isBookLocked(bookMeta)) {
                        lockBook(bookMeta, player);
                        player.sendMessage("Book successfully protected from signatures.");
                    } else {
                        player.sendMessage("§cThis book is already protected from signatures.");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                } else if (args.length == 1 && args[0].equalsIgnoreCase("s")) {
                    if (isBookLocked(bookMeta)) {
                        player.sendMessage("§cThis book cannot be signed; it is protected from signatures.");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    } else {
                        addSignature(bookMeta, player);
                        player.sendMessage("Signature successfully added.");
                    }
                } else {
                    player.sendMessage("Use /sign lock to protect from signatures or /sign s to add a signature.");
                }

                item.setItemMeta(bookMeta);
                return true;
            } else {
                player.sendMessage("§eError: Use a book to protect from signatures.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return false;
            }
        } else {
            sender.sendMessage("§cInvalid command. Use /sign lock or /sign s.");
            return false;
        }
    }

    private boolean isBookLocked(BookMeta bookMeta) {
        return bookMeta.getLore() != null && bookMeta.getLore().contains("§cProtected: " + getLockOwner(bookMeta));
    }

    private void lockBook(BookMeta bookMeta, Player player) {
        List<String> lore = bookMeta.getLore();

        if (lore == null) {
            lore = new ArrayList<>();
        }

        lockedBooks.add(player.getName());
        lore.add("§cProtected: " + player.getName());

        bookMeta.setLore(lore);
    }

    private void addSignature(BookMeta bookMeta, Player player) {
        List<String> lore = bookMeta.getLore();

        if (lore == null) {
            lore = new ArrayList<>();
            bookMeta.setLore(lore);
        }

        if (isBookLocked(bookMeta)) {
            player.sendMessage("§cThis book is protected from signatures and cannot be modified.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        String signatureLine = null;
        int signatureIndex = -1;

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.toLowerCase().startsWith("§7signed:")) {
                signatureLine = line;
                signatureIndex = i;
                break;
            }
        }

        if (signatureLine != null) {
            String[] names = signatureLine.replace("§7Signed: ", "").split(", ");
            List<String> signedPlayers = new ArrayList<>(Arrays.asList(names));

            if (signedPlayers.size() < 8) {
                if (!signedPlayers.contains(player.getName())) {
                    signedPlayers.add(player.getName());
                    lore.set(signatureIndex, "§7Signed: " + String.join(", ", signedPlayers));
                } else {
                    player.sendMessage("§cYou have already signed this book.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            } else {
                player.sendMessage("§cThe book already contains the maximum number of signatures (8).");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        } else {
            lore.add("§7Signed: " + player.getName());
        }

        bookMeta.setLore(lore);
    }

    private String getLockOwner(BookMeta bookMeta) {
        for (String lore : bookMeta.getLore()) {
            if (lore.startsWith("§cProtected: ")) {
                return lore.substring("§cProtected: ".length());
            }
        }
        return "";
    }
}
