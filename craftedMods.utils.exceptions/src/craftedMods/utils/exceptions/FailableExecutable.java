package craftedMods.utils.exceptions;

@FunctionalInterface
public interface FailableExecutable {

	public void execute() throws Exception;

}
