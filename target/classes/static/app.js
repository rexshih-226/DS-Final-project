const el = {
    //搜尋按鈕
    searchBtn: document.getElementById('searchBtn'),

    //提示文字
    Msg: document.getElementById('emptyMsg'),

    //搜尋的內容
    searchString: document.getElementById('searchString'),

    //顯示結果
    result: document.getElementById('result'),
    title: document.getElementById('title'),
    link: document.getElementById('link'),
    snippet: document.getElementById('snippet'),

    //主題切換按鈕
    themeToggleBtn: document.getElementById('themeToggle')
}

function search(e){

    e.preventDefault();  //避免表單提交後頁面重新加載
    el.result.innerHTML = ''; //清空之前的結果
    let html = '';

    el.Msg.innerText = "載入中";
    el.Msg.style.display = "block";

    const query = el.searchString.value;
    fetch(`/api/search?q=${query}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();        // 🔥 一定要加這段
        })
        .then(data => {

            data.forEach(item => {
                html += `
                <li>
                    <a class="title" href="${item.link}" target="_blank">${item.title}</a><br>
                    <small class="link">${item.link}</small>
                    <div class="snippet">${item.snippet}</div>
                </li>
                `;
            });
            el.result.innerHTML = html;
            el.Msg.style.display = "none";
        })
        .catch(error => {
            console.error('Error during search:', error);
            el.Msg.innerText = "搜尋失敗，請稍後再試";
        });

    document.getElementById("emptyMsg").style.display = "none";
}

function toggleTheme() {
    if(document.body.className === "dark"){
        document.body.className = "light";
    } else {
        document.body.className = "dark";
    }
}

el.searchBtn.addEventListener('click', search);

el.themeToggleBtn.addEventListener("click", toggleTheme);