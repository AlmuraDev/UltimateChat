package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.plugin.PluginContainer;

public class UCVHelper56 implements UCVHelper{

	@Override
	public Cause getCause(CommandSource src) {
		return Cause.source(src).named(NamedCause.notifier(src)).build();
	}

	@Override
	public Cause getCause(PluginContainer instance) {
		return Cause.of(NamedCause.owner(instance));
	}

}
