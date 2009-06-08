/*
 * Traduzione dei messaggi di default per il pugin jQuery validation.
 * Language: IT
 * Traduzione a cura di Davide Falchetto
 * E-mail: d.falchetto@d4solutions.it
 * Web: www.d4solutions.it
 */
jQuery.extend(jQuery.validator.messages, {
       required: "Campo obbligatorio.",
       remote: "Controlla questo campo.",
       email: "Inserisci un indirizzo email valido.",
       url: "Inserisci un indirizzo web valido.",
       date: "Inserisci una data valida.",
       dateISO: "Inserisci una data valida (ISO).",
       number: "Inserisci un numero valido.",
       digits: "Inserisci solo numeri.",
       creditcard: "Inserisci un numero di carta di credito valido.",
       equalTo: "Il valore non corrisponde.",
       accept: "Inserisci un valore con un&apos;estensione valida.",
       maxlength: jQuery.format("Non inserire pi&ugrave; di {0} caratteri."),
       minlength: jQuery.format("Inserisci almeno {0} caratteri."),
       rangelength: jQuery.format("Inserisci un valore compreso tra {0} e {1} caratteri."),
       range: jQuery.format("Inserisci un valore compreso tra {0} e {1}."),
       max: jQuery.format("Inserisci un valore minore o uguale a {0}."),
       min: jQuery.format("Inserisci un valore maggiore o uguale a {0}.")
});