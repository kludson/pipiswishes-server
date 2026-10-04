(() => {
  'use strict';

  const byId = (id) => document.getElementById(id);
  const tabs = [byId('wishes-tab'), byId('kisses-tab')];
  const button = byId('kiss-button');
  const endpoint = '/kisscount?id=1';
  let count = 0;
  let pending = 0;
  let busy = false;
  let ready = false;
  let active = false;
  let heartAnimation;
  const particles = new Set();

  function animateKiss() {
    const art = byId('kiss-button-art');
    if (typeof art.animate !== 'function' ||
        (typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches)) return;
    heartAnimation?.cancel();
    heartAnimation = art.animate([
      { transform: 'scale(1)' },
      { transform: 'scale(.9, .86)', offset: .18 },
      { transform: 'scale(1.07, 1.04)', offset: .48 },
      { transform: 'scale(.98, .99)', offset: .75 },
      { transform: 'scale(1)' }
    ], { duration: 460, easing: 'cubic-bezier(.2,.7,.3,1)' });

    // Keep the little hearts decorative and bounded during rapid taps.
    for (const [x, y, rotate] of [[-80, -100, -18], [0, -135, 8], [80, -95, 18]]) {
      if (particles.size >= 12) break;
      const particle = document.createElement('span');
      particle.className = 'kiss-particle';
      particle.textContent = '♡';
      particle.setAttribute('aria-hidden', 'true');
      byId('kiss-heart-wrap').append(particle);
      particles.add(particle);
      const remove = () => { particle.remove(); particles.delete(particle); };
      const animation = particle.animate([
        { transform: 'translate(-50%, -50%) scale(.6)', opacity: 0 },
        { opacity: .8, offset: .2 },
        { transform: `translate(calc(-50% + ${x}px), ${y}px) rotate(${rotate}deg) scale(1.1)`, opacity: 0 }
      ], { duration: 700, easing: 'cubic-bezier(.15,.6,.35,1)' });
      animation.onfinish = remove;
      animation.oncancel = remove;
    }
  }

  function render() {
    const visibleCount = count + pending;
    byId('kiss-count').textContent = ready ? String(visibleCount) : '—';
    const last = visibleCount % 10;
    const teen = visibleCount % 100 >= 11 && visibleCount % 100 <= 14;
    byId('kiss-unit').textContent = teen ? 'поцелуйчиков' : last === 1 ? 'поцелуйчик' : last >= 2 && last <= 4 ? 'поцелуйчика' : 'поцелуйчиков';
    button.disabled = !ready;
    byId('kiss-status').textContent = busy
      ? 'Бережно сохраняем твои поцелуйчики…'
      : !ready ? 'Сначала узнаем точное число поцелуйчиков.'
      : count ? 'Все поцелуйчики записаны. Жду встречи с тобой ♡' : 'Сердечко ждёт твоего первого жама.';
  }

  function showError(message = '') {
    byId('kiss-error').textContent = message;
    byId('kiss-error').hidden = !message;
    byId('kiss-retry').hidden = !message;
  }

  async function api(method = 'GET') {
    const response = await fetch(endpoint, { method, cache: 'no-store', headers: { Accept: 'application/json' } });
    if (!response.ok) {
      const error = new Error('Не удалось сохранить поцелуйчик. Обнови счётчик перед следующим жамом.');
      error.serverResponse = true;
      throw error;
    }
    // PUT returns 200 with an empty body.
    if (method === 'PUT') return null;
    const value = await response.json();
    if (!Number.isSafeInteger(value.counter) || value.counter < 0) throw new Error('Invalid kiss count');
    return value.counter;
  }

  async function refresh() {
    if (busy) return;
    busy = true;
    ready = false;
    showError();
    render();
    try {
      count = await api();
      ready = true;
    } catch {
      showError('Не удалось узнать, сколько поцелуйчиков на сегодня. Попробуй обновить счётчик.');
    } finally {
      busy = false;
      render();
    }
  }

  async function saveClicks() {
    if (busy) return;
    busy = true;
    render();
    try {
      // Keep quick taps in a queue: each tap sends exactly one PUT.
      while (pending > 0) {
        await api('PUT');
        pending--;
        count++;
        render();
      }
      ready = false;
      render();
      // The server is authoritative, including when midnight passed during tapping.
      count = await api();
      ready = true;
    } catch (error) {
      pending = 0;
      ready = false;
      // A lost response may hide a successful PUT; never automatically replay it.
      showError(error.serverResponse ? error.message : 'Связь прервалась. Часть жамов могла сохраниться — обнови счётчик, чтобы увидеть точное число.');
    } finally {
      busy = false;
      render();
    }
  }

  function selectTab(index, focus = false) {
    tabs.forEach((tab, i) => {
      tab.setAttribute('aria-selected', String(i === index));
      tab.tabIndex = i === index ? 0 : -1;
      byId(tab.getAttribute('aria-controls')).hidden = i !== index;
    });
    active = index === 1;
    if (focus) tabs[index].focus();
    if (active) refresh();
  }

  tabs.forEach((tab, index) => {
    tab.addEventListener('click', () => selectTab(index));
    tab.addEventListener('keydown', (event) => {
      const next = { ArrowRight: (index + 1) % 2, ArrowLeft: (index + 1) % 2, Home: 0, End: 1 }[event.key];
      if (next === undefined) return;
      event.preventDefault();
      selectTab(next, true);
    });
  });
  button.addEventListener('click', () => {
    if (!ready) return;
    animateKiss();
    pending++;
    render();
    saveClicks();
  });
  byId('kiss-retry').addEventListener('click', refresh);
  document.addEventListener('visibilitychange', () => {
    if (active && !document.hidden) refresh();
  });
  // Refresh a visible idle counter across the daily boundary and other devices' taps.
  setInterval(() => {
    if (active && !document.hidden) refresh();
  }, 60000);
})();
