package craftedMods.versionChecker.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import craftedMods.versionChecker.api.EnumVersionState;
import craftedMods.versionChecker.api.SemanticVersion;

public class DefaultSemanticVersionTest {
	private SemanticVersion alpha = new DefaultSemanticVersion(EnumVersionState.ALPHA, 1, 2, 3, -1);
	private SemanticVersion beta = new DefaultSemanticVersion(EnumVersionState.BETA, 1, 2, 3, -1);
	private SemanticVersion full = new DefaultSemanticVersion(EnumVersionState.FULL, 1, 2, 3, -1);

	@Test
	public void testConstructorNullVersionState() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			new DefaultSemanticVersion(null, 10, 20, 30, 40);
		});
	}

	@Test
	public void testConstructorMajorVersionLessThanZero() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DefaultSemanticVersion(EnumVersionState.ALPHA, -10, 20, 30, 40);
		});
	}

	@Test
	public void testConstructorMinorVersionLessThanZero() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DefaultSemanticVersion(EnumVersionState.ALPHA, 10, -20, 30, 40);
		});
	}

	@Test
	public void testConstructorPatchVersionLessThanZero() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DefaultSemanticVersion(EnumVersionState.ALPHA, 10, 20, -30, 40);
		});
	}

	@Test
	public void testConstructorPreVersionLessThanZeroFix() {
		Assertions.assertEquals(-1,
				new DefaultSemanticVersion(EnumVersionState.ALPHA, 10, 20, 30, -40).getPreReleaseVersion());
	}

	@Test
	public void testIsPreRelease() {
		Assertions.assertFalse(this.alpha.isPreRelease());
		Assertions.assertTrue(new DefaultSemanticVersion(EnumVersionState.ALPHA, 10, 20, 30, 40).isPreRelease());
	}

	@Test
	public void testCompareToSelf() {
		Assertions.assertEquals(0, this.alpha.compareTo(this.alpha));
		Assertions.assertEquals(0, this.beta.compareTo(this.beta));
		Assertions.assertEquals(0, this.full.compareTo(this.full));
	}

	@Test
	public void testCompareToVersionState() {
		Assertions.assertTrue(this.alpha.compareTo(this.beta) < 0);
		Assertions.assertTrue(this.alpha.compareTo(this.full) < 0);

		Assertions.assertTrue(this.beta.compareTo(this.alpha) > 0);
		Assertions.assertTrue(this.beta.compareTo(this.full) < 0);

		Assertions.assertTrue(this.full.compareTo(this.alpha) > 0);
		Assertions.assertTrue(this.full.compareTo(this.beta) > 0);
	}

	@Test
	public void testCompareToMajorVersion() {
		Assertions
				.assertTrue(this.alpha.compareTo(new DefaultSemanticVersion(EnumVersionState.ALPHA, 2, 2, 3, -1)) < 0);
		Assertions
				.assertTrue(this.alpha.compareTo(new DefaultSemanticVersion(EnumVersionState.ALPHA, 0, 2, 3, -1)) > 0);
	}

	@Test
	public void testCompareToMinorVersion() {
		Assertions
				.assertTrue(this.alpha.compareTo(new DefaultSemanticVersion(EnumVersionState.ALPHA, 1, 3, 3, -1)) < 0);
		Assertions
				.assertTrue(this.alpha.compareTo(new DefaultSemanticVersion(EnumVersionState.ALPHA, 1, 1, 3, -1)) > 0);
	}

	@Test
	public void testCompareToPatchVersion() {
		Assertions
				.assertTrue(this.alpha.compareTo(new DefaultSemanticVersion(EnumVersionState.ALPHA, 1, 2, 4, -1)) < 0);
		Assertions
				.assertTrue(this.alpha.compareTo(new DefaultSemanticVersion(EnumVersionState.ALPHA, 1, 2, 2, -1)) > 0);
	}

	@Test
	public void testCompareToPreRelease() {
		Assertions.assertTrue(this.alpha.compareTo(new DefaultSemanticVersion(EnumVersionState.ALPHA, 1, 2, 3, 0)) > 0);
	}

	@Test
	public void testToString() {
		Assertions.assertEquals("1.2.3-ALPHA", this.alpha.toString());
		Assertions.assertEquals("1.2.3-BETA", this.beta.toString());
		Assertions.assertEquals("1.2.3", this.full.toString());

		Assertions.assertEquals("100.2.3-ALPHA.5",
				new DefaultSemanticVersion(EnumVersionState.ALPHA, 100, 2, 3, 5).toString());
		Assertions.assertEquals("1.200.3-BETA.3",
				new DefaultSemanticVersion(EnumVersionState.BETA, 1, 200, 3, 3).toString());
		Assertions.assertEquals("1.2.300-PRE.2",
				new DefaultSemanticVersion(EnumVersionState.FULL, 1, 2, 300, 2).toString());
	}

	@Test
	public void testOfInvalidVersionEmpty() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("");
		});
	}

	@Test
	public void testOfInvalidVersionBlank() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("	 	");
		});
	}

	@Test
	public void testOfInvalidVersionLetters() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.5d");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.d5");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5d.5");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.d5.5");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0d.5.5");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("d0.5.5");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("d0d.d5d.d5d");
		});
	}

	@Test
	public void testOfInvalidVersionWrongSeparator() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0:5:3");
		});
	}

	@Test
	public void testOfInvalidVersionWrongSuffix() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3-");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3-PI");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3-GREEN");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3-ALPH");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3-BGTA");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3-PRE2");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3-2PRE");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.32PRE");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3PRE");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3.PRE");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3.ALPHA");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3.BETA");
		});
	}

	@Test
	public void testOfInvalidVersionPreRelease() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3-BETA.3m");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3-BETA.");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3-BETA.d");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DefaultSemanticVersion.of("0.5.3-PRE.-1");
		});
	}

	@Test
	public void testOfInvalidVersionNull() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			DefaultSemanticVersion.of(null);
		});
	}

	@Test
	public void testOfValidVersion() {
		Assertions.assertTrue(this.alpha.equals(DefaultSemanticVersion.of("1.2.3-ALPHA")));
		Assertions.assertTrue(this.beta.equals(DefaultSemanticVersion.of("1.2.3-BETA")));
		Assertions.assertTrue(this.full.equals(DefaultSemanticVersion.of("1.2.3")));
		Assertions.assertTrue(new DefaultSemanticVersion(EnumVersionState.ALPHA, 10, 20, 30, 40)
				.equals(DefaultSemanticVersion.of("10.20.30-ALPHA.40")));
		Assertions.assertTrue(new DefaultSemanticVersion(EnumVersionState.BETA, 10, 20, 30, 40)
				.equals(DefaultSemanticVersion.of("10.20.30-BETA.40")));
		Assertions.assertTrue(new DefaultSemanticVersion(EnumVersionState.FULL, 10, 20, 30, 40)
				.equals(DefaultSemanticVersion.of("10.20.30-PRE.40")));
	}

	@Test
	public void testOfValidVersionWithTrailingWhitespaces() {
		Assertions.assertTrue(this.alpha
				.equals(DefaultSemanticVersion.of("	 	 	 	 	 		 	 1.2.3-ALPHA	 	 	")));
		Assertions.assertTrue(DefaultSemanticVersion.of("1.2.3-ALPHA	  ")
				.equals(DefaultSemanticVersion.of("	 	 	 	 	 		 	 1.2.3-ALPHA	 	 	")));
	}
}
