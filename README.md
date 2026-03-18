# Sistema de Usuários - Spring Boot

Sistema de gerenciamento de usuários com autenticação via Spring Security.

## Funcionalidades
- Cadastro de usuários
- Login com Spring Security (tela padrão)
- Criptografia de senhas com BCrypt
- API REST básica para usuários

## Tecnologias
- Java 17
- Spring Boot 3.2
- Spring Data JPA
- MySQL
- Maven

## Como rodar
1. Clonar o repositório
2. Criar banco `sistema_usuarios` no MySQL
3. Copiar `application.properties.example` para `application.properties` e preencher os dados
4. Rodar a aplicação: `mvn spring-boot:run`
5. Acessar: `http://localhost:8080/usuarios` e fazer login