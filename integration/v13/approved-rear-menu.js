/* V13 approved rear-drawer menu. Staged only; not linked into production HTML.
 * Mount using exact existing DOM references after the V13 page loads.
 * Existing onclick handlers, navigation, and databases remain untouched.
 */
(function (root) {
  "use strict";
  const OMIT = Object.freeze([
    "1~300회차 복구 연구실", "항행 자동 A방", "항행 자동 B방", "원문 분석"
  ]);
  const REQUIRED = Object.freeze([
    "선장방", "신서방(구 도사방)", "항해사방", "항행의자유방",
    "성적 관리", "누적 DB", "원문 채팅방", "작업 기록", "설정"
  ]);
  const STYLE_ID = "v13-approved-rear-menu-css";
  function normalize(s) {
    return String(s || "").replace(/[\s\u00a0]+/g, "").replace(/[ⓐⒶⒷⓑ]/g, "").toLowerCase();
  }
  const OMIT_NORMALIZED = OMIT.map(normalize);
  function addStyles(doc) {
    if (doc.getElementById(STYLE_ID)) return;
    const css = doc.createElement("style");
    css.id = STYLE_ID;
    css.textContent = [
      ".v13-rear-drawer{position:fixed!important;z-index:10000!important;left:0;top:0;bottom:0;",
      "width:min(82vw,320px);overflow-y:auto;box-sizing:border-box;transform:translateX(-105%);",
      "transition:transform .24s ease;visibility:hidden;background:rgba(2,21,43,.95);",
      "border-right:2px solid #c79a44;box-shadow:4px 0 20px #0009;}",
      ".v13-rear-drawer.is-open{transform:translateX(0);visibility:visible;}",
      ".v13-rear-backdrop{position:fixed;inset:0;z-index:9999;background:rgba(0,0,0,.35);",
      "display:none;}",
      ".v13-rear-backdrop.is-open{display:block;}",
      ".v13-rear-drawer [data-v13-menu-omitted]{display:none!important;}",
      "@media(prefers-reduced-motion:reduce){.v13-rear-drawer{transition:none;}}"
    ].join("");
    doc.head.appendChild(css);
  }
  function mount({ menu, toggle, items, centralRawButton, doc = document }) {
    if (!menu || !toggle || !Array.isArray(items)) throw Error("V13_MENU_DOM_REQUIRED");
    if (menu.dataset.v13RearMounted === "true") throw Error("V13_MENU_ALREADY_MOUNTED");
    if (!centralRawButton || !centralRawButton.isConnected) throw Error("V13_CENTRAL_RAW_BUTTON_REQUIRED");
    const present = new Set(items.map(({ label }) => normalize(label)));
    const missing = REQUIRED.filter(label => !present.has(normalize(label)));
    if (missing.length) throw Error("V13_REQUIRED_MENU_MISSING:" + missing.join(","));
    addStyles(doc);
    const previous = { menuClass: menu.className, menuRole: menu.getAttribute("role"),
      toggleExpanded: toggle.getAttribute("aria-expanded") };
    const hidden = [];
    items.forEach(({ node, label }) => {
      if (!node) throw Error("V13_MENU_ITEM_NODE_REQUIRED:" + label);
      if (OMIT_NORMALIZED.includes(normalize(label))) {
        hidden.push({ node, oldHidden: node.hidden, oldAria: node.getAttribute("aria-hidden") });
        node.hidden = true;
        node.setAttribute("aria-hidden", "true");
        node.setAttribute("data-v13-menu-omitted", "true");
      }
    });
    const backdrop = doc.createElement("div");
    backdrop.className = "v13-rear-backdrop";
    backdrop.setAttribute("aria-hidden", "true");
    doc.body.appendChild(backdrop);
    menu.classList.add("v13-rear-drawer");
    menu.setAttribute("role", "navigation");
    menu.setAttribute("aria-label", "V13 메뉴");
    menu.dataset.v13RearMounted = "true";
    toggle.setAttribute("aria-controls", menu.id || (menu.id = "v13-rear-menu"));
    toggle.setAttribute("aria-expanded", "false");
    let open = false;
    function show(value) {
      open = Boolean(value);
      menu.classList.toggle("is-open", open);
      backdrop.classList.toggle("is-open", open);
      toggle.setAttribute("aria-expanded", String(open));
      menu.setAttribute("aria-hidden", String(!open));
    }
    function onToggle(e) { e.preventDefault(); show(!open); }
    function onKey(e) { if (e.key === "Escape" && open) { show(false); toggle.focus(); } }
    function onBackdrop() { show(false); }
    toggle.addEventListener("click", onToggle);
    backdrop.addEventListener("click", onBackdrop);
    doc.addEventListener("keydown", onKey);
    show(false);
    return Object.freeze({
      open: () => show(true),
      close: () => show(false),
      audit: () => ({ mode: "rear-drawer", requiredCount: REQUIRED.length,
        preserved: REQUIRED.slice(), omitted: OMIT.slice(), menuOpen: open }),
      destroy: () => {
        toggle.removeEventListener("click", onToggle);
        backdrop.removeEventListener("click", onBackdrop);
        doc.removeEventListener("keydown", onKey);
        show(false); backdrop.remove();
        menu.classList.remove("v13-rear-drawer", "is-open");
        menu.removeAttribute("data-v13-rear-mounted");
        if (previous.menuRole === null) menu.removeAttribute("role");
        else menu.setAttribute("role", previous.menuRole);
        if (previous.toggleExpanded === null) toggle.removeAttribute("aria-expanded");
        else toggle.setAttribute("aria-expanded", previous.toggleExpanded);
        hidden.forEach(({ node, oldHidden, oldAria }) => {
          node.hidden = oldHidden; node.removeAttribute("data-v13-menu-omitted");
          if (oldAria === null) node.removeAttribute("aria-hidden");
          else node.setAttribute("aria-hidden", oldAria);
        });
      }
    });
  }
  root.V13ApprovedRearMenu = Object.freeze({ mount, REQUIRED, OMIT });
})(globalThis);
