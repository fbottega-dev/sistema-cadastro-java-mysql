"use strict";
const $ = (id) => document.getElementById(id);
let currentUser = null;
let page = 0;
function notice(text, error = false) {
  $("notice").textContent = text;
  $("notice").classList.toggle("error", error);
}
async function request(url, options = {}) {
  const method = options.method || "GET";
  const headers = { Accept: "application/json", ...options.headers };
  if (method !== "GET") {
    const tokenResponse = await fetch("/csrf");
    if (!tokenResponse.ok)
      throw Error("Não foi possível obter a proteção da sessão.");
    const token = await tokenResponse.json();
    headers[token.headerName] = token.token;
  }
  const response = await fetch(url, { ...options, headers });
  if (!response.ok) {
    let detail = "";
    try {
      detail = (await response.json()).detail || "";
    } catch {}
    throw Error(
      detail ||
        {
          401: "Usuário ou senha inválidos, ou sessão expirada.",
          403: "Operação não permitida. Atualize a página.",
          409: "A operação conflita com o estado atual.",
        }[response.status] ||
        "Não foi possível concluir a operação.",
    );
  }
  return response.status === 204 ? null : response.json();
}
const json = (body) => ({
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(body),
});
function bindForm(id, handler) {
  $(id)?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const button = event.target.querySelector("button");
    button.disabled = true;
    try {
      await handler(Object.fromEntries(new FormData(event.target)));
    } catch (error) {
      notice(error.message, true);
    } finally {
      button.disabled = false;
    }
  });
}
async function refresh() {
  try {
    currentUser = await request("/usuarios/me");
  } catch {
    currentUser = null;
  }
  $("guest").hidden = !!currentUser;
  $("workspace").hidden = !currentUser;
  if (currentUser) {
    $("greeting").textContent = "Olá, " + currentUser.username;
  }
}
bindForm("login", async (values) => {
  await request("/usuarios/login", {
    method: "POST",
    body: new URLSearchParams(values),
  });
  $("login").reset();
  notice("Acesso autorizado.");
  await refresh();
});
bindForm("register", async (values) => {
  await request("/usuarios/register", json(values));
  $("register").reset();
  notice("Conta criada. Agora entre com seu usuário e senha.");
});
$("logout").onclick = async () => {
  try {
    await request("/usuarios/logout", { method: "POST" });
    await refresh();
    notice("Você saiu da conta.");
  } catch (error) {
    notice(error.message, true);
  }
};

refresh().catch((e) => notice(e.message, true));
