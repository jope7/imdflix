# IMDFlix

Catálogo de filmes, séries e documentários  

## Requisitos
- JDK: 25.0.5.0.3
- JavaFX SDK: 25.0.5

## Como compilar e executar os testes (testes sem JavaFX)

- linux (desconsiderar os erros do OlaJavaFX.java na compilação)
```bash
javac -d out $(find src test -name "*.java" ! -name "OlaJavaFX.java")
java -ea -cp out test.TesteMidia
java -ea -cp out test.TesteUsuario
java -ea -cp out test.TesteServicos
```

- windows 
```bash
javac -d out (Get-ChildItem -Path src, test -Recurse -Filter "*.java" | Where-Object { $_.Name -ne "OlaJavaFX.java" }).FullName
java -ea -cp out test.TesteMidia
java -ea -cp out test.TesteUsuario
java -ea -cp out test.TesteServicos
```

Lembrar da flag `-ea`, sem ela o Java ignora os `assert`