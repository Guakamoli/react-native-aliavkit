import i18n from 'i18n-js';
import * as RNLocalize from 'react-native-localize';

export const LANGUAGES = [
	{
		label: 'English',
		value: 'en',
		file: () => require('./locales/en.json')
	}, {
		label: `${I18n.t('Simplified_Chinese')}`,
		value: 'zh-CN',
		file: () => require('./locales/zh-CN.json')
	}
];

const translations = LANGUAGES.reduce((ret, item) => {
	ret[item.value] = item.file;
	return ret;
}, {});

export const setLanguage = (l) => {
	if (!l) {
		return;
	}
	// server uses lowercase pattern (pt-br), but we're forced to use standard pattern (pt-BR)
	let locale = LANGUAGES.find(ll => ll.value.toLowerCase() === l.toLowerCase())?.value;
	if (!locale) {
		locale = 'en';
	}
	locale = 'zh-CN'
	// don't go forward if it's the same language and default language (en) was setup already
	if (i18n.locale === locale && i18n.translations?.en) {
		return;
	}
	i18n.locale = locale;
	i18n.translations = { ...i18n.translations, [locale]: translations[locale]?.() };
};

i18n.translations = { en: translations.en?.() };
const defaultLanguage = { languageTag: 'en', isRTL: false };
const availableLanguages = Object.keys(translations);
const { languageTag } = RNLocalize.findBestAvailableLanguage(availableLanguages) || defaultLanguage;

setLanguage(languageTag);
i18n.fallbacks = true;

export default i18n;
