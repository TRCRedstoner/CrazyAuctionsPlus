# CrazyAuctionPlus

* This is an enhanced version of the CrazyAuctions plugin, rewritten from open source by @BadBones69 (original author).
* Based on the original CrazyAuctions plugin, I added a variety of new features, and modified and optimized part of the code according to my own characteristics.
* I hope you will like this plugin, and your support is my motivation.;)
* Depend plugin: Vault.(https://www.spigotmc.org/resources/vault.34315/)
* Command system added by Maxlego08, based on https://githum.com/Maxlego08/TemplatePlugin
* <sub><sup>Do not hesitate to see the <a href="https://www.spigotmc.org/resources/zauctionhouse-1-7-1-15-auction-house-plugin.63010/">zAuctionhouse</a> plugin, also an auction house plugin but paid. The plugin is more optimized, offers more functionality.</sup></sub>

# Features

* Selling Items, Sell your items on the global market.
* Buying Items, Let others sell you items.
* Bidding Items, Public auction your goods on the global market!
* UUID Support.
* GUI Global Market Interface.
* Custom Category selector.
* Support CrazyAuctions data synchronization.
* Click sound.
* Custom(Making...) Item blacklist.
* Json Item Information View Support(Making...)
* PlayerPoints Support(Marking...)
* Item repricing.
* Tax Rate.
* Easy Shop Sign.
* Easy command settings.
* 80% Customizable. (Messages, Permissions, GUI Settings and more)
* Permission group settings. (Number of products available)
* MySQL Support.
* SQLite Support.
* Split Database.
* Feature Toggle.
* Database Backup. (Can be set automatically)
* Database Rollback. (Roll back all data to the specified database backup file)
* Tab Complete.
* Item Collection

# Commands

* /Ca View <Player> - View all items for one player.
* /Ca Sell <Price> [Amount of items] - Let the items in your hand on the market as sold.
* /Ca Bid <Price> [Amount of items] - Let the items in hand be put on the market in the form of an auction.
* /Ca Buy <Reward> [Amount of items] [Item name] - Acquire items in your hands or specific items in the market.
* /Ca Gui [sell/buy/bid] - Open the main GUI of the market.
* /Ca Mail - View and manage your canceled and expired items.
* /Ca Listed - View and manage the items you are selling.
* /Ca Reload [Object] - Reload the plugin settings and database.
* /Ca Admin - Admin commands.
* /Ca Help - View this help menu.
  
# Admin commands:

* /Ca Admin Backup - Back up all data of the plug-in to a local directory (including the database).
* /Ca Admin RollBack [Backup File] - Retrieve all currently stored data (excluding configuration files).
* /Ca Admin Info [Player] - View information about a player.
* /Ca Admin Synchronize - Sync all merchandise and player data stored in the old market to the Plus version.
* /Ca Admin ItemCollection - Collection of management items.
* /Ca Admin ItemColletion Add [DisplayName] - Add items to collection.
* /Ca Admin ItemColletion Delete [DisplayName/UID] - Delete an item in the collection.
* /Ca Admin ItemColletion List - List all items in the collection.
* /Ca Admin ItemColletion Give [DisplayName/UID] [Player] - Give the player an item in the collection.

# How to sync from CrazyAuctions to the Plus version

1. Download CrazyAuctionsPlus.jar and Vault.jar (https://www.spigotmc.org/resources/vault.34315/)
2. Into plugins folder
3. Start the server and wait for the CrazyAuctionsPlus folder to be generated
4. Put the 'Data.yml' file of CrazyAuctions into the 'CrazyAuctionsPlus' folder
5. Use the command: /ca admin synchronize . This process can take seconds or even minutes.
6. Done! All old data has been synced to this version!

PS: You can configure your database (such as MySQL) before step 5. The synchronization command also works for the database mode.
