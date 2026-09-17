# Exemplos da API

## Cadastro

Após GET /csrf, envie seu cookie e o header indicado pela resposta.

```http
POST /usuarios/register
Content-Type: application/json
X-CSRF-TOKEN: <token da sessão>

{"username":"ana.dev","password":"Exemplo123!"}
```

Resposta: 201 com id e username. Nome duplicado retorna 409; entrada inválida retorna 400. O login usa application/x-www-form-urlencoded, com username e password.



## Erros de acesso

- 401: sem sessão válida ou credenciais inválidas.
- 403: CSRF inválido/ausente ou permissão insuficiente.
- 404: recurso não encontrado ou não acessível ao cliente.

O arquivo scripts/smoke.mjs é um exemplo executável que gerencia cookie e CSRF.
