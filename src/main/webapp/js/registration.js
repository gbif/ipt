/*
 * GBIF registration page (admin/registration.ftl): the public URL check that unlocks the registration form, the
 * organization picker (shared token request link, warning when the organization already has IPTs), the network
 * picker and copy buttons.
 */
document.addEventListener("DOMContentLoaded", () => {
    const $id = (id) => document.getElementById(id);

    // --- copy buttons
    document.addEventListener("click", (e) => {
        const b = e.target.closest(".st-copy");
        if (!b || !navigator.clipboard) return;
        navigator.clipboard.writeText(b.dataset.copy).then(() => {
            b.classList.add("st-copied");
            setTimeout(() => b.classList.remove("st-copied"), 1500);
        }, () => { /* nothing copied */ });
    });

    // --- saving the details or the network updates every registered resource: show progress
    ["registration", "networkForm"].forEach((id) => {
        const form = $id(id);
        if (form && typeof displayProcessing === "function") form.addEventListener("submit", displayProcessing);
    });

    // --- network picker
    const network = $id("networkKey");
    if (network) {
        $(network).select2({
            placeholder: network.dataset.placeholder,
            minimumResultsForSearch: 15,
            allowClear: true,
            width: "100%",
            theme: "bootstrap4"
        });
    }

    // --- public URL check
    const validate = $id("validate");
    const steps = $id("rg-steps");
    function unlock() {
        steps.disabled = false;
        $id("rg-locked").hidden = true;
        document.querySelectorAll("button[name='save']").forEach((b) => { b.disabled = false; });
    }
    if (validate) {
        validate.addEventListener("click", () => {
            const spinner = validate.querySelector(".spinner-border");
            ["validation-success", "validation-failed", "validation-failed-development"].forEach((id) => { $id(id).hidden = true; });
            validate.disabled = true;
            spinner.hidden = false;
            const done = (ok) => {
                validate.disabled = false;
                spinner.hidden = true;
                if (ok) {
                    $id("validation-success").hidden = false;
                    unlock();
                } else if (validate.dataset.development === "true") {
                    $id("validation-failed-development").hidden = false;
                    unlock();
                } else {
                    $id("validation-failed").hidden = false;
                }
            };
            $.getJSON(validate.dataset.checkUrl).done((data) => done(data && data.success === true)).fail(() => done(false));
        });
    }

    // --- organization picker
    const org = $id("organisation.key");
    if (!org) return;
    const set = (id, value) => { $id(id).value = value || ""; };
    const request = $id("requestDetails");
    const duplicate = $id("installations-warning");

    function link(href, text) {
        const a = document.createElement("a");
        a.href = href;
        a.target = "_blank";
        a.rel = "noopener";
        a.textContent = text;
        return a;
    }

    $(org).select2({
        placeholder: org.dataset.placeholder,
        allowClear: true,
        width: "100%",
        theme: "bootstrap4"
    }).on("change", () => {
        const key = org.value;
        const name = key ? org.options[org.selectedIndex].text : "";
        set("organisation.name", name);
        set("ipt.organisationKey", key);
        if ($id("organisation.alias").value.trim() === "" || $id("organisation.alias").dataset.auto === "true") {
            $id("organisation.alias").value = name;
            $id("organisation.alias").dataset.auto = "true";
        }
        ["primaryContactType", "primaryContactName", "primaryContactEmail", "nodeKey", "nodeName"]
            .forEach((f) => set("organisation." + f, ""));
        request.hidden = true;
        request.replaceChildren();
        duplicate.hidden = true;
        if (!key) return;

        // the organization's contact, to ask for the shared token
        $.getJSON(org.dataset.registry + "organisation/" + key + ".json", (data) => {
            if (org.value !== key) return;
            ["primaryContactType", "primaryContactName", "primaryContactEmail", "nodeKey", "nodeName"]
                .forEach((f) => set("organisation." + f, data[f]));
            if (data.primaryContactEmail) {
                const subject = "Shared token request for " + name;
                const body = "Dear " + (data.primaryContactName || "colleague") + ",\n\n"
                    + "I am setting up an Integrated Publishing Toolkit (IPT) hosted by " + name + ". "
                    + "To register it with GBIF, I need the organization's shared token. Could you please send it to me?\n\n"
                    + "Thank you.";
                const a = link("mailto:" + data.primaryContactEmail + "?subject=" + encodeURIComponent(subject)
                    + "&body=" + encodeURIComponent(body), org.dataset.textRequest.replace("{0}", data.primaryContactName || data.primaryContactEmail));
                a.removeAttribute("target");
                request.replaceChildren(a);
                request.hidden = false;
            }
        });

        // IPTs already registered under this organization: maybe this one was registered before
        $.getJSON(org.dataset.api + "organization/" + key + "/installation", (data) => {
            if (org.value !== key) return;
            const ipts = (data.results || []).filter((i) => i.type === "IPT_INSTALLATION").length;
            if (ipts === 0) return;
            const text = duplicate.querySelector(".rg-dup-text");
            const parts = org.dataset.textDuplicate.split("{0}");
            text.replaceChildren();
            text.insertAdjacentHTML("beforeend", parts[0]);
            text.appendChild(link(org.dataset.portal + "/publisher/" + key, name));
            text.insertAdjacentHTML("beforeend", parts.slice(1).join("{0}"));
            duplicate.hidden = false;
        });
    });
    $id("organisation.alias").addEventListener("input", (e) => { e.target.dataset.auto = "false"; });
});
