function login(event) {
  event.preventDefault();

  const form = document.getElementById('loginForm');
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;

  // 入力チェック
  if (!username || !password) {
    alert('ユーザー名とパスワードを入力してください');
    return;
  }

  form.action = '/login'; // Spring SecurityのデフォルトのログインURLに合わせる
  form.method = 'post'; // POSTメソッドで送信
  form.submit(); // フォームを送信してサーバー側で認証処理を行う（成功すればセッションが開始され、失敗すればクエリパラメータ?errorが付いてリダイレクトされる）
}

window.addEventListener('DOMContentLoaded', () => {
  const params = new URLSearchParams(window.location.search);

  // ログイン失敗のクエリパラメータがある場合はアラートを表示
  if (params.has('error')) {
    alert('ログイン失敗（ユーザー名またはパスワードが違います）');
  }
});