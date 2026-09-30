/* LMS client enhancements: 3D tilt, ripple, toasts, sidebar, AJAX search */
(function () {
  'use strict';

  /* ---------- 3D Tilt on .card-3d[data-tilt] ---------- */
  function initTilt() {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    document.querySelectorAll('.card-3d[data-tilt]').forEach(function (card) {
      card.addEventListener('mousemove', function (e) {
        var r = card.getBoundingClientRect();
        var x = (e.clientX - r.left) / r.width - 0.5;
        var y = (e.clientY - r.top) / r.height - 0.5;
        card.style.setProperty('--ty', (x * 10).toFixed(2) + 'deg');
        card.style.setProperty('--tx', (-y * 8).toFixed(2) + 'deg');
        card.classList.add('js-tilt');
      });
      card.addEventListener('mouseleave', function () {
        card.classList.remove('js-tilt');
        card.style.removeProperty('--tx');
        card.style.removeProperty('--ty');
      });
    });
  }

  /* ---------- Material ripple on .ripple ---------- */
  function initRipple() {
    document.querySelectorAll('.ripple').forEach(function (el) {
      el.addEventListener('click', function (e) {
        var r = el.getBoundingClientRect();
        var ink = document.createElement('span');
        ink.className = 'ripple-ink';
        var size = Math.max(r.width, r.height);
        ink.style.width = ink.style.height = size + 'px';
        ink.style.left = (e.clientX - r.left - size / 2) + 'px';
        ink.style.top = (e.clientY - r.top - size / 2) + 'px';
        el.appendChild(ink);
        setTimeout(function () { ink.remove(); }, 650);
      });
    });
  }

  /* ---------- Auto-dismiss toasts ---------- */
  function initToasts() {
    var toast = document.getElementById('flashToast');
    if (!toast) return;
    setTimeout(function () {
      toast.classList.add('toast--out');
    }, 4500);
  }

  /* ---------- Mobile sidebar ---------- */
  function initSidebar() {
    var toggle = document.getElementById('sidebarToggle');
    var sidebar = document.getElementById('sidebar');
    if (!toggle || !sidebar) return;
    toggle.addEventListener('click', function () {
      sidebar.classList.toggle('sidebar--open');
    });
    document.addEventListener('click', function (e) {
      if (sidebar.classList.contains('sidebar--open') &&
          !sidebar.contains(e.target) && e.target !== toggle) {
        sidebar.classList.remove('sidebar--open');
      }
    });
  }

  /* ---------- Live AJAX search on student catalog ---------- */
  function initCatalogSearch() {
    var input = document.getElementById('searchInput');
    var grid = document.getElementById('catalogGrid');
    if (!input || !grid || !window.__LMS__) return;

    var timer = null;

    function esc(s) {
      var d = document.createElement('div');
      d.textContent = s == null ? '' : String(s);
      return d.innerHTML;
    }

    /* Category → {badge class, emoji} mapping (mirrors catalog.jsp) */
    var CATEGORY_STYLES = [
      { match: 'programming',  cls: 'chip-cat-blue',   emoji: '💻' },
      { match: 'database',     cls: 'chip-cat-indigo', emoji: '🗄️' },
      { match: 'algorithm',    cls: 'chip-cat-violet', emoji: '🧮' },
      { match: 'network',      cls: 'chip-cat-teal',   emoji: '🌐' },
      { match: 'math',         cls: 'chip-cat-pink',   emoji: '📐' },
      { match: 'intelligence', cls: 'chip-cat-amber',  emoji: '🤖' },
      { match: 'ai',           cls: 'chip-cat-amber',  emoji: '🤖' },
      { match: 'web',          cls: 'chip-cat-cyan',   emoji: '🕸️' },
      { match: 'framework',    cls: 'chip-cat-green',  emoji: '🌱' },
      { match: 'software',     cls: 'chip-cat-slate',  emoji: '🖥️' },
      { match: 'engineering',  cls: 'chip-cat-slate',  emoji: '🖥️' },
      { match: 'computer',     cls: 'chip-cat-deep',   emoji: '🎓' }
    ];

    function categoryStyle(category) {
      var cat = (category || '').toLowerCase();
      for (var i = 0; i < CATEGORY_STYLES.length; i++) {
        if (cat.indexOf(CATEGORY_STYLES[i].match) !== -1) {
          return CATEGORY_STYLES[i];
        }
      }
      return { cls: 'chip-cyan', emoji: '📖' };
    }

    function availabilityChip(copies) {
      if (copies > 1) {
        return '<span class="chip chip-green avail"><span class="avail-dot"></span>' + copies + ' available</span>';
      }
      if (copies === 1) {
        return '<span class="chip chip-amber avail"><span class="avail-dot"></span>Only 1 left!</span>';
      }
      return '<span class="chip chip-red"><span class="avail-dot avail-dot--off"></span>Not available</span>';
    }

    function render(books) {
      if (!books.length) {
        grid.innerHTML = '<div class="empty empty--wide">No books match your search. 🔎</div>';
        return;
      }
      grid.innerHTML = books.map(function (b, idx) {
        var cat = categoryStyle(b.category);
        var available = b.availableCopies > 0;
        return '' +
          '<article class="book-card card-3d" data-tilt style="animation-delay:' + (idx * 0.04) + 's">' +
            '<div class="book-card__cover">' +
              '<span class="book-card__emoji">' + cat.emoji + '</span>' +
              '<span class="chip ' + cat.cls + ' book-card__cat">' + esc(b.category) + '</span>' +
            '</div>' +
            '<div class="book-card__body">' +
              '<h3 class="book-card__title">' + esc(b.title) + '</h3>' +
              '<p class="book-card__author">✍️ ' + esc(b.author) + '</p>' +
              '<div class="book-card__meta">' +
                '<span class="muted mono small">' + esc(b.isbn) + '</span>' +
                availabilityChip(b.availableCopies) +
              '</div>' +
              '<div class="book-card__actions">' +
                (available
                  ? '<form method="post" action="' + window.__LMS__.ctx + '/student/issue" ' +
                    'onsubmit="return confirm(\'Issue this book for 14 days?\')">' +
                    '<input type="hidden" name="_csrf" value="' + window.__LMS__.csrf + '">' +
                    '<input type="hidden" name="bookId" value="' + b.id + '">' +
                    '<button class="btn btn-sm btn-primary ripple" type="submit">📘 Issue</button></form>'
                  : '') +
                '<a class="btn btn-sm btn-outline ripple" href="' + window.__LMS__.ctx + '/student/books?q=' + encodeURIComponent(b.title) + '">Details</a>' +
              '</div>' +
            '</div>' +
          '</article>';
      }).join('');
      initTilt(); // rebind tilt for new cards
    }

    input.addEventListener('input', function () {
      clearTimeout(timer);
      timer = setTimeout(function () {
        var q = input.value.trim();
        var cat = document.getElementById('categorySelect').value;
        fetch(window.__LMS__.ctx + '/student/books?format=json&q=' + encodeURIComponent(q) + '&category=' + encodeURIComponent(cat), {
          headers: { 'Accept': 'application/json' }
        })
          .then(function (r) { return r.json(); })
          .then(render)
          .catch(function () { /* silent — server render still works */ });
      }, 250);
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    initTilt();
    initRipple();
    initToasts();
    initSidebar();
    initCatalogSearch();
  });
})();
