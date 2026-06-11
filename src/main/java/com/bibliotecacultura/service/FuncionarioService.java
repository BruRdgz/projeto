package com.bibliotecacultura.service;

import com.bibliotecacultura.dto.FuncionarioDTO;
import com.bibliotecacultura.entity.Funcionario;
import com.bibliotecacultura.exception.DuplicadoException;
import com.bibliotecacultura.exception.EntidadeNaoEncontradaException;
import com.bibliotecacultura.exception.NegocioException;
import com.bibliotecacultura.repository.FuncionarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepo;

    public FuncionarioService(FuncionarioRepository funcionarioRepo) {
        this.funcionarioRepo = funcionarioRepo;
    }

    // ------------------------------------------------------------------ LEITURA

    public List<FuncionarioDTO> listarTodos(String termo) {
        return funcionarioRepo.search(termo).stream()
                .map(this::toDTO)
                .toList();
    }

    public FuncionarioDTO buscarPorId(Long id) {
        return funcionarioRepo.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Funcionário #" + id + " não encontrado."));
    }

    /**
     * RF001 — validar credenciais para login.
     * Retorna o DTO do funcionário se válido, lança NegocioException caso contrário.
     *
     * NOTA: as senhas são armazenadas como texto simples aqui como um placeholder.
     * Quando você adicionar o Spring Security (a tarefa de autenticação adiada), substitua isto por
     * uma verificação PasswordEncoder.matches().
     */
    public FuncionarioDTO autenticar(String matricula, String senha) {
        Funcionario f = funcionarioRepo.findByMatriculaAndAtivoTrue(matricula)
                .orElseThrow(() -> new NegocioException("Matrícula ou senha inválidos."));

        // TODO: substituir por passwordEncoder.matches(senha, f.getSenhaHash())
        if (!f.getSenhaHash().equals(senha)) {
            throw new NegocioException("Matrícula ou senha inválidos.");
        }

        return toDTO(f);
    }

    // ----------------------------------------------------------------- ESCRITA

    /** RF014 — cadastrar um novo membro da equipe. */
    @Transactional
    public FuncionarioDTO cadastrar(FuncionarioDTO dto) {
        if (funcionarioRepo.existsByCpf(dto.getCpf())) {
            throw new DuplicadoException("CPF " + dto.getCpf() + " já cadastrado.");
        }
        if (funcionarioRepo.existsByMatricula(dto.getMatricula())) {
            throw new DuplicadoException("Matrícula " + dto.getMatricula() + " já cadastrada.");
        }
        if (dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new NegocioException("Senha é obrigatória no cadastro.");
        }

        Funcionario f = new Funcionario();
        f.setNome(dto.getNome());
        f.setCpf(dto.getCpf());
        f.setMatricula(dto.getMatricula());
        f.setCargo(Funcionario.Cargo.valueOf(dto.getCargo()));
        // TODO: armazenar passwordEncoder.encode(dto.getSenha()) em vez disso
        f.setSenhaHash(dto.getSenha());
        f.setAtivo(true);

        return toDTO(funcionarioRepo.save(f));
    }

    /** RF015 — atualizar nome, cargo ou status de ativo. */
    @Transactional
    public FuncionarioDTO alterar(Long id, FuncionarioDTO dto) {
        Funcionario f = funcionarioRepo.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Funcionário #" + id + " não encontrado."));

        f.setNome(dto.getNome());
        f.setCargo(Funcionario.Cargo.valueOf(dto.getCargo()));
        f.setAtivo(dto.isAtivo());

        // Atualizar senha apenas se uma nova foi fornecida
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            // TODO: f.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
            f.setSenhaHash(dto.getSenha());
        }

        return toDTO(funcionarioRepo.save(f));
    }

    // --------------------------------------------------------------- MAPEAMENTO

    public FuncionarioDTO toDTO(Funcionario f) {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setId(f.getId());
        dto.setNome(f.getNome());
        dto.setCpf(f.getCpf());
        dto.setMatricula(f.getMatricula());
        dto.setCargo(f.getCargo().name());
        dto.setAtivo(f.isAtivo());
        // nunca expor senhaHash
        return dto;
    }
}
