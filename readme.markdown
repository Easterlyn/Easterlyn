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
		- SendAccess (Public, Private)
		- ListenAccess (Public, Private)

		
Note for Dublek: Chat messages are formatted thusly
	[$channel]<$player> $message
where:
	[]:
		WHITE
	$channel:
		AQUA if sender = owner
		RED if sender = mod
		else GOLD
	<>:
		GREEN if Region = Earth
		YELLOW if Region = InnerCircle
		PURPLE if Region = OuterCircle
		GRAY? if Region = FurthestRing
		Medium colors tbd
	$player:
		DARKRED if admin
		BLUE if mod
		GOLD if Godtier (not yet implemented)
		GREEN if donator
		else WHITE
		note: this also applies to overhead nametag via TagAPI