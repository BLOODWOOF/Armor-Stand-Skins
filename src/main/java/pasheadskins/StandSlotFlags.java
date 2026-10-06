package pasheadskins;

import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.ResolvableProfile;

// High bits vanilla never uses for equipment locks (those live in 0-5, 8-13, 16-21).
// Bit 28 marks that we wrote the other flags so "cape off" still syncs.
// Bits 26-27 are the cape source (0 both, 1 mojang, 2 essential).
// Bit 25 is a quiet extra flag.
// DisabledSlots is a full int but vanilla doesnt tracker-sync it. Head Z used to
// carry a copy, but thats cramped and twists the skull a hair. Scale is in the
// same Armor Poser sync packet and actually replicates as an attribute, so the
// flags ride in the leftover millionths there instead.
public final class StandSlotFlags {
	public static final int SOURCE_SHIFT = 26;
	public static final int SOURCE_MASK = 3 << SOURCE_SHIFT;
	public static final int ASTHMA = 1 << 25;
	public static final int PRESENT = 1 << 28;
	public static final int CAPE = 1 << 29;
	public static final int HEAD_OFF = 1 << 30;
	public static final int LOCK = 1 << 31;
	public static final int MASK = PRESENT | CAPE | HEAD_OFF | LOCK | SOURCE_MASK | ASTHMA;

	private static final String SLOTS_KEY = "DisabledSlots";
	private static final String POSE_KEY = "Pose";
	private static final String SCALE_KEY = "Scale";
	private static final float STEP = 0.01F;
	private static final double SCALE_UNIT = 1_000_000.0;
	private static final int SCALE_CODE_MOD = 1000;

	private static final ThreadLocal<Integer> remembered = ThreadLocal.withInitial(() -> 0);
	private static final ThreadLocal<Float> rememberedScale = ThreadLocal.withInitial(() -> 1.0F);

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

	public static boolean asthmaOn(ArmorStand stand) {
		return (flagsOf(stand) & ASTHMA) != 0;
	}

	public static CapeSource capeSource(ArmorStand stand) {
		return CapeSource.fromWire((flagsOf(stand) >>> SOURCE_SHIFT) & 3);
	}

	public static int flagsOf(ArmorStand stand) {
		int fromScale = flagsFromScale(scaleValue(stand));
		if ((fromScale & PRESENT) != 0) {
			return fromScale;
		}
		// older jars stuffed this into Head Z; still honor that copy
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
		boolean asthmaOn = holder.pasheadskins$asthmaForced();
		CapeSource source = holder.pasheadskins$capeSource();
		if (!headOff && !capeOn && !lockOn && !asthmaOn && source == CapeSource.BOTH && (holder.pasheadskins$disabledSlots() & PRESENT) == 0) {
			return 0;
		}
		return pack(headOff, capeOn, lockOn, source, asthmaOn);
	}

	public static int pack(boolean headOff, boolean capeOn, boolean lockOn, CapeSource source, boolean asthmaOn) {
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
		if (asthmaOn) {
			bits |= ASTHMA;
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
		// With the mod on the server the payloads carry the flags, so nothing
		// rides in scale or pose. This channel only serves client-only setups.
		if (HeadSkinFlags.packetChannelOpen()) {
			return;
		}
		if (!(stand instanceof HeadSkinHolder holder)) {
			return;
		}
		int flags = packFromHolder(stand);
		if (flags == 0) {
			return;
		}
		holder.pasheadskins$setDisabledSlots(merge(holder.pasheadskins$disabledSlots(), flags));
		encodeScale(stand, flags);
		stand.setHeadPose(encodeHead(stand.getHeadPose(), flags));
		remembered.set(flags);
		rememberedScale.set(visualScale(scaleValue(stand)));
	}

	// One-time cleanup for stands that still carry the hidden record from
	// older jars: strip the flag bits from DisabledSlots, scale, and head
	// pose. The vanilla lock bits from StandPassGuard stay.
	public static void scrubFromStand(ArmorStand stand) {
		if (!(stand instanceof HeadSkinHolder holder)) {
			return;
		}
		holder.pasheadskins$setDisabledSlots(holder.pasheadskins$disabledSlots() & ~MASK);
		try {
			var attr = stand.getAttribute(Attributes.SCALE);
			if (attr != null) {
				attr.setBaseValue(visualScale((float) attr.getBaseValue()));
			}
		} catch (Throwable ignored) {
		}
		Rotations head = stand.getHeadPose();
		stand.setHeadPose(new Rotations(head.x(), head.y(), visualHeadZ(head.z())));
	}

	public static void remember(ArmorStand stand) {
		remembered.set(packFromHolder(stand));
		rememberedScale.set(visualScale(scaleValue(stand)));
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
		keepScaleInTag(tag, flags);
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
		tag.putDouble(SCALE_KEY, encodedScale(rememberedScale.get(), flags));
		PoserNameVisible.writeCurrent(stand, tag);
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
		holder.pasheadskins$setAsthmaForced(asthmaOn(stand));
		if (lockOn(stand) && !holder.pasheadskins$isLocked()) {
			ResolvableProfile profile = holder.pasheadskins$lockedProfile();
			if (profile == null) {
				profile = HeadSkinFlags.lockedProfile(stand);
			}
			if (profile == null) {
				profile = helmetProfile(stand);
			}
			if (profile != null || holder.pasheadskins$lockedBody() != null) {
				holder.pasheadskins$setLocked(true, profile);
			} else {
				holder.pasheadskins$setLocked(true, null);
			}
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

	private static void keepScaleInTag(CompoundTag tag, int flags) {
		if (flags == 0) {
			return;
		}
		float visual = rememberedScale.get();
		if (tag.contains(SCALE_KEY)) {
			visual = visualScale((float) tag.getDoubleOr(SCALE_KEY, visual));
		}
		tag.putDouble(SCALE_KEY, encodedScale(visual, flags));
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
		if ((flags & ASTHMA) != 0) {
			encoded += 48;
		}
		return stripPoseZ(z) + encoded * STEP;
	}

	public static float visualHeadZ(float z) {
		return stripPoseZ(z);
	}

	public static float visualScale(float scale) {
		long millionths = Math.round(scale * SCALE_UNIT);
		int code = (int) Math.floorMod(millionths, SCALE_CODE_MOD);
		if (code == 0) {
			return scale;
		}
		return (float) ((millionths - code) / SCALE_UNIT);
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
		int asthma = 0;
		if (n >= 49) {
			n -= 48;
			asthma = ASTHMA;
		}
		int flags = 0;
		if (n >= 1 && n <= 15) {
			flags = n << 28;
		} else if (n >= 17 && n <= 31) {
			int nibble = n - 16;
			if (nibble >= 1 && nibble <= 15) {
				flags = (nibble << 28) | (1 << SOURCE_SHIFT);
			}
		} else if (n >= 33 && n <= 47) {
			int nibble = n - 32;
			if (nibble >= 1 && nibble <= 15) {
				flags = (nibble << 28) | (2 << SOURCE_SHIFT);
			}
		}
		if (flags == 0) {
			return 0;
		}
		return flags | asthma;
	}

	private static boolean encodedPoseCode(int n) {
		return n >= 1 && n <= 15 || n >= 17 && n <= 31 || n >= 33 && n <= 47
			|| n >= 49 && n <= 63 || n >= 65 && n <= 79 || n >= 81 && n <= 95;
	}

	private static int poseCode(float z) {
		int hundredths = Math.round(z * 100.0F);
		return Math.floorMod(hundredths, 100);
	}

	private static int flagsFromScale(float scale) {
		long millionths = Math.round(scale * SCALE_UNIT);
		int code = (int) Math.floorMod(millionths, SCALE_CODE_MOD);
		return expand(code);
	}

	private static int compact(int flags) {
		int code = 1;
		if ((flags & HEAD_OFF) != 0) {
			code |= 2;
		}
		if ((flags & CAPE) != 0) {
			code |= 4;
		}
		if ((flags & LOCK) != 0) {
			code |= 8;
		}
		if ((flags & ASTHMA) != 0) {
			code |= 16;
		}
		code |= ((flags >>> SOURCE_SHIFT) & 3) << 5;
		return code;
	}

	private static int expand(int code) {
		if (code < 1) {
			return 0;
		}
		int flags = PRESENT;
		if ((code & 2) != 0) {
			flags |= HEAD_OFF;
		}
		if ((code & 4) != 0) {
			flags |= CAPE;
		}
		if ((code & 8) != 0) {
			flags |= LOCK;
		}
		if ((code & 16) != 0) {
			flags |= ASTHMA;
		}
		flags |= ((code >>> 5) & 3) << SOURCE_SHIFT;
		return flags;
	}

	private static double encodedScale(float visual, int flags) {
		float size = visual;
		if (size > 15.998F) {
			size = 15.998F;
		}
		if (size < 0.063F) {
			size = 0.063F;
		}
		return size + compact(flags) / SCALE_UNIT;
	}

	private static void encodeScale(ArmorStand stand, int flags) {
		try {
			var attr = stand.getAttribute(Attributes.SCALE);
			if (attr == null) {
				return;
			}
			attr.setBaseValue(encodedScale(visualScale((float) attr.getBaseValue()), flags));
		} catch (Throwable ignored) {
		}
	}

	private static float scaleValue(ArmorStand stand) {
		try {
			var attr = stand.getAttribute(Attributes.SCALE);
			if (attr != null) {
				return (float) attr.getBaseValue();
			}
		} catch (Throwable ignored) {
		}
		return stand.getScale();
	}
}
