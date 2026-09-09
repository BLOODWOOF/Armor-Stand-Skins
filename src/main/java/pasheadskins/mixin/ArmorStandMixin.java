package pasheadskins.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pasheadskins.CapeSource;
import pasheadskins.HeadSkinHolder;

@Mixin(ArmorStand.class)
public class ArmorStandMixin implements HeadSkinHolder {
	@Shadow
	private int disabledSlots;

	@Unique
	private boolean pasheadskins$disabled;

	@Unique
	private boolean pasheadskins$locked;

	@Unique
	private ResolvableProfile pasheadskins$lockedProfile;

	@Unique
	private Identifier pasheadskins$lockedCape;

	@Unique
	private Identifier pasheadskins$lockedElytra;

	@Unique
	private boolean pasheadskins$capeEnabled;

	@Unique
	private CapeSource pasheadskins$capeSource = CapeSource.BOTH;

	@Override
	public boolean pasheadskins$isDisabled() {
		return this.pasheadskins$disabled;
	}

	@Override
	public void pasheadskins$setDisabled(boolean disabled) {
		this.pasheadskins$disabled = disabled;
	}

	@Override
	public boolean pasheadskins$isLocked() {
		return this.pasheadskins$locked && this.pasheadskins$lockedProfile != null;
	}

	@Override
	public ResolvableProfile pasheadskins$lockedProfile() {
		return this.pasheadskins$lockedProfile;
	}

	@Override
	public void pasheadskins$setLocked(boolean locked, ResolvableProfile profile) {
		if (locked && profile != null) {
			this.pasheadskins$locked = true;
			this.pasheadskins$lockedProfile = profile;
		} else {
			this.pasheadskins$locked = false;
			this.pasheadskins$lockedProfile = null;
			this.pasheadskins$lockedCape = null;
			this.pasheadskins$lockedElytra = null;
		}
	}

	@Override
	public Identifier pasheadskins$lockedCape() {
		return this.pasheadskins$lockedCape;
	}

	@Override
	public Identifier pasheadskins$lockedElytra() {
		return this.pasheadskins$lockedElytra;
	}

	@Override
	public void pasheadskins$setLockedCloak(Identifier cape, Identifier elytra) {
		this.pasheadskins$lockedCape = cape;
		this.pasheadskins$lockedElytra = elytra;
	}

	@Override
	public boolean pasheadskins$isCapeEnabled() {
		return this.pasheadskins$capeEnabled;
	}

	@Override
	public void pasheadskins$setCapeEnabled(boolean enabled) {
		this.pasheadskins$capeEnabled = enabled;
	}

	@Override
	public CapeSource pasheadskins$capeSource() {
		return this.pasheadskins$capeSource == null ? CapeSource.BOTH : this.pasheadskins$capeSource;
	}

	@Override
	public void pasheadskins$setCapeSource(CapeSource source) {
		this.pasheadskins$capeSource = source == null ? CapeSource.BOTH : source;
	}

	@Override
	public int pasheadskins$disabledSlots() {
		return this.disabledSlots;
	}

	@Override
	public void pasheadskins$setDisabledSlots(int slots) {
		this.disabledSlots = slots;
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void pasheadskins$loadLegacyFlags(ValueInput input, CallbackInfo ci) {
		this.pasheadskins$disabled = input.getBooleanOr("HeadSkinDisabled", false);
		this.pasheadskins$lockedProfile = input.read("HeadSkinLockProfile", ResolvableProfile.CODEC).orElse(null);
		this.pasheadskins$locked = input.getBooleanOr("HeadSkinLocked", false) && this.pasheadskins$lockedProfile != null;
	}
}
