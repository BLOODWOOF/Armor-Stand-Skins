package pasheadskins;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;

public interface HeadSkinHolder {
	boolean pasheadskins$isDisabled();

	void pasheadskins$setDisabled(boolean disabled);

	boolean pasheadskins$isLocked();

	ResolvableProfile pasheadskins$lockedProfile();

	void pasheadskins$setLocked(boolean locked, ResolvableProfile profile);

	Identifier pasheadskins$lockedCape();

	Identifier pasheadskins$lockedElytra();

	void pasheadskins$setLockedCloak(Identifier cape, Identifier elytra);

	Identifier pasheadskins$lockedBody();

	boolean pasheadskins$lockedSlim();

	void pasheadskins$setLockedBody(Identifier body, boolean slim);

	boolean pasheadskins$isCapeEnabled();

	void pasheadskins$setCapeEnabled(boolean enabled);

	CapeSource pasheadskins$capeSource();

	void pasheadskins$setCapeSource(CapeSource source);

	int pasheadskins$disabledSlots();

	void pasheadskins$setDisabledSlots(int slots);

	String pasheadskins$passwordHash();

	void pasheadskins$setPasswordHash(String hash);

	boolean pasheadskins$asthmaForced();

	void pasheadskins$setAsthmaForced(boolean forced);
}
