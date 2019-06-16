package world.bentobox.challenges.utils;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;


/**
 * Util methods used in different situations.
 */
public class Utils
{
	/**
	 * This method groups input items in single itemstack with correct amount and returns it.
	 * Allows to remove duplicate items from list.
	 * @param requiredItems Input item list
	 * @return List that contains unique items that cannot be grouped.
	 */
	public static List<ItemStack> groupEqualItems(List<ItemStack> requiredItems)
	{
		List<ItemStack> returnItems = new ArrayList<>(requiredItems.size());

		// Group all equal items in singe stack, as otherwise it will be too complicated to check if all
		// items are in players inventory.
		for (ItemStack item : requiredItems)
		{
			boolean isUnique = true;

			int i = 0;
			final int requiredSize = returnItems.size();

			while (i < requiredSize && isUnique)
			{
				ItemStack required = returnItems.get(i);

				// Merge items which meta can be ignored or is similar to item in required list.
				if (Utils.canIgnoreMeta(item.getType()) && item.getType().equals(required.getType()) ||
					required.isSimilar(item))
				{
					required.setAmount(required.getAmount() + item.getAmount());
					isUnique = false;
				}

				i++;
			}

			if (isUnique)
			{
				// The same issue as in other places. Clone prevents from changing original item.
				returnItems.add(item.clone());
			}
		}

		return returnItems;
	}


	/**
	 * This method returns if meta data of these items can be ignored. It means, that items will be searched
	 * and merged by they type instead of using ItemStack#isSimilar(ItemStack) method.
	 *
	 * This limits custom Challenges a lot. It comes from ASkyBlock times, and that is the reason why it is
	 * still here. It would be a great Challenge that could be completed by collecting 4 books, that cannot
	 * be crafted. Unfortunately, this prevents it.
	 * The same happens with firework rockets, enchanted books and filled maps.
	 * In future it should be able to specify, which items meta should be ignored when adding item in required
	 * item list.
	 *
	 * @param material Material that need to be checked.
	 * @return True if material meta can be ignored, otherwise false.
	 */
	public static boolean canIgnoreMeta(Material material)
	{
		return material.equals(Material.FIREWORK_ROCKET) ||
			material.equals(Material.ENCHANTED_BOOK) ||
			material.equals(Material.WRITTEN_BOOK) ||
			material.equals(Material.FILLED_MAP);
	}
}
