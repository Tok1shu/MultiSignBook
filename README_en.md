# MultiSignBook

## Overview
MultiSignBook is a Bukkit/Spigot plugin designed to facilitate the creation of in-game contracts and agreements on Minecraft servers. Originally developed for the Haku server, this plugin allows players to create and protect written agreements, ensuring the integrity of the contract by limiting the number of signatures.

## Features
- **Contract Signing:** Allows players to create formal agreements using written books.
- **Secure Agreements:** Locks written books to prevent unauthorized signatures.
- **Signature Tracking:** Records players who have signed the contract.
- **Signature Limit:** Defines the maximum number of signatures in a contract (default limit: 8).
- **Command Usage:**
  - `/sign lock`: Protects the contract from additional signatures.
  - `/sign s`: Adds your signature to the contract (if it's not locked).

## Important Note
MultiSignBook provides formality for role-playing servers, and it does not regulate the execution of contracts through in-game mechanics. It provides the means to create formal agreements but leaves their execution and adherence to server rules up to the players.

## Installation
1. Download the latest release from the [releases page](https://github.com/Tok1shu/MultiSignBook/releases).
2. Place the JAR file in your server's `plugins` folder.
3. Restart the server.

## Usage
- Hold a written book in your hand.
- Use `/sign lock` to protect the contract from additional signatures.
- Use `/sign s` to add your signature to the contract.

## Default `config.yml`
```
# MultiSignBook
settings:
  max_signatures: 8

# Messages
# Use "§" to set chat color
messages:
  book_locked: "§cThis book is already protected from signatures."
  book_already_signed: "§cYou have already signed this book."
  book_max_signatures: "§cThe book already contains the maximum number of signatures."
  book_protected: "§aThe book has been successfully protected from signatures."
  signature_added: "§aSignature successfully added."
  book_locked_cannot_sign: "§cYou cannot sign this book; it is protected from signatures."
  invalid_command: "§cUse /sign lock to protect from signatures or /sign s to add a signature."
  not_signed_book: "§cHold a signed book in your hand to execute /sign commands."
  invalid_command_console: "§cInvalid command. Use /sign lock or /sign s."

# Lore headers in the book
# It is not recommended to change them if the books on the server have been signed; this may break them.
lore:
  locked: "§cProtected: "
  signed: "§7Signed: "
```

## Contribution
Feel free to contribute to the development of MultiSignBook! Fork this repository, make changes, and submit a pull request.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
