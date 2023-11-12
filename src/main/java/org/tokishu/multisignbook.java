package org.tokishu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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

public class multisignbook extends JavaPlugin implements Listener {

    private Set<String> lockedBooks = new HashSet<>();
    private FileConfiguration config;

    private String lockedBookMessage;
    private String bookAlreadySignedMessage;
    private String bookMaxSignaturesMessage;
    private String bookProtectedMessage;
    private String signatureAddedMessage;
    private String bookLockedCannotSignMessage;
    private String invalidCommandMessage;
    private String notSignedBookMessage;
    private String invalidCommandConsoleMessage;
    private String lockedLore;
    private String signedLore;
    private Boolean debugmode;
    private String predebugmode;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("sign").setExecutor(this);

        // Загружаем текст из конфига
        loadConfigText();
    }

    private void loadConfigText() {
        // Загружаем текст из конфига
        lockedBookMessage = config.getString("messages.book_locked", "Error: Config value 'messages.book_locked' is missing. Please check your configuration.");
        bookAlreadySignedMessage = config.getString("messages.book_already_signed", "Error: Config value 'messages.book_already_signed' is missing. Please check your configuration.");
        bookMaxSignaturesMessage = config.getString("messages.book_max_signatures", "Error: Config value 'book_max_signatures' is missing. Please check your configuration.");
        bookProtectedMessage = config.getString("messages.book_protected", "Error: Config value 'messages.book_protected' is missing. Please check your configuration.");
        signatureAddedMessage = config.getString("messages.signature_added", "Error: Config value 'messages.signature_added' is missing. Please check your configuration.");
        bookLockedCannotSignMessage = config.getString("messages.book_locked_cannot_sign", "Error: Config value 'messages.book_locked_cannot_sign' is missing. Please check your configuration.");
        invalidCommandMessage = config.getString("messages.invalid_command", "Error: Config value 'messages.invalid_command' is missing. Please check your configuration.");
        notSignedBookMessage = config.getString("messages.not_signed_book", "Error: Config value 'messages.not_signed_book' is missing. Please check your configuration.");
        invalidCommandConsoleMessage = config.getString("messages.invalid_command_console", "Error: Config value 'messages.invalid_command_console' is missing. Please check your configuration.");

        lockedLore = config.getString("lore.locked", "Error: Config value 'lore.locked' is missing. Please check your configuration.");
        signedLore = config.getString("lore.signed", "Error: Config value 'lore.signed' is missing. Please check your configuration.");

        predebugmode = config.getString("settings.debug_mode", "Error: Config value 'settings.debug_mode' is missing. Please check your configuration.");
        if (predebugmode == null){
            debugmode = false;
        }else{
            debugmode = Boolean.parseBoolean(config.getString("settings.debug_mode", "Error: Config value 'settings.debug_mode' is missing. Please check your configuration."));
        }
    }

    private void logDebug(String message) {
        if (debugmode) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("sign") && sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack item = player.getInventory().getItemInMainHand();
            logDebug("Начинаю выполнение команды /sign");
            if (item.getType() == Material.WRITTEN_BOOK) {
                BookMeta bookMeta = (BookMeta) item.getItemMeta();
                logDebug("В руке книга");
                if (args.length == 1 && args[0].equalsIgnoreCase("lock")) { // Команда /sign lock
                    logDebug("Команда /sign lock");
                    if (!isBookLocked(bookMeta)) { // Проверка на заблокированую книгу
                        logDebug("Книга не заблокирована, начинаю блокировку");
                        // Блокируем книгу
                        lockBook(bookMeta, player);
                        logDebug("Блокировка - Успех");
                        player.sendMessage(bookProtectedMessage);
                    } else {
                        logDebug("Книга уже заблокирована");
                        // Если книга заблокирована
                        player.sendMessage(lockedBookMessage);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                } else if (args.length == 1 && args[0].equalsIgnoreCase("s")) { // Команда /sign s
                    logDebug("Команда /sign lock");
                    if (isBookLocked(bookMeta)) {
                        logDebug("Книга заблокирована");
                        // Если книга заблокирована
                        player.sendMessage(bookLockedCannotSignMessage);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    } else {
                        // Добавляем подпись
                        logDebug("Начинаю добавлять подпись...");
                        addSignature(bookMeta, player);
                    }
                } else { // Неизвесная команда
                    player.sendMessage(invalidCommandMessage);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }

                item.setItemMeta(bookMeta);
                return true;
            } else {
                player.sendMessage(notSignedBookMessage);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return false;
            }
        } else if (command.getName().equalsIgnoreCase("sign") && args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("multisignbook.reload")) {
            // Команда для перезагрузки конфига
            reloadConfig();
            config = getConfig();
            loadConfigText();
            sender.sendMessage("Config reloaded.");
            return true;
        } else {
            sender.sendMessage(invalidCommandConsoleMessage);
            return false;
        }
    }

    private boolean isBookLocked(BookMeta bookMeta) {
        logDebug("Начинаю проверку isBookLocked...");
        List<String> lore = bookMeta.getLore();
        if (lore != null){
            logDebug("Результат isBookLocked: " + lore.contains(lockedLore + getLockOwner(bookMeta)));
        }else{
            logDebug("Результат: null");
        }
        return lore != null && lore.contains(lockedLore + getLockOwner(bookMeta));
    }

    private void lockBook(BookMeta bookMeta, Player player) {
        List<String> lore = bookMeta.getLore();
        logDebug("lockBook");
        if (lore == null) {
            lore = new ArrayList<>();
            logDebug("Lore = ничему");
        }
        lockedBooks.add(player.getName());
        lore.add(lockedLore + player.getName());
        bookMeta.setLore(lore);  // Устанавливаем обновленный лор обратно в книгу
    }

    private void addSignature(BookMeta bookMeta, Player player) {
        List<String> lore = bookMeta.getLore();
        logDebug("addSignature");
        if (lore == null) {
            lore = new ArrayList<>();
            bookMeta.setLore(lore);
            logDebug("Lore = ничему");
        }

        if (isBookLocked(bookMeta)) {
            logDebug("Книга заблокирована - Конец");
            player.sendMessage(lockedBookMessage);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return; // Выйти из метода, так как книга заблокирована
        }

// Ищем строку "Подписано:"
        String signatureLine = null;
        int signatureIndex = -1;

        for (int i = 0; i < lore.size(); i++) {
            logDebug("Цикл проверки - начало");
            String line = lore.get(i);
            logDebug("Результат - " + line.startsWith(signedLore));
            logDebug("Результат signatureLine: " + line);
            logDebug("Результат signatureIndex: " + i);
            if (line.startsWith(signedLore)) {
                logDebug("Результат - " + line.startsWith(signedLore));
                logDebug("Результат signatureLine: " + line);
                logDebug("Результат signatureIndex: " + i);
                signatureLine = line;
                signatureIndex = i;
                logDebug("Конец цикла.");
                break;
            }
        }


        // Если строка "Подписано:" уже существует, обновляем ее
        if (signatureLine != null) {
            logDebug("Строка существует");
            String[] names = signatureLine.replace(signedLore, "").split(", ");
            List<String> signedPlayers = new ArrayList<>(Arrays.asList(names));
            int maxSignatures = Integer.parseInt(config.getString("settings.max_signatures", "8"));
            logDebug("maxSignatures =" + maxSignatures);
            if (signedPlayers.size() < maxSignatures) {
                logDebug(signedPlayers.size() + "<" + maxSignatures);
                if (!signedPlayers.contains(player.getName())) {
                    signedPlayers.add(player.getName());
                    lore.set(signatureIndex, signedLore + String.join(", ", signedPlayers));
                    logDebug("Успех добавления, результат: " + signedPlayers.contains(player.getName()));
                    player.sendMessage(signatureAddedMessage);
                } else {
                    logDebug("Книга уже подписана. Конец");
                    player.sendMessage(bookAlreadySignedMessage);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            } else {
                logDebug("Максимальное кол-во подписей. Конец");
                player.sendMessage(bookMaxSignaturesMessage + " (" + maxSignatures + ").");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        } else {
            logDebug("Строки нет, подписываем новую");
            // Если строки "Подписано:" еще нет, добавляем новую
            lore.add(signedLore + player.getName());
            player.sendMessage(signatureAddedMessage);
        }

        bookMeta.setLore(lore);
        logDebug("Конец.");
    }

    private String getLockOwner(BookMeta bookMeta) {
        logDebug("Получаем getLockOwner...");
        List<String> lore = bookMeta.getLore();
        if (lore != null) {
            for (String line : lore) {
                if (line.startsWith(lockedLore)) {
                    logDebug("Результат: " + line.substring(lockedLore.length()));
                    return line.substring(lockedLore.length());
                }
            }
        }
        return "";
    }

}
