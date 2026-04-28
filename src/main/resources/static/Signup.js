async function register() {
  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;

  // 入力チェック
  if (!username || !password) {
    alert('ユーザー名とパスワードを入力してください');
    return;
  }

  try {
    const res = await fetch('/api/auth/signup', { // サーバー側のユーザー登録エンドポイントに合わせる
      method: 'POST', // POSTメソッドで送信
      headers: { 'Content-Type': 'application/json' }, // JSON形式で送信
      body: JSON.stringify({ username, password }) // ユーザー名とパスワードをJSON形式で送信
    });

    // レスポンスがOKでない場合はエラーを投げる
    if (!res.ok) throw new Error();
    alert('登録が完了しました！');
    location.href = 'Login.html'; // 登録後はログインページへ遷移
  } catch {
    alert('登録に失敗しました（ユーザー名重複の可能性あり）');
  }
}