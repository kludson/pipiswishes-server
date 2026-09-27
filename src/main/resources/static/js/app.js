(() => {
  'use strict';

  const endpoints = { all: '/wish', pending: '/wish/pending', executed: '/wish/executed' };
  const emptyMessages = {
    all: 'Пока здесь нет желаний. Самое время загадать первое ✨',
    pending: 'Все желания исполнены ♥',
    executed: 'Здесь пока нет исполненных желаний.'
  };
  const state = { filter: 'all', wishes: [], loadId: 0, controller: null, editing: null, deleting: null };
  const byId = (id) => document.getElementById(id);
  const list = byId('wish-list');
  const createForm = byId('create-form');
  const editForm = byId('edit-form');
  const deleteForm = byId('delete-form');
  let toastTimer;

  // API: POST/PUT accept title and description; status is managed by the server.
  async function request(url, { method = 'GET', body, signal } = {}) {
    const response = await fetch(url, {
      method, signal, cache: 'no-store',
      headers: body ? { 'Content-Type': 'application/json', Accept: 'application/json' } : { Accept: 'application/json' },
      body: body ? JSON.stringify(body) : undefined
    });
    if (!response.ok) throw new Error(`Request failed: ${method} ${url} (${response.status})`);
    if (method === 'DELETE' || response.status === 204) return null;
    return response.json();
  }

  function notify(message, celebrate = false) {
    clearTimeout(toastTimer);
    const toast = byId('toast');
    toast.textContent = message;
    toast.classList.toggle('celebrate', celebrate);
    toast.hidden = false;
    toastTimer = setTimeout(() => { toast.hidden = true; }, 4500);
  }

  function element(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
  }

  function action(text, className, handler) {
    const button = element('button', className, text);
    button.type = 'button';
    button.addEventListener('click', handler);
    return button;
  }

  function renderWish(wish) {
    const executed = wish.status === 'EXECUTED';
    const pending = wish.status === 'PENDING';
    const card = element('article', `wish-card${executed ? ' executed' : ''}`);
    card.append(element('span', 'status', executed ? '✓ Исполнено' : pending ? '✧ В ожидании' : 'Статус не указан'));
    card.append(element('h3', '', wish.title || 'Желание без названия'));
    if (wish.description) card.append(element('p', 'wish-description', wish.description));
    const actions = element('div', 'card-actions');
    const edit = action('✎ Изменить', 'quiet', () => openEdit(wish));
    edit.disabled = !pending;
    actions.append(edit, action('Удалить', 'quiet', () => openDelete(wish)));
    if (pending) actions.append(action('♥ Исполнить желание', 'execute', () => executeWish(wish, card)));
    if (executed) actions.append(element('span', 'completed-note', 'Ещё одна мечта стала настоящей ♥'));
    card.append(actions);
    if (executed) card.append(element('small', 'locked-note', 'Исполненные желания уже нельзя изменить.'));
    return card;
  }

  function showListState(message, retry = false) {
    byId('list-state').hidden = false;
    byId('state-text').textContent = message;
    byId('retry').hidden = !retry;
  }

  // Only the latest filter request may update the list.
  async function loadWishes() {
    const loadId = ++state.loadId;
    state.controller?.abort();
    state.controller = new AbortController();
    list.replaceChildren();
    list.setAttribute('aria-busy', 'true');
    byId('list-count').textContent = '';
    showListState('Собираем наши желания…');
    try {
      const wishes = await request(endpoints[state.filter], { signal: state.controller.signal });
      if (loadId !== state.loadId) return;
      if (!Array.isArray(wishes)) throw new Error('Expected a wish array');
      state.wishes = wishes;
      list.replaceChildren(...wishes.map(renderWish));
      byId('list-count').textContent = `В списке: ${wishes.length}`;
      byId('list-state').hidden = wishes.length > 0;
      if (!wishes.length) showListState(emptyMessages[state.filter]);
    } catch (error) {
      if (error.name === 'AbortError' || loadId !== state.loadId) return;
      console.error(error);
      showListState('Не удалось загрузить желания. Попробуй ещё раз.', true);
    } finally {
      if (loadId === state.loadId) list.setAttribute('aria-busy', 'false');
    }
  }

  function formError(form, message = '') {
    const error = form.querySelector('.form-error');
    error.textContent = message;
    error.hidden = !message;
  }

  function readWish(form) {
    const title = form.elements.title.value.trim();
    if (!title) {
      formError(form, 'Напиши, о чём ты мечтаешь.');
      form.elements.title.focus();
      return null;
    }
    return { title, description: form.elements.description.value.trim() };
  }

  async function submitForm(form, operation) {
    const fieldset = form.querySelector('fieldset');
    if (fieldset.disabled) return;
    formError(form);
    fieldset.disabled = true;
    form.setAttribute('aria-busy', 'true');
    try {
      await operation();
    } catch (error) {
      console.error(error);
      formError(form, 'Что-то пошло не так. Попробуй ещё раз.');
    } finally {
      fieldset.disabled = false;
      form.setAttribute('aria-busy', 'false');
    }
  }

  function openEdit(wish) {
    state.editing = wish.id;
    editForm.elements.title.value = wish.title || '';
    editForm.elements.description.value = wish.description || '';
    formError(editForm);
    byId('edit-dialog').showModal();
    editForm.elements.title.focus();
  }

  function openDelete(wish) {
    state.deleting = wish.id;
    byId('delete-preview').textContent = wish.title || 'Желание без названия';
    formError(deleteForm);
    byId('delete-dialog').showModal();
  }

  async function executeWish(wish, card) {
    if (card.getAttribute('aria-busy') === 'true') return;
    card.setAttribute('aria-busy', 'true');
    card.querySelectorAll('button').forEach((button) => { button.disabled = true; });
    try {
      await request(`/wish/${wish.id}/execute`, { method: 'POST' });
      notify('Ещё одно желание исполнилось. Спасибо тебе ♥', true);
      await loadWishes();
      document.querySelector('[data-filter][aria-pressed="true"]').focus({ preventScroll: true });
    } catch (error) {
      console.error(error);
      notify('Что-то пошло не так. Попробуй ещё раз.');
    } finally {
      card.setAttribute('aria-busy', 'false');
      card.querySelectorAll('button').forEach((button) => { button.disabled = false; });
    }
  }

  createForm.addEventListener('submit', (event) => {
    event.preventDefault();
    const body = readWish(createForm);
    if (!body) return;
    submitForm(createForm, async () => {
      await request('/wish', { method: 'POST', body });
      createForm.reset();
      notify('Желание добавлено. Пусть оно сбудется ♥');
      await loadWishes();
    });
  });

  editForm.addEventListener('submit', (event) => {
    event.preventDefault();
    const body = readWish(editForm);
    if (!body) return;
    submitForm(editForm, async () => {
      await request(`/wish/${state.editing}`, { method: 'PUT', body });
      byId('edit-dialog').close();
      notify('Желание обновлено ♥');
      await loadWishes();
      document.querySelector('[data-filter][aria-pressed="true"]').focus({ preventScroll: true });
    });
  });

  deleteForm.addEventListener('submit', (event) => {
    event.preventDefault();
    submitForm(deleteForm, async () => {
      await request(`/wish/${state.deleting}`, { method: 'DELETE' });
      byId('delete-dialog').close();
      notify('Желание удалено. Для новых мечтаний всегда есть место.');
      await loadWishes();
      document.querySelector('[data-filter][aria-pressed="true"]').focus({ preventScroll: true });
    });
  });

  document.querySelectorAll('[data-filter]').forEach((button) => {
    button.addEventListener('click', () => {
      state.filter = button.dataset.filter;
      document.querySelectorAll('[data-filter]').forEach((item) => item.setAttribute('aria-pressed', String(item === button)));
      loadWishes();
    });
  });
  document.querySelectorAll('[data-close]').forEach((button) => {
    button.addEventListener('click', () => byId(button.dataset.close).close());
  });
  document.querySelectorAll('dialog').forEach((dialog) => {
    dialog.addEventListener('cancel', (event) => {
      if (dialog.querySelector('fieldset').disabled) event.preventDefault();
    });
  });
  byId('retry').addEventListener('click', loadWishes);
  loadWishes();
})();
