package com.br.transaction;

import lombok.Data;

@Data
public class PessoaUsuarioDTO {
    String nome;
    String email;
    String sexo;
    Boolean ativo;
    String login;
    String senha;
    Integer role;
}
