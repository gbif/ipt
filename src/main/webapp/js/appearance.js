/*
 * Appearance page (admin/appearance.ftl): colour schemes, hex fields with a picker, WCAG contrast checks, a live
 * preview through the page's own CSS variables, logo checks, and an unsaved-changes bar.
 */
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("appearance-form");
    if (!form) {
        return;
    }
    const text = form.dataset;
    const testMode = text.testMode === "true";
    const root = document.documentElement;
    const rows = Array.from(form.querySelectorAll(".ap-row"));
    const checkIcons = document.getElementById("ap-check-icons")?.content;
    const field = (name) => form.querySelector('.ap-row[data-field="' + name + '"] .ap-hex');

    // the CSS variable each field sets (inc/header.ftl)
    const cssVar = {
        primaryColor: "--color-gbif-primary",
        navbarColor: "--navbar-color",
        navbarLinkColor: "--navbar-link-color",
        navbarGbifLogoColor: "--navbar-gbif-logo-color",
        navbarActiveTabColor: "--navbar-active-tab-color",
        linkColor: "--link-color"
    };
    const schemeAttr = {
        primaryColor: "primaryColor",
        navbarColor: "navbarColor",
        navbarLinkColor: "navbarLinkColor",
        navbarGbifLogoColor: "navbarGbifLogoColor",
        navbarActiveTabColor: "navbarActiveTabColor",
        linkColor: "linkColor"
    };

    // #abc and abc are accepted, as IptColorScheme.normalizeHex does
    function normalizeHex(value) {
        const m = /^#?([0-9a-f]{6}|[0-9a-f]{3})$/i.exec((value || "").trim());
        if (!m) {
            return null;
        }
        let hex = m[1].toLowerCase();
        if (hex.length === 3) {
            hex = hex[0] + hex[0] + hex[1] + hex[1] + hex[2] + hex[2];
        }
        return "#" + hex;
    }

    function rgb(hex) {
        return [1, 3, 5].map(i => parseInt(hex.slice(i, i + 2), 16));
    }

    function luminance(hex) {
        const l = rgb(hex).map(v => {
            v /= 255;
            return v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
        });
        return 0.2126 * l[0] + 0.7152 * l[1] + 0.0722 * l[2];
    }

    function contrast(a, b) {
        const la = luminance(a), lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    // the test-mode stripe colour, as IptColorScheme.getNavbarStripeColor
    function stripe(hex) {
        const l = luminance(hex);
        const darken = l > 0.5 ? 0.94 : 0.88;
        return "#" + rgb(hex).map(v => {
            const mixed = l < 0.05 ? Math.round(v + (255 - v) * 0.12) : Math.round(v * darken);
            return mixed.toString(16).padStart(2, "0");
        }).join("");
    }

    function format(template, value) {
        return template.replace("{0}", value);
    }

    const numberFormat = new Intl.NumberFormat(root.lang || undefined, {minimumFractionDigits: 1, maximumFractionDigits: 1});
    const ratioText = (r) => numberFormat.format(Math.floor(r * 10) / 10) + " : 1";

    function value(name) {
        return normalizeHex(field(name).value);
    }

    // one check: "Contrast with the navigation bar: 7.4 : 1", with a warning below the minimum
    function checkLine(label, ratio, min) {
        const ok = ratio >= min;
        const line = document.createElement("div");
        line.className = "ap-check " + (ok ? "ap-check-ok" : "ap-check-low");
        const icon = checkIcons && checkIcons.querySelector(ok ? ".ap-check-ok" : ".ap-check-low");
        if (icon) {
            line.append(icon.cloneNode(true));
        }
        line.append(format(label, ratioText(ratio)) + (ok ? "" : " \u00b7 " + format(text.textLow, ratioText(min))));
        return line;
    }

    function updateChecks() {
        const navbar = value("navbarColor");
        rows.forEach(row => {
            const box = row.querySelector(".ap-checks");
            box.replaceChildren();
            const own = value(row.dataset.field);
            if (!row.dataset.checks) {
                return;
            }
            if (!own) {
                return;
            }
            row.dataset.checks.split(" ").forEach(check => {
                const [against, min] = check.split(":");
                const minimum = parseFloat(min);
                if (against === "white") {
                    box.append(checkLine(text.textWhite, contrast(own, "#ffffff"), minimum));
                } else if (against === "navbar" && navbar) {
                    box.append(checkLine(text.textNavbar, contrast(own, navbar), minimum));
                    if (testMode) {
                        box.append(checkLine(text.textStripes, contrast(own, stripe(navbar)), minimum));
                    }
                }
            });
        });
    }

    // repaints this page through the CSS variables: the navigation bar above is the preview
    function preview() {
        Object.keys(cssVar).forEach(name => {
            const hex = value(name);
            if (hex) {
                root.style.setProperty(cssVar[name], rgb(hex).join(", "));
            }
        });
        const navbar = value("navbarColor");
        if (navbar) {
            root.style.setProperty("--navbar-stripe-color", rgb(stripe(navbar)).join(", "));
        }
    }

    // the scheme matching the current values is shown as pressed
    function updateSchemes() {
        form.querySelectorAll(".ap-scheme").forEach(button => {
            const match = Object.keys(schemeAttr).every(name => value(name) === normalizeHex(button.dataset[schemeAttr[name]]));
            button.setAttribute("aria-pressed", match ? "true" : "false");
        });
    }

    // an invalid value is marked, with the message under the field; Save is blocked until it's fixed
    function validate(input) {
        const ok = normalizeHex(input.value) !== null;
        input.classList.toggle("is-invalid", !ok);
        input.setCustomValidity(ok ? "" : text.textInvalid);
        const box = input.closest(".ap-row-input");
        let message = box.querySelector(".ap-hex-error");
        if (!ok && !message) {
            message = document.createElement("div");
            message.className = "invalid-feedback d-block ap-hex-error";
            message.textContent = text.textInvalid;
            box.querySelector(".ap-pick").after(message);
        }
        if (message) {
            message.hidden = ok;
        }
        // the server's message for this field is replaced by this one
        box.querySelectorAll(".invalid-feedback:not(.ap-hex-error)").forEach(e => e.hidden = true);
        return ok;
    }

    // --- unsaved changes ---
    const initial = {};
    rows.forEach(row => initial[row.dataset.field] = normalizeHex(row.querySelector(".ap-hex").value));
    let logoChanged = false;
    let leaving = false;
    const savebar = document.getElementById("ap-savebar");

    function isDirty() {
        return logoChanged || rows.some(row => normalizeHex(row.querySelector(".ap-hex").value) !== initial[row.dataset.field]);
    }

    function updateSavebar() {
        const dirty = isDirty();
        savebar.classList.toggle("show", dirty);
        savebar.setAttribute("aria-hidden", dirty ? "false" : "true");
    }

    function refresh() {
        preview();
        updateChecks();
        updateSchemes();
        updateSavebar();
    }

    rows.forEach(row => {
        const hex = row.querySelector(".ap-hex");
        const picker = row.querySelector(".ap-swatch");
        picker.addEventListener("input", () => {
            hex.value = picker.value;
            validate(hex);
            refresh();
        });
        hex.addEventListener("input", () => {
            const h = normalizeHex(hex.value);
            if (h) {
                picker.value = h;
            }
            validate(hex);
            refresh();
        });
        hex.addEventListener("blur", () => {
            const h = normalizeHex(hex.value);
            if (h) {
                hex.value = h;
            }
        });
    });

    form.querySelectorAll(".ap-scheme").forEach(button => {
        button.addEventListener("click", () => {
            Object.keys(schemeAttr).forEach(name => {
                const h = normalizeHex(button.dataset[schemeAttr[name]]);
                const input = field(name);
                input.value = h;
                validate(input);
                input.closest(".ap-row").querySelector(".ap-swatch").value = h;
            });
            refresh();
        });
    });

    // --- logo: checked here, uploaded with Save ---
    const fileInput = document.getElementById("file");
    const image = document.getElementById("applogo-image");
    const defaultMark = form.querySelector(".ap-logo-default");
    const removeButton = document.getElementById("buttonRemove");
    const removeLogo = document.getElementById("removeLogo");
    const logoError = document.getElementById("ap-logo-error");
    const logoTypes = ["image/png", "image/jpeg", "image/gif"];
    const savedLogoSrc = image.getAttribute("src");
    let previewUrl = null;

    function showLogo(src) {
        if (previewUrl && previewUrl !== src) {
            URL.revokeObjectURL(previewUrl);
            previewUrl = null;
        }
        if (src) {
            image.src = src;
            image.hidden = false;
            defaultMark.hidden = true;
        } else {
            image.removeAttribute("src");
            image.hidden = true;
            defaultMark.hidden = false;
        }
        removeButton.hidden = !src;
    }

    fileInput.addEventListener("change", () => {
        const file = fileInput.files[0];
        logoError.hidden = true;
        if (!file) {
            return;
        }
        if (!logoTypes.includes(file.type)) {
            logoError.textContent = text.textLogoType;
            logoError.hidden = false;
            fileInput.value = "";
            return;
        }
        previewUrl = URL.createObjectURL(file);
        showLogo(previewUrl);
        removeLogo.value = "false";
        logoChanged = true;
        updateSavebar();
    });

    removeButton.addEventListener("click", () => {
        fileInput.value = "";
        logoError.hidden = true;
        showLogo(null);
        removeLogo.value = savedLogoSrc ? "true" : "false";
        logoChanged = !!savedLogoSrc;
        updateSavebar();
    });

    // the image may be missing although a logo file exists (e.g. an unreadable file)
    image.addEventListener("error", () => {
        if (image.getAttribute("src") === savedLogoSrc) {
            showLogo(null);
        }
    });

    form.addEventListener("submit", (e) => {
        const invalid = rows.map(row => row.querySelector(".ap-hex")).filter(input => !validate(input));
        if (invalid.length) {
            e.preventDefault();
            invalid[0].closest(".ap-row").scrollIntoView({behavior: "smooth", block: "center"});
            invalid[0].focus({preventScroll: true});
            return;
        }
        leaving = true;
    });
    document.getElementById("ap-discard").addEventListener("click", () => leaving = true);
    window.addEventListener("beforeunload", (e) => {
        if (!leaving && isDirty()) {
            e.preventDefault();
            e.returnValue = text.textLeave;
        }
    });

    rows.forEach(row => validate(row.querySelector(".ap-hex")));
    refresh();
});
