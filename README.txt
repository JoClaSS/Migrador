Migrador de dados desenvolvido com Spring Batch

Foram mapeadas as classes Pessoa e Usuário do AuthManager para inserção de dados num DB POSTGRES, utilizando um .CSV

O .CSV deve conter ("nome", "email", "sexo", "ativo", "login", "senha", "role")

Onde,

STRING nome: Nome da Pessoa
STRING email: Email da Pessoa
STRING sexo: Sexo da pessoa
BOOLEAN ativo: Status do usuário
STRING login: Login do usuário
STRING senha: Senha do usuário
INTEGER role: Tipo do usuário 1 ou 2, ADMIN ou COMUM, respectivamente

OBS: As senhas serão encriptadas antes de serem inseridas no banco
