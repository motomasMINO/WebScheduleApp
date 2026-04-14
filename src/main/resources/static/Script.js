const scheduleForm = document.getElementById('scheduleForm');
const scheduleList = document.getElementById('scheduleList');
const searchDate = document.getElementById('searchDate');
const cancelEditBtn = document.getElementById('cancelEditBtn');

// 予定を取得して表示する関数
function fetchAndRenderSchedules(url) {
  fetch(url) // APIにGETリクエストを送信して予定を取得
    .then(res => res.json())
    .then(data => {
      scheduleList.innerHTML = ''; // 予定リストをクリア
      if (data.length === 0) {
        scheduleList.innerHTML = '<li>予定がありません</li>'; // 予定がない場合の表示
        return;
      }

      // 予定をリストに追加
      data.forEach((s) => {
        const date = s.startTime.split('T')[0]; // YYYY-MM-DD
        const time = s.startTime.split('T')[1].slice(0, 5); // HH:mm
        const li = document.createElement('li'); // <li>2024-06-01 09:00 - 会議<br>詳細情報<button>編集</button><button>削除</button></li>
        
        // 予定の内容と編集・削除のボタンを表示
        li.innerHTML = `<strong>${date} ${time}</strong> - ${s.title}<br>${s.description}
        <button onclick="editSchedule(${s.id})">編集</button>
        <button onclick="deleteSchedule(${s.id})">削除</button>`; // 編集と削除のボタンを追加
        scheduleList.appendChild(li); // <ul id="scheduleList"><li>予定がありません</li></ul>
      });
    });
}

// 削除処理
function deleteSchedule(id) {
  if (!confirm('この予定を削除しますか？')) return; // 確認ダイアログを表示

  // APIにDELETEリクエストを送信して予定を削除
  fetch(`/api/schedules/${id}`, {
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

// 編集用のIDを保持
let editingId = null;

// 編集処理
function editSchedule(id) { 
  fetch(`/api/schedules/${id}`) // APIにGETリクエストを送信して予定の詳細を取得
    .then(res => {
      if (!res.ok) throw new Error('予定取得失敗'); // レスポンスがOKでない場合はエラーをスロー
      return res.json(); // レスポンスをJSONとして解析
    })
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
  fetchAndRenderSchedules('/api/schedules');
});

// 登録処理
scheduleForm.addEventListener('submit', function (e) {
  e.preventDefault(); // フォームのデフォルトの送信を防止

  const date = document.getElementById('date').value; // フォームから日付を取得
  const title = document.getElementById('title').value; // フォームからタイトルを取得
  const description = document.getElementById('description').value; // フォームから説明を取得

  // APIに送信するデータを作成
  const scheduleData = {
    title,
    description,
    startTime: date + 'T09:00:00',
    endTime: date + 'T10:00:00'
  }; // 予定の開始時間と終了時間を固定で設定（例: 09:00 - 10:00）

  if (editingId) {
    // 編集モード
    fetch(`/api/schedules/${editingId}`, {
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
    fetch('/api/schedules', {
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