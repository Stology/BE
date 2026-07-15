const state = {
  accessToken: localStorage.getItem('inquiryAccessToken') || null,
  studyId: null,
  page: 0,
  size: 10,
  lastQuestionPage: null,
  expandedQuestionId: null,
  editingQuestionId: null,
  pendingDelete: null, // { type: 'question' | 'reply', questionId, replyId }
  questionCache: {}
};

/* ---------- low-level API helpers ---------- */
async function api(path, method = 'GET', body) {
  const headers = { 'Content-Type': 'application/json' };
  if (state.accessToken) headers['Authorization'] = 'Bearer ' + state.accessToken;

  const res = await fetch(path, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined
  });

  return handleApiResponse(res);
}

async function apiUpload(path, files) {
  const formData = new FormData();
  for (const file of files) formData.append('images', file);

  const headers = {};
  if (state.accessToken) headers['Authorization'] = 'Bearer ' + state.accessToken;

  const res = await fetch(path, { method: 'POST', headers, body: formData });
  return handleApiResponse(res);
}

async function handleApiResponse(res) {
  const data = await res.json().catch(() => null);

  if (res.status === 401) {
    throw new Error('인증에 실패했습니다. accessToken을 다시 확인해주세요.');
  }
  if (!data || data.isSuccess === false) {
    throw new Error((data && data.message) || '요청에 실패했습니다.');
  }
  return data.result;
}

/* ---------- toast ---------- */
let toastTimer;
function showToast(message) {
  const toast = document.getElementById('toast');
  toast.textContent = message;
  toast.classList.remove('hidden');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.add('hidden'), 2500);
}

/* ---------- modal helpers ---------- */
function openModal(id) { document.getElementById(id).classList.remove('hidden'); }
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }

/* ---------- auth ---------- */
function saveToken() {
  const token = document.getElementById('token-input').value.trim();
  if (!token) return;
  state.accessToken = token;
  localStorage.setItem('inquiryAccessToken', token);
  document.getElementById('auth-status').textContent = 'accessToken이 저장되었습니다. 이제 스터디를 불러올 수 있습니다.';
  showToast('토큰이 저장되었습니다.');

  loadTemplates();
  loadMyStudies();
}

/* ---------- study room create / list ---------- */
async function loadTemplates() {
  const select = document.getElementById('create-study-template');
  try {
    const result = await api('/api/template');
    if (!result.templates.length) {
      select.innerHTML = '<option value="">등록된 템플릿이 없습니다</option>';
      return;
    }
    select.innerHTML = result.templates
      .map(t => `<option value="${t.templateId}">${escapeHtml(t.name)} (${escapeHtml(t.uploader)})</option>`)
      .join('');
  } catch (e) {
    select.innerHTML = '<option value="">템플릿을 불러오지 못했습니다</option>';
    showToast(e.message);
  }
}

async function loadMyStudies() {
  const el = document.getElementById('my-studies');
  try {
    const result = await api('/api/user/me/study?status=active');
    if (!result.studies.length) {
      el.innerHTML = '<p class="muted">참여 중인 스터디가 없습니다.</p>';
      return;
    }
    el.innerHTML = result.studies.map(s => `
      <button class="study-card" onclick="selectStudy(${s.studyId})">
        <div class="study-card-name">${escapeHtml(s.name)}</div>
        <div class="study-card-meta">studyId ${s.studyId} · 시작일 ${s.startDate}</div>
      </button>
    `).join('');
  } catch (e) {
    el.innerHTML = '<p class="muted">스터디 목록을 불러오지 못했습니다.</p>';
    showToast(e.message);
  }
}

async function handleCreateStudy() {
  const name = document.getElementById('create-study-name').value.trim();
  const manualTemplateId = document.getElementById('create-study-template-manual').value.trim();
  const selectedTemplateId = document.getElementById('create-study-template').value;
  const templateId = Number(manualTemplateId || selectedTemplateId);
  const startDate = document.getElementById('create-study-start').value;
  const description = document.getElementById('create-study-desc').value.trim();

  if (!name || !templateId || !startDate) {
    showToast('스터디 이름, 템플릿 ID, 시작일은 필수입니다. 목록에 템플릿이 없다면 템플릿 ID를 직접 입력해주세요.');
    return;
  }

  try {
    const studyId = await api('/api/study', 'POST', { name, templateId, startDate, description: description || null });
    document.getElementById('create-study-name').value = '';
    document.getElementById('create-study-desc').value = '';
    document.getElementById('create-study-template-manual').value = '';
    showToast('스터디 방이 생성되었습니다.');
    await loadMyStudies();
    selectStudy(studyId);
  } catch (e) {
    showToast(e.message);
  }
}

function selectStudy(studyId) {
  document.getElementById('study-id-input').value = studyId;
  loadStudy();
}

/* ---------- study ---------- */
async function loadStudy() {
  const studyId = Number(document.getElementById('study-id-input').value);
  if (!studyId) {
    showToast('studyId를 입력해주세요.');
    return;
  }
  if (!state.accessToken) {
    showToast('먼저 accessToken을 저장해주세요.');
    return;
  }

  state.studyId = studyId;
  state.page = 0;
  state.expandedQuestionId = null;
  document.getElementById('qna-panel').classList.remove('hidden');

  await loadQuestions(0);
}

/* ---------- question list ---------- */
async function loadQuestions(page = 0) {
  state.page = page;

  try {
    const result = await api(`/api/study/${state.studyId}/question?page=${page}&size=${state.size}`);
    state.lastQuestionPage = result;
    renderQuestions(result);
  } catch (e) {
    showToast(e.message);
  }
}

function renderQuestions(result) {
  const listEl = document.getElementById('qna-list');
  const emptyEl = document.getElementById('qna-empty');
  const statusBadge = document.getElementById('study-status-badge');

  statusBadge.textContent = result.studyEnded ? '종료됨' : '진행 중';
  statusBadge.classList.toggle('ended', result.studyEnded);
  document.getElementById('btn-write-question').classList.toggle('hidden', result.studyEnded);

  if (!result.questionList.length) {
    listEl.innerHTML = '';
    emptyEl.classList.remove('hidden');
    document.getElementById('qna-pagination').innerHTML = '';
    return;
  }
  emptyEl.classList.add('hidden');

  listEl.innerHTML = result.questionList.map(q => `
    <div class="qna-item" id="qna-item-${q.questionId}">
      <div class="qna-item-row" onclick="toggleQuestion(${q.questionId})">
        <div class="qna-item-title">${escapeHtml(q.title)}</div>
        <div class="qna-item-meta">
          ${escapeHtml(q.authorName)} · ${formatDate(q.createdAt)} · 답글 ${q.answerCount}${q.hasImage ? ' · 첨부 있음' : ''}
        </div>
      </div>
      <div class="qna-detail hidden" id="qna-detail-${q.questionId}"></div>
    </div>
  `).join('');

  renderPagination(result);
}

function renderPagination(result) {
  const el = document.getElementById('qna-pagination');
  if (result.totalPage <= 1) { el.innerHTML = ''; return; }

  let html = '';
  for (let i = 0; i < result.totalPage; i++) {
    html += `<button class="${i === state.page ? 'active' : ''}" onclick="loadQuestions(${i})">${i + 1}</button>`;
  }
  el.innerHTML = html;
}

/* ---------- question inline detail ---------- */
async function toggleQuestion(questionId) {
  const detailEl = document.getElementById('qna-detail-' + questionId);

  if (state.expandedQuestionId === questionId) {
    detailEl.classList.add('hidden');
    state.expandedQuestionId = null;
    return;
  }

  if (state.expandedQuestionId !== null) {
    const prev = document.getElementById('qna-detail-' + state.expandedQuestionId);
    if (prev) prev.classList.add('hidden');
  }
  state.expandedQuestionId = questionId;

  await renderQuestionDetail(questionId);
  detailEl.classList.remove('hidden');
}

async function renderQuestionDetail(questionId) {
  const detailEl = document.getElementById('qna-detail-' + questionId);

  try {
    const q = await api(`/api/study/${state.studyId}/question/${questionId}`);
    state.questionCache[questionId] = q;

    const images = q.imageUrls.length
      ? `<div class="qna-detail-images">${q.imageUrls.map(url => `<img src="${escapeHtml(url)}">`).join('')}</div>`
      : '';

    const ownerActions = q.isMine && !q.studyEnded
      ? `<div class="qna-detail-actions">
           <button class="btn btn-secondary" onclick="openQuestionModal(${questionId})">수정</button>
           <label class="btn btn-secondary file-inline">이미지 추가
             <input type="file" accept="image/*" multiple onchange="uploadQuestionImagesInline(${questionId}, this.files)">
           </label>
           <button class="btn btn-danger" onclick="openDeleteModal('question', ${questionId})">삭제</button>
         </div>`
      : '';

    const replyCompose = !q.studyEnded
      ? `<div class="reply-compose">
           <div class="reply-compose-fields">
             <textarea id="reply-input-${questionId}" placeholder="답글을 입력하세요"></textarea>
             <input type="file" id="reply-image-input-${questionId}" accept="image/*" multiple>
           </div>
           <button class="btn btn-primary" onclick="submitReply(${questionId})">답글 작성</button>
         </div>`
      : '';

    const replyList = q.answerList.length
      ? `<div class="reply-list">${q.answerList.map(r => renderReply(questionId, r, q.studyEnded)).join('')}</div>`
      : (q.studyEnded ? `<p class="reply-empty">아직 답글이 없습니다.</p>` : '');

    detailEl.innerHTML = `
      <div class="qna-detail-body">${escapeHtml(q.content)}</div>
      ${images}
      ${ownerActions}
      ${replyCompose}
      ${replyList}
    `;
  } catch (e) {
    showToast(e.message);
  }
}

async function uploadQuestionImagesInline(questionId, files) {
  if (!files.length) return;
  try {
    await apiUpload(`/api/study/${state.studyId}/question/${questionId}/image`, files);
    showToast('이미지가 첨부되었습니다.');
    await renderQuestionDetail(questionId);
    await loadQuestions(state.page);
    document.getElementById('qna-detail-' + questionId).classList.remove('hidden');
  } catch (e) {
    showToast(e.message);
  }
}

function renderReply(questionId, reply, studyEnded) {
  const actions = (reply.isMine && !studyEnded)
    ? `<div class="reply-item-actions">
         <button class="btn btn-secondary" onclick="startEditReply(${questionId}, ${reply.answerId})">수정</button>
         <label class="btn btn-secondary file-inline">이미지 추가
           <input type="file" accept="image/*" multiple onchange="uploadAnswerImagesInline(${questionId}, ${reply.answerId}, this.files)">
         </label>
         <button class="btn btn-danger" onclick="openDeleteModal('reply', ${questionId}, ${reply.answerId})">삭제</button>
       </div>`
    : '';

  const images = reply.imageUrls.length
    ? `<div class="qna-detail-images">${reply.imageUrls.map(url => `<img src="${escapeHtml(url)}">`).join('')}</div>`
    : '';

  return `
    <div class="reply-item" id="reply-item-${reply.answerId}">
      <div class="reply-item-head"><span>${escapeHtml(reply.authorName)}</span><span>${formatDate(reply.createdAt)}</span></div>
      <div class="reply-item-body" id="reply-body-${reply.answerId}">${escapeHtml(reply.content)}</div>
      ${images}
      ${actions}
    </div>
  `;
}

async function submitReply(questionId) {
  const input = document.getElementById('reply-input-' + questionId);
  const content = input.value.trim();
  if (!content) return;
  const fileInput = document.getElementById('reply-image-input-' + questionId);

  try {
    const result = await api(`/api/study/${state.studyId}/question/${questionId}/answer`, 'POST', { content });
    if (fileInput.files.length) {
      await apiUpload(`/api/study/${state.studyId}/question/${questionId}/answer/${result.answerId}/image`, fileInput.files);
    }
    await renderQuestionDetail(questionId);
    await loadQuestions(state.page);
    document.getElementById('qna-detail-' + questionId).classList.remove('hidden');
  } catch (e) {
    showToast(e.message);
  }
}

async function uploadAnswerImagesInline(questionId, answerId, files) {
  if (!files.length) return;
  try {
    await apiUpload(`/api/study/${state.studyId}/question/${questionId}/answer/${answerId}/image`, files);
    showToast('이미지가 첨부되었습니다.');
    await renderQuestionDetail(questionId);
  } catch (e) {
    showToast(e.message);
  }
}

function startEditReply(questionId, replyId) {
  const bodyEl = document.getElementById('reply-body-' + replyId);
  const original = bodyEl.textContent;
  bodyEl.dataset.original = original;
  bodyEl.innerHTML = `
    <textarea id="reply-edit-${replyId}">${escapeHtml(original)}</textarea>
    <div class="reply-item-actions">
      <button class="btn btn-secondary" onclick="cancelEditReply(${replyId})">취소</button>
      <button class="btn btn-primary" onclick="saveEditReply(${questionId}, ${replyId})">저장</button>
    </div>
  `;
}

function cancelEditReply(replyId) {
  const bodyEl = document.getElementById('reply-body-' + replyId);
  bodyEl.textContent = bodyEl.dataset.original;
}

async function saveEditReply(questionId, replyId) {
  const value = document.getElementById('reply-edit-' + replyId).value.trim();
  if (!value) return;

  try {
    await api(`/api/study/${state.studyId}/question/${questionId}/answer/${replyId}`, 'PATCH', { content: value });
    await renderQuestionDetail(questionId);
  } catch (e) {
    showToast(e.message);
  }
}

/* ---------- question write / edit modal ---------- */
function openQuestionModal(questionId) {
  const question = questionId ? state.questionCache[questionId] : null;
  state.editingQuestionId = question ? questionId : null;
  document.getElementById('question-modal-title').textContent = question ? '질문 수정' : '질문 작성';
  document.getElementById('question-title-input').value = question ? question.title : '';
  document.getElementById('question-content-input').value = question ? question.content : '';
  document.getElementById('question-image-input').value = '';
  document.getElementById('btn-question-submit').textContent = question ? '수정하기' : '질문하기';
  updateQuestionCharCount();
  openModal('modal-question');
}

function updateQuestionCharCount() {
  const title = document.getElementById('question-title-input').value;
  const content = document.getElementById('question-content-input').value;
  document.getElementById('question-char-count').textContent = `${title.length}/50 · ${content.length}/1000`;
  document.getElementById('btn-question-submit').disabled = !title.trim() || !content.trim();
}

async function handleSubmitQuestion() {
  const title = document.getElementById('question-title-input').value.trim();
  const content = document.getElementById('question-content-input').value.trim();
  const files = document.getElementById('question-image-input').files;

  try {
    let questionId;
    if (state.editingQuestionId) {
      const result = await api(`/api/study/${state.studyId}/question/${state.editingQuestionId}`, 'POST', { title, content });
      questionId = result.questionId;
      showToast('질문이 수정되었습니다.');
    } else {
      const result = await api(`/api/study/${state.studyId}/question`, 'POST', { title, content });
      questionId = result.questionId;
      showToast('질문이 등록되었습니다.');
    }

    if (files.length) {
      await apiUpload(`/api/study/${state.studyId}/question/${questionId}/image`, files);
    }

    closeModal('modal-question');
    await loadQuestions(state.page);
    if (state.editingQuestionId) {
      state.expandedQuestionId = null;
      await toggleQuestion(questionId);
    }
  } catch (e) {
    showToast(e.message);
  }
}

/* ---------- delete ---------- */
function openDeleteModal(type, questionId, replyId) {
  state.pendingDelete = { type, questionId, replyId };
  openModal('modal-delete');
}

async function handleConfirmDelete() {
  const { type, questionId, replyId } = state.pendingDelete;

  try {
    if (type === 'question') {
      await api(`/api/study/${state.studyId}/question/${questionId}`, 'DELETE');
      state.expandedQuestionId = null;
    } else {
      await api(`/api/study/${state.studyId}/question/${questionId}/answer/${replyId}`, 'DELETE');
    }
    closeModal('modal-delete');
    showToast('삭제되었습니다.');
    await loadQuestions(state.page);
    if (type === 'reply' && state.expandedQuestionId === questionId) {
      await renderQuestionDetail(questionId);
    }
  } catch (e) {
    closeModal('modal-delete');
    showToast(e.message);
    await loadQuestions(state.page);
  }
}

/* ---------- utils ---------- */
function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str ?? '';
  return div.innerHTML.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

function formatDate(iso) {
  if (!iso) return '';
  return iso.slice(0, 10);
}

/* ---------- init ---------- */
(function init() {
  if (state.accessToken) {
    document.getElementById('token-input').value = state.accessToken;
    document.getElementById('auth-status').textContent = '저장된 accessToken을 불러왔습니다.';
    loadTemplates();
    loadMyStudies();
  }
})();
