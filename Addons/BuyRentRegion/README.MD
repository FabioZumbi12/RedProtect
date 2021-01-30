## RedProtect ADD-ON: BuyRentRegions

This add-on allows you to create signs which can be used to buy or rent WorldGuard/RedProtect regions. The plugin works
with all Vault supported economy systems as it requires Vault for economy support.

### Permissions:

* buyrentregion.command.create - allows the creation of \[BuyRegion\] and \[RentRegion\] signs.
* buyrentregion.command.admin - allows usage of admin commands.
* buyrentregion.command.exempt - allows player to bypass BuyRegionMax
* buyrentregion.command.buy - allows player to buy a region (if config requires)
* buyrentregion.command.rent - allows player to rent a region (if config requires)

### Signs:

**Create a BuyRegion Sign**

```
[BuyRegion]
<regionname>
<price>
unused/any
```

Example Sign:

```
[BuyRegion]
epic_store
200
```

_The previous would make a sign that would sell region 'epic_store' for '200' of your economy currency._

**Create a RentRegion Sign**

```
[RentRegion]
<regionname>
<price>
<timespan>
```

Example Sign:

```
[RentRegion]
epic_store
200
7 days
```

_The previous would make a sign that would rent region 'epic_store' for '200' of your economy currency for 7 days. Once
rented properties expire, the region is automatically put back up for rent. If the renter has auto-renew turned on, the
property will be renewed to them if they have the available money. See notes for information about sign replacement._

**Buy a Region**

1. The player must enter buy mode by typing /buyrentregion
1. The player must right-click a BuyRegion sign.

_If the player right-clicks a BuyRegion sign while not in buy mode, they will be prompted to enter buy mode.  
If RequireBuyMode is set to false the player will not be required to enter buy mode before buying or renting a region._

**Rent a Region**  
Same as buying a region, see above.

### Commands:

**Player Commands**

```
/buyrentregion - toggles buy mode  
/buyrentregion renew <region> - renews rental on <region>  
/buyrentregion autorenew - shows current autorenew status  
/buyrentregion autorenew <true/false> - sets auto-renew for all of the player's rentals  
/buyrentregion help - shows help screen  
```

**Admin Commands**

```
/buyrentregion reload - reloads config and locale  
/buyrentregion buycheck <player> - checks total bought regions for <player>  
/buyrentregion rentcheck <player> - checks total rented regions for <player>  
/buyrentregion buyset <player> <amount> - sets total bought regions for <player>  
/buyrentregion rentset <player> <amount> - sets total rented regions for <player>  
/buyrentregion buymax - displays current BuyRegionMax  
/buyrentregion buymax <amount> - sets BuyRegionMax  
/buyrentregion rentmax - displays current RentRegionMax  
/buyrentregion rentmax <amount> - sets RentRegionMax  
/buyrentregion buymode - shows the current RequireBuyMode setting  
/buyrentregion buymode <true/false> - sets RequireBuyMode  
/buyrentregion buyperms - shows the current RequireBuyPerms setting  
/buyrentregion buyperms <true/false> - sets RequireBuyPerms  
/buyrentregion rentperms - shows the current RequireRentPerms setting  
/buyrentregion rentperms <true/false> - sets RequireRentPerms  
/buyrentregion evict <region> - performs an eviction on <region>  
/buyrentregion help - shows all commands  
```

_(note: Setting BuyRegionMax or RentRegionMax to 0 makes it unlimited)_

### Timespans

**Timespan Examples**

```
1 day
15 secs
20 mins
5 h
```

**Accepted Time Values**

```
Seconds: s, sec, secs, second, seconds
Minutes: m, min, mins, minute, minutes
Hours: h, hour, hours
Days: d, day, days
```

### Config Options

* SignHeaderBuy - top line of signs for buy regions, default \[BuyRegion\]
* SignHeaderRent - top line of signs for rent regions, default \[RentRegion\]
* PayRentOwners - pays rent to owner of region
* BuyRegionMax - max regions a player can buy with BuyRegion
* RentRegionMax - max regions a player can rent with BuyRegion
* CheckExpirationsInMins - how often to check for expired rented regions IN MINUTES
* RequireBuyMode - is buy mode required to be on to use buy/rent signs? Recommended: true
* RequireBuyPerms - require the player to have buyrentregion.command.buy to buy a region
* RequireRentPerms - require the player to have buyrentregion.command.rent to rent a region
* DateFormat - Sets the date format - Accepted Values: Default, US, EU

**Date Formats**

* Default - year/month/day
* US - month/day/year
* EU - day/month/year

**Message Configuration**

* All messages that are sent to a player can be changed by editing your config.yml in the 'Messages' section.
* Between the name of the field and the contents you should be able to figure out which is which.
* Sentences that seem to just end will probably have the value placed there... consider this when making changes.
* No admin messages can be changed... would be a lot of work on my end for nothing.

### Notes

* Auto-renew is defaulted to OFF for all players. They will only need to turn it on once, however.
* Only use 1 \[RentRegion\] sign per rentable region. For performance reasons it does not properly support multiple
  signs per region. It will let you create them, but it will probably lead to strange behavior. This applies to
  \[BuyRegion\] signs also.
* I would recommend trying to avoid original RentRegion signs being destroyed... the replacement of them is still a
  little sketchy for positioning. I do plan to look at this but was trying to get a release out with the renting for all
  the people currently dead in the water from that other plugin going inactive.
* The CheckExpirationsInMins config setting can accept decimals... so 0.05 = 3 seconds for instance. Although I wouldn't
  recommend going less than 0.5 for that setting. With a lot of rented regions it could be a taxing operation. Best left
  to 5+ minutes for average servers that rent things for days, and not minutes or seconds.
* I didn't bother making the signs indestructible as the idea is to place it inside the already defined protected
  region... which would typically make it unbreakable anyways. Furthermore, for performance reasons I'm trying very hard
  to avoid hooking the block break event.

### License/About Dev:

This plugin was originally on Spigot in this link: https://www.spigotmc.org/resources/buyregion-1-13.60189/ by **
Luke199** and **GentleGravel**.  
People using the previous plugin w/ RedProtect can move all rentedRegions from ``plugins/BuyRegion`` folder to
new ``plugins/BuyRentRegion`` of this add-on to migrate rents and configs.

### Download:

Download: https://github.com/FabioZumbi12/RedProtect/tree/master/add-ons