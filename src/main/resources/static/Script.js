const scheduleForm = document.getElementById('scheduleForm');
const scheduleList = document.getElementById('scheduleList');
const searchDate = document.getElementById('searchDate');
const cancelEditBtn = document.getElementById('cancelEditBtn');

// 編集用のIDを保持
let editingId = null;

// 認証付きのfetch関数
function fetchWithAuth(url, options = {}) {
  return fetch(url, {
    credentials: 'include',
    ...options
  }).then(res => {
    // 明示的な401ハンドリング
    if (res.status === 401) {
      alert('ログインしてください');
      location.href = 'Login.html';
      throw new Error('未ログイン');
    }

    const ct = (res.headers.get('content-type') || '').toLowerCase();

    // API呼び出しでHTMLが返ってきたら、セッション切れなどの可能性が高いのでログインへ誘導
    if (url.startsWith('/api/') && ct.includes('text/html')) {
      alert('セッションが切れました。ログインしてください。');
      location.href = 'Login.html';
      throw new Error('HTMLレスポンスを受け取りました(未ログイン可能性)');
    }

    return res;
  });
}

// 予定を取得して表示する関数
function fetchAndRenderSchedules(url) {
  fetchWithAuth(url)
    .then(res => {
      const ct = (res.headers.get('content-type') || '').toLowerCase();

      if (!res.ok) {
        // 204など対応(APIが204を返すケースは空配列扱い)
        if (res.status === 204) return [];
        throw new Error('ネットワーク応答がOKではありません: ' + res.status);
      }

      if (!ct.includes('application/json')) {
        // JSON以外(HTMLなど)が返ってきたら、ログインページへ遷移して処理中止
        throw new Error('非JSONレスポンスを受け取りました');
      }

      return res.json();
    })
    .then(data => {
      scheduleList.innerHTML = '';
      if (!data || data.length === 0) {
        scheduleList.innerHTML = '<li>予定がありません</li>';
        return;
      }

      data.forEach((s) => {
        const date = s.startTime.split('T')[0];
        const time = s.startTime.split('T')[1].slice(0, 5);
        const li = document.createElement('li');
        li.innerHTML = `<strong>${date} ${time}</strong> - ${s.title}<br>${s.description || ''}<br>
          <button onclick="editSchedule(${s.id})">編集</button>
          <button onclick="deleteSchedule(${s.id})">削除</button>`;
        scheduleList.appendChild(li);
      });
    })
    .catch(err => {
      console.error('予定の取得エラー:', err);
      // 未ログイン系のエラーなら既にリダイレクトしているはずだが念のため
      if (String(err).includes('未ログイン') || String(err).includes('非JSON')) {
        location.href = 'Login.html';
        return;
      }
      alert('予定の取得に失敗しました。');
    });
}

// 削除処理
function deleteSchedule(id) {
  if (!confirm('この予定を削除しますか？')) return; // 確認ダイアログを表示

  // APIにDELETEリクエストを送信して予定を削除
  fetchWithAuth(`/api/schedules/${id}`, {
    method: 'DELETE'
  })
  .then(res => {
    if (!res.ok) throw new Error('削除失敗'); // レスポンスがOKでない場合はエラーをスロー
    fetchAndRenderSchedules('/api/schedules'); // 削除後に予定を再取得して表示を更新
  })
  .catch(err => {
    console.error('削除エラー:', err); // エラーが発生した場合はコンソールにエラーを表示
    alert('削除に失敗しました。'); // ユーザーに削除失敗のメッセージを表示
  });
}

// 編集処理
function editSchedule(id) { 
  fetchWithAuth(`/api/schedules/${id}`) // APIにGETリクエストを送信して予定の詳細を取得
    .then(res => res.json()) // 取得した予定のデータをJSON
    .then(data => { // 取得した予定のデータをフォームにセット
      const date = data.startTime.split('T')[0]; // 予定の開始日時から日付部分を抽出（YYYY-MM-DD）
      document.getElementById('date').value = date; // フォームの日付入力に取得した日付をセット
      document.getElementById('title').value = data.title; // フォームのタイトル入力に取得したタイトルをセット
      document.getElementById('description').value = data.description; // フォームの説明入力に取得した説明をセット
      editingId = id; // 編集対象のIDを保存

      // ボタンのラベルを「更新」に変更
      scheduleForm.querySelector('button[type="submit"]').textContent = '更新';
    })
    .catch(err => {
      console.error('編集データ取得エラー:', err);
      alert('予定の取得に失敗しました。');
    });
    cancelEditBtn.style.display = 'inline-block'; // 編集時に表示
}

// 編集キャンセル処理
cancelEditBtn.addEventListener('click', () => {
  editingId = null; // 編集対象のIDをリセット
  scheduleForm.reset(); // フォームをリセット
  scheduleForm.querySelector('button[type="submit"]').textContent = '追加'; // ボタンのラベルを「追加」に戻す
  cancelEditBtn.style.display = 'none'; // ボタン非表示
});

// 初回ロード時に全件取得
window.addEventListener('DOMContentLoaded', () => {
  renderAuthButtons(); // 認証状態に応じてボタンを表示
  fetchAndRenderSchedules('/api/schedules');
});

// 登録処理
scheduleForm.addEventListener('submit', function (e) {
  e.preventDefault(); // フォームのデフォルトの送信を防止

  const date = document.getElementById('date').value; // フォームから日付を取得
  const title = document.getElementById('title').value; // フォームからタイトルを取得
  const description = document.getElementById('description').value; // フォームから説明を取得

  if(!date || !title) {
    alert('日付とタイトルは必須です');
    return;
  }

  // APIに送信するデータを作成
  const scheduleData = {
    title,
    description,
    startTime: date + 'T09:00:00',
    endTime: date + 'T10:00:00'
  }; // 予定の開始時間と終了時間を固定で設定(例: 9:00 - 10:00)

  if (editingId) {
    // 編集モード
    fetchWithAuth(`/api/schedules/${editingId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(scheduleData)
    }) // APIにPUTリクエストを送信して予定を更新
    .then(res => {
      if (!res.ok) throw new Error('更新失敗');
      alert('予定を更新しました！');
      editingId = null;
      scheduleForm.querySelector('button[type="submit"]').textContent = '追加'; // 元に戻す
      scheduleForm.reset();
      fetchAndRenderSchedules('/api/schedules'); // 更新後に予定を再取得して表示を更新
    })
    .catch(err => {
      console.error('更新失敗:', err);
      alert('予定の更新に失敗しました。');
    });
  } else {
    // 新規追加モード
    fetchWithAuth('/api/schedules', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(scheduleData)
    })
    .then(res => {
      if (!res.ok) throw new Error("登録に失敗");
      return res.json();
    })
    .then(() => {
      alert("予定を追加しました！");
      scheduleForm.reset();
      fetchAndRenderSchedules('/api/schedules');
    })
    .catch(err => {
      console.error("追加失敗:", err);
      alert("予定の追加に失敗しました。");
    });
  }
});

// 検索処理
searchDate.addEventListener('change', () => {
  const date = searchDate.value; // 検索用の日付を取得
  if (date) {
    fetchAndRenderSchedules(`/api/schedules/search?date=${date}`);
  } else {
    fetchAndRenderSchedules('/api/schedules');
  }
});

// ログイン状態の確認
function checkLoginStatus() {
  return fetch('/api/auth/me', {
    credentials: 'include'
  }).then(res => {
    const ct = (res.headers.get('content-type') || '').toLowerCase();
    if (!res.ok) throw new Error('未ログインまたはエラー');
    if (!ct.includes('application/json')) throw new Error('非JSONレスポンス');
    return res.json();
  });
}

// ログアウト処理
function logout() {
  fetch('/logout', {
    method: 'POST',
    credentials: 'include'
  })
  .then(() => {
    alert('ログアウトしました');
    location.reload(); // ログアウト後にページをリロードして状態を更新
  });
}

// アカウント削除処理
function deleteAccount() {
  if (!confirm('本当に削除しますか？')) return;

  // POSTに切り替え(/api/auth/me/delete) サーバー側で削除と同時にログアウトする
  fetchWithAuth('/api/auth/me/delete', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  })
  .then(res => {
    if (!res.ok) throw new Error('削除失敗');

    // 削除成功後はログアウトされているはずなので、ログインページへ遷移
    alert('アカウントを削除しました');
    location.reload();
  })
  .catch(() => alert('アカウント削除に失敗しました'));
}

// 認証状態に応じてボタンを表示
function renderAuthButtons() {
  const loginBtn = document.getElementById('loginBtn');
  const signupBtn = document.getElementById('signupBtn');

  checkLoginStatus()
    .then(() => {
      loginBtn.textContent = 'ログアウト';
      loginBtn.onclick = logout;

      signupBtn.textContent = 'ユーザー削除';
      signupBtn.onclick = deleteAccount;
    })
    .catch(() => {
      loginBtn.textContent = 'ログイン';
      loginBtn.onclick = () => location.href = 'Login.html';

      signupBtn.textContent = 'ユーザー登録';
      signupBtn.onclick = () => location.href = 'Signup.html';
    });
}