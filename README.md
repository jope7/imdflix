# IMDFlix

Catálogo de filmes, séries e documentários  

## Requisitos
- JDK: 25.0.5.0.3
- JavaFX SDK: 25.0.5

## Como compilar e executar os testes (testes sem JavaFX)

```bash
javac -d out $(find src test -name "*.java")
java -ea -cp out TesteMidia
java -ea -cp out TesteUsuario
java -ea -cp out TesteServicos
```

Lembrar da flag `-ea`, sem ela o Java ignora os `assert`