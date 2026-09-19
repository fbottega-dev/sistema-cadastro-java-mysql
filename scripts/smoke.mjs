import assert from "node:assert/strict";

const base = process.env.BASE_URL || "http://localhost:8080";
let cookie = "";

async function call(url, options = {}) {
    const res = await fetch(base + url, {
        ...options,
        headers: { Cookie: cookie, ...options.headers },
    });
    const session = res.headers
        .getSetCookie()
        .find((value) => value.startsWith("accounts-session="));
    if (session) cookie = session.split(";")[0];
    return res;
}

async function token() {
    const response = await call("/csrf");
    assert.equal(response.status, 200);
    return response.json();
}

async function login(username, password) {
    const csrf = await token();
    const response = await call("/usuarios/login", {
        method: "POST",
        headers: { [csrf.headerName]: csrf.token },
        body: new URLSearchParams({ username, password }),
    });
    assert.equal(response.status, 200);
}

async function logout() {
    const csrf = await token();
    const response = await call("/usuarios/logout", {
        method: "POST",
        headers: { [csrf.headerName]: csrf.token },
    });
    assert.equal(response.status, 204);
    assert.equal((await call("/usuarios/me")).status, 401);
}

for (let attempt = 0; attempt < 60; attempt++) {
    try {
        if ((await fetch(base)).ok) break;
    } catch {}
    await new Promise((resolve) => setTimeout(resolve, 1000));
}

assert.equal((await call("/usuarios/me")).status, 401);
const username = "smoke" + Date.now();
const password = "Smoke12345!";
let csrf = await token();
const registration = await call("/usuarios/register", {
    method: "POST",
    headers: {
        [csrf.headerName]: csrf.token,
        "Content-Type": "application/json",
    },
    body: JSON.stringify({ username, password }),
});
assert.equal(registration.status, 201);
assert.equal((await registration.json()).displayName, username);

await login(username, password);
const original = await (await call("/usuarios/me")).json();
assert.deepEqual(original, {
    username,
    displayName: username,
    roles: ["ROLE_USER"],
});

const withoutCsrf = await call("/usuarios/me", {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ displayName: "Não deve salvar" }),
});
assert.equal(withoutCsrf.status, 403);
assert.deepEqual(await (await call("/usuarios/me")).json(), original);

csrf = await token();
const edited = await call("/usuarios/me", {
    method: "PATCH",
    headers: {
        [csrf.headerName]: csrf.token,
        "Content-Type": "application/json",
    },
    body: JSON.stringify({ displayName: "  João da Silva  " }),
});
assert.equal(edited.status, 200);
const expected = {
    username,
    displayName: "João da Silva",
    roles: ["ROLE_USER"],
};
assert.deepEqual(await edited.json(), expected);

await logout();
await login(username, password);
for (const path of ["/usuarios/me", "/usuarios/home"]) {
    const profile = await call(path);
    assert.equal(profile.status, 200);
    assert.deepEqual(await profile.json(), expected);
}
await logout();
console.log("MySQL + HTTP smoke passed");
