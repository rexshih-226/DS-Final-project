const el = {
    //搜尋的內容
    searchString: document.getElementById('searchString'),

    //顯示結果
    result: document.getElementById('result'),
    title: document.getElementById('title'),
    link: document.getElementById('link'),
    snippet: document.getElementById('snippet'),

}

function search(){
    const query = el.searchString.value;
    fetch(`/api/search?q=${query}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
        })
        .then(data => {

            data.forEach(item => {
                html += `
            <a id="title">${item.title}</a><br>
            <small id="link">${item.link}</small>
            <div class="snippet" id="snippet">${item.snippet}</div>
        `;
    });
            el.result.innerHTML = html;
        })
        .catch(error => {
            console.error('Error during search:', error);
        });
}

document.getElementById('searchBtn').addEventListener('click', search);