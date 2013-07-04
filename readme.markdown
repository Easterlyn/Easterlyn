Required .jars
--------------
* [CraftBukkit 1.5.2 R1.0](http://dl.bukkit.org/downloads/craftbukkit/view/02169_1.5.2-R1.0/)
* [PostgreSQL JDBC](http://jdbc.postgresql.org/download.html)

Database Guidelines
-------------------
* Table: PlayerData 
	* Stores all Classpect data
* Table: TexturePackURLs (Resource Packs?)
	* Stores URL for each Region pack
* Table: ChatChannels
	* Stores info for each channel
		- Name
		- Alias
		- Type (Normal, Regional, RP, Nick, Temp)
		- Owner
		- Mods (List?)