package systems.plurality.foc.minemachine;

import io.github.kawamuray.wasmtime.*;
import io.github.kawamuray.wasmtime.Module;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import static systems.plurality.foc.minemachine.MineMachine.GLOBALS_PROGRAMS_DIR;
import static systems.plurality.foc.minemachine.MineMachine.LOGGER;

@NotNull
public class Machine {
	public Store store;
	public Engine engine;

	public Machine() {
		this.store = Store.withoutData();
		this.engine = store.engine();
	}

	public Machine.ExitStatus executeWasm(String programName) {
		Path program = Paths.get(GLOBALS_PROGRAMS_DIR.toString(), programName);
		if (!Files.exists(program)) {
			LOGGER.error("The file you're trying to access doesn't exist! File location: "+program.toString());
			return ExitStatus.Failure;
		}

		Module module;
		try {
			module = Module.fromBinary(engine, Files.readAllBytes(program));
		} catch (IOException e) {
			LOGGER.error("Something went wrong!");
			throw new RuntimeException(e);
		}

		Func mainFunc = WasmFunctions.wrap(store, () -> {});

		Collection<Extern> imports = Arrays.asList(Extern.fromFunc(mainFunc));
		Instance instance = new Instance(store, module, imports);

		Func f = (Func) instance.getFunc(store, "main").get();

		WasmFunctions.Consumer0 fn = WasmFunctions.consumer(store, f);
		fn.accept();

		return ExitStatus.Success;
	}

	public static enum ExitStatus {
		Success, Failure
	}
}
