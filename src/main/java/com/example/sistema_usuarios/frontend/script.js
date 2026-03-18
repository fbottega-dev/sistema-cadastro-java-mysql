const apiUrl = "http://localhost:8080/usuarios";

async function registerUser() {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    if (!username || !password) {
        alert("Preencha todos os campos!");
        return;
    }

    try {
        const response = await fetch(`${apiUrl}/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        const text = await response.text();

        if(response.ok) {
            alert(text);
            window.location.href = "login.html";
        } else {
            alert("Erro: " + text);
        }
    } catch(err) {
        alert("Erro na conexão: " + err);
    }
}

async function loginUser() {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    if (!username || !password) {
        alert("Preencha todos os campos!");
        return;
    }

    try {
        const response = await fetch(`${apiUrl}/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        const text = await response.text();

        if(response.ok) {
            alert(text);
            window.location.href = "dashboard.html";
        } else {
            alert("Erro: " + text);
        }
    } catch(err) {
        alert("Erro na conexão: " + err);
    }

    
}

function logout() {
    window.location.href = "login.html";
}