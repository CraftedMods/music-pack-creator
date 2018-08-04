package craftedMods.language.provider;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;

@Component(service = ResourceBundleLoader.class)
public class ResourceBundleLoader {

	public static final String DEFAULT_BUNDLE_NAME = "lang";

	public ResourceBundle loadResourceBunde(Locale locale, boolean searchSimilar) {
		ResourceBundle bundle = null;
		try {
			bundle = ResourceBundle.getBundle("." + DEFAULT_BUNDLE_NAME, locale);
			if (locale != bundle.getLocale()) {
				bundle = null;
			}
		} catch (MissingResourceException e) {
		}
		if (bundle == null && searchSimilar) {
			for (Locale locale2 : Locale.getAvailableLocales()) {
				if (!locale2.equals(locale)) {
					if (locale2.getLanguage().equals(locale.getLanguage())) {
						bundle = loadResourceBunde(locale2, false);
						if (bundle != null)
							break;
					}
				}
			}
		}
		return bundle;
	}

}
