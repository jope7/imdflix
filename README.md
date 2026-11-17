# IMDFlix

Catálogo de filmes, séries e documentários  

## Requisitos
- JDK: 25.0.5.0.3
- JavaFX SDK: 25.0.5

## Como compilar e executar os testes (testes sem JavaFX)

- linux (desconsiderando o OlaJavaFX.java na compilação)

```bash
javac -cp "lib\gson-2.11.0.jar" -d out (Get-ChildItem -Path src, test -Recurse -Filter "*.java" | Where-Object { $_.Name -ne "OlaJavaFX.java" }).FullName
java -ea -cp out test.TesteMidia
java -ea -cp out test.TesteUsuario
java -ea -cp out test.TesteServicos
java -ea -cp "out:lib\gson-2.11.0.jar" test.TesteLeituraJson
```

- windows (desconsiderando o OlaJavaFX.java na compilação)

```bash
javac -cp "lib\gson-2.11.0.jar" -d out (Get-ChildItem -Path src, test -Recurse -Filter "*.java" | Where-Object { $_.Name -ne "OlaJavaFX.java" -and $_.FullName -notlike "*\src\ui\*" }).FullName
java -ea -cp out test.TesteMidia
java -ea -cp out test.TesteUsuario
java -ea -cp out test.TesteServicos
java -ea -cp "out;lib\gson-2.11.0.jar" test.TesteLeituraJson
java -ea -cp "out;lib\gson-2.11.0.jar" test.TesteCatalogoJson
java -ea -cp "out;lib\gson-2.11.0.jar" test.TesteMidiaFactory
java -ea -cp "out;lib\gson-2.11.0.jar" test.TesteCarregadorDados
java -ea -cp "out;lib\gson-2.11.0.jar" test.TesteSessaoUsuario

Lembrar da flag `-ea`, sem ela o Java ignora os `assert`