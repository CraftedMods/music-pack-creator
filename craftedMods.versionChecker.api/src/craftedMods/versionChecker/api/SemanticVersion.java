package craftedMods.versionChecker.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface SemanticVersion extends Comparable<SemanticVersion> {

	public EnumVersionState getVersionState();

	public int getMajorVersion();

	public int getMinorVersion();

	public int getPatchVersion();

	public int getPreReleaseVersion();

	public boolean isPreRelease();

}
