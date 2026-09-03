package pasheadskins;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasHeadSkinsClient implements ClientModInitializer {
	public static final String MOD_ID = "pasheadskins";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("PAS Head Skins ready");
	}
}
