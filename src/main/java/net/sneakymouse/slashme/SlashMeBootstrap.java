package net.sneakymouse.slashme;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.sneakymouse.slashme.commands.SlashMeCommands;

public final class SlashMeBootstrap implements PluginBootstrap {

	@Override
	public void bootstrap(BootstrapContext context) {
		context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
				event -> SlashMeCommands.register(event.registrar()));
	}

}
