package pasheadskins.mixin;

import com.danrus.pas.render.armorstand.PasEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import pasheadskins.HeadDrivenSkin;

@Mixin(PasEntityRenderState.class)
public class PasEntityRenderStateMixin implements HeadDrivenSkin {
	@Unique
	private boolean pasheadskins$fromHead;

	@Override
	public void pasheadskins$setFromHead(boolean fromHead) {
		this.pasheadskins$fromHead = fromHead;
	}

	@Override
	public boolean pasheadskins$isFromHead() {
		return this.pasheadskins$fromHead;
	}
}
