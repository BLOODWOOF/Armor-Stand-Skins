package pasheadskins;

import com.mojang.blaze3d.platform.NativeImage;
import dev.tr7zw.skinlayers.SkinUtil;
import dev.tr7zw.skinlayers.accessor.ModelPartInjector;
import dev.tr7zw.skinlayers.accessor.PlayerSettings;
import dev.tr7zw.skinlayers.api.Mesh;
import dev.tr7zw.skinlayers.api.MeshHelper;
import dev.tr7zw.skinlayers.api.OffsetProvider;
import dev.tr7zw.skinlayers.api.SkinLayersAPI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

final class SkinLayerSupport {
	private static final Map<LayerKey, CachedLayers> LAYER_CACHE = new ConcurrentHashMap<>();

	private SkinLayerSupport() {
	}

	static void apply(HeadStandModel model, Identifier texture, boolean slim) {
		CachedLayers layers = LAYER_CACHE.computeIfAbsent(new LayerKey(texture, slim), key -> new CachedLayers());
		if (!buildIfNeeded(layers, texture, slim)) {
			return;
		}

		inject(model.hat, layers.getHeadMesh(), OffsetProvider.HEAD);
		inject(model.jacket, layers.getTorsoMesh(), OffsetProvider.BODY);
		inject(model.leftPants, layers.getLeftLegMesh(), OffsetProvider.LEFT_LEG);
		inject(model.rightPants, layers.getRightLegMesh(), OffsetProvider.RIGHT_LEG);
		if (slim) {
			inject(model.leftSleeve, layers.getLeftArmMesh(), OffsetProvider.LEFT_ARM_SLIM);
			inject(model.rightSleeve, layers.getRightArmMesh(), OffsetProvider.RIGHT_ARM_SLIM);
		} else {
			inject(model.leftSleeve, layers.getLeftArmMesh(), OffsetProvider.LEFT_ARM);
			inject(model.rightSleeve, layers.getRightArmMesh(), OffsetProvider.RIGHT_ARM);
		}
	}

	private static boolean buildIfNeeded(CachedLayers layers, Identifier texture, boolean slim) {
		if (texture.equals(layers.getCurrentSkin()) && slim == layers.hasThinArms() && layers.getHeadMesh() != null) {
			return true;
		}

		NativeImage image = SkinUtil.getTexture(texture, null);
		if (image == null || image.getWidth() != 64 || image.getHeight() != 64) {
			return layers.getHeadMesh() != null;
		}

		MeshHelper helper = SkinLayersAPI.getMeshHelper();
		layers.setLeftLegMesh(helper.create3DMesh(image, 4, 12, 4, 0, 48, true, 0.0F));
		layers.setRightLegMesh(helper.create3DMesh(image, 4, 12, 4, 0, 32, true, 0.0F));
		if (slim) {
			layers.setLeftArmMesh(helper.create3DMesh(image, 3, 12, 4, 48, 48, true, -2.0F));
			layers.setRightArmMesh(helper.create3DMesh(image, 3, 12, 4, 40, 32, true, -2.0F));
		} else {
			layers.setLeftArmMesh(helper.create3DMesh(image, 4, 12, 4, 48, 48, true, -2.0F));
			layers.setRightArmMesh(helper.create3DMesh(image, 4, 12, 4, 40, 32, true, -2.0F));
		}
		layers.setTorsoMesh(helper.create3DMesh(image, 8, 12, 4, 16, 32, true, 0.0F));
		layers.setHeadMesh(helper.create3DMesh(image, 8, 8, 8, 32, 0, false, 0.6F));
		layers.setCurrentSkin(texture);
		layers.setThinArms(slim);
		return true;
	}

	private static void inject(ModelPart part, Mesh mesh, OffsetProvider offset) {
		if (part == null) {
			return;
		}

		part.visible = true;
		part.skipDraw = false;
		((ModelPartInjector) (Object) part).setInjectedMesh(mesh, offset);
	}

	private record LayerKey(Identifier texture, boolean slim) {
	}

	private static final class CachedLayers implements PlayerSettings {
		private Mesh head;
		private Mesh torso;
		private Mesh leftArm;
		private Mesh rightArm;
		private Mesh leftLeg;
		private Mesh rightLeg;
		private Identifier currentSkin;
		private boolean thinArms;

		@Override
		public Mesh getHeadMesh() {
			return head;
		}

		@Override
		public Mesh getTorsoMesh() {
			return torso;
		}

		@Override
		public Mesh getLeftArmMesh() {
			return leftArm;
		}

		@Override
		public Mesh getRightArmMesh() {
			return rightArm;
		}

		@Override
		public Mesh getLeftLegMesh() {
			return leftLeg;
		}

		@Override
		public Mesh getRightLegMesh() {
			return rightLeg;
		}

		@Override
		public void setHeadMesh(Mesh mesh) {
			this.head = mesh;
		}

		@Override
		public void setTorsoMesh(Mesh mesh) {
			this.torso = mesh;
		}

		@Override
		public void setLeftArmMesh(Mesh mesh) {
			this.leftArm = mesh;
		}

		@Override
		public void setRightArmMesh(Mesh mesh) {
			this.rightArm = mesh;
		}

		@Override
		public void setLeftLegMesh(Mesh mesh) {
			this.leftLeg = mesh;
		}

		@Override
		public void setRightLegMesh(Mesh mesh) {
			this.rightLeg = mesh;
		}

		@Override
		public Identifier getCurrentSkin() {
			return currentSkin;
		}

		@Override
		public void setCurrentSkin(Identifier identifier) {
			this.currentSkin = identifier;
		}

		@Override
		public boolean hasThinArms() {
			return thinArms;
		}

		@Override
		public void setThinArms(boolean slim) {
			this.thinArms = slim;
		}
	}
}
