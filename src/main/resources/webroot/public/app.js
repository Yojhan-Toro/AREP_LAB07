async function fetchHello() {
    const name = document.getElementById('nameInput').value || 'mundo';
    const text = await fetchAndExtract('/App/hello?name=' + encodeURIComponent(name));
    document.getElementById('result').textContent = text;
}

async function fetchEndpoint(url) {
    const text = await fetchAndExtract(url);
    document.getElementById('result2').textContent = text;
}

async function fetchAndExtract(url) {
    try {
        const res  = await fetch(url);
        const html = await res.text();
        const doc  = new DOMParser().parseFromString(html, 'text/html');
        return doc.body.textContent.trim();
    } catch (e) {
        return 'Error: ' + e.message;
    }
}

async function shutdownServer() {
    const msg = document.getElementById('shutdownMsg');
    msg.textContent = 'Enviando señal...';
    try {
        await fetch('/shutdown');
        msg.textContent = 'Servidor apagándose. Esta página dejará de responder.';
    } catch (e) {
        msg.textContent = 'Servidor apagándose. Esta página dejará de responder.';
    }
}
