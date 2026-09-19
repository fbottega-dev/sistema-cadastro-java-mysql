# Organização das telas

A interface tem duas partes: acesso à conta e perfil. Ambas continuam no mesmo HTML, servido pelo Spring Boot.

- `index.html`: formulários, abas, textos e ícones SVG.
- `style.css`: cores, espaçamento, componentes e ajustes para telas menores.
- `app.js`: chamadas à API e atualização dos elementos da página.

## Entrada e cadastro

As abas mostram um formulário por vez. O JavaScript altera `hidden`, `aria-selected` e `tabIndex` para manter a tela e a navegação pelo teclado de acordo. Setas, Home e End trocam a aba selecionada.

O botão de mostrar senha troca o tipo do campo entre `password` e `text`. Ele não envia o formulário. Depois de um cadastro bem-sucedido, a tela volta para a entrada e preenche o usuário recém-criado.

Durante o envio, o `fieldset` fica desabilitado e o botão informa o que está acontecendo. Isso evita outro envio enquanto a primeira resposta está pendente. Os campos continuam preenchidos se houver erro.

## Perfil

`currentUser` guarda a última resposta do servidor. Ao digitar, apenas o campo e o contador mudam; o cartão continua mostrando o nome que está salvo.

Os botões de salvar e descartar são habilitados quando o texto difere do nome salvo. Descartar repõe esse valor no campo, sem chamar a API. Depois de salvar, a resposta atualiza o nome, o cartão e as iniciais.

O avatar usa a primeira letra do primeiro e do último nome. Ele é uma representação em texto; não há envio de foto nesta versão. Nomes são inseridos com `textContent`, sem interpretar HTML.

As regras de autenticação, CSRF e validação continuam no servidor. Mudar o aspecto do campo não dá permissão para alterar usuário, senha ou perfil de acesso.

## Layout e revisão visual

As variáveis no início do CSS concentram a paleta. Grid divide a tela em duas colunas; as media queries reorganizam o conteúdo em telas menores. Nomes longos quebram a linha para não alargar o painel.

Para conferir uma mudança visual, teste entrada, cadastro, senha incorreta, edição e descarte do nome. Veja a tela em 1440, 390 e 360 pixels de largura e percorra as abas e os campos usando o teclado. As capturas do README vêm da aplicação em execução com dados de demonstração.
