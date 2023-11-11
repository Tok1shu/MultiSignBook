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
                        player.sendMessage("Книга успешно защищена от подписей.");
                    } else {
                        player.sendMessage("§cЭта книга уже защищена от подписей.");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                } else if (args.length == 1 && args[0].equalsIgnoreCase("s")) {
                    if (isBookLocked(bookMeta)) {
                        player.sendMessage("§cЭту книгу нельзя подписать, она защищена от подписей.");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    } else {
                        addSignature(bookMeta, player);
                        player.sendMessage("Подпись успешно добавлена.");
                    }
                } else {
                    player.sendMessage("Используйте /sign lock для защиты от подписей или /sign s для добавления подписи.");
                }

                item.setItemMeta(bookMeta);
                return true;
            } else {
                player.sendMessage("§eОшибка: Для защиты от подписей используйте книгу.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return false;
            }
        } else {
            sender.sendMessage("§cНеверная команда. Используйте /sign lock или /sign s.");
            return false;
        }
    }
    private boolean isBookLocked(BookMeta bookMeta) {
        return bookMeta.getLore() != null && bookMeta.getLore().contains("§cЗащищено: " + getLockOwner(bookMeta));
    }
    private void lockBook(BookMeta bookMeta, Player player) {
        List<String> lore = bookMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lockedBooks.add(player.getName());
        lore.add("§cЗащищено: " + player.getName());
        bookMeta.setLore(lore);  // Устанавливаем обновленный лор обратно в книгу
    }
    private void addSignature(BookMeta bookMeta, Player player) {
        List<String> lore = bookMeta.getLore();

        if (lore == null) {
            lore = new ArrayList<>();
            bookMeta.setLore(lore);
        }

        if (isBookLocked(bookMeta)) {
            player.sendMessage("§cЭта книга защищена от подписей и не может быть изменена.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return; // Выйти из метода, так как книга заблокирована
        }

        // Ищем строку "Подписано:"
        String signatureLine = null;
        int signatureIndex = -1;

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.toLowerCase().startsWith("§7подписано:")) {
                signatureLine = line;
                signatureIndex = i;
                break;
            }
        }

        // Если строка "Подписано:" уже существует, обновляем ее
        if (signatureLine != null) {
            String[] names = signatureLine.replace("§7Подписано: ", "").split(", ");
            List<String> signedPlayers = new ArrayList<>(Arrays.asList(names));

            if (signedPlayers.size() < 8) {
                if (!signedPlayers.contains(player.getName())) {
                    signedPlayers.add(player.getName());
                    lore.set(signatureIndex, "§7Подписано: " + String.join(", ", signedPlayers));
                } else {
                    player.sendMessage("§cВы уже подписались в этой книге.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            } else {
                player.sendMessage("§cКнига уже содержит максимальное количество подписей (8).");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        } else {
            // Если строки "Подписано:" еще нет, добавляем новую
            lore.add("§7Подписано: " + player.getName());
        }

        bookMeta.setLore(lore);
    }




    private String getLockOwner(BookMeta bookMeta) {
        for (String lore : bookMeta.getLore()) {
            if (lore.startsWith("§cЗащищено: ")) {
                return lore.substring("§cЗащищено: ".length());
            }
        }
        return "";
    }
}
