package systems.plurality.foc.minemachine;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.*;

public class MineMachine implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("MineMachine");
	public static final Path CONFIG_DIR = Paths.get("config", "minemachine").toAbsolutePath();
	public static final Path GLOBALS_PROGRAMS_DIR = Paths.get(CONFIG_DIR.toString(), "programs");

	@Override
	public void onInitialize(ModContainer mod) {
		this.initialiseConfig();
		this.registerServersideCommands();
		LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());
	}

	private void initialiseConfig() {
		try {
			Files.createDirectories(CONFIG_DIR);
			Files.createDirectories(GLOBALS_PROGRAMS_DIR);
		} catch (IOException e) {
			LOGGER.error("Cannot create the config directories!");
			throw new RuntimeException(e);
		}
	}

	private void registerServersideCommands() {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("minemachine");

		// Main command, just brings up the help page
		command = command.executes(context -> {
			context.getSource().getPlayer().sendMessage(Text.of(Messages.HELP_TEXT), false);
			return 0;
		});

		// Execute command, runs WASM programs in the programs folder, please end my suffering
		command = command.then(literal("execute").then(argument("program", StringArgumentType.string()).executes(context -> {
			String program = context.getArgument("program", String.class);
			Machine machine = new Machine();
			if (machine.executeWasm(program) == Machine.ExitStatus.Failure) {
				return 1;
			}

			return 0;
		})));

		// Programs command, list all programs in the config directory
		command = command.then(literal("programs").executes(context -> {
			try {
				List<String> programList = Files.list(GLOBALS_PROGRAMS_DIR)
						.map(p -> p.getFileName().toString())
						.collect(Collectors.toList());

				String programs = "Global programs: " + String.join(", ", programList);

				context.getSource().getPlayer().sendMessage(Text.of(programs), false);
			} catch (IOException e) {
				LOGGER.error("There was an error while trying to query the globally installed programs!");
				throw new RuntimeException(e);
			}
			return 0;
		}));

		// Register the commands
		LiteralArgumentBuilder<ServerCommandSource> finalCommand = command;
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(finalCommand)
		);
	}
}
