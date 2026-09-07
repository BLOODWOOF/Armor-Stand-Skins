package pasheadskins;

import net.minecraft.world.item.component.ResolvableProfile;

public interface HeadSkinHolder {
	boolean pasheadskins$isDisabled();

	void pasheadskins$setDisabled(boolean disabled);

	boolean pasheadskins$isLocked();

	ResolvableProfile pasheadskins$lockedProfile();

	void pasheadskins$setLocked(boolean locked, ResolvableProfile profile);
}
