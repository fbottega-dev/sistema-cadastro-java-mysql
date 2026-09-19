# Exemplos da API

## Cadastro

Após GET /csrf, envie seu cookie e o header indicado pela resposta.

```http
POST /usuarios/register
Content-Type: application/json
X-CSRF-TOKEN: <token da sessão>

{"username":"ana.dev","password":"Exemplo123!"}
```

Resposta: 201 com id, username e displayName (inicialmente igual ao username). Usuário de acesso duplicado retorna 409; entrada inválida retorna 400. O login usa application/x-www-form-urlencoded, com username e password.

## Editar o nome de exibição

Após o login, obtenha um novo token em GET /csrf e mantenha o cookie de sessão:

```http
PATCH /usuarios/me
Content-Type: application/json
X-CSRF-TOKEN: <token da sessão>

{"displayName":"Ana Souza"}
```

Resposta 200, também retornada por GET /usuarios/me:

```json
{"username":"ana.dev","displayName":"Ana Souza","roles":["ROLE_USER"]}
```

O nome é obrigatório e deve ter de 2 a 80 caracteres após remover espaços das pontas. Valores inválidos retornam 400. A conta vem da sessão: não há parâmetro de ID ou usuário a editar. Campos extras são ignorados, sem alterar login, senha ou permissões. Dois usuários podem escolher o mesmo nome de exibição.

O alias GET /usuarios/home continua disponível e retorna os mesmos campos.

## Erros de acesso

- 401: sem sessão válida ou credenciais inválidas.
- 403: CSRF inválido/ausente ou permissão insuficiente.
- 404: recurso não encontrado ou não acessível ao cliente.

O arquivo scripts/smoke.mjs é um exemplo executável que gerencia cookie e CSRF.
