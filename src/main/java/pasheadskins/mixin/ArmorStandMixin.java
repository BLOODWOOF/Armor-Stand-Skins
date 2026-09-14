package pasheadskins.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pasheadskins.CapeSource;
import pasheadskins.HeadSkinHolder;
import pasheadskins.PasHeadSkins;

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
	private Identifier pasheadskins$lockedBody;

	@Unique
	private boolean pasheadskins$lockedSlim;

	@Unique
	private boolean pasheadskins$capeEnabled;

	@Unique
	private CapeSource pasheadskins$capeSource = CapeSource.BOTH;

	@Unique
	private String pasheadskins$passwordHash = "";

	@Unique
	private boolean pasheadskins$asthmaForced;

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
		return this.pasheadskins$locked;
	}

	@Override
	public ResolvableProfile pasheadskins$lockedProfile() {
		return this.pasheadskins$lockedProfile;
	}

	@Override
	public void pasheadskins$setLocked(boolean locked, ResolvableProfile profile) {
		if (locked) {
			this.pasheadskins$locked = true;
			if (profile != null) {
				this.pasheadskins$lockedProfile = profile;
			}
		} else {
			this.pasheadskins$locked = false;
			this.pasheadskins$lockedProfile = null;
			this.pasheadskins$lockedCape = null;
			this.pasheadskins$lockedElytra = null;
			this.pasheadskins$lockedBody = null;
			this.pasheadskins$lockedSlim = false;
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
	public Identifier pasheadskins$lockedBody() {
		return this.pasheadskins$lockedBody;
	}

	@Override
	public boolean pasheadskins$lockedSlim() {
		return this.pasheadskins$lockedSlim;
	}

	@Override
	public void pasheadskins$setLockedBody(Identifier body, boolean slim) {
		this.pasheadskins$lockedBody = body;
		this.pasheadskins$lockedSlim = slim;
		if (body != null) {
			this.pasheadskins$locked = true;
		}
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

	@Override
	public String pasheadskins$passwordHash() {
		return this.pasheadskins$passwordHash == null ? "" : this.pasheadskins$passwordHash;
	}

	@Override
	public void pasheadskins$setPasswordHash(String hash) {
		this.pasheadskins$passwordHash = hash == null ? "" : hash;
	}

	@Override
	public boolean pasheadskins$asthmaForced() {
		return this.pasheadskins$asthmaForced;
	}

	@Override
	public void pasheadskins$setAsthmaForced(boolean forced) {
		this.pasheadskins$asthmaForced = forced;
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void pasheadskins$loadLegacyFlags(ValueInput input, CallbackInfo ci) {
		this.pasheadskins$disabled = input.getBooleanOr("HeadSkinDisabled", false);
		this.pasheadskins$lockedProfile = input.read("HeadSkinLockProfile", ResolvableProfile.CODEC).orElse(null);
		this.pasheadskins$locked = input.getBooleanOr("HeadSkinLocked", false);
	}

	@Inject(method = "setInvisible", at = @At("TAIL"), require = 0)
	private void pasheadskins$offHeadSkinWhenInvisible(boolean invisible, CallbackInfo ci) {
		if (!invisible) {
			return;
		}
		PasHeadSkins.onStandInvisible((ArmorStand) (Object) this);
	}

	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void pasheadskins$blockUse(Player player, InteractionHand hand, Vec3 hit, CallbackInfoReturnable<InteractionResult> cir) {
		ArmorStand stand = (ArmorStand) (Object) this;
		if (stand.level().isClientSide()) {
			return;
		}
		if (!PasHeadSkins.mayEdit(player, stand)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void pasheadskins$blockHurt(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		Entity attacker = source.getEntity();
		if (attacker instanceof Player player && !PasHeadSkins.mayEdit(player, (ArmorStand) (Object) this)) {
			cir.setReturnValue(false);
		}
	}
}
