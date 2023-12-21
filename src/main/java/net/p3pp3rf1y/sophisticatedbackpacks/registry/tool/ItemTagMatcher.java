package net.p3pp3rf1y.sophisticatedbackpacks.registry.tool;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;

import java.util.function.Predicate;

class ItemTagMatcher implements Predicate<ItemStack> {
	private final TagKey<Item> itemTag;

	public ItemTagMatcher(TagKey<Item> itemTag) {
		this.itemTag = itemTag;
	}

	@Override
	public boolean test(ItemStack stack) {
		return stack.isIn(itemTag);
	}
}
