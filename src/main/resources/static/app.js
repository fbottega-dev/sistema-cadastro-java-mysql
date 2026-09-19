"use strict";
const $ = (id) => document.getElementById(id);
let currentUser = null;
let noticeTimer;
function notice(text, error = false) {
  clearTimeout(noticeTimer);
  $("notice").textContent = text;
  $("notice").classList.toggle("error", error);
  if (text && !error) noticeTimer = setTimeout(() => notice(""), 6000);
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
    if (response.status === 400 && url === "/usuarios/me")
      detail =
        "Use um nome de 2 a 80 caracteres, sem deixar o campo em branco.";
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
    const form = event.target;
    const values = Object.fromEntries(new FormData(form));
    const fields = form.querySelector("fieldset");
    const button = form.querySelector('button[type="submit"]');
    const label = button.querySelector("[data-button-label]");
    const originalLabel = label.textContent;
    fields.disabled = true;
    form.setAttribute("aria-busy", "true");
    label.textContent = button.dataset.loadingLabel;
    if (id === "profile") $("logout").disabled = true;
    else {
      $("login-tab").disabled = true;
      $("register-tab").disabled = true;
    }
    notice("");
    try {
      await handler(values);
    } catch (error) {
      notice(error.message, true);
    } finally {
      fields.disabled = false;
      form.removeAttribute("aria-busy");
      label.textContent = originalLabel;
      if (id === "profile") {
        $("logout").disabled = false;
        updateProfileActions();
      } else {
        $("login-tab").disabled = false;
        $("register-tab").disabled = false;
      }
    }
  });
}

function selectAuthTab(mode) {
  for (const name of ["login", "register"]) {
    const active = name === mode;
    $(name).hidden = !active;
    $(name + "-tab").setAttribute("aria-selected", String(active));
    $(name + "-tab").tabIndex = active ? 0 : -1;
  }
  document.title = (mode === "login" ? "Entrar" : "Criar conta") + " · Portal";
  notice("");
}
for (const mode of ["login", "register"]) {
  const tab = $(mode + "-tab");
  tab.onclick = () => selectAuthTab(mode);
  tab.onkeydown = (event) => {
    if (!["ArrowLeft", "ArrowRight", "Home", "End"].includes(event.key)) return;
    event.preventDefault();
    const next =
      event.key === "Home"
        ? "login"
        : event.key === "End"
          ? "register"
          : mode === "login"
            ? "register"
            : "login";
    selectAuthTab(next);
    $(next + "-tab").focus();
  };
}

function setPasswordVisible(button, visible) {
  $(button.getAttribute("aria-controls")).type = visible ? "text" : "password";
  button.setAttribute("aria-pressed", String(visible));
  button.textContent = visible ? "Ocultar" : "Mostrar";
  button.setAttribute(
    "aria-label",
    visible ? "Ocultar senha" : "Mostrar senha",
  );
}
document.querySelectorAll("[data-password-toggle]").forEach((button) => {
  button.onclick = () =>
    setPasswordVisible(button, button.getAttribute("aria-pressed") !== "true");
});

async function refresh() {
  try {
    currentUser = await request("/usuarios/me");
  } catch {
    currentUser = null;
  }
  $("guest").hidden = !!currentUser;
  $("workspace").hidden = !currentUser;
  $("session-loading").hidden = true;
  if (currentUser) showProfile();
  else {
    $("profile").reset();
    selectAuthTab("login");
  }
}
function showProfile() {
  document.title = "Meu perfil · Portal";
  $("greeting").textContent = "Olá, " + currentUser.displayName;
  $("profile-username").textContent = currentUser.username;
  $("display-name").value = currentUser.displayName;
  $("profile-name").textContent = currentUser.displayName;
  $("card-username").textContent = "@" + currentUser.username;
  const words = currentUser.displayName.trim().split(/\s+/);
  $("avatar").textContent = [
    words[0],
    ...(words.length > 1 ? [words.at(-1)] : []),
  ]
    .map((word) => Array.from(word)[0] || "")
    .join("")
    .toLocaleUpperCase("pt-BR");
  $("profile-role").textContent = currentUser.roles.includes("ROLE_TECHNICIAN")
    ? "Técnico"
    : "Usuário";
  updateProfileActions();
}
function updateProfileActions() {
  const changed =
    currentUser && $("display-name").value !== currentUser.displayName;
  $("name-count").textContent = $("display-name").value.length + "/80";
  $("profile-save").disabled = !changed;
  $("discard").disabled = !changed;
  $("save-hint").textContent = changed
    ? "Alterações ainda não salvas."
    : "Nenhuma alteração pendente.";
}
$("display-name").addEventListener("input", updateProfileActions);
$("discard").onclick = () => {
  showProfile();
  notice("Alterações descartadas.");
  $("display-name").focus();
};
bindForm("login", async (values) => {
  await request("/usuarios/login", {
    method: "POST",
    body: new URLSearchParams(values),
  });
  $("login").reset();
  setPasswordVisible($("login").querySelector("[data-password-toggle]"), false);
  await refresh();
  $("greeting").focus();
  notice("Acesso autorizado.");
});
bindForm("register", async (values) => {
  await request("/usuarios/register", json(values));
  $("register").reset();
  setPasswordVisible(
    $("register").querySelector("[data-password-toggle]"),
    false,
  );
  selectAuthTab("login");
  $("username").value = values.username;
  $("password").focus();
  notice("Conta criada. Agora entre com seu usuário e senha.");
});
bindForm("profile", async (values) => {
  currentUser = await request("/usuarios/me", {
    ...json({ displayName: values.displayName }),
    method: "PATCH",
  });
  showProfile();
  notice("Nome atualizado.");
});
$("logout").onclick = async () => {
  $("logout").disabled = true;
  try {
    await request("/usuarios/logout", { method: "POST" });
    await refresh();
    $("username").focus();
    notice("Você saiu da conta.");
  } catch (error) {
    notice(error.message, true);
  } finally {
    $("logout").disabled = false;
  }
};

refresh().catch((e) => notice(e.message, true));
