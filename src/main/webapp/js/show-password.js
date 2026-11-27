/*
 * Show / Hide button for a password or token field: <button type="button" data-show-password aria-controls="ID"
 * data-show-text="Show" data-hide-text="Hide">. The fields start empty, so only what was typed is shown.
 * A shown field is hidden again when its form is sent, so the browser doesn't keep it as plain text.
 */
(function () {
    function setShown(button, input, shown) {
        input.type = shown ? "text" : "password";
        button.textContent = shown ? button.dataset.hideText : button.dataset.showText;
        button.setAttribute("aria-pressed", String(shown));
    }

    document.addEventListener("click", function (e) {
        var button = e.target.closest("[data-show-password]");
        if (!button) return;
        var input = document.getElementById(button.getAttribute("aria-controls"));
        if (!input) return;
        setShown(button, input, input.type === "password");
        input.focus();
    });

    // only when the form really goes (a page check that stops it leaves the field as it is), and only its fields
    document.addEventListener("submit", function (e) {
        if (e.defaultPrevented) return;
        document.querySelectorAll("[data-show-password][aria-pressed=true]").forEach(function (button) {
            var input = document.getElementById(button.getAttribute("aria-controls"));
            if (input && input.form === e.target) setShown(button, input, false);
        });
    });
})();
