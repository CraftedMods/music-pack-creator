package craftedMods.versionChecker.base;

import java.util.Objects;
import java.util.regex.Pattern;

import craftedMods.versionChecker.api.EnumVersionState;
import craftedMods.versionChecker.api.SemanticVersion;

public class DefaultSemanticVersion implements SemanticVersion {

	private final EnumVersionState versionState;
	private final int majorVersion;
	private final int minorVersion;
	private final int patchVersion;
	private final int preReleaseVersion;

	private final String versionString;// Since this object is immutable, the version string doesn't
										// has to be computed every call of toString

	public DefaultSemanticVersion(EnumVersionState versionState, int majorVersion, int minorVersion, int patchVersion,
			int preReleaseVersion) {
		Objects.requireNonNull(versionState);
		if (majorVersion < 0)
			throw new IllegalArgumentException("Major version is less then zero");
		if (minorVersion < 0)
			throw new IllegalArgumentException("Minor version is less then zero");
		if (patchVersion < 0)
			throw new IllegalArgumentException("Patch version is less then zero");
		if (preReleaseVersion < -1) {
			preReleaseVersion = -1;
		}
		this.versionState = versionState;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.patchVersion = patchVersion;
		this.preReleaseVersion = preReleaseVersion;
		this.versionString = this.buildVersionString();
	}

	public DefaultSemanticVersion(EnumVersionState versionState, int majorVersion, int minorVersion, int patchVersion) {
		this(versionState, majorVersion, minorVersion, patchVersion, -1);
	}

	public EnumVersionState getVersionState() {
		return this.versionState;
	}

	public int getMajorVersion() {
		return this.majorVersion;
	}

	public int getMinorVersion() {
		return this.minorVersion;
	}

	public int getPatchVersion() {
		return this.patchVersion;
	}

	public int getPreReleaseVersion() {
		return this.preReleaseVersion;
	}

	public boolean isPreRelease() {
		return this.preReleaseVersion >= 0;
	}

	@Override
	public int compareTo(SemanticVersion comparable) {
		int stateComp = this.versionState.compareTo(comparable.getVersionState());
		int majorComp = Integer.compare(this.majorVersion, comparable.getMajorVersion());
		int minorComp = Integer.compare(this.minorVersion, comparable.getMinorVersion());
		int patchComp = Integer.compare(this.patchVersion, comparable.getPatchVersion());
		int preReleaseComp = Integer.compare(this.preReleaseVersion, comparable.getPreReleaseVersion());
		if (this.preReleaseVersion < 0 || comparable.getPreReleaseVersion() < 0) {
			preReleaseComp = -preReleaseComp;
		}
		return stateComp == 0
				? majorComp == 0 ? minorComp == 0 ? patchComp == 0 ? preReleaseComp : patchComp : minorComp : majorComp
				: stateComp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.majorVersion;
		result = prime * result + this.minorVersion;
		result = prime * result + this.patchVersion;
		result = prime * result + this.preReleaseVersion;
		result = prime * result + (this.versionState == null ? 0 : this.versionState.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		SemanticVersion other = (SemanticVersion) obj;
		if (this.majorVersion != other.getMajorVersion())
			return false;
		if (this.minorVersion != other.getMinorVersion())
			return false;
		if (this.patchVersion != other.getPatchVersion())
			return false;
		if (this.preReleaseVersion != other.getPreReleaseVersion())
			return false;
		if (this.versionState != other.getVersionState())
			return false;
		return true;
	}

	private String buildVersionString() {
		return String.format("%d.%d.%d%s%s", this.majorVersion, this.minorVersion, this.patchVersion,
				this.versionState == EnumVersionState.FULL ? "" : String.format("-%s", this.versionState.toString()),
				this.preReleaseVersion >= 0 ? String.format("%s.%d",
						this.versionState == EnumVersionState.FULL ? "-PRE" : "", this.preReleaseVersion) : "");
	}

	@Override
	public String toString() {
		return this.versionString;
	}

	public static SemanticVersion of(String versionString) {
		Objects.requireNonNull(versionString);
		if (!isSemanticVersion(versionString))
			throw new IllegalArgumentException(
					String.format("The version string \"%s\" doesn't match the specification", versionString.trim()));
		String[] parts = versionString.trim().split("-");
		String[] mainVersion = parts[0].split("\\.");
		Integer majorVersion = Integer.parseInt(mainVersion[0]);
		Integer minorVersion = Integer.parseInt(mainVersion[1]);
		Integer patchVersion = Integer.parseInt(mainVersion[2]);
		EnumVersionState versionState = EnumVersionState.FULL;
		Integer preReleaseVersion = -1;
		if (parts.length > 1) {
			String[] parts2 = parts[1].split("\\.");
			String state = parts2[0].replaceAll("-", "");
			if (!state.equals("PRE")) {
				versionState = EnumVersionState.valueOf(state);
			}
			if (parts2.length > 1) {
				preReleaseVersion = Integer.parseInt(parts2[1]);
			}
		}
		return new DefaultSemanticVersion(versionState, majorVersion, minorVersion, patchVersion, preReleaseVersion);
	}

	public static boolean isSemanticVersion(String versionString) {
		Objects.requireNonNull(versionString);
		return Pattern.matches("^[0-9]+\\.[0-9]+\\.[0-9]+($|(-PRE)|(-ALPHA)|(-BETA))($|(\\.[0-9]+))$",
				versionString.trim());
	}
}
