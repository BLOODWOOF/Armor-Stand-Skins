package pasheadskins;

import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.ResolvableProfile;

// High bits vanilla never uses for equipment locks (those live in 0-5, 8-13, 16-21).
// Bit 28 marks that we wrote the other flags so "cape off" still syncs.
// Bits 26-27 are the cape source (0 both, 1 mojang, 2 essential).
// DisabledSlots is a full int (no mask) but vanilla doesnt tracker-sync it, so the
// same nibble also rides on Head Z. Pose is allowed and actually replicates.
public final class StandSlotFlags {
	public static final int SOURCE_SHIFT = 26;
	public static final int SOURCE_MASK = 3 << SOURCE_SHIFT;
	public static final int PRESENT = 1 << 28;
	public static final int CAPE = 1 << 29;
	public static final int HEAD_OFF = 1 << 30;
	public static final int LOCK = 1 << 31;
	public static final int MASK = PRESENT | CAPE | HEAD_OFF | LOCK | SOURCE_MASK;

	private static final String SLOTS_KEY = "DisabledSlots";
	private static final String POSE_KEY = "Pose";
	private static final float STEP = 0.01F;

	private static final ThreadLocal<Integer> remembered = ThreadLocal.withInitial(() -> 0);

	private StandSlotFlags() {
	}

	public static boolean hasRecord(ArmorStand stand) {
		return (flagsOf(stand) & PRESENT) != 0;
	}

	public static boolean headOff(ArmorStand stand) {
		return (flagsOf(stand) & HEAD_OFF) != 0;
	}

	public static boolean capeOn(ArmorStand stand) {
		return (flagsOf(stand) & CAPE) != 0;
	}

	public static boolean lockOn(ArmorStand stand) {
		return (flagsOf(stand) & LOCK) != 0;
	}

	public static CapeSource capeSource(ArmorStand stand) {
		return CapeSource.fromWire((flagsOf(stand) >>> SOURCE_SHIFT) & 3);
	}

	public static int flagsOf(ArmorStand stand) {
		// Pose Z actually tracker-syncs. DisabledSlots often stays local, so
		// remote clients have to trust the pose copy first.
		int fromPose = flagsFromPoseZ(stand.getHeadPose().z());
		if ((fromPose & PRESENT) != 0) {
			return fromPose;
		}
		if (stand instanceof HeadSkinHolder holder && (holder.pasheadskins$disabledSlots() & PRESENT) != 0) {
			return holder.pasheadskins$disabledSlots() & MASK;
		}
		return 0;
	}

	public static int packFromHolder(ArmorStand stand) {
		if (!(stand instanceof HeadSkinHolder holder)) {
			return 0;
		}
		boolean headOff = holder.pasheadskins$isDisabled();
		boolean capeOn = holder.pasheadskins$isCapeEnabled();
		boolean lockOn = holder.pasheadskins$isLocked();
		CapeSource source = holder.pasheadskins$capeSource();
		if (!headOff && !capeOn && !lockOn && source == CapeSource.BOTH && (holder.pasheadskins$disabledSlots() & PRESENT) == 0) {
			return 0;
		}
		return pack(headOff, capeOn, lockOn, source);
	}

	public static int pack(boolean headOff, boolean capeOn, boolean lockOn, CapeSource source) {
		int bits = PRESENT;
		if (headOff) {
			bits |= HEAD_OFF;
		}
		if (capeOn) {
			bits |= CAPE;
		}
		if (lockOn) {
			bits |= LOCK;
		}
		if (source != null && source != CapeSource.BOTH) {
			bits |= (source.wire() & 3) << SOURCE_SHIFT;
		}
		return bits;
	}

	public static int merge(int slots, int flags) {
		return (slots & ~MASK) | (flags & MASK);
	}

	public static void writeOntoStand(ArmorStand stand) {
		if (!(stand instanceof HeadSkinHolder holder)) {
			return;
		}
		int flags = packFromHolder(stand);
		if (flags == 0) {
			return;
		}
		holder.pasheadskins$setDisabledSlots(merge(holder.pasheadskins$disabledSlots(), flags));
		stand.setHeadPose(encodeHead(stand.getHeadPose(), flags));
		remembered.set(flags);
	}

	public static void remember(ArmorStand stand) {
		remembered.set(packFromHolder(stand));
	}

	public static void keepInTag(CompoundTag tag) {
		int flags = remembered.get();
		if (tag == null || flags == 0) {
			return;
		}
		if (tag.contains(SLOTS_KEY)) {
			tag.putInt(SLOTS_KEY, merge(tag.getIntOr(SLOTS_KEY, 0), flags));
		}
		keepPoseInTag(tag, flags);
	}

	public static CompoundTag syncTag(ArmorStand stand) {
		writeOntoStand(stand);
		CompoundTag tag = new CompoundTag();
		if (!(stand instanceof HeadSkinHolder holder)) {
			return tag;
		}
		int flags = remembered.get();
		if (flags == 0) {
			return tag;
		}
		tag.putInt(SLOTS_KEY, holder.pasheadskins$disabledSlots());
		CompoundTag pose = new CompoundTag();
		pose.store("Head", Rotations.CODEC, encodeHead(stand.getHeadPose(), flags));
		pose.store("Body", Rotations.CODEC, stand.getBodyPose());
		pose.store("LeftArm", Rotations.CODEC, stand.getLeftArmPose());
		pose.store("RightArm", Rotations.CODEC, stand.getRightArmPose());
		pose.store("LeftLeg", Rotations.CODEC, stand.getLeftLegPose());
		pose.store("RightLeg", Rotations.CODEC, stand.getRightLegPose());
		tag.put(POSE_KEY, pose);
		return tag;
	}

	public static void applyToHolder(ArmorStand stand) {
		if (!(stand instanceof HeadSkinHolder holder) || !hasRecord(stand)) {
			return;
		}
		holder.pasheadskins$setDisabled(headOff(stand));
		holder.pasheadskins$setCapeEnabled(capeOn(stand));
		holder.pasheadskins$setCapeSource(capeSource(stand));
		if (lockOn(stand)) {
			if (!holder.pasheadskins$isLocked()) {
				ResolvableProfile helmet = helmetProfile(stand);
				holder.pasheadskins$setLocked(helmet != null, helmet);
			}
		} else if (holder.pasheadskins$isLocked()) {
			holder.pasheadskins$setLocked(false, null);
		}
	}

	public static ResolvableProfile helmetProfile(ArmorStand stand) {
		return stand.getItemBySlot(EquipmentSlot.HEAD).get(DataComponents.PROFILE);
	}

	private static void keepPoseInTag(CompoundTag tag, int flags) {
		if (!tag.contains(POSE_KEY) || flags == 0) {
			return;
		}
		CompoundTag pose = tag.getCompoundOrEmpty(POSE_KEY);
		pose.read("Head", Rotations.CODEC).ifPresent(head -> {
			pose.store("Head", Rotations.CODEC, encodeHead(head, flags));
		});
	}

	private static Rotations encodeHead(Rotations head, int flags) {
		return new Rotations(head.x(), head.y(), encodePoseZ(head.z(), flags));
	}

	private static float encodePoseZ(float z, int flags) {
		int nibble = (flags >>> 28) & 0xF;
		int source = (flags >>> SOURCE_SHIFT) & 3;
		int encoded = nibble;
		if (source > 0) {
			encoded = source * 16 + nibble;
		}
		return stripPoseZ(z) + encoded * STEP;
	}

	private static float stripPoseZ(float z) {
		int n = poseCode(z);
		if (encodedPoseCode(n)) {
			return z - n * STEP;
		}
		return z;
	}

	private static int flagsFromPoseZ(float z) {
		int n = poseCode(z);
		if (n >= 1 && n <= 15) {
			return n << 28;
		}
		if (n >= 17 && n <= 31) {
			int nibble = n - 16;
			if (nibble >= 1 && nibble <= 15) {
				return (nibble << 28) | (1 << SOURCE_SHIFT);
			}
		}
		if (n >= 33 && n <= 47) {
			int nibble = n - 32;
			if (nibble >= 1 && nibble <= 15) {
				return (nibble << 28) | (2 << SOURCE_SHIFT);
			}
		}
		return 0;
	}

	private static boolean encodedPoseCode(int n) {
		return n >= 1 && n <= 15 || n >= 17 && n <= 31 || n >= 33 && n <= 47;
	}

	private static int poseCode(float z) {
		int hundredths = Math.round(z * 100.0F);
		return Math.floorMod(hundredths, 100);
	}
}
