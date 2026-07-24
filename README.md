# Gerador de Laudos Técnicos

Aplicativo desktop em Java Swing para gerar, copiar, salvar e consultar laudos técnicos por OS, usando SQLite como banco local.

## Recursos

- Criação de abas por OS.
- Geração automática do texto do laudo a partir do checklist.
- Cadastro de componentes e discos/SSDs com regras de troca, backup e placa-mãe.
- Salvamento de rascunhos e finalização de laudos.
- Histórico com pesquisa por número de OS, status e pré-visualização.
- Reabertura de OS salvas com os campos restaurados quando o registro possui `dados_interface`.

## Rodar no VS Code

1. Instale o Java 17 e a extensão `Extension Pack for Java`.
2. Abra a pasta do projeto no VS Code.
3. Deixe o Java sincronizar o `pom.xml`.
4. Rode a classe `lauds.Main`.

## Gerar JAR executável

Com Maven instalado, execute:

```powershell
mvn package
```

O JAR com dependências será gerado em `target/`.

## Banco de dados

O arquivo `laudos.db` é criado/atualizado automaticamente na raiz do projeto.
