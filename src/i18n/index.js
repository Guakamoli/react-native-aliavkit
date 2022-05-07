import i18n from 'i18n-js';
import * as RNLocalize from 'react-native-localize';

export const LANGUAGES = [
	{
		label: 'English',
		value: 'en',
		file: () => require('./locales/en.json')
	}, {
		label: '简体中文',
		value: 'zh-CN',
		file: () => require('./locales/zh-CN.json')
	}
];

const translations = LANGUAGES.reduce((ret, item) => {
	ret[item.value] = item.file;
	return ret;
}, {});
let hasSet = false
export const setLanguage = (l) => {
	console.info('Youma ', l)
	if (!l) {
		return;
	}
	// server uses lowercase pattern (pt-br), but we're forced to use standard pattern (pt-BR)
	let locale = LANGUAGES.find(ll => ll.value.toLowerCase() === l.toLowerCase())?.value;
	if (locale === 'zh-Hans') {
		locale = 'zh-CN';
	} else {
		locale = 'en';
	}
	// don't go forward if it's the same language and default language (en) was setup already
	// if (i18n.locale === locale && i18n.translations?.en) {
	// 	return;
	// }
	if (hasSet) {
		return 
	}
	if (i18n.translations) {
		if (i18n.translations[locale]) {
			i18n.translations[locale] = Object.assign(i18n.translations[locale], translations[locale]?.())
		} else {
			i18n.locale = locale;
			i18n.translations = { ...i18n.translations, [locale]: translations[locale]?.() };
		}
	}
	hasSet = true
	// i18n.locale = locale;
	// i18n.translations = { ...i18n.translations, [locale]: translations[locale]?.() };
};

// i18n.translations = { en: translations.en?.() };
const defaultLanguage = { languageTag: 'en', isRTL: false };
const availableLanguages = Object.keys(translations);
const { languageTag } = RNLocalize.findBestAvailableLanguage(availableLanguages) || defaultLanguage;

setLanguage(languageTag);
i18n.fallbacks = true;

export default i18n;
