const scheduleForm = document.getElementById('scheduleForm');
const scheduleList = document.getElementById('scheduleList');
const searchDate = document.getElementById('searchDate');
const cancelEditBtn = document.getElementById('cancelEditBtn');

// 予定を取得して表示する関数
function fetchAndRenderSchedules(url) {
  fetch(url)
    .then(res => res.json())
    .then(data => {
      scheduleList.innerHTML = '';
      if (data.length === 0) {
        scheduleList.innerHTML = '<li>予定がありません</li>';
        return;
      }

      data.forEach((s) => {
        const date = s.startTime.split('T')[0];
        const time = s.startTime.split('T')[1].slice(0, 5); // HH:mm
        const li = document.createElement('li');
        li.innerHTML = `<strong>${date} ${time}</strong> - ${s.title}<br>${s.description}
        <button onclick="editSchedule(${s.id})">編集</button>
        <button onclick="deleteSchedule(${s.id})">削除</button>`;
        scheduleList.appendChild(li);
      });
    });
}

// 削除処理
function deleteSchedule(id) {
  if (!confirm('この予定を削除しますか？')) return;

  fetch(`/api/schedules/${id}`, {
    method: 'DELETE'
  })
  .then(res => {
    if (!res.ok) throw new Error('削除失敗');
    fetchAndRenderSchedules('/api/schedules');
  })
  .catch(err => {
    console.error('削除エラー:', err);
    alert('削除に失敗しました。');
  });
}

// 編集用のIDを保持
let editingId = null;

// 編集処理
function editSchedule(id) { 
  fetch(`/api/schedules/${id}`)
    .then(res => {
      if (!res.ok) throw new Error('予定取得失敗');
      return res.json();
    })
    .then(data => {
      const date = data.startTime.split('T')[0];
      document.getElementById('date').value = date;
      document.getElementById('title').value = data.title;
      document.getElementById('description').value = data.description;
      editingId = id;

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
  editingId = null;
  scheduleForm.reset();
  scheduleForm.querySelector('button[type="submit"]').textContent = '追加';
  cancelEditBtn.style.display = 'none'; // ボタン非表示
});

// 初回ロード時に全件取得
window.addEventListener('DOMContentLoaded', () => {
  fetchAndRenderSchedules('/api/schedules');
});

// 登録処理
scheduleForm.addEventListener('submit', function (e) {
  e.preventDefault();

  const date = document.getElementById('date').value;
  const title = document.getElementById('title').value;
  const description = document.getElementById('description').value;

  const scheduleData = {
    title,
    description,
    startTime: date + 'T09:00:00',
    endTime: date + 'T10:00:00'
  };

  if (editingId) {
    // 編集モード
    fetch(`/api/schedules/${editingId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(scheduleData)
    })
    .then(res => {
      if (!res.ok) throw new Error('更新失敗');
      alert('予定を更新しました！');
      editingId = null;
      scheduleForm.querySelector('button[type="submit"]').textContent = '追加'; // 元に戻す
      scheduleForm.reset();
      fetchAndRenderSchedules('/api/schedules');
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
  const date = searchDate.value;
  if (date) {
    fetchAndRenderSchedules(`/api/schedules/search?date=${date}`);
  } else {
    fetchAndRenderSchedules('/api/schedules');
  }
});