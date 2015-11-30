package br.net.fabiozumbi12.RedProtect.API;

import br.net.fabiozumbi12.RedProtect.RPEconomy;
import br.net.fabiozumbi12.RedProtect.Region;

public class RPEconomyAPI {

	/**
	 * Put a region to sell.
	 * <p>
	 * @param region - Region to sell.
	 * @param owner - Owner of the region;
	 * @param value - Value to sell this region;
	 * @return {@code true} if successfull sell flag. {@code false} if theres an error on sell the region and the money will return to player.
	 */
	public static boolean SellRegion(Region region, String owner, long value){
		return RPEconomy.putToSell(region, owner, value);
	}
	
	/**
	 * Buy a region.
	 * <p>
	 * @param region - Region to buy.
	 * @param owner - Owner of the region;
	 * @return {@code true} if successfull buy. {@code false} if theres an error on buy the region and the money will return to player.
	 */
	public static boolean BuyRegion(Region region, String owner){
		return RPEconomy.BuyRegion(region, owner);
	}
	
	/**
	 * Get the region value based on blocks, chests, itens inside chests and item enchantements inside chests too.
	 * <p>
	 * @param region - Region to get value.
	 * @return {@code Long} value of the region.
	 */
	public static long getRegionValue(Region region){
		return RPEconomy.getRegionValue(region);
	}
}
