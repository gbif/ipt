/*
 * Translated default messages for the jQuery validation plugin.
 * Language: HU
 * Skipped dateISO/DE, numberDE
 */
jQuery.extend(jQuery.validator.messages, {
	required: "Kötelező megadni.",
	maxlength: jQuery.format("Legfeljebb {0} karakter hosszú legyen."),
	minlength: jQuery.format("Legalább {0} karakter hosszú legyen."),
	rangelength: jQuery.format("Legalább {0} és legfeljebb {1} karakter hosszú legyen."),
	email: "Érvényes e-mail címnek kell lennie.",
	url: "Érvényes URL-nek kell lennie.",
	date: "Dátumnak kell lennie.",
	number: "Számnak kell lennie.",
	digits: "Csak számjegyek lehetnek.",
	equalTo: "Meg kell egyeznie a két értéknek.",
	range: jQuery.format("{0} és {1} közé kell esnie."),
	max: jQuery.format("Nem lehet nagyobb, mint {0}."),
	min: jQuery.format("Nem lehet kisebb, mint {0}."),
	creditcard: "Érvényes hitelkártyaszámnak kell lennie."
});
