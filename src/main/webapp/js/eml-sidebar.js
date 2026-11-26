/*
 * "On this page" on the EML metadata pages (manage/eml/eml_sidebar.ftl): one link per element marked
 * data-toc="label" (its id is the anchor), and the part in view is highlighted as the page scrolls.
 * Hidden when the page has fewer than two parts. The links look like the other pages' "On this page" (main.css
 * .sidebar-navigation-link).
 */
(function () {
    function init() {
        var box = document.getElementById('eml-toc');
        var list = document.getElementById('eml-toc-list');
        if (!box || !list) return;
        var parts = Array.prototype.slice.call(document.querySelectorAll('.bd-content [data-toc]'))
            .filter(function (el) { return el.id; });
        if (parts.length < 2) return;

        var links = parts.map(function (el) {
            var li = document.createElement('li');
            var a = document.createElement('a');
            a.href = '#' + el.id;
            a.className = 'sidebar-navigation-link';
            a.textContent = el.getAttribute('data-toc');
            li.appendChild(a);
            list.appendChild(li);
            return a;
        });
        box.hidden = false;

        // a clicked part stays highlighted while the page scrolls to it, also when it is near the end of the page and
        // can't reach the top, until the user scrolls
        var clicked = -1;
        links.forEach(function (a, i) {
            a.addEventListener('click', function () {
                clicked = i;
                spy();
            });
        });
        ['wheel', 'touchmove', 'keydown', 'mousedown'].forEach(function (type) {
            window.addEventListener(type, function () { clicked = -1; }, { passive: true });
        });

        // the last part whose top has passed the fixed navbar plus a little
        function spy() {
            if (clicked >= 0) {
                links.forEach(function (a, i) { a.classList.toggle('active', i === clicked); });
                return;
            }
            var cur = 0;
            parts.forEach(function (el, i) {
                if (el.getBoundingClientRect().top < 140) cur = i;
            });
            // at the bottom of the page the last part can't scroll up that far
            if (window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 2) cur = parts.length - 1;
            links.forEach(function (a, i) { a.classList.toggle('active', i === cur); });
        }
        window.addEventListener('scroll', spy, { passive: true });
        spy();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
