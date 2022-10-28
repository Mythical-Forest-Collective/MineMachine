package systems.plurality.foc.minemachine;

public final class Messages {
	public static final String HELP_TEXT = """
====MineMachine Help Page====
Here we'll use a format for our commands, text surrounded with [square brackets] mean that it's an optional argument, while {braces} mean that the argument is required. Do not include the braces or square brackets in the actual command.

Commands:
/minemachine execute {wasm file name}
  Allows you to execute a WASM program from the programs dir.

/minemachine programs
  Lists all of the programs in the WASM dir.
""";
}
