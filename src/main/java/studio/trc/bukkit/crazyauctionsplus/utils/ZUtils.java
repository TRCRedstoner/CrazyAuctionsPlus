package studio.trc.bukkit.crazyauctionsplus.utils;

import java.util.Arrays;

import org.bukkit.Material;

public abstract class ZUtils {

	private transient Material[] byId;

	@SuppressWarnings("deprecation")
	public ZUtils() {
		byId = new Material[0];
		for (Material material : Material.values()) {
			if (byId.length > material.getId()) {
				byId[material.getId()] = material;
			} else {
				byId = Arrays.copyOfRange(byId, 0, material.getId() + 2);
				byId[material.getId()] = material;
			}
		}
	}

	/**
	 * @param id
	 * @return the material according to his id
	 */
	protected Material getMaterial(int id) {
		return byId.length > id && id >= 0 ? byId[id] : null;
	}

}
