/*
 * IPT.confirm(options): an alert dialog (styles/dialogs.css) that asks one question. Returns a Promise that
 * resolves to {summary, select} when the main button is pressed, or to null on Cancel, the close button or Esc.
 *
 * options:
 *   tone           'danger' (default) | 'warn' | 'info' | 'ok': the colour of the icon tile; 'danger' makes
 *                  the main button solid red and puts the focus on Cancel
 *   icon           a Material icon from images/icons/material-icons.svg for the tile (default: the tone's own,
 *                  e.g. 'delete' for danger); pick the action's, e.g. 'upload' for publishing
 *   title          the actual question, with the object's name ("Delete source occurrence.txt?")
 *   text           one or two sentences of consequence (HTML, from the templates' translated texts)
 *   confirm        the main button's label, a verb ("Delete", "Register")
 *   cancel         the other button's label (default: Cancel)
 *   agreement      label of a box that must be ticked before the main button works (HTML)
 *   typeToConfirm  a word (e.g. the shortname) that must be typed before the main button works
 *   summary        placeholder of an optional textarea; its text is returned as result.summary
 *   select         {label, placeholder, options: [{value, label}]}: a choice that must be made; result.select
 *
 * The texts for Cancel and the hints, and the icon sprite's URL, come from data attributes on #dialog-confirm (inc/menu.ftl).
 */
(function () {
    "use strict";

    var counter = 0;

    function text(key, fallback) {
        var holder = document.getElementById("dialog-confirm");
        var value = holder && holder.getAttribute("data-" + key);
        return value || fallback;
    }

    function escapeAttr(s) {
        return String(s).replace(/[&<>"]/g, function (c) {
            return {"&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;"}[c];
        });
    }

    var TONE_SYMBOL = {danger: "!", warn: "!", info: "i", ok: "\u2713"};
    var TONE_ICON = {danger: "delete", warn: "warning", info: "info", ok: "check_circle"};

    // the tile's icon from the sprite, or the tone's symbol if the page doesn't say where the sprite is
    function toneIcon(tone, icon) {
        var sprite = text("icons", null);
        if (!sprite) {
            return TONE_SYMBOL[tone];
        }
        var name = String(icon || TONE_ICON[tone]).replace(/[^a-z0-9_]/g, "");
        return '<svg aria-hidden="true" focusable="false"><use href="' + escapeAttr(sprite) + '#' + name + '"/></svg>';
    }

    function confirm(options) {
        var o = {tone: "danger", cancel: text("cancel", "Cancel")};
        // options left undefined (e.g. passed on by jConfirmAction) keep the defaults
        Object.keys(options || {}).forEach(function (key) {
            if (options[key] !== undefined) {
                o[key] = options[key];
            }
        });
        if (!TONE_SYMBOL[o.tone]) {
            o.tone = "danger";
        }
        var id = "ipt-dialog-" + (++counter);
        var html = "";
        html += '<div class="modal fade" id="' + id + '" tabindex="-1" role="alertdialog" aria-labelledby="' + id + '-title" aria-describedby="' + id + '-text">';
        html += '<div class="modal-dialog modal-dialog-centered dlg-alert"><div class="modal-content">';
        html += '<div class="modal-header"><span class="dlg-tone ' + o.tone + '" aria-hidden="true">' + toneIcon(o.tone, o.icon) + '</span>';
        html += '<h2 class="modal-title" id="' + id + '-title"></h2>';
        html += '<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="' + escapeAttr(o.cancel) + '"></button></div>';
        html += '<div class="modal-body"><div id="' + id + '-text"></div>';
        if (o.select) {
            html += '<div class="dlg-extra"><label class="form-label" for="' + id + '-select"></label>';
            html += '<select class="form-select form-select-sm" id="' + id + '-select"><option value=""></option></select></div>';
        }
        if (o.summary !== undefined) {
            html += '<div class="dlg-extra"><textarea class="form-control form-control-sm" rows="4" id="' + id + '-summary" placeholder="' + escapeAttr(o.summary) + '" aria-label="' + escapeAttr(o.summary) + '"></textarea></div>';
        }
        if (o.typeToConfirm) {
            html += '<div class="dlg-extra dlg-type"><label class="form-label" for="' + id + '-type"></label>';
            html += '<input type="text" class="form-control form-control-sm" id="' + id + '-type" autocomplete="off" spellcheck="false"></div>';
        }
        if (o.agreement) {
            html += '<div class="dlg-extra form-check"><input class="form-check-input" type="checkbox" id="' + id + '-agree">';
            html += '<label class="form-check-label" for="' + id + '-agree"></label></div>';
        }
        html += '</div>';
        html += '<div class="modal-footer"><span class="dlg-hint" id="' + id + '-hint" hidden></span>';
        html += '<button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"></button>';
        html += '<button type="button" class="btn btn-sm ' + (o.tone === "danger" ? "btn-danger" : "btn-gbif-solid") + '" data-dlg-confirm aria-describedby="' + id + '-hint"></button>';
        html += '</div></div></div></div>';

        var wrap = document.createElement("div");
        wrap.innerHTML = html;
        var el = wrap.firstChild;
        var q = function (sel) { return el.querySelector(sel); };

        // texts: the title and button labels as plain text, the body and agreement as the templates' HTML
        q(".modal-title").textContent = stripTags(o.title || "");
        q("#" + id + "-text").innerHTML = o.text || "";
        var cancelBtn = q("[data-bs-dismiss].btn");
        cancelBtn.textContent = o.cancel;
        var okBtn = q("[data-dlg-confirm]");
        okBtn.textContent = stripTags(o.confirm || text("confirm", "OK"));
        if (o.select) {
            q("label[for='" + id + "-select']").innerHTML = o.select.label || "";
            var sel = q("#" + id + "-select");
            sel.options[0].textContent = o.select.placeholder || "";
            (o.select.options || []).forEach(function (opt) {
                var option = document.createElement("option");
                option.value = opt.value;
                option.textContent = opt.label;
                sel.appendChild(option);
            });
        }
        if (o.typeToConfirm) {
            var typeLabel = text("type", "Type {0} to confirm");
            var parts = typeLabel.split("{0}");
            var label = q("label[for='" + id + "-type']");
            label.appendChild(document.createTextNode(parts[0]));
            var code = document.createElement("code");
            code.textContent = o.typeToConfirm;
            label.appendChild(code);
            label.appendChild(document.createTextNode(parts.slice(1).join("{0}")));
        }
        if (o.agreement) {
            q("label[for='" + id + "-agree']").innerHTML = o.agreement;
        }

        // the main button works only once the box is ticked, the name typed and the choice made; the hint says why
        var hint = q("#" + id + "-hint");
        function update() {
            var why = null;
            if (o.select && !q("#" + id + "-select").value) {
                why = text("hint-select", "Make a choice to continue");
            } else if (o.typeToConfirm && q("#" + id + "-type").value.trim() !== o.typeToConfirm) {
                why = text("hint-type", "Type the name to continue");
            } else if (o.agreement && !q("#" + id + "-agree").checked) {
                why = text("hint-agree", "Tick the box to continue");
            }
            okBtn.disabled = why !== null;
            hint.hidden = why === null;
            hint.textContent = why || "";
        }
        el.addEventListener("input", update);
        el.addEventListener("change", update);
        update();

        document.body.appendChild(el);
        var modal = new bootstrap.Modal(el);
        var trigger = document.activeElement;

        return new Promise(function (resolve) {
            var result = null;
            okBtn.addEventListener("click", function () {
                if (okBtn.disabled) {
                    return;
                }
                result = {
                    summary: o.summary !== undefined ? q("#" + id + "-summary").value : undefined,
                    select: o.select ? q("#" + id + "-select").value : undefined
                };
                modal.hide();
            });
            el.addEventListener("shown.bs.modal", function () {
                // the field to fill if there is one; otherwise Cancel for destructive questions, the main action for the rest
                var first = q("#" + id + "-select") || q("#" + id + "-type") || q("#" + id + "-summary") || q("#" + id + "-agree");
                (first || (o.tone === "danger" ? cancelBtn : okBtn)).focus();
            });
            el.addEventListener("hidden.bs.modal", function () {
                modal.dispose();
                el.remove();
                if (trigger && typeof trigger.focus === "function" && document.contains(trigger)) {
                    trigger.focus();
                }
                resolve(result);
            });
            modal.show();
        });
    }

    // the text of an HTML string, e.g. a title with entities. DOMParser documents are inert: no scripts run and no
    // images load, so names pasted into titles (data-name) can't run code
    function stripTags(s) {
        return new DOMParser().parseFromString(String(s), "text/html").body.textContent;
    }

    // Bootstrap sets role="dialog" on every modal it shows; alert dialogs (.dlg-alert) get their role back before
    // anything inside takes the focus (capture phase, so this runs before the listeners on the dialog itself)
    document.addEventListener("shown.bs.modal", function (e) {
        if (e.target && e.target.querySelector(".dlg-alert")) {
            e.target.setAttribute("role", "alertdialog");
        }
    }, true);

    // templates' dialogs: the element marked autofocus gets the focus when the dialog opens (Bootstrap doesn't do it)
    document.addEventListener("shown.bs.modal", function (e) {
        var target = e.target && e.target.querySelector("[autofocus]");
        if (target) {
            target.focus();
        }
    });

    window.IPT = window.IPT || {};
    window.IPT.confirm = confirm;
})();
